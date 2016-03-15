package wave.caribe.dashboard;

/**
 * Caribe Wave
 * Created by tchap on 28/12/15.
 */
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.StringTokenizer;

public class MQTTClient implements MqttCallback {

    private static final String TAG = "CW:MQTT BROKER";

    MqttClient mClient;
    MqttConnectOptions connOpt;
    CallbackInterface mCallbackInterface;

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
        Log.i(TAG,"Connection LOST, reconnecting");
        // code to reconnect to the broker :
        try {
            connect();
            subscribeToAll(mCallbackInterface);
        } catch (MqttException e) {
            e.printStackTrace();
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

        Log.i(TAG, "-------------------------------------------------");
        Log.i(TAG, "| Topic:" + topic);
        Log.i(TAG, "| Message: " + new String(message.getPayload()));
        Log.i(TAG, "-------------------------------------------------");

        // id=%s&w=%d&t=%.2f&h=%d
        StringTokenizer tokens = new StringTokenizer(new String(message.getPayload()), "&");

        if(mCallbackInterface != null) {
            mCallbackInterface.execute();
        }
    }

    public void subscribeToAll(CallbackInterface ci){

        // setup topic
        int subQoS = 2;

        try {
            if (sharedPref.getString("pref_data", "").length() > 1) {
                mClient.subscribe(sharedPref.getString("pref_data", ""), subQoS);
            }
            if (sharedPref.getString("pref_alert", "").length() > 1) {
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

        // Connect to Broker
        mClient = new MqttClient(sharedPref.getString("pref_url", "") + ":" + sharedPref.getString("pref_port", "1883"), sharedPref.getString("pref_client", ""), new MemoryPersistence());
        mClient.setCallback(this);
        mClient.connect(connOpt);
        Log.i(TAG, "Connected to " + sharedPref.getString("pref_url", ""));

    }

    public void reconnect(CallbackInterface ci) {
        try {
            disconnect();
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
        // disconnect
        try {
            mClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return mClient != null && mClient.isConnected();
    }
}