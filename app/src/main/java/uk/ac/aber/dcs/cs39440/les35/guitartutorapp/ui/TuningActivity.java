package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.ui;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.R;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.SpinnerAdapter;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource.DataManager;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.NotesViewModel;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.InstrumentType;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Note;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Tuning;

public class TuningActivity extends AppCompatActivity {

    // Static integers required as permission codes for requesting and checking permissions
    private static final int REQUEST_MICROPHONE = 1;

    // Determines the size of one "cent" section of the tuning gauge
    final double oneSectionOfGauge = 0.9;

    // Notes 5 cents either way of the exact frequency
    final double tuningLeeway = 5;

    // Integer that determines how many "checks" of the current frequency must be in tune for a string
    // to be determined as actually in tune
    final int noteCorrectLimit = 12;

    // Counter for the
    int noteCorrectIndicator = 0;

    // View Model to access th notes table of the Room Database
    NotesViewModel notesView;

    // TarsosDSP object to allow for detection of frequency
    AudioDispatcher dispatcher;

    // Views for the tuning screen
    TextView noteText;
    TextView noteDown;
    TextView noteUp;
    TextView indicatorText;
    TextView[] tuningNoteNames = new TextView[6];
    ImageView needle;
    Button startTunerButton;

    // Tuning currently selected by the user
    Tuning currentlySelectedTuning;
    // List of all available tunings
    List<Tuning> tunings;

    // Closest tuning note previously detected
    Note previousNoteDetected;

    // "old color" of text on the screen, used to set colors back to default
    ColorStateList oldColor;

    // Spinners for selecting the tuning
    Spinner instrumentSpinner;
    Spinner tuningSpinner;

    // A thread to run the pitch detection algorithm
    Thread audioThread = new Thread();

