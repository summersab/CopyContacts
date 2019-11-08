package com.summersab.copycontacts;

import android.app.Activity;
import android.os.Build.VERSION;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;

public class checkPerm extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    static void checkPermission(String[] Manifests, Activity activity) {
        if (VERSION.SDK_INT >= 23) {
            for (String Manifest : Manifests) {
                if (activity.checkSelfPermission(Manifest) != 0) {
                    ActivityCompat.requestPermissions(activity, Manifests, 1);
                    return;
                }
            }
        }
    }
}
