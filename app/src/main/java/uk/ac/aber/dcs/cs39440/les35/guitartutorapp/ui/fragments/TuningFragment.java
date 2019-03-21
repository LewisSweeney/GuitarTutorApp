package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.ui.fragments;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

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
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.R;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.SpinnerAdapter;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource.CsvReader;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.NotesViewModel;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.InstrumentType;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Note;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Tuning;

import static android.app.Activity.RESULT_OK;

/**
 *
 */
public class TuningFragment extends Fragment {

    // Static integers required as permission codes for requesting and checking permissions
    private static final int REQUEST_MICROPHONE = 1;
    private static final int ACCESS_REQUESTED = 1;

    private AlertDialog.Builder builder;
    private AlertDialog alert;

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
    Thread audioThread;

    public TuningFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TuningFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TuningFragment newInstance(String param1, String param2) {
        TuningFragment fragment = new TuningFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        notesView = ViewModelProviders.of(this).get(NotesViewModel.class);
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_tuning, container, false);

        // Finds the views required for the tuner screen
        noteText = view.getRootView().findViewById(R.id.noteText);
        noteUp = view.getRootView().findViewById(R.id.note_up);
        noteDown = view.getRootView().findViewById(R.id.note_down);
        indicatorText = view.getRootView().findViewById(R.id.indicatorText);
        instrumentSpinner = view.getRootView().findViewById(R.id.instrumentSpinner);
        tuningSpinner = view.getRootView().findViewById(R.id.tuningSpinner);
        needle = view.getRootView().findViewById(R.id.needle);

        // Some setup methods to populate the spinners and read the tunings from the CSV file
        readTunings(view);
        setupSpinners();
        checkPermissions();

        return view;
    }

    public void startTuner() {

        // Creates a new TarsosDSP PitchDetectionHandler, which does the work of detecting what
        // the pitch of the sound it is currently detecting
        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult res, AudioEvent e) {
                final float pitchInHz = res.getPitch();
                getActivity().runOnUiThread(new Runnable() {
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
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(44100, 5120, 0);
        // Creates an AudioProcessor which is a TarsosDSP handler for most of its audio manipulation techniques
        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 44100, 5120, pdh);
        dispatcher.addAudioProcessor(pitchProcessor);
        // Creates a new thread which runs the AudioProcessor, allowing the tuner to run indefinitely
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
    private void readTunings(View view) {
        List<Note> tuningNotes = new ArrayList<>();

        tuningNoteNames[0] = view.findViewById(R.id.tuningNoteOne);
        tuningNoteNames[1] = view.findViewById(R.id.tuningNoteTwo);
        tuningNoteNames[2] = view.findViewById(R.id.tuningNoteThree);
        tuningNoteNames[3] = view.findViewById(R.id.tuningNoteFour);
        tuningNoteNames[4] = view.findViewById(R.id.tuningNoteFive);
        tuningNoteNames[5] = view.findViewById(R.id.tuningNoteSix);

        oldColor = tuningNoteNames[0].getTextColors();

        try {
            CsvReader reader = new CsvReader(this.getActivity().getApplicationContext());
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
        SpinnerAdapter instrumentAdapter = new SpinnerAdapter(this.getActivity().getApplicationContext(), getResources().getStringArray(R.array.instrument_spinner_array));
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


        SpinnerAdapter tuningSpinnerArrayAdapter = new SpinnerAdapter(this.getActivity().getApplicationContext(), tuningNamesAsArray);
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
            AssetFileDescriptor afd = getActivity().getAssets().openFd(getString(R.string.sound_intune));
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
        // to add an sound only tuner, allowing the user to match to a sound played on the device.
        if (ActivityCompat.checkSelfPermission(this.getActivity().getApplicationContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(),
                    Manifest.permission.RECORD_AUDIO)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_MICROPHONE);
            }
        } else {
            startTuner();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        if (ActivityCompat.checkSelfPermission(this.getActivity().getApplicationContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Toast toast = Toast.makeText(this.getActivity(), "TUNER DIDN'T START FOR SOME REASON", Toast.LENGTH_SHORT);
            //toast.show();
            // Creates a new AlertDialog builder to be displayed if the user leaves any boxes blank or
            // attempts to leave the NewWordActivity
            builder = new AlertDialog.Builder(this.getActivity());
            // Cancels the word entry if the user clicks this AlertDialog option
            // Replies intent that result of this activity is Canceled
            builder.setPositiveButton("RETRY", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    Intent replyIntent = new Intent();
                    dialog.dismiss();
                    getActivity().setResult(RESULT_OK, replyIntent);
                    checkPermissions();
                }
            });
            builder.setNegativeButton("EXIT", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    // Do nothing
                    dialog.dismiss();
                }
            });


        } else {
            startTuner();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        audioThread.interrupt();
        dispatcher.stop();
    }

    @Override
    public void onStop() {
        super.onStop();
        audioThread.interrupt();
        dispatcher.stop();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStart(){
        super.onStart();
        checkPermissions();
    }

}