    Boolean showStartTunerButton = true;
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tuning);

        // Adds the toolbar to the screen
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        assert actionbar != null;

        notesView = ViewModelProviders.of(this).get(NotesViewModel.class);
        // Inflate the layout for this fragment

        // Finds the views required for the tuner screen
        noteText = findViewById(R.id.noteText);
        noteUp = findViewById(R.id.note_up);
        noteDown = findViewById(R.id.note_down);
        indicatorText = findViewById(R.id.indicatorText);
        instrumentSpinner = findViewById(R.id.instrumentSpinner);
        tuningSpinner = findViewById(R.id.tuningSpinner);
        needle = findViewById(R.id.needle);
        startTunerButton = findViewById(R.id.startTuner);
        startTunerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartTunerButtonClick();
                onStartTunerButtonClick();
            }
        });
        buttonStateChange(true);

        // Some setup methods to populate the spinners and read the tunings from the CSV file
        readTunings();
        setupSpinners();
        checkPermissions();
    }

    public void startTuner() {

        // Creates a new TarsosDSP PitchDetectionHandler, which does the work of detecting what
        // the pitch of the sound it is currently detecting
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
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(44100, 4096, 0);

        // Creates an AudioProcessor which is a TarsosDSP handler for most of its audio manipulation techniques
        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 44100, 4096, pdh);
        dispatcher.addAudioProcessor(pitchProcessor);
        // Creates a new thread which runs the AudioProcessor, allowing the tuner to run indefinitely
        audioThread = new Thread(dispatcher, "AudioThread");
        audioThread.start();
    }

    /**
     * This method takes the current frequency being detected and converts the frequency of the note
     * to the nearest note in the chosen instrument currentlySelectedTuning.
     *
     * @param pitchInHz This is the current pitch being picked up by the microphone
     */
    public void processPitch(float pitchInHz) throws IOException {
        System.out.println("PROCESSING PITCH");
        Note currentClosestNote = null;
        List<Note> tuningNotes = currentlySelectedTuning.getNotes();
        for (int i = 0; i < tuningNotes.size(); i++) {
            float currentNoteFreq = tuningNotes.get(i).getFrequency();
            if (i == 0) {
                float freqDifferenceUp = tuningNotes.get(i + 1).getFrequency() - currentNoteFreq;
                if (pitchInHz <= tuningNotes.get(i).getFrequency() + (freqDifferenceUp / 2)) {
                    currentClosestNote = tuningNotes.get(i);
                }
            } else if (i == tuningNotes.size() - 1) {
                float freqDifferenceDown = currentNoteFreq - tuningNotes.get(i - 1).getFrequency();
                if (pitchInHz >= currentNoteFreq - (freqDifferenceDown / 2)) {
                    currentClosestNote = tuningNotes.get(i);
                }
            } else {
                float freqDifferenceUp = tuningNotes.get(i + 1).getFrequency() - currentNoteFreq;
                float freqDifferenceDown = currentNoteFreq - tuningNotes.get(i - 1).getFrequency();
                if (pitchInHz >= currentNoteFreq - (freqDifferenceDown / 2) && pitchInHz <= tuningNotes.get(i).getFrequency() + (freqDifferenceUp / 2)) {
                    currentClosestNote = tuningNotes.get(i);
                }
            }
        }

        int currentNoteId = currentClosestNote.getId();

        Note previousChromaticNote = notesView.getNoteBefore(currentNoteId);
        Note nextChromaticNote = notesView.getNoteAfter(currentNoteId);

        float frequencyDifferenceDown = currentClosestNote.getFrequency() - previousChromaticNote.getFrequency();
        float frequencyDifferenceUp = nextChromaticNote.getFrequency() - currentClosestNote.getFrequency();

        float centDown = frequencyDifferenceDown / 100;
        float centUp = frequencyDifferenceUp / 100;

        noteText.setText(currentClosestNote.getNoteName());
        noteUp.setText(nextChromaticNote.getNoteName());
        noteDown.setText(previousChromaticNote.getNoteName());

        if (pitchInHz >= currentClosestNote.getFrequency() - (tuningLeeway * centDown) && pitchInHz <= currentClosestNote.getFrequency() + (tuningLeeway * centUp)) {
            indicatorText.setText(getString(R.string.inTuneText));
            indicatorText.setTextColor(Color.GREEN);
            noteCorrectIndicator++;
            needle.setRotation(0);
            checkNoteIsInTune();
        } else if (pitchInHz < currentClosestNote.getFrequency()) {
            indicatorText.setText(getString(R.string.tightenText));
            indicatorText.setTextColor(Color.RED);
            noteCorrectIndicator = 0;
        } else if (pitchInHz > currentClosestNote.getFrequency()) {
            indicatorText.setText(getString(R.string.loosenText));
            indicatorText.setTextColor(Color.RED);
            noteCorrectIndicator = 0;
        }

        for (TextView tuningNoteName : tuningNoteNames) {
            if (currentClosestNote.getNoteName().contentEquals(tuningNoteName.getText())) {
                tuningNoteName.setTextColor(getResources().getColor(R.color.colorNoteIndication));
            } else {
                tuningNoteName.setTextColor(oldColor);
            }
        }

        setGaugeRotation(pitchInHz, currentClosestNote, centDown, centUp, previousChromaticNote, nextChromaticNote);


        previousNoteDetected = currentClosestNote;

    }


    private void setGaugeRotation(float pitchInHz, Note currentClosestNote, float centDown, float centUp, Note previousNote, Note nextNote) {
        if (pitchInHz < currentClosestNote.getFrequency()) {
            float angle;
            for (int i = 1; i < 100; i++) {
                if (pitchInHz < currentClosestNote.getFrequency() - (i * centDown) && pitchInHz > previousNote.getFrequency() + ((99 - i) * centDown)) {
                    needle.clearAnimation();

                    angle = i * (float) oneSectionOfGauge;
                    needle.setRotation(-angle);
                    break;
                }
                if (pitchInHz < currentClosestNote.getFrequency() - (100 * centDown)) {
                    needle.setRotation(-90);
                }
            }
        } else if (pitchInHz > currentClosestNote.getFrequency()) {
            float angle;
            for (int i = 1; i < 100; i++) {
                if (pitchInHz > currentClosestNote.getFrequency() + (i * centUp) && pitchInHz < nextNote.getFrequency() - ((99 - i) * centUp)) {
                    needle.clearAnimation();

                    angle = i * (float) oneSectionOfGauge;
                    needle.setRotation(angle);
                    break;
                }
            }
            if (pitchInHz > currentClosestNote.getFrequency() + (100 * centUp)) {
                needle.setRotation(90);
            }
        }

        if (pitchInHz == -1.0) {
            noteText.setText("---");
            noteUp.setText("");
            noteDown.setText("");
            needle.setRotation(0);
            indicatorText.setText(getString(R.string.playPromptText));
            indicatorText.setTextColor(oldColor);
            for (TextView tuningNoteName : tuningNoteNames) {
                tuningNoteName.setTextColor(oldColor);
            }
        }
    }


    /**
     * This method is called when a user selects a different tuning from the Tuning spinner.
     * The method will find the tuning selected within the tuning list and set it as the currently
     * selected tuning
     * The method then changes the notes displayed on the screen to the user to the notes specified
     * by the now currently selected tuning
     */
    private void changeTuning() {
        String selectedTuningAsString = tuningSpinner.getSelectedItem().toString();
        for (Tuning t : tunings) {
            if (t.getTuningName().equals(selectedTuningAsString)) {
                currentlySelectedTuning = t;
            }
        }

        List<Note> tuningNotes;
        tuningNotes = currentlySelectedTuning.getNotes();

        // Empties current note names
        for (TextView tuningNoteName : tuningNoteNames) {
            tuningNoteName.setText("");
        }

        // Sets new note names
        for (int i = 0; i < tuningNotes.size(); i++) {
            tuningNoteNames[i].setText(tuningNotes.get(i).getNoteName());
        }

    }

    /**
     * Method called in the onCreate method to read tunings in from the CSV file into an ArrayList
     */
    private void readTunings() {
        List<Note> tuningNotes = new ArrayList<>();

        tuningNoteNames[0] = findViewById(R.id.tuningNoteOne);
        tuningNoteNames[1] = findViewById(R.id.tuningNoteTwo);
        tuningNoteNames[2] = findViewById(R.id.tuningNoteThree);
        tuningNoteNames[3] = findViewById(R.id.tuningNoteFour);
        tuningNoteNames[4] = findViewById(R.id.tuningNoteFive);
        tuningNoteNames[5] = findViewById(R.id.tuningNoteSix);

        oldColor = tuningNoteNames[0].getTextColors();

        try {
            DataManager reader = new DataManager(this.getApplicationContext());
            reader.readTunings(notesView.getAllNotesAsList());
            tunings = reader.getTunings();
            reader.readChords();
            currentlySelectedTuning = tunings.get(0);
            tuningNotes = currentlySelectedTuning.getNotes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < tuningNotes.size(); i++) {
            tuningNoteNames[i].setText(tuningNotes.get(i).getNoteName());
        }
    }

    /**
     * Initialises both the Instrument and Tuning spinners
     */
    private void setupSpinners() {
        SpinnerAdapter instrumentAdapter = new SpinnerAdapter(this.getApplicationContext(), getResources().getStringArray(R.array.instrument_spinner_array));
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
    }

    /**
     * This method sets the tuning spinner to contain all tunings from the currently selected
     * InstrumentType in the instrument spinner
     */
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


        SpinnerAdapter tuningSpinnerArrayAdapter = new SpinnerAdapter(this.getApplicationContext(), tuningNamesAsArray);
        tuningSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tuningSpinner.setAdapter(tuningSpinnerArrayAdapter);

        changeTuning();

    }


    /**
     * This method will check whether the current note being played has been in tune long enough
     * for it to definitely be in tune. If this is the case, the counter for the check is reset
     * and a sound is played to the user to indicate the string is now in tune
     *
     * @throws IOException
     */
    private void checkNoteIsInTune() throws IOException {
        if (noteCorrectIndicator >= noteCorrectLimit) {
            noteCorrectIndicator = 0;
            System.out.println(noteCorrectIndicator);
            AssetFileDescriptor afd = getAssets().openFd(getString(R.string.sound_intune));
            MediaPlayer mPlayer = new MediaPlayer();
            mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mPlayer.prepare();
            mPlayer.start();
        }
    }

    /**
     * Required method for extending Fragment
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    private void checkPermissions() {
        // Checks if the RECORD_AUDIO permission is granted, and if it is not it prompts the user to
        // allow this permission.
        // CURRENTLY app will not function without the microphone permsission, but there is the intent
        // to add a sound only tuner, allowing the user to match to a sound played on the device.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE);
            }
        } else {
            startTuner();
            buttonStateChange(false);
        }
    }

    private void buttonStateChange(boolean state) {
        startTunerButton.setClickable(state);
        if (state) {
            startTunerButton.setVisibility(View.VISIBLE);
        } else {
            startTunerButton.setVisibility(View.GONE);
        }

    }

    private void onStartTunerButtonClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.permission_alert_title));
        builder.setMessage(getString(R.string.permission_alert_message));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void stopDispatcher() {
        dispatcher.stop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_MICROPHONE: {
                if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(this).getApplicationContext(), Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

                    Toast toast = Toast.makeText(this, "Microphone Permission required for tuner", Toast.LENGTH_SHORT);
                    toast.show();

                } else {
                    startTuner();
                    buttonStateChange(false);
                }
                break;
            }
        }
    }

        @Override
    public void onPause() {
        super.onPause();
        System.out.println("STOPPING TUNING");
        dispatcher.stop();


    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("STOPPING TUNING");
        dispatcher.stop();

    }
}
