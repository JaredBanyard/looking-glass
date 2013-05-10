/*
Copyright 2013 Jared Banyard tachyondev@tachyondev.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.tachyondev.android.glass.looking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.google.api.services.youtube.YouTubeScopes;

public class MainActivity extends Activity {

    private static String TAG = "LookingGlass";

    static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
    static final int REQUEST_AUTHORIZATION = 1;

    static final String CLIENT_ID = "23860076599-ka381igf74an4fjll153m40f9h0ubnvl.apps.googleusercontent.com";
    static final String CLIENT_SECRET = "Rs19DNVAcc8NezV2pTFg8ea_";
    static final String GRANT_TYPE = "http://oauth.net/grant_type/device/1.0";

    private Handler mHandler = new Handler();

    private static String scope = YouTubeScopes.YOUTUBE + " " + YouTubeScopes.YOUTUBE_READONLY;

    private static String email;

    private static String device_code;
    private static String user_code;
    private static String verification_url;
    private static int expires_in;
    private static int interval;
    private static String access_token;
    private static String token_type;
    private static String refresh_token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GoogleAccountManager googleAccountManager = new GoogleAccountManager(
                this);
        Account[] googleAccounts = googleAccountManager.getAccounts();
        if ((googleAccounts != null) && (googleAccounts.length > 0)) {
            email = googleAccounts[0].name;
            Log.d(TAG, "account: " + email);
        }

        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

        new AuthTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    public class AuthTask extends AsyncTask<String, Void, JSONObject>
    {

        @Override
        protected JSONObject doInBackground(String... email) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("https://accounts.google.com/o/oauth2/device/code");

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("client_id", CLIENT_ID));
                nameValuePairs.add(new BasicNameValuePair("scope", scope));

                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

                Log.e(TAG, "Status: " + response.getStatusLine());

                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                StringBuilder builder = new StringBuilder();
                for (String line = null; (line = reader.readLine()) != null;) {
                    builder.append(line).append("\n");
                }
                JSONTokener tokener = new JSONTokener(builder.toString());
                JSONObject json = new JSONObject(tokener);
                return json;
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            if (json != null) {
                Log.e(TAG, json.toString());
                try {
                    device_code = json.getString("device_code");
                    user_code = json.getString("user_code");
                    verification_url = json.getString("verification_url");
                    expires_in = json.getInt("expires_in");
                    interval = json.getInt("interval");
                    displayAuth(0);
                    new VerifyTask().execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void displayAuth(int state) {
        TextView titleView = (TextView) findViewById(R.id.tv_title);
        TextView urlView = (TextView) findViewById(R.id.tv_first);
        TextView codeView = (TextView) findViewById(R.id.tv_second);
        TextView instructionsView = (TextView) findViewById(R.id.tv_third);

        switch (state) {
            case 0:
                urlView.setText("Go to: " + verification_url);
                codeView.setText("and enter: " + user_code);
                break;
            case 1:
                instructionsView.setText("using your Glass account within " + expires_in + " seconds.");
                break;
            case 2:
                urlView.setVisibility(View.GONE);
                codeView.setText("DONE!");
                instructionsView.setVisibility(View.GONE);
                break;
        }
    }

    public class VerifyTask extends AsyncTask<String, Void, JSONObject>
    {

        @Override
        protected JSONObject doInBackground(String... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("https://accounts.google.com/o/oauth2/token");

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("client_id", CLIENT_ID));
                nameValuePairs.add(new BasicNameValuePair("client_secret", CLIENT_SECRET));
                nameValuePairs.add(new BasicNameValuePair("code", device_code));
                nameValuePairs.add(new BasicNameValuePair("grant_type", GRANT_TYPE));

                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

                Log.e(TAG, "Status: " + response.getStatusLine());

                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                StringBuilder builder = new StringBuilder();
                for (String line = null; (line = reader.readLine()) != null;) {
                    builder.append(line).append("\n");
                }
                JSONTokener tokener = new JSONTokener(builder.toString());
                JSONObject json = new JSONObject(tokener);
                return json;
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            if (json != null) {
                Log.e(TAG, json.toString());
                try {
                    access_token = json.getString("access_token");
                    token_type = json.getString("token_type");
                    refresh_token = json.getString("refresh_token");
                    expires_in = json.getInt("expires_in");
                    displayAuth(2);
                } catch (JSONException e) {
                    expires_in -= interval;
                    displayAuth(1);
                    mHandler.postDelayed(VerifyRunnable, interval * 1000);
                }
            }
        }
    }

    private Runnable VerifyRunnable = new Runnable() {

        @Override
        public void run() {
            new VerifyTask().execute();
        }
    };
    

    public class InsertTask extends AsyncTask<String, Void, JSONObject>
    {

        @Override
        protected JSONObject doInBackground(String... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("https://www.googleapis.com/youtube/v3/liveBroadcasts");

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("client_id", CLIENT_ID));
                nameValuePairs.add(new BasicNameValuePair("client_secret", CLIENT_SECRET));
                nameValuePairs.add(new BasicNameValuePair("code", device_code));
                nameValuePairs.add(new BasicNameValuePair("grant_type", GRANT_TYPE));

                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

                Log.e(TAG, "Status: " + response.getStatusLine());

                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                StringBuilder builder = new StringBuilder();
                for (String line = null; (line = reader.readLine()) != null;) {
                    builder.append(line).append("\n");
                }
                JSONTokener tokener = new JSONTokener(builder.toString());
                JSONObject json = new JSONObject(tokener);
                return json;
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            if (json != null) {
                Log.e(TAG, json.toString());
                try {
                    access_token = json.getString("access_token");
                    token_type = json.getString("token_type");
                    refresh_token = json.getString("refresh_token");
                    expires_in = json.getInt("expires_in");
                    displayAuth(2);
                } catch (JSONException e) {
                    expires_in -= interval;
                    displayAuth(1);
                    mHandler.postDelayed(VerifyRunnable, interval * 1000);
                }
            }
        }
    }

}
