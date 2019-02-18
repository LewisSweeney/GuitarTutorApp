package uk.ac.aber.dcs.cs39440.les35.guitartutorapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.NotesViewModel;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.InstrumentType;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Note;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Tuning;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_MICROPHONE = 1;
    private static final int ACCESS_REQUESTED = 1;
    AudioDispatcher dispatcher;
    TextView noteText;
    TextView indicatorText;
    Button testSounds;
    Note[] notes;
    Tuning tuning;
    NotesViewModel notesView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notesView = new NotesViewModel(this.getApplication());
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


        noteText = findViewById(R.id.noteText);
        indicatorText = findViewById(R.id.indicatorText);
        // testSounds = findViewById(R.id.testSound);

       /* testSounds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(110, 88200);
            }
        }); */

        notes = notesView.getAllNotesAsList();

        Note[] tuningNotes = new Note[6];
        tuningNotes[0] = notesView.getNoteByName("E2");
        tuningNotes[1] = notesView.getNoteByName("A2");
        tuningNotes[2] = notesView.getNoteByName("D3");
        tuningNotes[3] = notesView.getNoteByName("G3");
        tuningNotes[4] = notesView.getNoteByName("B3");
        tuningNotes[5] = notesView.getNoteByName("E4");
        tuning = new Tuning("E Standard", InstrumentType.GUITAR, tuningNotes);

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
        Note currentNotePlayed = null;
        Note[] tuningNotes = tuning.getNotes();
        for (int i = 0; i < tuningNotes.length; i++) {
            float currentNoteFreq = tuningNotes[i].getFrequency();
            if (i == 0) {
                float freqDifferenceUp = tuningNotes[i + 1].getFrequency() - currentNoteFreq;
                if (pitchInHz <= tuningNotes[i].getFrequency() + (freqDifferenceUp / 2)) {
                    currentNotePlayed = tuningNotes[i];
                }
            } else if (i == tuningNotes.length - 1) {
                float freqDifferenceDown = currentNoteFreq - tuningNotes[i - 1].getFrequency();
                if (pitchInHz >= currentNoteFreq - (freqDifferenceDown / 2)) {
                    currentNotePlayed = tuningNotes[i];
                }
            } else {
                float freqDifferenceUp = tuningNotes[i + 1].getFrequency() - currentNoteFreq;
                float freqDifferenceDown = currentNoteFreq - tuningNotes[i - 1].getFrequency();
                if (pitchInHz >= currentNoteFreq - (freqDifferenceDown / 2) && pitchInHz <= tuningNotes[i].getFrequency() + (freqDifferenceUp / 2)) {
                    currentNotePlayed = tuningNotes[i];
                }
            }
        }

        int currentNoteId = currentNotePlayed.getId();
        Note previousNote = notesView.getNoteBefore(currentNoteId);
        Note nextNote = notesView.getNoteAfter(currentNoteId);


        if (currentNotePlayed != null) {
            noteText.setText(currentNotePlayed.getNoteName());
        }
        if (pitchInHz < currentNotePlayed.getFrequency()) {
            indicatorText.setText("Tighten this string!");
            indicatorText.setTextColor(Color.RED);
        } else if (pitchInHz > currentNotePlayed.getFrequency()) {
            indicatorText.setText("Loosen this string!");
            indicatorText.setTextColor(Color.RED);
        } else {
            indicatorText.setText("Perfect!");
            indicatorText.setTextColor(Color.GREEN);
        }
        if (pitchInHz == -1.0) {
            noteText.setText("---");
            indicatorText.setText("Play a string!");
            indicatorText.setTextColor(Color.BLACK);
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

    private void playSound(double frequency, int duration) {
        // AudioTrack definition
        int mBufferSize = AudioTrack.getMinBufferSize(44100,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_8BIT);

        AudioTrack mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                mBufferSize, AudioTrack.MODE_STREAM);

        // Sine wave
        double[] mSound = new double[duration];
        short[] mBuffer = new short[duration];
        for (int i = 0; i < mSound.length; i++) {
            mSound[i] = Math.sin((2.0 * Math.PI * i / (44100 / frequency)));
            mBuffer[i] = (short) (mSound[i] * Short.MAX_VALUE);
        }

        mAudioTrack.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume());
        mAudioTrack.play();

        mAudioTrack.write(mBuffer, 0, mSound.length);
        mAudioTrack.stop();
        mAudioTrack.release();
    }
}

