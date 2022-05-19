package com.rumireakharitonovpractice7.httpurlconnection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private TextView textViewIp;
    private TextView textViewRegion;
    private TextView textViewCountry;
    private TextView textViewCity;
    private EditText editTextURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewIp = findViewById(R.id.textViewIP);
        textViewRegion = findViewById(R.id.textViewRegion);
        textViewCountry = findViewById(R.id.textViewCountry);
        textViewCity = findViewById(R.id.textViewCity);
        editTextURL = findViewById(R.id.editTextURL);
    }

    public void onClick(View view) {
        String url = editTextURL.getText().toString();
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = null;
        if (connectivityManager != null) {
            networkinfo = connectivityManager.getActiveNetworkInfo();
        }
        if (networkinfo != null && networkinfo.isConnected()) {
            new DownloadPageTask().execute(url); // запускаем в новом потоке
        } else {
            Toast.makeText(this, "Нет интернета", Toast.LENGTH_SHORT).show();
        }
    }

    private class DownloadPageTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadIpInfo(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return "error";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("JSON", result);
            try {
                JSONObject responseJson = new JSONObject(result);
                Log.d(MainActivity.class.getSimpleName(), String.valueOf(responseJson));
                String ip = responseJson.getString("query");
                String country = responseJson.getString("country");
                String regionName = responseJson.getString("regionName");
                String city = responseJson.getString("city");

                textViewIp.setText(ip);
                textViewRegion.setText(regionName);
                textViewCountry.setText(country);
                textViewCity.setText(city);
                Log.d(MainActivity.class.getSimpleName(), ip);
            } catch (JSONException e) {
                Log.d("JSON", "parsing error " + e.getLocalizedMessage());
                e.printStackTrace();
            }
            super.onPostExecute(result);
        }
    }

    private String downloadIpInfo(String address) throws IOException {
        InputStream inputStream = null;
        String data = "";
        try {
            URL url = new URL(address);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setReadTimeout(100000);
            connection.setConnectTimeout(100000);
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            int responseCode = connection.getResponseCode();
            Log.d("JSON","Привет");
            Log.d("JSON", String.valueOf(responseCode));
            Log.d("JSON", String.valueOf(responseCode == HttpURLConnection.HTTP_OK));
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d("JSON", String.valueOf(responseCode));
                inputStream = connection.getInputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int read = 0;
                while ((read = inputStream.read()) != -1) {
                    bos.write(read);
                }
                bos.close();
                data = bos.toString();
                Log.d("JSON","data: " +  data);

            } else {
                data = connection.getResponseMessage() + " . Error Code : " + responseCode;
            }
            connection.disconnect();
            //return data;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return data;
    }
}