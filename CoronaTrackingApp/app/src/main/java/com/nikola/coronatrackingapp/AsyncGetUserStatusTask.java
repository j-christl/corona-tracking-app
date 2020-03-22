package com.nikola.coronatrackingapp;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.SAXParserFactory;

public class AsyncGetUserStatusTask extends AsyncTask<String, Integer, String> {

    @Override
    protected String doInBackground(String... strings) {

        String jwt = strings[0];

        HttpURLConnection conn = null;
        try{
            String url = "http://18.196.201.130:8080/userstatus?jwt=" + jwt;
            URL urlObj = new URL(url);
            conn = (HttpURLConnection) urlObj.openConnection();
            //conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setInstanceFollowRedirects(false);
            //conn.setRequestProperty("Content-Length", jsonStr.length() + "");
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.connect();

            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            JSONObject jsonObj = new JSONObject(result.toString());
            JSONObject payloadJsonObj = jsonObj.getJSONObject("payload");
            String status = payloadJsonObj.getString("status");

            Log.d("Corona-Tracking-App", "result from server:\n" + result.toString() + " => status=" + status);
            return status;

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return "-1";
    }
}
