package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.R;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.NotesViewModel;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Note;

public class NoteRecognitionActivity extends AppCompatActivity {

    Button buttonOptionOne;
    Button buttonOptionTwo;
    Button buttonOptionThree;
    Button buttonOptionFour;
    Button correctAnswerButton;

    boolean isPlaying = false;

    Bitmap imageViewBitmap;
    Bitmap playBitmap;
    Bitmap pauseBitmap;

    Drawable playButton;
    Drawable pauseButton;

    // The IDs for the notes that the first 12 frets on each string of the guitar can play
    int lowerBoundID = 28;
    int upperBoundID = 64;

    ImageView playPauseButton;

    NotesViewModel notesView;

    Note currentCorrectNote;
    Note[] incorrectNotes;

    List<Integer> idList;
    List<Integer> buttonIdList;

    TextView scoreDisplay;

    int score = 0;
    int totalAnswered = 0;

    float currentNoteFrequency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_recognition);

        notesView = ViewModelProviders.of(this).get(NotesViewModel.class);

        incorrectNotes = new Note[3];

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        buttonOptionOne = findViewById(R.id.button_option_one);
        buttonOptionTwo = findViewById(R.id.button_option_two);
        buttonOptionThree = findViewById(R.id.button_option_three);
        buttonOptionFour = findViewById(R.id.button_option_four);

        playPauseButton = findViewById(R.id.play_pause_button);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePlayPauseButtonState();
            }
        });

        scoreDisplay = findViewById(R.id.score);

        playButton = getDrawable(R.drawable.ic_play_circle_outline_black_24dp);
        pauseButton = getResources().getDrawable(R.drawable.ic_pause_circle_outline_black_24dp);

        idList = new ArrayList<>();
        buttonIdList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            buttonIdList.add(i);
        }

        resetArrayLists();
        setNotes();
        setButtons();
    }

    private void setButtons() {

    }

    private Button getButtonForAnswer(int id){
        Button tempButton;
        switch (id) {
            case 0:
                tempButton = buttonOptionOne;
                break;
            case 1:
                tempButton =  buttonOptionTwo;
                break;
            case 2:
                tempButton =  buttonOptionThree;
                break;
            case 3:
                tempButton =  buttonOptionFour;
                break;
            default:
                tempButton =  buttonOptionOne;
                break;
        }
        return tempButton;
    }

    /**
     * Sets the notes for the choices to the first 4 values of the IDList
     */
    private void setNotes() {
        currentCorrectNote = notesView.getNoteById(idList.get(0));
        incorrectNotes[0] = notesView.getNoteById(idList.get(1));
        incorrectNotes[1] = notesView.getNoteById(idList.get(2));
        incorrectNotes[2] = notesView.getNoteById(idList.get(3));
    }

    /**
     * Resets the ID arraylist to allow for random generation of note IDs again
     * Then shuffles the list to effectively randomly choose the note IDs.
     * <p>
     * The reason for this instead of generating numbers on the fly is so that each
     * note ID is guaranteed to be unique, to avoid issues.
     */
    private void resetArrayLists() {
        idList.clear();
        for (int i = lowerBoundID; i < upperBoundID; i++) {
            idList.add(i);
        }
        Collections.shuffle(idList);
        Collections.shuffle(buttonIdList);
    }

    private void changePlayPauseButtonState() {
        if (isPlaying) {
            playPauseButton.setImageDrawable(playButton);
            isPlaying = false;
        } else if (!isPlaying) {
            playPauseButton.setImageDrawable(pauseButton);
            isPlaying = true;
        }

        System.out.println(isPlaying);
    }
}
