package com.javacodegeeks.examples.rivu.chakraborty.androidsettingsexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent modifySettings = new Intent(MainActivity.this, SettingsActivity2.class);
                startActivity(modifySettings);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String builder = ("\n" + "Perform Sync:\t" + sharedPrefs.getBoolean("perform_sync", false)) +
                "\n" + "Sync Intervals:\t" + sharedPrefs.getString("sync_interval", "-1") +
                "\n" + "Name:\t" + sharedPrefs.getString("full_name", "Not known to us") +
                "\n" + "Email Address:\t" + sharedPrefs.getString("email_address", "No EMail Address Provided") +
                "\n" + "Customized Notification Ringtone:\t" + sharedPrefs.getString("notification_ringtone", "") +
                "\n\nClick on Settings Button at bottom right corner to Modify Your Prefrences";

        TextView settingsTextView = (TextView) findViewById(R.id.settingsContent);
        settingsTextView.setText(builder);
    }
}
