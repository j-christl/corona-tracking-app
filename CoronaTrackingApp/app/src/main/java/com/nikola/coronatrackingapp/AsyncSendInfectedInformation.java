package com.nikola.coronatrackingapp;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class AsyncSendInfectedInformation extends AsyncTask<String, Integer, Boolean> {
    @Override
    protected Boolean doInBackground(String... strings) {

        String jwt = strings[0];
        String vorname = strings[1];
        String nachname = strings[2];
        String nummer = strings[3];

        HttpURLConnection conn = null;
        try{
            String url = "http://18.196.201.130:8080/infected?jwt="+jwt+"&firstname="+vorname+"&lastname="+nachname+"&phonenumber="+nummer;
            URL urlObj = new URL(url);
            conn = (HttpURLConnection) urlObj.openConnection();
            //conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setInstanceFollowRedirects(false);
            //conn.setRequestProperty("Content-Length", jsonStr.length() + "");
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.connect();

            PrintWriter pw = new PrintWriter(new OutputStreamWriter(conn.getOutputStream()));

            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            Log.d("Corona-Tracking-App", "result from server:\n" + result.toString());
            return true;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return false;
    }
}
