package com.summersab.copycontacts;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

/* summersab
public class MyAdapter extends Adapter<ViewHolder> {
*/
public class MyAdapter extends Adapter<RecyclerView.ViewHolder> {
    public List<mycList> mDataset;


    public class ViewHolder extends RecyclerView.ViewHolder {
        public CheckBox check;
        public TextView displayname;
        public ImageView picture;

        public ViewHolder(View view) {
            super(view);
            this.displayname = (TextView) view.findViewById(R.id.displayname);
            this.picture = (ImageView) view.findViewById(R.id.picture);
            this.check = (CheckBox) view.findViewById(R.id.checkBox);
        }
    }

    public MyAdapter(List<mycList> myDataset) {
        this.mDataset = myDataset;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        final mycList clist = this.mDataset.get(i);
        final MyAdapter.ViewHolder myHolder = (MyAdapter.ViewHolder) holder;
        myHolder.displayname .setText(clist.displayname);
        myHolder.displayname.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolean z;
                boolean z2 = true;
                mycList myclist = clist;
                if (!clist.selected.booleanValue()) {
                    z = true;
                } else {
                    z = false;
                }
                myclist.selected = Boolean.valueOf(z);
                CheckBox checkBox = myHolder.check;
                if (myHolder.check.isChecked()) {
                    z2 = false;
                }
                checkBox.setChecked(z2);
                Main.calc_selected(view.getContext(), MyAdapter.this.mDataset);
            }
        });
        myHolder.picture.setImageDrawable(clist.picture);
        myHolder.check.setOnCheckedChangeListener(null);
        myHolder.check.setChecked(clist.selected.booleanValue());
        myHolder.check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                clist.selected = Boolean.valueOf(isChecked);
                Main.calc_selected(buttonView.getContext(), MyAdapter.this.mDataset);
            }
        });
    }

    public int getItemCount() {
        return this.mDataset.size();
    }
}
