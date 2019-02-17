package uk.ac.aber.dcs.cs39440.les35.guitartutorapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource.CsvReader;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.NotesDAO;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.NotesViewModel;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Note;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_MICROPHONE = 1;
    private static final int ACCESS_REQUESTED = 1;
    AudioDispatcher dispatcher;
    TextView pitchText;
    TextView noteText;

    Note[] notes;
    NotesViewModel view;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = new NotesViewModel(this.getApplication());
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


        pitchText = findViewById(R.id.pitchText);
        noteText = findViewById(R.id.noteText);
        CsvReader reader;
        notes = null;
        try {
            reader = new CsvReader(getResources().getString(R.string.notes_file_name), this);
            reader.readFile();
            notes = reader.getNotes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        NotesViewModel noteView = new NotesViewModel(this.getApplication());

        Note note = new Note("TEST", 1);

        System.out.println("inserting at main");
        //noteView.insert(notes);

        ;

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
        System.out.println(pitchInHz);
        Note currentNotePlayed = null;

        for (int i = 0; i < notes.length; i++) {
            float currentNoteFreq = notes[i].getFrequency();
            if (i == 0) {
                float freqDifferenceUp = notes[i + 1].getFrequency() - currentNoteFreq;
                if (pitchInHz <= notes[i].getFrequency() + (freqDifferenceUp / 2)) {
                    currentNotePlayed = notes[i];
                }
            } else if (i == notes.length - 1) {
                float freqDifferenceDown = currentNoteFreq - notes[i - 1].getFrequency();
                if (pitchInHz >= currentNoteFreq - (freqDifferenceDown / 2)){
                    currentNotePlayed = notes[i];
                }
            } else {
                float freqDifferenceUp = notes[i + 1].getFrequency() - currentNoteFreq;
                float freqDifferenceDown = currentNoteFreq - notes[i - 1].getFrequency();
                if (pitchInHz >= currentNoteFreq - (freqDifferenceDown / 2) && pitchInHz <= notes[i].getFrequency() + (freqDifferenceUp / 2)) {
                    currentNotePlayed = notes[i];
                }
            }
        }

        if(currentNotePlayed != null){
            noteText.setText(currentNotePlayed.getNoteName());
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ACCESS_REQUESTED: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}

