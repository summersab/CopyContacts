package com.summersab.copycontacts;

import android.graphics.drawable.Drawable;

public class mycList {
    public String displayname;
    public Drawable picture;
    public String rawID;
    public Boolean selected;

    public mycList(String displayname2, Drawable picture2, Boolean selected2, String rawID2) {
        this.displayname = displayname2;
        this.picture = picture2;
        this.selected = selected2;
        this.rawID = rawID2;
    }
}
