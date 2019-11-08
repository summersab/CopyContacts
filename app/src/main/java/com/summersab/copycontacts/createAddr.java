package com.summersab.copycontacts;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class createAddr extends Activity {
    static HashMap<Integer, Integer> groups = new HashMap<>();

    public static int updateContactList(Context context, String rawId, String type, String name, SharedPreferences prefs) {
        int newGID;
        ArrayList<ContentProviderOperation> newops = new ArrayList<>();
        Long valueOf = Long.valueOf(0);
        Log.i("CopyContacts", "createAddr: " + rawId);
        newops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI).withValue("account_name", name).withValue("account_type", type).build());
        Cursor cur = context.getContentResolver().query(Data.CONTENT_URI, null, "raw_contact_id = ?", new String[]{rawId}, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                if (cur.getString(cur.getColumnIndex("mimetype")).equals("vnd.android.cursor.item/group_membership")) {
                    int oldGID = cur.getInt(cur.getColumnIndex("data1"));
                    Log.i("CopyContacts", "found groupid " + oldGID);
                    if (groups.containsKey(Integer.valueOf(oldGID))) {
                        newGID = groups.get(Integer.valueOf(oldGID)).intValue();
                    } else {
                        newGID = createGroup(context, oldGID, type, name);
                    }
                    if (newGID > 0) {
                        groups.put(Integer.valueOf(oldGID), Integer.valueOf(newGID));
                        newops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI).withValueBackReference("raw_contact_id", 0).withValue("mimetype", "vnd.android.cursor.item/group_membership").withValue("data1", Integer.valueOf(newGID)).build());
                    }
                } else if (cur.getString(cur.getColumnIndex("mimetype")).equals("vnd.android.cursor.item/photo")) {
                    byte[] bArr = new byte[0];
                    if (cur.getInt(cur.getColumnIndex("data14")) > 0) {
                        try {
                            InputStream photo_stream = Contacts.openContactPhotoInputStream(context.getContentResolver(), Uri.withAppendedPath(RawContacts.CONTENT_URI, rawId), true);
                            byte[] targetArray = new byte[photo_stream.available()];
                            photo_stream.read(targetArray);
                            Log.d("CopyContacts", "Image " + targetArray.length);
                            newops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI).withValueBackReference("raw_contact_id", 0).withValue("mimetype", "vnd.android.cursor.item/photo").withValue("data15", targetArray).build());
                        } catch (Exception e) {
                            Log.e("CopyContacts_ee", e.toString());
                        }
                    } else if (cur.getBlob(cur.getColumnIndex("data15")) != null) {
                        Log.d("CopyContacts", "only thumbnail");
                        newops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI).withValueBackReference("raw_contact_id", 0).withValue("mimetype", "vnd.android.cursor.item/photo").withValue("data15", cur.getBlob(cur.getColumnIndex("data15"))).build());
                    } else {
                        Log.e("CopyContacts", "empty Image");
                    }
                } else {
                    newops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI).withValueBackReference("raw_contact_id", 0).withValue("mimetype", cur.getString(cur.getColumnIndex("mimetype"))).withValue("data1", cur.getString(cur.getColumnIndex("data1"))).withValue("data2", cur.getString(cur.getColumnIndex("data2"))).withValue("data3", cur.getString(cur.getColumnIndex("data3"))).withValue("data4", cur.getString(cur.getColumnIndex("data4"))).withValue("data5", cur.getString(cur.getColumnIndex("data5"))).withValue("data6", cur.getString(cur.getColumnIndex("data6"))).withValue("data7", cur.getString(cur.getColumnIndex("data7"))).withValue("data8", cur.getString(cur.getColumnIndex("data8"))).withValue("data9", cur.getString(cur.getColumnIndex("data9"))).withValue("data10", cur.getString(cur.getColumnIndex("data10"))).withValue("data11", cur.getString(cur.getColumnIndex("data11"))).withValue("data12", cur.getString(cur.getColumnIndex("data12"))).withValue("data13", cur.getString(cur.getColumnIndex("data13"))).withValue("data14", cur.getString(cur.getColumnIndex("data14"))).withValue("data15", cur.getString(cur.getColumnIndex("data15"))).build());
                }
            }
            cur.close();
            try {
                Log.i("CopyContacts", "Added count " + context.getContentResolver().applyBatch("com.android.contacts", newops)[0].count);
                if (!prefs.getBoolean("copy", false)) {
                    context.getContentResolver().delete(Uri.withAppendedPath(RawContacts.CONTENT_URI, rawId), null, null);
                }
                return 1;
            } catch (Exception e2) {
                Log.e("CopyContacts", "error add " + e2.toString());
                return 0;
            }
        } else {
            Log.e("CopyContacts", "error no data");
            return 0;
        }
    }

    private static int createGroup(Context context, int oldGID, String type, String name) {
        Log.d("CopyContacts", "Searching title for group " + oldGID);
        Cursor cursor = context.getContentResolver().query(Uri.withAppendedPath(Groups.CONTENT_URI, Integer.toString(oldGID)), new String[]{"title"}, null, null, null);
        if (cursor.moveToFirst()) {
            String title = cursor.getString(0);
            Log.d("CopyContacts", "Searching group " + title + " for " + name);
            Cursor cursor2 = context.getContentResolver().query(Groups.CONTENT_URI, new String[]{"_id"}, "deleted=0 AND title =? AND account_type=? AND account_name=?", new String[]{title, type, name}, null);
            if (cursor2.moveToFirst()) {
                Log.d("CopyContacts", "Found group id" + cursor2.getString(0));
                return cursor2.getInt(0);
            }
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            Log.d("CopyContacts", "Creating new group " + title + " for " + name);
            ops.add(ContentProviderOperation.newInsert(Groups.CONTENT_URI).withValue("title", title).withValue("account_type", type).withValue("account_name", name).withValue("should_sync", Boolean.valueOf(true)).build());
            try {
                return (int) ContentUris.parseId(context.getContentResolver().applyBatch("com.android.contacts", ops)[0].uri);
            } catch (Exception e) {
                Log.e("Error", e.toString());
            }
            return 1;
        } else {
            Log.d("CopyContacts", "no group" + oldGID + " found for " + name);
            return 0;
        }
    }
}
