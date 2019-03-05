package uk.ac.aber.dcs.cs39440.les35.guitartutorapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource.CsvReader;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.ChordsViewModel;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Chord;


public class ChordsActivity extends AppCompatActivity {

    final int TOP_MARGIN_OF_CHART_FOR_MARKERS =  48;
    final int PADDING_INCREASE_FOR_MARKERS = 45;

    TextView chordNameTextView;
    Spinner rootNoteSpinner;
    Spinner chordSpinner;
    Chord[] chords;
    ImageView[] fretMarkers = new ImageView[6];

    Chord currentChord;

    Canvas canvas;
    Paint paint = new Paint();
    Bitmap bitmap;
    ImageView mImageView;

    ChordsViewModel chordsViewModel;

    Canvas chordCanvas;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chords);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        chordsViewModel = new ChordsViewModel(this.getApplication());

        chordNameTextView = findViewById(R.id.chordName);
        rootNoteSpinner = findViewById(R.id.chordRootSpinner);
        chordSpinner = findViewById(R.id.chordSpinner);
        getFretMarkers();

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
        for (Chord c : chords) {
            if (chordSpinner.getSelectedItem().toString().equals(c.getName())) {
                currentChord = c;
            }
        }
        drawChord();
    }

    private void getFretMarkers(){
        fretMarkers[0] = findViewById(R.id.fret_marker_one);
        fretMarkers[1] = findViewById(R.id.fret_marker_two);
        fretMarkers[2] = findViewById(R.id.fret_marker_three);
        fretMarkers[3] = findViewById(R.id.fret_marker_four);
        fretMarkers[4] = findViewById(R.id.fret_marker_five);
        fretMarkers[5] = findViewById(R.id.fret_marker_six);

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(25, 25);
        layoutParams.setMargins(0,0, 0,0);
        fretMarkers[0].setLayoutParams(layoutParams);
        fretMarkers[0].bringToFront();
    }

    private void drawChord() {
        chordNameTextView.setText(currentChord.getName());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

}
