package com.nikola.coronatrackingapp;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.HashSet;
import java.util.Set;

/*
Handles scanning of QR-Codes and stores explored user-ids.
TODO Find better places for user-ids than Shared Preferences
 */

public class QRScanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_r_scan);





    }








    private void storeUserId(String userId){
        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences),MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        Set<String> otherIds = preferences.getStringSet(getString(R.string.otherIds), null);

        if (otherIds == null)
            otherIds = new HashSet<>();

        otherIds.add(userId);

        editor.putStringSet(getString(R.string.otherIds), otherIds);
        editor.commit();
    }
}
