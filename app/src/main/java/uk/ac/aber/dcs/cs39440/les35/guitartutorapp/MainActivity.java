package uk.ac.aber.dcs.cs39440.les35.guitartutorapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource.CsvReader;
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
    ImageView needle;
    Note[] notes;
    Note previousNoteDetected;
    Tuning currentlySelectedTuning;
    List<Tuning> tunings;
    NotesViewModel notesView;
    ColorStateList oldColor;
    TextView[] tuningNoteNames = new TextView[6];
    Spinner instrumentSpinner;
    Spinner tuningSpinner;

    Thread audioThread;
    int noteCorrectIndicator = 0;
    final int noteCorrectLimit = 20;


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
        instrumentSpinner = findViewById(R.id.instrumentSpinner);
        tuningSpinner = findViewById(R.id.tuningSpinner);
        needle = findViewById(R.id.needle);

        needle.setRotation((float) 34);


        List<Note> tuningNotes = new ArrayList<>();

        tuningNoteNames[0] = findViewById(R.id.tuningNoteOne);
        tuningNoteNames[1] = findViewById(R.id.tuningNoteTwo);
        tuningNoteNames[2] = findViewById(R.id.tuningNoteThree);
        tuningNoteNames[3] = findViewById(R.id.tuningNoteFour);
        tuningNoteNames[4] = findViewById(R.id.tuningNoteFive);
        tuningNoteNames[5] = findViewById(R.id.tuningNoteSix);

        oldColor = tuningNoteNames[0].getTextColors();

        try {
            CsvReader reader = new CsvReader("csv/tunings.csv", this.getApplicationContext());
            reader.readTunings(notesView.getAllNotesAsList());
            tunings = reader.getTunings();
            currentlySelectedTuning = tunings.get(0);
            tuningNotes = currentlySelectedTuning.getNotes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < tuningNotes.size(); i++) {
            tuningNoteNames[i].setText(tuningNotes.get(i).getNoteName());
        }


        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult res, AudioEvent e) {
                final float pitchInHz = res.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            processPitch(pitchInHz);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }
        };


        ArrayAdapter<CharSequence> instrumentAdapter = ArrayAdapter.createFromResource(
                this, R.array.instrument_spinner_array, android.R.layout.simple_spinner_item);
        instrumentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        instrumentSpinner.setAdapter(instrumentAdapter);
        instrumentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                setTuningSpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        setTuningSpinner();
        tuningSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                changeTuning();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.AMDF, 22050, 1024, pdh);
        dispatcher.addAudioProcessor(pitchProcessor);


        audioThread = new Thread(dispatcher, "Audio Thread");

        audioThread.start();
    }

    /**
     * This method takes the current frequency being detected and converts the frequency of the note
     * to the nearest note in the chosen instrument currentlySelectedTuning.
     *
     * @param pitchInHz This is the current pitch being picked up by the microphone
     */
    public void processPitch(float pitchInHz) throws IOException {
        Note currentNotePlayed = null;
        List<Note> tuningNotes = currentlySelectedTuning.getNotes();
        for (int i = 0; i < tuningNotes.size(); i++) {
            float currentNoteFreq = tuningNotes.get(i).getFrequency();
            if (i == 0) {
                float freqDifferenceUp = tuningNotes.get(i + 1).getFrequency() - currentNoteFreq;
                if (pitchInHz <= tuningNotes.get(i).getFrequency() + (freqDifferenceUp / 2)) {
                    currentNotePlayed = tuningNotes.get(i);
                }
            } else if (i == tuningNotes.size() - 1) {
                float freqDifferenceDown = currentNoteFreq - tuningNotes.get(i - 1).getFrequency();
                if (pitchInHz >= currentNoteFreq - (freqDifferenceDown / 2)) {
                    currentNotePlayed = tuningNotes.get(i);
                }
            } else {
                float freqDifferenceUp = tuningNotes.get(i + 1).getFrequency() - currentNoteFreq;
                float freqDifferenceDown = currentNoteFreq - tuningNotes.get(i - 1).getFrequency();
                if (pitchInHz >= currentNoteFreq - (freqDifferenceDown / 2) && pitchInHz <= tuningNotes.get(i).getFrequency() + (freqDifferenceUp / 2)) {
                    currentNotePlayed = tuningNotes.get(i);
                }
            }
        }

        int currentNoteId = currentNotePlayed.getId();
        Note previousNote = notesView.getNoteBefore(currentNoteId);
        Note nextNote = notesView.getNoteAfter(currentNoteId);

        float frequencyDifferenceDown = currentNotePlayed.getFrequency() - previousNote.getFrequency();
        float frequencyDifferenceUp = nextNote.getFrequency() - currentNotePlayed.getFrequency();

        float centDown = frequencyDifferenceDown / 100;
        float centUp = frequencyDifferenceUp / 100;

        if (currentNotePlayed != null) {
            noteText.setText(currentNotePlayed.getNoteName());
        }

        if (pitchInHz >= currentNotePlayed.getFrequency() - (5 * centDown) && pitchInHz <= currentNotePlayed.getFrequency() + (5 * centUp)) {
            indicatorText.setText(getString(R.string.inTuneText));
            indicatorText.setTextColor(Color.GREEN);
            noteCorrectIndicator++;
            needle.setRotation(0);
            checkNoteIsInTune();
        } else if (pitchInHz < currentNotePlayed.getFrequency()) {
            indicatorText.setText(getString(R.string.tightenText));
            indicatorText.setTextColor(Color.RED);
            noteCorrectIndicator = 0;
        } else if (pitchInHz > currentNotePlayed.getFrequency()) {
            indicatorText.setText(getString(R.string.loosenText));
            indicatorText.setTextColor(Color.RED);
            noteCorrectIndicator = 0;
        }

        for (int i = 0; i < tuningNoteNames.length; i++) {
            if (currentNotePlayed.getNoteName().equals(tuningNoteNames[i].getText())) {
                tuningNoteNames[i].setTextColor(getResources().getColor(R.color.colorNoteIndication));
            } else {
                tuningNoteNames[i].setTextColor(oldColor);
            }
        }

        double oneSectionOfGauge = 0.9;
        System.out.println();

        RotateAnimation rotate;
        if (pitchInHz < currentNotePlayed.getFrequency()) {
            float angle;
            for (int i = 1; i < 100; i++) {
                if (pitchInHz < currentNotePlayed.getFrequency() - (i * centDown) && pitchInHz > previousNote.getFrequency() + ((99 - i) * centDown)) {
                    needle.clearAnimation();

                    angle = i * (float) oneSectionOfGauge;
                    System.out.println("SETTING ANGLE NEG: " + angle);
                    needle.setRotation(-angle);
                    break;
                }
                if (pitchInHz < currentNotePlayed.getFrequency() - (100 * centDown)) {
                    needle.setRotation(-90);
                }
            }
        } else if (pitchInHz > currentNotePlayed.getFrequency()) {
            float angle;
            for (int i = 1; i < 100; i++) {
                if (pitchInHz > currentNotePlayed.getFrequency() + (i * centDown) && pitchInHz < nextNote.getFrequency() - ((99 - i) * centDown)) {
                    needle.clearAnimation();

                    angle = i * (float) oneSectionOfGauge;
                    System.out.println("SETTING ANGLE POS: " + angle);

                    needle.setRotation(angle);
                    break;
                }
            }
            if (pitchInHz > currentNotePlayed.getFrequency() + (100 * centUp)) {
                needle.setRotation(90);
            }
        }

        if (pitchInHz == -1.0) {
            noteText.setText("---");

            needle.setRotation(0);
            indicatorText.setText(getString(R.string.playPromptText));
            indicatorText.setTextColor(oldColor);
            for (int i = 0; i < tuningNoteNames.length; i++) {
                tuningNoteNames[i].setTextColor(oldColor);
            }
        }
        previousNoteDetected = currentNotePlayed;

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
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

    private void checkNoteIsInTune() throws IOException {
        if (noteCorrectIndicator >= noteCorrectLimit) {
            noteCorrectIndicator = 0;
            System.out.println(noteCorrectIndicator);
            AssetFileDescriptor afd = getAssets().openFd("sounds/correct.mp3");
            MediaPlayer mPlayer = new MediaPlayer();
            mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mPlayer.prepare();
            mPlayer.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        audioThread.interrupt();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        audioThread.interrupt();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioThread.interrupt();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    private void setTuningSpinner() {
        String selectedInstrumentAsString = instrumentSpinner.getSelectedItem().toString();
        InstrumentType selectedInstrument;
        switch (selectedInstrumentAsString) {
            case "Guitar":
                selectedInstrument = InstrumentType.GUITAR;
                break;
            case "Bass":
                selectedInstrument = InstrumentType.BASS;
                break;
            case "Ukulele":
                selectedInstrument = InstrumentType.UKULELE;
                break;
            default:
                selectedInstrument = InstrumentType.GUITAR;
        }

        List<String> tuningsForSpinner = new ArrayList<>();
        for (Tuning t : tunings) {
            if (t.getInstrument() == selectedInstrument) {
                tuningsForSpinner.add(t.getTuningName());
            }
        }

        String[] tuningNamesAsArray = new String[tuningsForSpinner.size()];

        for (int i = 0; i < tuningsForSpinner.size(); i++) {
            tuningNamesAsArray[i] = tuningsForSpinner.get(i);
        }


        ArrayAdapter<String> tuningSpinnerArrayAdapter = new ArrayAdapter<>(this,   android.R.layout.simple_spinner_item, tuningNamesAsArray);
        tuningSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tuningSpinner.setAdapter(tuningSpinnerArrayAdapter);

        changeTuning();

    }

    private void changeTuning(){
        String selectedTuningAsString = tuningSpinner.getSelectedItem().toString();
        for(Tuning t: tunings){
            if(t.getTuningName().equals(selectedTuningAsString)){
                currentlySelectedTuning = t;
            }
        }

        List<Note> tuningNotes = new ArrayList<>();
        tuningNotes = currentlySelectedTuning.getNotes();

        // Empties current note names
        for(int i = 0; i < tuningNoteNames.length; i++){
            tuningNoteNames[i].setText("");
        }

        // Sets new note names
        for (int i = 0; i < tuningNotes.size(); i++) {
            tuningNoteNames[i].setText(tuningNotes.get(i).getNoteName());
        }

    }
}


