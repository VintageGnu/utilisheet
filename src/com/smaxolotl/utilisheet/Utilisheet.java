package com.smaxolotl.utilisheet;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class Utilisheet extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        TabHost tabHost = getTabHost();
        TabHost.TabSpec spec;
        Intent intent;
        intent = new Intent().setClass(this, HealthPage.class);
        spec = tabHost.newTabSpec("health").setIndicator("HP").setContent(intent);
        tabHost.addTab(spec);
        
        intent = new Intent().setClass(this, DamagePage.class);
        spec = tabHost.newTabSpec("damage").setIndicator("Damage Roller").setContent(intent);
        tabHost.addTab(spec);
        
        tabHost.setCurrentTab(0);
    }
}