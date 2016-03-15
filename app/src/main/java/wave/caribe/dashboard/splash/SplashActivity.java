package wave.caribe.dashboard.splash;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import wave.caribe.dashboard.MainActivity;

/**
 * Caribe Wave Android App
 *
 * A simple splash screen
 *
 * Created by tchap on 14/03/16.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}