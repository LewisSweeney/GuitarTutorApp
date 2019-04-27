package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.R;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.ChordsViewModel;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.NotesViewModel;

/**
 * Splash screen that runs on app start up that moves quickly over to the MainActivity
 */
public class SplashScreenActivity extends AppCompatActivity {

    NotesViewModel notesViewModel;
    ChordsViewModel chordsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        notesViewModel = ViewModelProviders.of(this).get(NotesViewModel.class);
        chordsViewModel = ViewModelProviders.of(this).get(ChordsViewModel.class);
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
