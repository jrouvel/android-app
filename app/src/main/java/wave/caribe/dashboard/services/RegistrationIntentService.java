package wave.caribe.dashboard.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import wave.caribe.dashboard.MainActivity;
import wave.caribe.dashboard.R;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "CW:GCM REGISTERING";
    private static final String[] TOPICS = {"global"};

    private SharedPreferences sharedPref;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            Log.i(TAG, "GCM Registration Token: " + token);

            boolean reg_sent = sharedPref.getBoolean(MainActivity.SENT_TOKEN_TO_SERVER, false);
            Log.i(TAG, "Token already sent ?  " + reg_sent);
            if (!reg_sent) {
                boolean result = sendRegistrationToServer(token);

                // You should store a boolean that indicates whether the generated token has been
                // sent to your server. If the boolean is false, send the token to your server,
                // otherwise your server should have already received the token.
                if (result == true) {
                    sharedPref.edit().putBoolean(MainActivity.SENT_TOKEN_TO_SERVER, true).apply();
                }

            }

            // Subscribe to topic channels
            subscribeTopics(token);

            // [END register_for_gcm]
        } catch (Exception e) {
            Log.i(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPref.edit().putBoolean(MainActivity.SENT_TOKEN_TO_SERVER, false).apply();
        }

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(MainActivity.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private boolean sendRegistrationToServer(String token) {

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String reg_url = sharedPref.getString("pref_register_api", "");

        if (reg_url.equals("")){
            Log.i(TAG, "Register endpoint empty");
            return false;
        }

        try {
            Log.i(TAG, "Sending Token to endpoint " + reg_url);
            URL url = new URL(reg_url + token);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();

                String inputStr;
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);

                String result = responseStrBuilder.toString();
                if (result.equals("OK")){
                    Log.i(TAG, "OK From register server");
                    return true;
                } else {
                    Log.i(TAG, "Bad response from register server");
                    return false;
                }

            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {

        }

        Log.i(TAG, "Impossible to connect to token endpoint");
        return false;
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]

}
