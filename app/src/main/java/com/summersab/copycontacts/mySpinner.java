package com.summersab.copycontacts;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class mySpinner extends ArrayAdapter<myProvider> {
    ViewHolder holder = null;
    LayoutInflater inflater;
    List<myProvider> objects;

    static class ViewHolder {
        ImageView imgThumb;
        TextView name;

        ViewHolder() {
        }
    }

    public mySpinner(Context context, int textViewResourceId, List<myProvider> objects2) {
        super(context, textViewResourceId, objects2);
        this.inflater = ((Activity) context).getLayoutInflater();
        this.objects = objects2;
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        myProvider listItem = this.objects.get(position);
        View row = convertView;
        if (row == null) {
            this.holder = new ViewHolder();
            row = this.inflater.inflate(R.layout.spinner, parent, false);
            this.holder.name = (TextView) row.findViewById(R.id.to_text);
            this.holder.imgThumb = (ImageView) row.findViewById(R.id.to_icon);
            row.setTag(this.holder);
        } else {
            this.holder = (ViewHolder) row.getTag();
        }
        this.holder.name.setText(listItem.displayname);
        this.holder.imgThumb.setImageDrawable(listItem.icon);
        return row;
    }
}
