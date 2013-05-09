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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

//import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.LiveBroadcast;
import com.google.api.services.youtube.model.LiveBroadcastList;
import com.google.api.services.youtube.model.LiveBroadcastSnippet;
import com.google.api.services.youtube.model.LiveBroadcastStatus;
import com.google.api.services.youtube.model.LiveStream;
import com.google.api.services.youtube.model.LiveStreamCdn;
import com.google.api.services.youtube.model.LiveStreamSnippet;
//import com.google.common.collect.Lists;

public class MainActivity extends Activity {

    private static String TAG = "LookingGlass";
    private static String AUTH_TOKEN_TYPE = "youtube";
    private static String DEVELOPER_KEY = "AIzaSyB6hjJoY4GK_3macccQXtFl5GYjXnSPFfU";

    /** Global instance properties filename. */
    private static String PROPERTIES_FILENAME = "youtube.properties";

    static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;

    /** Global instance of the HTTP transport. */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();

    /**
     * Global instance of the max number of videos we want returned (50 = upper
     * limit per page).
     */
    private static final long NUMBER_OF_VIDEOS_RETURNED = 25;

    /** Global instance of Youtube object to make all API requests. */

//    private static YouTube youtube;

    com.google.api.services.youtube.YouTube youtube;
    // com.google.api.client.auth.oauth2

    // GoogleCredential credential = new GoogleCredential();

    GoogleAccountCredential credential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AccountManager accountManager = AccountManager.get(this);
        // Account[] accounts = accountManager.getAccountsByType("com.google");
        //
        // for(Account a : accounts) {
        // Log.e(TAG, "account: " + a.name + ", " + a.type);
        // }

        GoogleAccountManager googleAccountManager = new GoogleAccountManager(
                this);
        Account[] googleAccounts = googleAccountManager.getAccounts();

        // for(Account a : googleAccounts) {
        // Log.e(TAG, "googleAccount: " + a.name + ", " + a.type);
        // }

        Account a = googleAccounts[0];

        Log.e(TAG, "account: " + a.name + ", " + a.type);
//
//        try {
//            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
//                    JSON_FACTORY, MainActivity.class
//                            .getResourceAsStream("/client_secrets.json"));
//
//            // Checks that the defaults have been replaced (Default =
//            // "Enter X here").
//            if (clientSecrets.getDetails().getClientId().startsWith("Enter")
//                    || clientSecrets.getDetails().getClientSecret()
//                            .startsWith("Enter ")) {
//                // Log.e(TAG, 
//                // "Enter Client ID and Secret from https://code.google.com/apis/console/?api=youtube"
//                // +
//                // "into youtube-cmdline-listbroadcasts-sample/src/main/resources/client_secrets.json");
//            } else {
//                Log.e(TAG, clientSecrets.getDetails().getClientId());
//                Log.e(TAG, "clientSecrets.getDetails().getClientSecret()");
//            }
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

        credential = GoogleAccountCredential.usingOAuth2(this, YouTubeScopes.YOUTUBE, YouTubeScopes.YOUTUBE_READONLY);
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        credential.setSelectedAccountName(settings.getString(a.name, null));
        youtube = new com.google.api.services.youtube.YouTube.Builder(HTTP_TRANSPORT,
                        JSON_FACTORY, credential)
                        .setApplicationName("LookingGlass/1.0").build();
        
        new StartTask().execute();
//        start();
//        
//        service.

//        checkGooglePlayServicesAvailable();

    }

