package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.ui.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;

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
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.SpinnerAdapter;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.TabReader;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.TabRecycleAdapter;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.NotesViewModel;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Note;

import static java.lang.Thread.sleep;


public class TabListFragment extends Fragment {

    private static final int REQUEST_MICROPHONE = 1;
    private static final int ACCESS_REQUESTED = 1;

    // TarsosDSP object to allow for detection of frequency
    AudioDispatcher dispatcher;

    // A thread to run the pitch detection algorithm
    Thread audioThread;

    Thread vibrateThread;

    TabReader tabReader;
    List<String> fileNames;
    Spinner tabSpinner;
    TabRecycleAdapter adapter;
    RecyclerView recyclerView;
    int openNoteIds[] = new int[6];
    List<Integer> tabNoteIds = new ArrayList<>();
    List<Note> tabNotes = new ArrayList<>();
    List<Note> allNotes = new ArrayList<>();
    NotesViewModel notesViewModel;

    RadioButton autoscrollOff;
    RadioButton autoscrollSlow;
    RadioButton autoscrollMedium;
    RadioButton autoscrollFast;

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

        openNoteIds[0] = notesViewModel.getNoteByName("E4").getId();
        openNoteIds[1] = notesViewModel.getNoteByName("B3").getId();
        openNoteIds[2] = notesViewModel.getNoteByName("G3").getId();
        openNoteIds[3] = notesViewModel.getNoteByName("D3").getId();
        openNoteIds[4] = notesViewModel.getNoteByName("A2").getId();
        openNoteIds[5] = notesViewModel.getNoteByName("E2").getId();

        autoscrollOff = view.findViewById(R.id.radio_off);
        autoscrollOff.setChecked(true);
        autoscrollSlow = view.findViewById(R.id.radio_slow);
        autoscrollMedium = view.findViewById(R.id.radio_medium);
        autoscrollFast = view.findViewById(R.id.radio_fast);

        liveFeedbackSwitch = view.findViewById(R.id.switch_listen);

        vibrate = (Vibrator) getActivity().getSystemService(getActivity().VIBRATOR_SERVICE);

        vibrateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        vibrateTest();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        liveFeedbackSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!vibrateThread.isAlive()){
                    vibrateThread.start();
                }
                if(metronomeActive){
                    metronomeActive = false;
                }
                else if(!metronomeActive){
                    metronomeActive = true;
                }

            }
        });

        setupRadioButtons();

        allNotes.addAll(Arrays.asList(notesViewModel.getAllNotesAsList()));

        try {
            tabReader = new TabReader(this.getActivity().getApplicationContext(), openNoteIds);
            setupSpinners();
            List<String> tabStrings = new ArrayList<>(tabReader.getTab(tabSpinner.getSelectedItemPosition()));
            System.out.println(tabStrings.get(0));
            adapter.setTabList(tabStrings);
            tabNoteIds = tabReader.getTabNoteIds();
            convertTabToNotesList();
            System.out.println("DOING TABS");
        } catch (IOException e) {
            e.printStackTrace();
        }

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity().getApplicationContext()));

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
        dispatcher.stop();

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
        try {
            tabReader = new TabReader(this.getActivity().getApplicationContext(), openNoteIds);
            tabReader.getTabNames();
            List<String> tabStrings = new ArrayList<>(tabReader.getTab(pos));
            System.out.println(tabStrings.get(0));
            adapter.setTabList(tabStrings);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("SETTING TAB");

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

    }

    private void convertTabToNotesList() {
        for (int id : tabNoteIds) {
            tabNotes.add(notesViewModel.getNoteById(id));
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void processPitch(float pitchInHz) throws IOException {
        for (int i = 0; i < allNotes.size(); i++) {
            float currentNoteFreq = allNotes.get(i).getFrequency();
            if (i == 0) {
                float freqDifferenceUp = allNotes.get(i + 1).getFrequency() - currentNoteFreq;
                if (pitchInHz <= allNotes.get(i).getFrequency() + (freqDifferenceUp / 2)) {
                    currentClosestNote = allNotes.get(i);
                }
            } else if (i == allNotes.size() - 1) {
                float freqDifferenceDown = currentNoteFreq - allNotes.get(i - 1).getFrequency();
                if (pitchInHz >= currentNoteFreq - (freqDifferenceDown / 2)) {
                    currentClosestNote = allNotes.get(i);
                }
            } else {
                float freqDifferenceUp = allNotes.get(i + 1).getFrequency() - currentNoteFreq;
                float freqDifferenceDown = currentNoteFreq - allNotes.get(i - 1).getFrequency();
                if (pitchInHz >= currentNoteFreq - (freqDifferenceDown / 2) && pitchInHz <= allNotes.get(i).getFrequency() + (freqDifferenceUp / 2)) {
                    currentClosestNote = allNotes.get(i);
                }
            }
        }

        checkAgainstNextTabNote();
    }

    private boolean checkAgainstNextTabNote() {
        if (currentClosestNote.getNoteName().equals(tabNotes.get(tabNoteIndex).getNoteName())) {
            tabNoteIndex++;
            return true;
        } else {
            return false;
        }
    }

    private void setupRadioButtons() {
        autoscrollOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoscrollSlow.setChecked(false);
                autoscrollMedium.setChecked(false);
                autoscrollFast.setChecked(false);
            }
        });

        autoscrollSlow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoscrollOff.setChecked(false);
                autoscrollMedium.setChecked(false);
                autoscrollFast.setChecked(false);
            }
        });

        autoscrollMedium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoscrollSlow.setChecked(false);
                autoscrollOff.setChecked(false);
                autoscrollFast.setChecked(false);
            }
        });

        autoscrollFast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoscrollSlow.setChecked(false);
                autoscrollMedium.setChecked(false);
                autoscrollOff.setChecked(false);
            }
        });
    }

    private void vibrateTest() throws InterruptedException {
        if(metronomeActive) {
            vibrate.vibrate(150);
            sleep(250);
        }
    }
}
