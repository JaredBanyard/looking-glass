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

import java.io.IOException;
import java.util.List;

import android.accounts.Account;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.gson.GsonFactory;
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
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
//import com.google.android.gms.common.GooglePlayServicesUtil;

//import com.google.common.collect.Lists;

public class MainActivity extends Activity {

    private static String TAG = "LookingGlass";
    private static String AUTH_TOKEN_TYPE = "youtube";

    static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
    static final int REQUEST_AUTHORIZATION = 1;

    /** Global instance of Youtube object to make all API requests. */
    private static YouTube youtube;

    GoogleAccountCredential credential;

    String email;

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

        credential = GoogleAccountCredential.usingOAuth2(this, YouTubeScopes.YOUTUBE);
        credential.setSelectedAccountName(email);
        youtube = new YouTube.Builder(AndroidHttp.newCompatibleTransport(),
                new GsonFactory(), 
                credential).setApplicationName("LookingGlass/1.0").build();

        checkGooglePlayServicesAvailable();

        new InitTask().execute();
    }

    private boolean checkGooglePlayServicesAvailable() {
        final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        }
        return true;
    }

    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        runOnUiThread(new Runnable() {

            public void run() {
                Dialog dialog =
                        GooglePlayServicesUtil.getErrorDialog(connectionStatusCode, MainActivity.this,
                                REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

    public class InitTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            return init();
        }
    }

    public boolean init() {
        try {
            Log.e(TAG, "Token: " + GoogleAuthUtil.getToken(this, email, "oauth2:" + YouTubeScopes.YOUTUBE));
            search();
            return true;
        } catch (GooglePlayServicesAvailabilityException playEx) {
            checkGooglePlayServicesAvailable();
        } catch (UserRecoverableAuthException userRecoverableException) {
            startActivityForResult(userRecoverableException.getIntent(), REQUEST_AUTHORIZATION);
        } catch (GoogleAuthException fatalException) {
            Log.e(TAG, "GoogleAuthException error " + fatalException.getMessage(), fatalException);
        } catch (IOException fatalException) {
            Log.e(TAG, "IOException error " + fatalException.getMessage(), fatalException);
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    Log.e(TAG, "AUTHED!!");
                } else {
                    Log.e(TAG, "NOT AUTHED!!");
                }
                new InitTask().execute();
                break;
        }
    }

    public void test() {
        try {
            // Create request to list broadcasts.
            YouTube.LiveBroadcasts.List liveBroadcastRequest =
                    youtube.liveBroadcasts().list("id,snippet");

            // Modify results to have broadcasts in all states.
            liveBroadcastRequest.setBroadcastStatus("all");

            // List request is executed and list of broadcasts are returned
            LiveBroadcastList returnedListResponse = liveBroadcastRequest.execute();

            // Get the list of broadcasts associated with the user.
            List<LiveBroadcast> returnedList = returnedListResponse.getItems();

            // Print out returned results.
            System.out.println("\n================== Returned Broadcasts ==================\n");
            for (LiveBroadcast broadcast : returnedList) {
                System.out.println("  - Id: " + broadcast.getId());
                System.out.println("  - Title: " + broadcast.getSnippet().getTitle());
                System.out.println("  - Description: " + broadcast.getSnippet().getDescription());
                System.out.println("  - Published At: " + broadcast.getSnippet().getPublishedAt());
                System.out.println(
                        "  - Scheduled Start Time: " + broadcast.getSnippet().getScheduledStartTime());
                System.out.println(
                        "  - Scheduled End Time: " + broadcast.getSnippet().getScheduledEndTime());
                System.out.println("\n-------------------------------------------------------------\n");
            }
        } catch (Exception e) {

        }
    }

    public void start() throws IOException {
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

    }

    private void search() {
        try {

            // Get query term from user.
            String queryTerm = "turtles";

            YouTube.Search.List search = youtube.search().list("id,snippet");

            /*
             * It is important to set your developer key from the Google Developer Console for
             * non-authenticated requests (found under the API Access tab at this link:
             * code.google.com/apis/). This is good practice and increased your quota.
             */
//            search.setKey(apiKey);
            search.setQ(queryTerm);
            /*
             * We are only searching for videos (not playlists or channels). If we were searching for
             * more, we would add them as a string like this: "video,playlist,channel".
             */
            search.setType("video");
            /*
             * This method reduces the info returned to only the fields we need and makes calls more
             * efficient.
             */
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
            search.setMaxResults((long) 25);
            SearchListResponse searchResponse = search.execute();

            List<SearchResult> searchResultList = searchResponse.getItems();

            if (searchResultList != null) {
//              prettyPrint(searchResultList.iterator(), queryTerm);
                for (SearchResult result : searchResultList) {
                    Log.e(TAG, result.toPrettyString());
                }
            }
        } catch (GoogleJsonResponseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
