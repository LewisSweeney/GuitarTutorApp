package uk.ac.aber.dcs.cs39440.les35.guitartutorapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_MICROPHONE = 1;
    AudioDispatcher dispatcher;
    TextView pitchText;
    TextView noteText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_MICROPHONE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
        }

        pitchText = (TextView) findViewById(R.id.pitchText);
        noteText = (TextView) findViewById(R.id.noteText);

        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult res, AudioEvent e) {
                final float pitchInHz = res.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        processPitch(pitchInHz);
                    }
                });
            }
        };
        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
        dispatcher.addAudioProcessor(pitchProcessor);

        Thread audioThread = new Thread(dispatcher, "Audio Thread");
        audioThread.start();
    }

    /**
     * This method takes the current frequency being detected and converts the frequency of the note
     * to the nearest note in the chosen instrument tuning.
     *
     * @param pitchInHz
     */
    public void processPitch(float pitchInHz) {

        pitchText.setText("" + pitchInHz);

        if (pitchInHz >= 110 && pitchInHz < 123.47) {
            //A
            noteText.setText("A");
        } else if (pitchInHz >= 123.47 && pitchInHz < 130.81) {
            //B
            noteText.setText("B");
        } else if (pitchInHz >= 130.81 && pitchInHz < 146.83) {
            //C
            noteText.setText("C");
        } else if (pitchInHz >= 146.83 && pitchInHz < 164.81) {
            //D
            noteText.setText("D");
        } else if (pitchInHz >= 164.81 && pitchInHz <= 174.61) {
            //E
            noteText.setText("E");
        } else if (pitchInHz >= 174.61 && pitchInHz < 185) {
            //F
            noteText.setText("F");
        } else if (pitchInHz >= 185 && pitchInHz < 196) {
            //G
            noteText.setText("G");
        }
    }
}

