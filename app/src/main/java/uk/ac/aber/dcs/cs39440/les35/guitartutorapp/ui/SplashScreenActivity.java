package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.R;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startTuner();
            }
        }, 1500);
    }

    private void startTuner(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
