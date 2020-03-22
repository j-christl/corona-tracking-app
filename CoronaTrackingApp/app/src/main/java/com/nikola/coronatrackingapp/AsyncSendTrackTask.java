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

import javax.xml.parsers.SAXParserFactory;

public class AsyncSendTrackTask extends AsyncTask<String, Integer, Boolean> {

    @Override
    protected Boolean doInBackground(String... strings) {

        String jwt = strings[0];
        String otherId = strings[1];
        String timeString = strings[2];


        // {"positions": [[1, 2, "2016-11-16 06:55:40.11"]], "contacts": [[2, "2016-11-16 06:55:40.11"]]}
        String jsonStr = "{\"positions\": [[1, 2, \"" + timeString + "\"]], \"contacts\": [[" + otherId + ", \"" + timeString + "\"]]}";

        HttpURLConnection conn = null;
        try{
            String url = "http://18.196.201.130:8080/track?jwt=" + jwt;
            URL urlObj = new URL(url);
            conn = (HttpURLConnection) urlObj.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            //conn.setRequestProperty("Content-Length", jsonStr.length() + "");
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);

            // https://stackoverflow.com/questions/9365829/filenotfoundexception-for-httpurlconnection-in-ice-cream-sandwich
            conn.setInstanceFollowRedirects(false);
            
            conn.connect();

            PrintWriter pw = new PrintWriter(new OutputStreamWriter(conn.getOutputStream()));
            pw.println(jsonStr);
            pw.flush();

            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            System.out.println("result from server:\n" + result.toString());
            // TODO check status code if it worked. If so, return true. If not, return false
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
