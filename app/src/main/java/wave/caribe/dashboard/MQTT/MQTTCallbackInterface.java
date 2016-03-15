package wave.caribe.dashboard.MQTT;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Caribe Wave Android App
 *
 * Just a Callback interface for MQTT
 *
 * Created by tchap on 14/03/16.
 */
public interface MQTTCallbackInterface {
    void newMeasurement(String uid, JSONArray data);
    void alert(JSONObject data);
}
