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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;

import android.accounts.Account;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

//import com.google.api.client.extensions.android.http.AndroidHttp;
//import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
//import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
//import com.google.api.client.http.HttpResponse;
//import com.google.api.client.http.HttpTransport;
//import com.google.api.client.json.JsonFactory;
//import com.google.api.client.json.gson.GsonFactory;
//import com.google.api.services.youtube.YouTube;
//import com.google.api.services.youtube.YouTubeScopes;
//import com.google.android.gms.common.GooglePlayServicesUtil;

//import com.google.common.collect.Lists;

public class MainActivity extends Activity {

    private static String TAG = "LookingGlass";
    private static String AUTH_TOKEN_TYPE = "youtube";

    static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
    static final int REQUEST_AUTHORIZATION = 1;

    static final String CLIENT_ID = "23860076599-ka381igf74an4fjll153m40f9h0ubnvl.apps.googleusercontent.com";

//    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
//
//    final JsonFactory jsonFactory = new GsonFactory();

    /** Global instance of Youtube object to make all API requests. */
    private static YouTube youtube;

//    GoogleAccountCredential credential;

    private static String scope = YouTubeScopes.YOUTUBE + " " +
            YouTubeScopes.YOUTUBE_READONLY + " " +
            YouTubeScopes.YOUTUBE_UPLOAD + " " +
            YouTubeScopes.YOUTUBEPARTNER;

    private static String email;

    private static String device_code;
    private static String user_code;
    private static String verification_url;
    private static int expires_in;
    private static int interval;

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
                nameValuePairs.add(new BasicNameValuePair("scope", YouTubeScopes.YOUTUBE));

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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }
        

        @Override
        protected void onPostExecute(JSONObject json) {
            if (json != null) {
                try {
                    device_code = json.getString("device_code");
                    user_code = json.getString("user_code");
                    verification_url = json.getString("verification_url");
                    expires_in = json.getInt("expires_in");
                    interval = json.getInt("interval");
                    displayAuth();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } 
        }
    }
    
    private void displayAuth() {
        TextView titleView = (TextView) findViewById(R.id.tv_title);
        TextView urlView = (TextView) findViewById(R.id.tv_first);
        TextView codeView = (TextView) findViewById(R.id.tv_second);
        TextView instructionsView = (TextView) findViewById(R.id.tv_third);     
        
        urlView.setText("Go to: " + verification_url);
        codeView.setText("and enter: " + user_code);
        instructionsView.setText("using your Glass account within " + expires_in + " seconds.");
    }

//    
//    public static HttpResponse makeRequest(String path, Map params) throws Exception 
//    {
//        //instantiates httpclient to make request
//        DefaultHttpClient httpclient = new DefaultHttpClient();
//
//        //url with the post data
//        HttpPost httpost = new HttpPost(path);
//
//        //convert parameters into JSON object
//        JSONObject holder = getJsonObjectFromMap(params);
//
//        //passes the results to a string builder/entity
//        StringEntity se = new StringEntity(holder.toString());
//
//        //sets the post request as the resulting string
//        httpost.setEntity(se);
//        //sets a request header so the page receving the request
//        //will know what to do with it
//        httpost.setHeader("Accept", "application/json");
//        httpost.setHeader("Content-type", "application/json");
//
//        //Handles what is returned from the page 
//        ResponseHandler responseHandler = new BasicResponseHandler();
//        return httpclient.execute(httpost, responseHandler);
//    }

