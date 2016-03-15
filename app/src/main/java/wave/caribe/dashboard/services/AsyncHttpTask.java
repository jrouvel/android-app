package wave.caribe.dashboard.services;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * caribe-android-dashboard
 * Created by tchap on 15/03/16.
 */
public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {

    private static final String TAG = "CW:ASYNC HTTP TASK";

    private JSONArray sensors = null;

    public interface TaskListener {
        public void onFinished(JSONArray result);
    }

    // This is the reference to the associated listener
    private final TaskListener taskListener;

    public AsyncHttpTask(TaskListener listener) {
        // The listener reference is passed in through the constructor
        this.taskListener = listener;
    }


    @Override
    protected Integer doInBackground(String ... params) {
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        Integer result = 0;
        try {
                /* forming th java.net.URL object */
            URL url = new URL(params[0]);
            urlConnection = (HttpURLConnection) url.openConnection();

                 /* optional request header */
            urlConnection.setRequestProperty("Content-Type", "application/json");

                /* optional request header */
            urlConnection.setRequestProperty("Accept", "application/json");

                /* for Get request */
            urlConnection.setRequestMethod("GET");
            int statusCode = urlConnection.getResponseCode();

                /* 200 represents HTTP OK */
            if (statusCode ==  200) {
                inputStream = new BufferedInputStream(urlConnection.getInputStream());
                String response = convertInputStreamToString(inputStream);
                parseResult(response);
                result = 1; // Successful
            }else{
                result = 0; //"Failed to fetch data!";
            }
        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        return result; //"Failed to fetch data!";
    }

    @Override
    protected void onPostExecute(Integer result) {
        /* Download complete. Lets call mainactivity */
        if(result == 1){
            super.onPostExecute(result);

            // In onPostExecute we check if the listener is valid
            if(this.taskListener != null) {

                // And if it is we call the callback function on it.
                this.taskListener.onFinished(sensors);
            }
        }else{
            Log.e(TAG, "Failed to fetch data!");
        }
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null){
            result += line;
        }

        /* Close Stream */
        if(null!=inputStream){
            inputStream.close();
        }
        return result;
    }

    private void parseResult(String result) {
        try{
            sensors = new JSONArray(result);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
}