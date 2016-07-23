package com.myapps.documentscanner;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.support.v7.app.AppCompatActivity;

import com.myapps.documentscanner.helpers.Utils;

public class SettingsActivity extends AppCompatActivity {

    private SettingsFragment sf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        DocumentScannerApplication.getInstance().trackScreenView("Settings");
        FragmentManager fm=getFragmentManager();
        FragmentTransaction ft=fm.beginTransaction();

        sf=new SettingsFragment();
        ft.replace(android.R.id.content, sf);
        ft.commit();
    }

}
