package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.ui.fragments.tabs;

import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.R;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.adapters.SpinnerAdapter;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.adapters.TabRecycleAdapter;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource.TabReader;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.NotesViewModel;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Note;


public class TabListFragment extends Fragment {

    private static final int REQUEST_MICROPHONE = 1;
    private static final int ACCESS_REQUESTED = 1;
    private static final int NOTE_LEEWAY = 8;

    Boolean detectingNote = false;

    Note currentRequiredNote;

    // TarsosDSP object to allow for detection of frequency
    AudioDispatcher dispatcher;

    // A thread to run the pitch detection algorithm
    Thread audioThread;

    Thread vibrateThread;

    TabReader tabReader;

    Spinner tabSpinner;
    TextView nextNote;
    TextView pleasePlay;
    RecyclerView recyclerView;

    TabRecycleAdapter adapter;

    int openNoteIds[] = new int[6];
    List<String> fileNames;
    List<Integer> tabNoteIds = new ArrayList<>();
    List<Note> tabNotes = new ArrayList<>();
    List<Note> allNotes = new ArrayList<>();

    NotesViewModel notesViewModel;

    Switch liveFeedbackSwitch;

    Vibrator vibrate;

    Note currentClosestNote;

    Boolean metronomeActive = false;

    int tabNoteIndex = 0;


    public TabListFragment() {
        // Required empty public constructor
    }

    public static TabListFragment newInstance(String param1, String param2) {
        TabListFragment fragment = new TabListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_tab_list, container, false);

        notesViewModel = new NotesViewModel(this.getActivity().getApplication());

        recyclerView = view.findViewById(R.id.tab_recycler);
        adapter = new TabRecycleAdapter(this.getActivity().getApplicationContext());

        tabSpinner = view.findViewById(R.id.tab_spinner);
        nextNote = view.findViewById(R.id.next_note);
        pleasePlay = view.findViewById(R.id.please_play);

        openNoteIds[0] = notesViewModel.getNoteByName("E4").getId();
        openNoteIds[1] = notesViewModel.getNoteByName("B3").getId();
        openNoteIds[2] = notesViewModel.getNoteByName("G3").getId();
        openNoteIds[3] = notesViewModel.getNoteByName("D3").getId();
        openNoteIds[4] = notesViewModel.getNoteByName("A2").getId();
        openNoteIds[5] = notesViewModel.getNoteByName("E2").getId();

        liveFeedbackSwitch = view.findViewById(R.id.switch_listen);

        liveFeedbackSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(liveFeedbackSwitch.isChecked()){
                    initialiseFeedback();
                } else{
                    endFeedback();
                }
            }
        });

        allNotes.addAll(Arrays.asList(notesViewModel.getAllNotesAsList()));

        try {
            tabReader = new TabReader(this.getActivity().getApplicationContext(), openNoteIds);
            setupSpinners();
            List<String> tabStrings = new ArrayList<>(tabReader.getTab(tabSpinner.getSelectedItemPosition()));
            System.out.println(tabStrings.get(0));
            adapter.setTabList(tabStrings);
            convertTabToNotesList();
            System.out.println("DOING TABS");
        } catch (IOException e) {
            e.printStackTrace();
        }

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity().getApplicationContext()));

        return view;
    }


    private void setupSpinners() throws IOException {

        SpinnerAdapter tabSpinnerAdapter = new SpinnerAdapter(this.getContext(), tabReader.getTabNames());
        tabSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tabSpinner.setAdapter(tabSpinnerAdapter);
        tabSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                setTab(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

    }

    private void setTab(int pos) {
        endFeedback();
        try {
            tabReader = new TabReader(this.getActivity().getApplicationContext(), openNoteIds);
            tabReader.getTabNames();
            List<String> tabStrings = new ArrayList<>(tabReader.getTab(pos));
            System.out.println(tabStrings.get(0));
            adapter.setTabList(tabStrings);
            tabNoteIds.clear();
            tabNoteIds = tabReader.getTabNoteIds();
            convertTabToNotesList();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("SETTING TAB");

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

    }

    private void convertTabToNotesList() {
        tabNotes.clear();
        for (int id : tabNoteIds) {
            tabNotes.add(notesViewModel.getNoteById(id));
            System.out.println(notesViewModel.getNoteById(id).getNoteName());
        }
    }

    public interface OnFragmentInteractionListener {

    }

    /**
     * This method takes the current frequency being detected and compares it to the note that is
     * required for a correct answer
     *
     * @param pitchInHz This is the current pitch being picked up by the microphone
     */
    public void processPitch(float pitchInHz) throws IOException {

        if (detectingNote) {
            System.out.println("DETECTING PITCH");
            Note previousChromaticNote = notesViewModel.getNoteBefore(currentRequiredNote.getId());
            Note nextChromaticNote = notesViewModel.getNoteAfter(currentRequiredNote.getId());

            float frequencyDifferenceDown = currentRequiredNote.getFrequency() - previousChromaticNote.getFrequency();
            float frequencyDifferenceUp = nextChromaticNote.getFrequency() - currentRequiredNote.getFrequency();

            float centDown = frequencyDifferenceDown / 100;
            float centUp = frequencyDifferenceUp / 100;

            if (pitchInHz >= currentRequiredNote.getFrequency() - (NOTE_LEEWAY * centDown) && pitchInHz <= currentRequiredNote.getFrequency() + (NOTE_LEEWAY * centUp)) {
                System.out.println("CORRECT NOTE");
                nextNote();
            }
        }
    }

    private void startFeedback(){
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

    private void nextNote(){
        tabNoteIndex++;
        if(tabNoteIndex < tabNotes.size()){
            currentRequiredNote = tabNotes.get(tabNoteIndex);
            nextNote.setText(currentRequiredNote.getNoteName());
        } else{
            endFeedback();
        }
    }

    private void initialiseFeedback(){
        tabNoteIndex = 0;
        if(tabNoteIndex < tabNotes.size()){
            currentRequiredNote = tabNotes.get(tabNoteIndex);
            nextNote.setText(currentRequiredNote.getNoteName());
            pleasePlay.setText("Play");
            detectingNote = true;
        } else{
            endFeedback();
        }
    }

    private void endFeedback(){
        tabNoteIndex = 0;
        nextNote.setText("");
        detectingNote = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        liveFeedbackSwitch.setChecked(false);
        detectingNote = false;
    }

    @Override
    public void onPause(){
        super.onPause();
        liveFeedbackSwitch.setChecked(false);
        detectingNote = false;
    }
}
