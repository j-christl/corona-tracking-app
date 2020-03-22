package com.nikola.coronatrackingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;
import com.google.zxing.qrcode.encoder.QRCode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class MainActivity extends AppCompatActivity {

    String uid;
    String jwt;
    private ImageView qrImage;
    private QRGEncoder qrgEncoder;
    String TAG = "GenerateQR";

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getApplicationContext().getSharedPreferences("MyPref",0);
        editor = preferences.edit();

        if(preferences.getString(getString(R.string.userId), null)==null){

            String json = null;
            try {
                json = new AsyncReceiveJsonRegisterTask().execute().get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d("CoronaTrackingApp", "Received json: " + json);
            uid = extractUidFromJson(json);
            Log.d("CoronaTrackingApp", "Extracted uid: " + uid);
            jwt = extractJwtFromJson(json);
            Log.d("CoronaTrackingApp", "Extracted jwt: " + jwt);

            Toast.makeText(this, "First login, UID=" + uid, Toast.LENGTH_LONG).show();

            editor.putString(getString(R.string.userId), uid);
            editor.putString(getString(R.string.jwt), jwt);
            editor.commit();
        }else{
            uid = preferences.getString(getString(R.string.userId),null);
            jwt = preferences.getString(getString(R.string.jwt),null);
        }
        
        ((TextView) findViewById(R.id.userIdLabel)).setText("UID: " + (uid != null ? uid : "-1"));

        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = Math.min(width, height);
        smallerDimension = smallerDimension * 3 / 4;

        qrImage = (ImageView) findViewById(R.id.qr_image);
        qrgEncoder = new QRGEncoder(
                uid, null,
                QRGContents.Type.TEXT,
                smallerDimension);
        try {
            Bitmap bitmap = qrgEncoder.encodeAsBitmap();
            qrImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.v(TAG, e.toString());
        }

        checkUserStatus();

    }

    private void checkUserStatus() {
        String result = "NOT INITIALIZED";
        try {
            result = new AsyncGetUserStatusTask().execute(jwt).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        Log.d("Corona-Tracking-App", "Result: " + result);
        if(result.equals("4")) {
            ((Button)findViewById(R.id.importantButton)).setVisibility(View.VISIBLE);
        }
    }

    private String extractUidFromJson(String jsonString) {
        if (jsonString != null) {
            try {
                JSONObject jsonObj = new JSONObject(jsonString);
                JSONObject payloadJsonObj = jsonObj.getJSONObject("payload");
                String userId = payloadJsonObj.getString(getString(R.string.userId));
                return userId;
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return "-1";
    }

    private String extractJwtFromJson(String jsonString) {
        if (jsonString != null) {
            try {
                JSONObject jsonObj = new JSONObject(jsonString);
                JSONObject payloadJsonObj = jsonObj.getJSONObject("payload");
                String jwt = payloadJsonObj.getString(getString(R.string.jwt));
                return jwt;
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return "-1";
    }

    public void melden(View view){
        Intent intent = new Intent(this, QRScanActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( resultCode == QRScanActivity.RESULT_MISSING)
            return;
        else if ( resultCode == QRScanActivity.RESULT_OK ){
            String otherUserId = data.getStringExtra(QRScanActivity.EXTRA_CODE_USER_ID);
            String timeStamp = data.getStringExtra(QRScanActivity.EXTRA_CODE_TIMESTAMP);

            boolean worked = false;
            try {
                worked = new AsyncSendTrackTask().execute(jwt, otherUserId, timeStamp).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, worked ? " Sent track to server" : "Failed sending track to server", Toast.LENGTH_LONG).show();
            Log.d("RESULT", "SENT: " + otherUserId + " " + timeStamp + " => worked=" + worked);
        }
        else {
            //Something went wrong :(
        }
    }

    public void importantButtonClick(View view) {
        Intent intent = new Intent(this, ContactActivity.class);
        startActivityForResult(intent, 0);
    }

    public void bestPracticesClick(View view){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.bundesgesundheitsministerium.de/coronavirus.html"));
        startActivity(intent);
    }
}