//    private boolean checkGooglePlayServicesAvailable() {
//        final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
//        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
//            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
//            return false;
//        }
//        return true;
//    }
//
//    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
//        runOnUiThread(new Runnable() {
//
//            public void run() {
//                Dialog dialog =
//                        GooglePlayServicesUtil.getErrorDialog(connectionStatusCode, MainActivity.this,
//                                REQUEST_GOOGLE_PLAY_SERVICES);
//                dialog.show();
//            }
//        });
//    }
    
    public class StartTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            start();
            return null;
        }
        
    }

    public void start() {

        // Scope required to wrie data to YouTube.
//        List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");

        try {
            // Authorization.
//            Credential credential = authorize(scopes);
//            authoriz

            // YouTube object used to make all API requests.
//            youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(
//                    "youtube-cmdline-createbroadcast-sample").build();

            // Get the user's selected title for broadcast.
            String title = "Test Broadcast";
            Log.e(TAG, "You chose " + title + " for broadcast title.");

            // Create a snippet with title, scheduled start and end times.
            LiveBroadcastSnippet broadcastSnippet = new LiveBroadcastSnippet();
            broadcastSnippet.setTitle(title);
            broadcastSnippet.setScheduledStartTime(new DateTime("2024-01-30T00:00:00.000Z"));
            broadcastSnippet.setScheduledEndTime(new DateTime("2024-01-31T00:00:00.000Z"));

            // Create LiveBroadcastStatus with privacy status.
            LiveBroadcastStatus status = new LiveBroadcastStatus();
            status.setPrivacyStatus("private");

            LiveBroadcast broadcast = new LiveBroadcast();
            broadcast.setKind("youtube#liveBroadcast");
            broadcast.setSnippet(broadcastSnippet);
            broadcast.setStatus(status);

            // Create the insert request
            YouTube.LiveBroadcasts.Insert liveBroadcastInsert =
                    youtube.liveBroadcasts().insert("snippet,status", broadcast);

            // Request is executed and inserted broadcast is returned
            LiveBroadcast returnedBroadcast = liveBroadcastInsert.execute();

            // Print out returned results.
            Log.e(TAG, "\n================== Returned Broadcast ==================\n");
            Log.e(TAG, "  - Id: " + returnedBroadcast.getId());
            Log.e(TAG, "  - Title: " + returnedBroadcast.getSnippet().getTitle());
            Log.e(TAG, "  - Description: " + returnedBroadcast.getSnippet().getDescription());
            Log.e(TAG, "  - Published At: " + returnedBroadcast.getSnippet().getPublishedAt());
            Log.e(TAG, 
                    "  - Scheduled Start Time: " + returnedBroadcast.getSnippet().getScheduledStartTime());
            Log.e(TAG, 
                    "  - Scheduled End Time: " + returnedBroadcast.getSnippet().getScheduledEndTime());

            // Get the user's selected title for stream.
            title = "Test Stream";
            Log.e(TAG, "You chose " + title + " for stream title.");

            // Create a snippet with title.
            LiveStreamSnippet streamSnippet = new LiveStreamSnippet();
            streamSnippet.setTitle(title);

            // Create content distribution network with format and ingestion type.
            LiveStreamCdn cdn = new LiveStreamCdn();
            cdn.setFormat("1080p");
            cdn.setIngestionType("rtmp");

            LiveStream stream = new LiveStream();
            stream.setKind("youtube#liveStream");
            stream.setSnippet(streamSnippet);
            stream.setCdn(cdn);

            // Create the insert request
            YouTube.LiveStreams.Insert liveStreamInsert =
                    youtube.liveStreams().insert("snippet,cdn", stream);

            // Request is executed and inserted stream is returned
            LiveStream returnedStream = liveStreamInsert.execute();

            // Print out returned results.
            Log.e(TAG, "\n================== Returned Stream ==================\n");
            Log.e(TAG, "  - Id: " + returnedStream.getId());
            Log.e(TAG, "  - Title: " + returnedStream.getSnippet().getTitle());
            Log.e(TAG, "  - Description: " + returnedStream.getSnippet().getDescription());
            Log.e(TAG, "  - Published At: " + returnedStream.getSnippet().getPublishedAt());

            // Create the bind request
            YouTube.LiveBroadcasts.Bind liveBroadcastBind =
                    youtube.liveBroadcasts().bind(returnedBroadcast.getId(), "id,contentDetails");

            // Set stream id to bind
            liveBroadcastBind.setStreamId(returnedStream.getId());

            // Request is executed and bound broadcast is returned
            returnedBroadcast = liveBroadcastBind.execute();

            // Print out returned results.
            Log.e(TAG, "\n================== Returned Bound Broadcast ==================\n");
            Log.e(TAG, "  - Broadcast Id: " + returnedBroadcast.getId());
            Log.e(TAG, "  - Bound Stream Id: " + returnedBroadcast.getContentDetails().getBoundStreamId());

        } catch (GoogleJsonResponseException e) {
            Log.e(TAG, "GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
            e.printStackTrace();

        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (Throwable t) {
            Log.e(TAG, "Throwable: " + t.getMessage());
            t.printStackTrace();
        }
    }

    /*
     * Returns a broadcast title (String) from user via the terminal.
     */
//    private static String getBroadcastTitle() throws IOException {
//
//        String title = "";
//
//        System.out.print("Please enter a broadcast title: ");
//        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
//        title = bReader.readLine();
//
//        if (title.length() < 1) {
//            // If nothing is entered, defaults to "New Broadcast"
//            title = "New Broadcast";
//        }
//        return title;
//    }

    /*
     * Returns a stream title (String) from user via the terminal.
     */
//    private static String getStreamTitle() throws IOException {
//
//        String title = "";
//
//        System.out.print("Please enter a stream title: ");
//        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
//        title = bReader.readLine();
//
//        if (title.length() < 1) {
//            // If nothing is entered, defaults to "New Stream"
//            title = "New Stream";
//        }
//        return title;
//    }

}

// private static Credential authorize(List<String> scopes) throws Exception
// {
//
// // Load client secrets.
// GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
// JSON_FACTORY,
// MainActivity.class.getResourceAsStream("/client_secrets.json"));
//
// // Checks that the defaults have been replaced (Default =
// "Enter X here").
// if (clientSecrets.getDetails().getClientId().startsWith("Enter")
// || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
// // Log.e(TAG, 
// //
// "Enter Client ID and Secret from https://code.google.com/apis/console/?api=youtube"
// // +
// "into youtube-cmdline-listbroadcasts-sample/src/main/resources/client_secrets.json");
// }else {
// Log.e(TAG, clientSecrets.getDetails().getClientId());
// Log.e(TAG, "clientSecrets.getDetails().getClientSecret()");
// }
//
// // Set up file credential store.
// // FileCredentialStore credentialStore = new FileCredentialStore(
// // new
// File(System.getProperty("user.home"),".credentials/youtube-api-listbroadcasts.json"),
// // JSON_FACTORY);
//
// // CredentialStore credentialStore = new
// SharedPreferencesCredentialStore(prefs);
// // AccessTokenResponse accessTokenResponse = credentialStore.read();
//
// // Set up authorization code flow.
// // GoogleAuthorizationCodeFlow flow = new
// GoogleAuthorizationCodeFlow.Builder(
// // HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, scopes)
// // .setCredentialStore(credentialStore)
// // .build();
//
// // Authorize.
// // return new AuthorizationCodeInstalledApp(flow, new
// LocalServerReceiver()).authorize("user");
// }

