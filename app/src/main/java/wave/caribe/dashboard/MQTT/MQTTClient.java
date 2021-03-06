package wave.caribe.dashboard.MQTT;

/**
 * Caribe Wave Android App
 *
 * The MQTT Class manages the MQTT connection
 *
 * Created by tchap on 14/03/16.
 */
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.StringTokenizer;

import wave.caribe.dashboard.MainActivity;

public class MQTTClient implements MqttCallback {

    private static final String TAG = "CW:MQTT BROKER";

    MqttClient mClient;
    MqttConnectOptions connOpt;
    MQTTCallbackInterface mCallbackInterface;

    SharedPreferences sharedPref;

    // Used to generate a unique ID for the MQTT connection
    private String android_id;

    public MQTTClient(MainActivity activity) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);

        // Create the unique ID
        android_id = "android_" + Settings.Secure.getString(activity.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    /**
     *
     * connectionLost
     * This callback is invoked upon losing the MQTT connection.
     *
     */
    @Override
    public void connectionLost(Throwable t) {
        Log.i(TAG, "Connection LOST, reconnecting : " + t);
        // Wait 5 secs before reconnecting
        try {
            Thread.sleep(5000);
            connect();
            subscribeToAll(mCallbackInterface);
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (InterruptedException i) {
            i.printStackTrace();
        }
    }

    /**
     *
     * deliveryComplete
     * This callback is invoked when a message published by this client
     * is successfully received by the broker.
     *
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token){
        Log.i(TAG, "Delivery complete");
    }

    /**
     *
     * messageArrived
     * This callback is invoked when a message is received on a subscribed topic.
     *
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if(mCallbackInterface != null) {
            if (topic.equals("alert/general")) {
                Log.i(TAG, "General Alert : " + new String(message.getPayload()));
                mCallbackInterface.alert(new JSONObject(new String(message.getPayload())));
            } else if (topic.startsWith("measurement/")) {
                StringTokenizer tokens = new StringTokenizer(topic, "/");
                tokens.nextElement(); // skip first element
                String uid = tokens.nextElement().toString();
                Log.i(TAG, "New Measurement for " + uid + " : " + new String(message.getPayload()));
                mCallbackInterface.newMeasurement(uid, new JSONArray(new String(message.getPayload())));
            }
        }
    }

    public void subscribeToAll(MQTTCallbackInterface ci){

        // setup topic
        int subQoS = 1;

        try {
            if (sharedPref.getString("pref_data", "").length() > 1) {
                Log.i(TAG, "Subscribing to " + sharedPref.getString("pref_data", ""));
                mClient.subscribe(sharedPref.getString("pref_data", ""), subQoS);
            }
            if (sharedPref.getString("pref_alert", "").length() > 1) {
                Log.i(TAG, "Subscribing to " + sharedPref.getString("pref_alert", ""));
                mClient.subscribe(sharedPref.getString("pref_alert", ""), subQoS);
            }
            mCallbackInterface = ci;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     *
     */
    public void connect() throws MqttException {
        connOpt = new MqttConnectOptions();

        connOpt.setCleanSession(true);
        connOpt.setKeepAliveInterval(3600);
        connOpt.setConnectionTimeout(3600);
        connOpt.setUserName(android_id);
        connOpt.setPassword(sharedPref.getString("pref_token", "").toCharArray());

        String tmpDir = System.getProperty("java.io.tmpdir");
        MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);

        // Connect to Broker
        mClient = new MqttClient(sharedPref.getString("pref_url", "") + ":" + sharedPref.getString("pref_port", "1883"), android_id + "_client", dataStore);
        mClient.setCallback(this);
        mClient.connect(connOpt);
        Log.i(TAG, "Connected to " + sharedPref.getString("pref_url", ""));

    }

    public void reconnect(MQTTCallbackInterface ci) {
        try {
            //disconnect();
            connect();
            subscribeToAll(ci);
        } catch (MqttException e) {
            Log.i(TAG, "Impossible to connect to the broker. Please check the settings and that you have an available internet connection, and retry.");
            e.printStackTrace();
        } catch (Exception e) {
            Log.i(TAG, "Problem connecting. Please check the settings, and retry.");
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (mClient != null) {
            try {
                mClient.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}