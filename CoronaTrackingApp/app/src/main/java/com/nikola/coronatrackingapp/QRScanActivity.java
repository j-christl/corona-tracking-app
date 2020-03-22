package com.nikola.coronatrackingapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

public class QRScanActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private Button back;
    private Button done;
    private SurfaceView surfaceView;
    private TextView qrValue;
    private EditText editText;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;

    private String userId;
    private Timestamp timestamp;

    private static final int REQUEST_CAMERA_PERMISSION = 201;

    public static final int RESULT_OK = 0;
    public static final int RESULT_MISSING = 1;
    public static final String EXTRA_CODE_USER_ID = "userId";
    public static final String EXTRA_CODE_TIMESTAMP = "timeStamp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_r_scan);

        initViews();

        if (ActivityCompat.checkSelfPermission(QRScanActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            initialiseDetectorsAndSources();
        } else {
            ActivityCompat.requestPermissions(QRScanActivity.this, new
                    String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }

    }

    private void initViews() {
        qrValue = findViewById(R.id.qrValue);
        surfaceView = findViewById(R.id.surfaceView);
        back = findViewById(R.id.backButton);
        done = findViewById(R.id.done);
        editText = findViewById(R.id.editText);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                done();
            }
        });
    }

    private void initialiseDetectorsAndSources() {

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(QRScanActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(QRScanActivity.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {}

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {

                    qrValue.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("WICHTIG",barcodes.valueAt(0).displayValue);
                            userId = barcodes.valueAt(0).displayValue;
                            try {

                                Integer.parseInt(userId);

                                qrValue.setText(userId);
                                qrValue.setTextColor(Color.GREEN);


                                ImageView imageView = findViewById(R.id.imageView);
                                imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_black_24dp));
                                imageView.setColorFilter(Color.GREEN);

                                TextView succesTextLabel = findViewById(R.id.successTextLabel);
                                succesTextLabel.setTextColor(Color.GREEN);
                                succesTextLabel.setText("User-ID erkannt");

                                cameraSource.stop();
                            } catch (Exception e){
                                Log.d("ERROR", "Wrong user id format");
                                qrValue.setText(userId);
                                qrValue.setTextColor(Color.RED);

                                final ImageView imageView = findViewById(R.id.imageView);
                                imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_clear_black_24dp));
                                imageView.setColorFilter(Color.RED);

                                final TextView succesTextLabel = findViewById(R.id.successTextLabel);
                                succesTextLabel.setTextColor(Color.RED);
                                succesTextLabel.setText("Keine gültige User-ID!");

                                new CountDownTimer(1000, 1000) {
                                    public void onTick(long millisUntilFinished) {
                                    }
                                    public void onFinish() {
                                        imageView.setImageDrawable(null);
                                        succesTextLabel.setTextColor(Color.BLACK);
                                        succesTextLabel.setText("Suche QR-Code");
                                    }
                                }.start();
                            }
                        }
                    });
                }
            }
        });
    }

    /*
    @Override
    protected void onPause() {
        super.onPause();
        cameraSource.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();
    }*/

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

    private void back(){
        Intent intent = new Intent(QRScanActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void done(){
        if (userId == null){
            if (checkForValidUserId(editText.getText().toString())) {
                userId = editText.getText().toString();
                storeUserId(userId);
            }
            else {
                Toast toast = Toast.makeText(this, "Ungültige User-ID.\rKeine Meldung erstellt!" + userId, Toast.LENGTH_SHORT);
                toast.show();

                Intent returnIntent = new Intent();
                setResult(RESULT_MISSING, returnIntent);
                finish();

                return;
            }
        }

        Toast toast = Toast.makeText(this, "Meldung erfolgreich erstellt!\rUser-ID: " + userId, Toast.LENGTH_SHORT);
        toast.show();

        Intent returnIntent = new Intent();
        returnIntent.putExtra(EXTRA_CODE_USER_ID, userId);
        returnIntent.putExtra(EXTRA_CODE_TIMESTAMP, new Timestamp(System.currentTimeMillis()).toString());
        setResult(RESULT_OK,returnIntent);
        finish();

    }

    private boolean checkForValidUserId(String userId){
        try {
            Integer.parseInt(userId);
            return true;
        } catch (NumberFormatException e){
            return false;
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults){
        Intent intent = new Intent(this, QRScanActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Intent returnIntent = new Intent();
        returnIntent.putExtra(EXTRA_CODE_USER_ID,data.getStringExtra(EXTRA_CODE_USER_ID));
        returnIntent.putExtra(EXTRA_CODE_TIMESTAMP,data.getStringExtra(EXTRA_CODE_TIMESTAMP));
        setResult(RESULT_OK,returnIntent);
        finish();
    }
}



