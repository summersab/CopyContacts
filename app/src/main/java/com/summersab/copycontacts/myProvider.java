package com.summersab.copycontacts;

import android.graphics.drawable.Drawable;

public class myProvider {
    public String displayname;
    public Drawable icon;
    public String name;
    public String type;

    public myProvider(String displayname2, String type2, String name2, Drawable icon2) {
        this.displayname = displayname2;
        this.type = type2;
        this.name = name2;
        this.icon = icon2;
    }

    public String toString() {
        return this.displayname;
    }
}
