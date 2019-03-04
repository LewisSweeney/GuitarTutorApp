package uk.ac.aber.dcs.cs39440.les35.guitartutorapp;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource.CsvReader;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.ChordsViewModel;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Chord;


public class ChordsActivity extends AppCompatActivity {

    TextView chordTest;
    Spinner rootNoteSpinner;
    Spinner chordSpinner;
    Chord[] chords;
    Boolean[][] chordChart = new Boolean[5][5];
    Chord currentChord;

    ChordsViewModel chordsViewModel;

    Canvas chordCanvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chords);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        chordsViewModel = new ChordsViewModel(this.getApplication());

        chordTest = findViewById(R.id.chordNoteTest);
        rootNoteSpinner = findViewById(R.id.chordRootSpinner);
        chordSpinner = findViewById(R.id.chordSpinner);
        try {
            CsvReader reader = new CsvReader(this);
            reader.readChords();
            chords = reader.getChords();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(chordsViewModel.getAllChordsAsList().length);
        System.out.println("CHORDS LIST SIZE = " + chords.length);
        setupSpinners();

    }

    private void setupSpinners() {

        SpinnerAdapter rootNoteAdapter = new SpinnerAdapter(this, getResources().getStringArray(R.array.root_notes_array));
        rootNoteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rootNoteSpinner.setAdapter(rootNoteAdapter);
        rootNoteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                setChordSpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        setChordSpinner();
        chordSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                changeChord();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }

    private void setChordSpinner() {
        List<Chord> chordsForSpinner = new ArrayList<>();
        String selectedRootNote = rootNoteSpinner.getSelectedItem().toString();
        System.out.println("SELECTED ROOT NOTE = " + selectedRootNote);
        switch (selectedRootNote) {
            case "A":
                chordsForSpinner.addAll(getChordsForSpinner("A"));
                break;
            case "A#/Bb":
                chordsForSpinner.addAll(getChordsForSpinner("A#"));
                chordsForSpinner.addAll(getChordsForSpinner("Bb"));
                break;
            case "B":
                chordsForSpinner.addAll(getChordsForSpinner("B"));
                break;
            case "C":
                chordsForSpinner.addAll(getChordsForSpinner("C"));
                break;
            case "C#/Db":
                chordsForSpinner.addAll(getChordsForSpinner("C#"));
                chordsForSpinner.addAll(getChordsForSpinner("Db"));
                break;
            case "D":
                chordsForSpinner.addAll(getChordsForSpinner("D"));
                break;
            case "D#/Eb":
                chordsForSpinner.addAll(getChordsForSpinner("D#"));
                chordsForSpinner.addAll(getChordsForSpinner("Eb"));
                break;
            case "E":
                chordsForSpinner.addAll(getChordsForSpinner("E"));
                break;
            case "F":
                chordsForSpinner.addAll(getChordsForSpinner("F"));
                break;
            case "F#/Gb":
                chordsForSpinner.addAll(getChordsForSpinner("F#"));
                chordsForSpinner.addAll(getChordsForSpinner("Gb"));
                break;
            case "G":
                chordsForSpinner.addAll(getChordsForSpinner("G"));
                break;
            case "G#/Ab":
                chordsForSpinner.addAll(getChordsForSpinner("G#"));
                chordsForSpinner.addAll(getChordsForSpinner("Ab"));
                break;
        }

        System.out.println("CHORDS FOR SPINNER SIZE = " + chordsForSpinner.size());
        List<String> chordNames = new ArrayList<>();

        for (Chord c : chordsForSpinner) {
            chordNames.add(c.getName());
        }

        SpinnerAdapter chordAdapter = new SpinnerAdapter(this, chordNames.toArray(new String[chordsForSpinner.size()]));
        chordAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chordSpinner.setAdapter(chordAdapter);

        changeChord();


    }

    private List<Chord> getChordsForSpinner(String root) {
        List<Chord> chordsForSpinner = new ArrayList<>();
        char[] rootAsArray = root.toCharArray();

        System.out.println("TRYING TO GET CHORDS FOR SPINNER");
        if (rootAsArray.length == 2) {
            for (Chord c : chords) {
                String chordName = c.getName();
                char[] chordNameAsArray = chordName.toCharArray();
                if (chordNameAsArray.length > 1) {
                    if (rootAsArray[0] == chordNameAsArray[0] && rootAsArray[1] == chordNameAsArray[1]) {
                        chordsForSpinner.add(c);
                    }
                }
            }
        } else if (rootAsArray.length == 1) {
            for (Chord c : chords) {
                String chordName = c.getName();
                System.out.println("CHORD NAME = ");
                char[] chordNameAsArray = chordName.toCharArray();
                if (chordNameAsArray.length > 1) {
                    if (rootAsArray[0] == chordNameAsArray[0] && chordNameAsArray[1] != '#' && chordNameAsArray[1] != 'b') {
                        chordsForSpinner.add(c);
                    }
                } else if (rootAsArray[0] == chordNameAsArray[0]) {
                    chordsForSpinner.add(c);
                }

            }
        }


        return chordsForSpinner;
    }

    private void changeChord() {
        for(Chord c : chords){
            if(chordSpinner.getSelectedItem().toString().equals(c.getName())){
                currentChord = c;
            }
        }
    }

    private void drawChord(){

    }

}
