package com.nikola.coronatrackingapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class QRScanActivity extends AppCompatActivity {

    Button retry;
    Button back;
    Button done;
    SurfaceView surfaceView;
    TextView qrValue;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_r_scan);

        initViews();
    }

    private void initViews() {
        qrValue = findViewById(R.id.qrValue);
        surfaceView = findViewById(R.id.surfaceView);

        retry = findViewById(R.id.retry);
        back = findViewById(R.id.backButton);
        done = findViewById(R.id.done);

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retry();
            }
        });

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

        Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();

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
            public void release() {
                Toast.makeText(getApplicationContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {

                    qrValue.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("WICHTIG",barcodes.valueAt(0).displayValue);
                            String userId = barcodes.valueAt(0).displayValue;
                            try {

                                Integer.parseInt(userId);

                                qrValue.setText(userId);
                                qrValue.setTextColor(Color.GREEN);

                                storeUserId(userId);
                                cameraSource.stop();
                            } catch (NumberFormatException e){
                                Log.d("ERROR", "Wrong user id format");
                                qrValue.setText(userId);
                                qrValue.setTextColor(Color.RED);
                            }


                        }
                    });

                }
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        cameraSource.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();
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

    private void back(){
        Intent intent = new Intent(QRScanActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void retry(){
        Intent intent = new Intent(QRScanActivity.this, QRScanActivity.class);
        startActivity(intent);
    }

    private void done(){
        Intent intent = new Intent(QRScanActivity.this, MainActivity.class);
        startActivity(intent);

        Toast toast = Toast.makeText(this, "Meldung erfolgreich erstellt!", Toast.LENGTH_SHORT);
        toast.show();
    }
}



