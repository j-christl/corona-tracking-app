package com.nikola.coronatrackingapp;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

public class ContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
    }

    SharedPreferences preferences;

    public void sendData(View view){
        preferences = getApplicationContext().getSharedPreferences("MyPref",0);

        EditText n = findViewById(R.id.sendFirstName);
        String vorName = n.getText().toString();
        n = findViewById(R.id.sendLastName);
        String nachName = n.getText().toString();
        EditText t = findViewById(R.id.sendNumber);
        String telefonnummer = t.getText().toString();
        String jwt = preferences.getString(getString(R.string.jwt),null);

        boolean result=false;

        try {
             result = new AsyncSendInfectedInformation().execute(jwt, vorName, nachName, telefonnummer).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Toast.makeText(this, (result ? "Informationen Hochgeladen" : "Hochladen fehlgeschlagen"), Toast.LENGTH_LONG).show();
        Log.d("Corona-Tracking-App", "Result: " + result);
    }
}
