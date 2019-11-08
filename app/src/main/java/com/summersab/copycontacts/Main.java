package com.summersab.copycontacts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SyncAdapterType;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main extends Activity {
    public List<myProvider> actual = new ArrayList();
    private String appname = "CopyContacts";
    public ProgressBar bar;
    public MyAdapter cAdapter;
    public List<mycList> contactList = new ArrayList();
    public Spinner fromspin;
    public SharedPreferences prefs;
    public List<myProvider> provider = new ArrayList();
    public Spinner tospin;
    public String version = "";

    public class MyOnItemSelectedListener implements OnItemSelectedListener {
        public MyOnItemSelectedListener() {
        }

        public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
            try {
                if (ContextCompat.checkSelfPermission(Main.this.getApplicationContext(), "android.permission.READ_CONTACTS") == 0) {
                    new taskList().execute(Main.this.actual.get(pos));
                }
            } catch (Exception e) {
                Log.e("contactMover", Main.this.actual.size() + ":" + pos + ":" + e.toString());
            }
        }

        public void onNothingSelected(AdapterView parent) {
        }
    }

    class taskList extends AsyncTask<Object, Void, Long> {
        final TextView status = ((TextView) Main.this.findViewById(R.id.searchResult));

        taskList() {
        }

        public Long doInBackground(Object... params) {
            Cursor cur;
            Drawable default_image;
            Cursor cursor = null;
            /* summersab
            myProvider prov = params[0];
            */
            myProvider prov = (myProvider) params[0];
            Main.this.contactList.clear();
            try {
                Log.i("CopyContacts", "Reading contact '" + prov.type + "' + '" + prov.name + "'");
                if (prov.type == null) {
                    cur = Main.this.getContentResolver().query(RawContacts.CONTENT_URI, null, "deleted = 0 AND account_type is Null  AND account_name is null", null, null);
                } else {
                    cur = Main.this.getContentResolver().query(RawContacts.CONTENT_URI, null, "deleted = 0 AND account_type = ?  AND account_name = ?", new String[]{prov.type, prov.name}, null);
                }
                long i = 0;
                if (VERSION.SDK_INT >= 21) {
                    default_image = Main.this.getResources().getDrawable(R.drawable.contact_icon, Main.this.getTheme());
                } else {
                    default_image = Main.this.getResources().getDrawable(R.drawable.contact_icon);
                }
                if (cur.getCount() > 0) {
                    Main.this.bar.setMax(255);
                    while (cur.moveToNext()) {
                        Uri photoUri = Uri.withAppendedPath(ContentUris.withAppendedId(Contacts.CONTENT_URI, cur.getLong(cur.getColumnIndex("contact_id"))), "photo");
                        Drawable image = null;
                        cursor = Main.this.getContentResolver().query(photoUri, new String[]{"data15"}, null, null, null);
                        if (cursor.moveToFirst()) {
                            byte[] data = cursor.getBlob(0);
                            if (data != null) {
                                image = new BitmapDrawable(Main.this.getResources(), BitmapFactory.decodeByteArray(data, 0, data.length));
                            }
                        } else {
                            image = default_image;
                        }
                        cursor.close();
                        Main.this.contactList.add(new mycList(cur.getString(cur.getColumnIndex("display_name")), image, Boolean.valueOf(true), cur.getString(cur.getColumnIndex("_id"))));
                        i++;
                        onProgressUpdate((((int) i) * 255) / cur.getCount());
                    }
                } else {
                    Log.i("CopyContacts", "Data not found");
                }
                cur.close();
            } catch (Exception e) {
                Main.exception(e, Main.this.getApplicationContext(), "");
            } catch (Throwable th) {
                cursor.close();
                throw th;
            }
            return Long.valueOf((long) Main.this.contactList.size());
        }

        public void onPreExecute() {
            this.status.setText("");
            Main.this.bar.setVisibility(0);
            Main.this.fromspin.setEnabled(false);
            super.onPreExecute();
        }

        public void onPostExecute(Long result) {
            Main.this.bar.setVisibility(8);
            Main.this.fromspin.setEnabled(true);
            Main.this.cAdapter.notifyDataSetChanged();
            Button mButton = (Button) Main.this.findViewById(R.id.button);
            if (Main.this.prefs.getBoolean("copy", true)) {
                mButton.setText(Main.this.getString(R.string.t_copy_button, Integer.valueOf(Main.this.contactList.size())));
            } else {
                mButton.setText(Main.this.getString(R.string.t_move_button, Integer.valueOf(Main.this.contactList.size())));
            }
            super.onPostExecute(result);
            returner(Main.this.contactList.size());
        }

        public int returner(int result) {
            return result;
        }

        public void onProgressUpdate(int i) {
            Main.this.bar.setProgress(i);
        }
    }

    class taskmove extends AsyncTask<Void, Void, Integer> {
        private Context context;
        private myProvider from;
        private int i;
        final TextView status = ((TextView) Main.this.findViewById(R.id.searchResult));
        private myProvider to;

        taskmove(myProvider from2, myProvider to2, Context context2) {
            this.from = from2;
            this.to = to2;
            this.context = context2;
        }

        public Integer doInBackground(Void... params) {
            int success = 0;
            try {
                for (mycList con : Main.this.contactList) {
                    if (con.selected.booleanValue()) {
                        success += createAddr.updateContactList(Main.this.getApplicationContext(), con.rawID, this.to.type, this.to.name, Main.this.prefs);
                    }
                    this.i++;
                    onProgressUpdate((this.i * 255) / Main.this.contactList.size());
                }
            } catch (Exception e) {
                Main.exception(e, this.context, "");
            }
            if (Main.this.prefs.getBoolean("stripgroups", false)) {
                try {
                    Cursor cur = this.context.getContentResolver().query(Groups.CONTENT_SUMMARY_URI, new String[]{"_id", "summ_count"}, "account_name=? AND account_type=? AND summ_count=0", new String[]{this.from.name, this.from.type}, null);
                    while (cur.moveToNext()) {
                        this.context.getContentResolver().delete(Uri.withAppendedPath(Groups.CONTENT_URI, Integer.toString(cur.getInt(0))), null, null);
                    }
                    cur.close();
                } catch (Exception e2) {
                    Main.exception(e2, this.context, "");
                }
            }
            return Integer.valueOf(success);
        }

        public void onPreExecute() {
            Main.this.bar.setVisibility(0);
            Main.this.fromspin.setEnabled(false);
            this.status.setVisibility(8);
            super.onPreExecute();
        }

        public void onPostExecute(Integer result) {
            super.onPostExecute(result);
            Main.this.bar.setVisibility(8);
            this.status.setVisibility(0);
            Main.this.fromspin.setEnabled(true);
            if (!Main.this.isFinishing()) {
                Builder builder = new Builder(Main.this);
                if (Main.this.prefs.getBoolean("copy", false)) {
                    builder.setMessage(Main.this.getString(R.string.t_copied, result, Integer.valueOf(this.i), this.from.type, this.to.type)).setTitle(Main.this.getText(R.string.t_success)).setCancelable(false).setPositiveButton("Ok", new OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                } else {
                    builder.setMessage(Main.this.getString(R.string.t_moved, result, Integer.valueOf(this.i), this.from.type, this.to.type)).setTitle(Main.this.getText(R.string.t_success)).setCancelable(false).setPositiveButton("Ok", new OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                }
                builder.create().show();
            }
            Main.this.actual = Main.this.getactual();
            Main.this.fromspin.setSelection(0);
            Main.this.fromspin.setAdapter(new mySpinner(Main.this, R.layout.spinner, Main.this.actual));
        }

        private void onProgressUpdate(int i2) {
            Main.this.bar.setProgress(i2);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.prefs = getSharedPreferences("default", 0);
        if (this.prefs.getInt("disclaimer", 0) == 0) {
            Builder dlgBuilder = new Builder(this);
            dlgBuilder.setTitle(R.string.t_disclaimer);
            dlgBuilder.setMessage(R.string.t_dtext);
            dlgBuilder.setCancelable(false).setPositiveButton(R.string.t_button_ok, new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    Main.this.prefs.edit().putInt("disclaimer", 1).apply();
                    dialogInterface.dismiss();
                    Main.this.provider = Main.this.getproviders();
                    Main.this.actual = Main.this.getactual();
                    Main.this.fromspin.setSelection(0);
                    Main.this.fromspin.setAdapter(new mySpinner(Main.this, R.layout.spinner, Main.this.actual));
                    Main.this.tospin.setSelection(0);
                    Main.this.tospin.setAdapter(new mySpinner(Main.this, R.layout.spinner, Main.this.provider));
                }
            }).setNegativeButton(R.string.t_button_abort, new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    Main.this.finish();
                }
            });
            dlgBuilder.create().show();
        }
        try {
            this.version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
        }
        this.prefs.edit().putString("uaname", this.appname + " " + this.version).apply();
        String v = this.prefs.getString("version", "0");
        if (!v.equals(this.version)) {
            this.prefs.edit().putString("version", this.version).apply();
        }
        myScheduler.scheduleAlarm(this);
        checkPerm.checkPermission(new String[]{"android.permission.READ_CONTACTS", "android.permission.WRITE_CONTACTS"}, this);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        this.cAdapter = new MyAdapter(this.contactList);
        mRecyclerView.setAdapter(this.cAdapter);
        this.bar = (ProgressBar) findViewById(R.id.progressBar);
        this.provider = getproviders();
        this.actual = getactual();
        this.fromspin = (Spinner) findViewById(R.id.accfrom);
        this.tospin = (Spinner) findViewById(R.id.accto);
        this.fromspin.setAdapter(new mySpinner(this, R.layout.spinner, this.actual));
        this.fromspin.setOnItemSelectedListener(new MyOnItemSelectedListener());
        this.tospin.setAdapter(new mySpinner(this, R.layout.spinner, this.provider));
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == 0) {
                    this.actual = getproviders();
                    if (this.actual.size() > 0) {
                        new taskList().execute(this.actual.get(0));
                        return;
                    }
                    return;
                }
                return;
            default:
                return;
        }
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.version).setTitle(getResources().getString(R.string.app_name) + " " + this.version);
        menu.findItem(R.id.copy).setChecked(this.prefs.getBoolean("copy", false));
        menu.findItem(R.id.stripgroups).setChecked(this.prefs.getBoolean("stripgroups", false));
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        boolean z = false;
        switch (item.getItemId()) {
            case R.id.copy:
                if (!item.isChecked()) {
                    z = true;
                }
                item.setChecked(z);
                this.prefs.edit().putBoolean("copy", item.isChecked()).apply();
                calc_selected(this, this.contactList);
                return true;
            case R.id.stripgroups:
                if (!item.isChecked()) {
                    z = true;
                }
                item.setChecked(z);
                this.prefs.edit().putBoolean("stripgroups", item.isChecked()).apply();
                return true;
            case R.id.reload_contacts:
                this.provider = getproviders();
                this.actual = getactual();
                this.fromspin.setSelection(0);
                this.fromspin.setAdapter(new mySpinner(this, R.layout.spinner, this.actual));
                this.tospin.setSelection(0);
                this.tospin.setAdapter(new mySpinner(this, R.layout.spinner, this.provider));
                return true;
            case R.id.deselect:
                for (mycList list : this.contactList) {
                    list.selected = Boolean.valueOf(!list.selected.booleanValue());
                }
                this.cAdapter.notifyDataSetChanged();
                calc_selected(this, this.contactList);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    static void calc_selected(Context context, List<mycList> contactList2) {
        int i;
        int count = 0;
        for (mycList list : contactList2) {
            if (list.selected.booleanValue()) {
                i = 1;
            } else {
                i = 0;
            }
            count += i;
        }
        try {
            TextView button = (TextView) ((Activity) context).findViewById(R.id.button);
            if (context.getSharedPreferences("default", 0).getBoolean("copy", false)) {
                button.setText(context.getString(R.string.t_copy_button, Integer.valueOf(count)));
                return;
            }
            button.setText(context.getString(R.string.t_move_button, Integer.valueOf(count)));
        } catch (Exception e) {
            exception(e, context, "");
        }
    }

    static void exception(Exception e, Context context, String prefix) {
        Log.e("CopyContacts", e.toString());
    }

    public void verschiebeKontakte(View view) {
        Spinner mySpinner = (Spinner) findViewById(R.id.accfrom);
        Spinner mySpinner2 = (Spinner) findViewById(R.id.accto);
        if (!this.provider.isEmpty() && !this.actual.isEmpty() && this.provider.get(mySpinner2.getSelectedItemPosition()) != null) {
            new taskmove(this.actual.get(mySpinner.getSelectedItemPosition()), this.provider.get(mySpinner2.getSelectedItemPosition()), this).execute();
        }
    }

    private List<myProvider> geticons(List<myProvider> contacts) {
        AuthenticatorDescription[] mAuthDescs;
        String str;
        HashMap<String, Drawable> icons = new HashMap<>();
        for (AuthenticatorDescription mAuthDesc : AccountManager.get(this).getAuthenticatorTypes()) {
            icons.put(mAuthDesc.type, getPackageManager().getDrawable(mAuthDesc.packageName, mAuthDesc.iconId, null));
        }
        for (myProvider contact : contacts) {
            Drawable icon = icons.get(contact.type);
            String str2 = this.appname;
            StringBuilder append = new StringBuilder().append(contact.type).append(" ");
            if (icon != null) {
                str = icon.toString();
            } else {
                str = null;
            }
            Log.d(str2, append.append(str).toString());
            if (icon != null) {
                contact.icon = icon;
            } else if (containsIgnoreCase(contact.type, "phone") || contact.type == null) {
                contact.icon = ContextCompat.getDrawable(this, R.mipmap.phone_icon);
            } else if (containsIgnoreCase(contact.type, "sim")) {
                contact.icon = ContextCompat.getDrawable(this, R.mipmap.sim_icon);
            }
        }
        return contacts;
    }

    private static boolean containsIgnoreCase(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        int length = searchStr.length();
        if (length == 0) {
            return true;
        }
        for (int i = str.length() - length; i >= 0; i--) {
            if (str.regionMatches(true, i, searchStr, 0, length)) {
                return true;
            }
        }
        return false;
    }

    public List<myProvider> getproviders() {
        SyncAdapterType[] types;
        Account[] accountList;
        List<myProvider> contactAccounts = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(getApplicationContext(), "android.permission.READ_CONTACTS") == 0) {
            ArrayList arrayList = new ArrayList();
            for (SyncAdapterType typ : ContentResolver.getSyncAdapterTypes()) {
                Log.d(this.appname, typ.accountType + " " + typ.authority);
                /* summersab
                if (typ.authority.equals("com.android.contacts")) {
                 */
                if (typ.authority.equals("com.android.contacts")) {// || typ.authority.equals("at.bitfire.davdroid.addressbooks")) {
                    arrayList.add(typ.accountType);
                }
            }
            for (Account anAccountList : AccountManager.get(this).getAccounts()) {
                Log.d(this.appname, anAccountList.name);
                if (arrayList.contains(anAccountList.type)) {
                    contactAccounts.add(new myProvider(anAccountList.name, anAccountList.type, anAccountList.name, null));
                }
            }
            String accountType = this.prefs.getString("phonetype", "");
            String accountName = this.prefs.getString("phonename", "");
            Log.d(this.appname, "Killroy " + accountName + " " + accountType);
            if (accountName.equals("") || accountType.equals("")) {
                ContentProviderResult[] results = null;
                ArrayList<ContentProviderOperation> ops = new ArrayList<>();
                ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI).withValue("account_name", null).withValue("account_type", null).build());
                try {
                    results = getContentResolver().applyBatch("com.android.contacts", ops);
                } catch (Exception e) {
                    exception(e, getApplicationContext(), "");
                } finally {
                    ops.clear();
                }
                if (results != null) {
                    Uri rawContactUri = results[0].uri;
                    Log.d("CopyContacts", rawContactUri.toString());
                    Cursor c = getContentResolver().query(rawContactUri, null, null, null, null);
                    if (c.moveToFirst()) {
                        accountType = c.getString(c.getColumnIndex("account_type"));
                        accountName = c.getString(c.getColumnIndex("account_name"));
                    }
                    getContentResolver().delete(rawContactUri, null, null);
                    c.close();
                    if (accountType != null) {
                        this.prefs.edit().putString("phonetype", accountType).apply();
                        this.prefs.edit().putString("phonename", accountName).apply();
                        contactAccounts.add(new myProvider(accountName, accountType, accountName, null));
                    }
                }
            } else {
                contactAccounts.add(new myProvider(accountName, accountType, accountName, null));
            }
            AccountManager.get(this).getAuthenticatorTypes();
        }
        return geticons(contactAccounts);
    }

    public List<myProvider> getactual() {
        List<myProvider> contactAccounts = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(getApplicationContext(), "android.permission.READ_CONTACTS") == 0) {
            HashMap<String, Boolean> meMap = new HashMap<>();
            Cursor cur = getContentResolver().query(RawContacts.CONTENT_URI, null, "deleted = ?", new String[]{"0"}, null);
            if (cur.getCount() > 0) {
                Log.d(this.appname, "found " + cur.getCount());
                while (cur.moveToNext()) {
                    String type = cur.getString(cur.getColumnIndex("account_type"));
                    String name = cur.getString(cur.getColumnIndex("account_name"));
                    String displayname = name;
                    boolean contactExists = false;
                    for (int i = 0; i < contactAccounts.size(); i++) {
                        if (contactAccounts.get(i).displayname.equals(displayname))
                        {
                            contactExists = true;
                            break;
                        }
                    }
                    if (!contactExists) {
                        contactAccounts.add(new myProvider(displayname, type, name, null));
                    }
                    if (!meMap.containsKey(type)) {
                        //contactAccounts.add(new myProvider(displayname, type, name, null));
                        meMap.put(type, Boolean.valueOf(true));
                    }
                }
            }
            cur.close();
        }
        return geticons(contactAccounts);
    }
}