//    public void test() {
//        try {
//            // Create request to list broadcasts.
//            YouTube.LiveBroadcasts.List liveBroadcastRequest =
//                    youtube.liveBroadcasts().list("id,snippet");
//
//            // Modify results to have broadcasts in all states.
//            liveBroadcastRequest.setBroadcastStatus("all");
//
//            // List request is executed and list of broadcasts are returned
//            LiveBroadcastList returnedListResponse = liveBroadcastRequest.execute();
//
//            // Get the list of broadcasts associated with the user.
//            List<LiveBroadcast> returnedList = returnedListResponse.getItems();
//
//            // Print out returned results.
//            System.out.println("\n================== Returned Broadcasts ==================\n");
//            for (LiveBroadcast broadcast : returnedList) {
//                System.out.println("  - Id: " + broadcast.getId());
//                System.out.println("  - Title: " + broadcast.getSnippet().getTitle());
//                System.out.println("  - Description: " + broadcast.getSnippet().getDescription());
//                System.out.println("  - Published At: " + broadcast.getSnippet().getPublishedAt());
//                System.out.println(
//                        "  - Scheduled Start Time: " + broadcast.getSnippet().getScheduledStartTime());
//                System.out.println(
//                        "  - Scheduled End Time: " + broadcast.getSnippet().getScheduledEndTime());
//                System.out.println("\n-------------------------------------------------------------\n");
//            }
//        } catch (Exception e) {
//
//        }
//    }
//
//    public void start() throws IOException {
//        String title = "Test Broadcast";
//        Log.e(TAG, "You chose " + title + " for broadcast title.");
//
//        // Create a snippet with title, scheduled start and end times.
//        LiveBroadcastSnippet broadcastSnippet = new LiveBroadcastSnippet();
//        broadcastSnippet.setTitle(title);
//        broadcastSnippet.setScheduledStartTime(new DateTime("2024-01-30T00:00:00.000Z"));
//        broadcastSnippet.setScheduledEndTime(new DateTime("2024-01-31T00:00:00.000Z"));
//
//        // Create LiveBroadcastStatus with privacy status.
//        LiveBroadcastStatus status = new LiveBroadcastStatus();
//        status.setPrivacyStatus("private");
//
//        LiveBroadcast broadcast = new LiveBroadcast();
//        broadcast.setKind("youtube#liveBroadcast");
//        broadcast.setSnippet(broadcastSnippet);
//        broadcast.setStatus(status);
//
//        // Create the insert request
//        YouTube.LiveBroadcasts.Insert liveBroadcastInsert =
//                youtube.liveBroadcasts().insert("snippet,status", broadcast);
//
//        // Request is executed and inserted broadcast is returned
//        LiveBroadcast returnedBroadcast = liveBroadcastInsert.execute();
//
//        // Print out returned results.
//        Log.e(TAG, "\n================== Returned Broadcast ==================\n");
//        Log.e(TAG, "  - Id: " + returnedBroadcast.getId());
//        Log.e(TAG, "  - Title: " + returnedBroadcast.getSnippet().getTitle());
//        Log.e(TAG, "  - Description: " + returnedBroadcast.getSnippet().getDescription());
//        Log.e(TAG, "  - Published At: " + returnedBroadcast.getSnippet().getPublishedAt());
//        Log.e(TAG,
//                "  - Scheduled Start Time: " + returnedBroadcast.getSnippet().getScheduledStartTime());
//        Log.e(TAG,
//                "  - Scheduled End Time: " + returnedBroadcast.getSnippet().getScheduledEndTime());
//
//        // Get the user's selected title for stream.
//        title = "Test Stream";
//        Log.e(TAG, "You chose " + title + " for stream title.");
//
//        // Create a snippet with title.
//        LiveStreamSnippet streamSnippet = new LiveStreamSnippet();
//        streamSnippet.setTitle(title);
//
//        // Create content distribution network with format and ingestion type.
//        LiveStreamCdn cdn = new LiveStreamCdn();
//        cdn.setFormat("1080p");
//        cdn.setIngestionType("rtmp");
//
//        LiveStream stream = new LiveStream();
//        stream.setKind("youtube#liveStream");
//        stream.setSnippet(streamSnippet);
//        stream.setCdn(cdn);
//
//        // Create the insert request
//        YouTube.LiveStreams.Insert liveStreamInsert =
//                youtube.liveStreams().insert("snippet,cdn", stream);
//
//        // Request is executed and inserted stream is returned
//        LiveStream returnedStream = liveStreamInsert.execute();
//
//        // Print out returned results.
//        Log.e(TAG, "\n================== Returned Stream ==================\n");
//        Log.e(TAG, "  - Id: " + returnedStream.getId());
//        Log.e(TAG, "  - Title: " + returnedStream.getSnippet().getTitle());
//        Log.e(TAG, "  - Description: " + returnedStream.getSnippet().getDescription());
//        Log.e(TAG, "  - Published At: " + returnedStream.getSnippet().getPublishedAt());
//
//        // Create the bind request
//        YouTube.LiveBroadcasts.Bind liveBroadcastBind =
//                youtube.liveBroadcasts().bind(returnedBroadcast.getId(), "id,contentDetails");
//
//        // Set stream id to bind
//        liveBroadcastBind.setStreamId(returnedStream.getId());
//
//        // Request is executed and bound broadcast is returned
//        returnedBroadcast = liveBroadcastBind.execute();
//
//        // Print out returned results.
//        Log.e(TAG, "\n================== Returned Bound Broadcast ==================\n");
//        Log.e(TAG, "  - Broadcast Id: " + returnedBroadcast.getId());
//        Log.e(TAG, "  - Bound Stream Id: " + returnedBroadcast.getContentDetails().getBoundStreamId());
//
//    }
//
//    private void search() {
//        try {
//
//            // Get query term from user.
//            String queryTerm = "turtles";
//
//            YouTube.Search.List search = youtube.search().list("id,snippet");
//
//            /*
//             * It is important to set your developer key from the Google Developer Console for
//             * non-authenticated requests (found under the API Access tab at this link:
//             * code.google.com/apis/). This is good practice and increased your quota.
//             */
////            search.setKey(apiKey);
//            search.setQ(queryTerm);
//            /*
//             * We are only searching for videos (not playlists or channels). If we were searching for
//             * more, we would add them as a string like this: "video,playlist,channel".
//             */
//            search.setType("video");
//            /*
//             * This method reduces the info returned to only the fields we need and makes calls more
//             * efficient.
//             */
//            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
//            search.setMaxResults((long) 25);
//            SearchListResponse searchResponse = search.execute();
//
//            List<SearchResult> searchResultList = searchResponse.getItems();
//
//            if (searchResultList != null) {
////              prettyPrint(searchResultList.iterator(), queryTerm);
//                for (SearchResult result : searchResultList) {
//                    Log.e(TAG, result.toPrettyString());
//                }
//            }
//        } catch (GoogleJsonResponseException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (Throwable t) {
//            t.printStackTrace();
//        }
//    }
}
