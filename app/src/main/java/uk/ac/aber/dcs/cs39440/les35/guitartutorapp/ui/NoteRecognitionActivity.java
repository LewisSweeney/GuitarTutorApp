package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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

    final int BUTTON_NUMBER = 3;
    final int NUMBER_OF_QUESTIONS = 5;

    Thread noteThread;

    Button[] buttons;
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

    // List for Note IDs, shuffled for unique random IDs
    List<Integer> idList;
    // List for button IDs, shuffled for unique random IDs
    List<Integer> buttonIdList;

    TextView scoreDisplay;
    TextView questionDisplay;

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

        buttons[0] = findViewById(R.id.button_option_one);
        buttons[1] = findViewById(R.id.button_option_two);
        buttons[2] = findViewById(R.id.button_option_three);
        buttons[3] = findViewById(R.id.button_option_four);

        for (int i = 0; i < BUTTON_NUMBER; i++) {
            final Button tempButton = buttons[i];
            buttons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonClicked(tempButton);
                }
            });
        }

        playPauseButton = findViewById(R.id.play_pause_button);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePlayPauseButtonState();
            }
        });

        scoreDisplay = findViewById(R.id.score);
        questionDisplay = findViewById(R.id.total);

        playButton = getDrawable(R.drawable.ic_play_circle_outline_black_24dp);
        pauseButton = getResources().getDrawable(R.drawable.ic_pause_circle_outline_black_24dp);

        idList = new ArrayList<>();
        buttonIdList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            buttonIdList.add(i);
        }

        nextQuestion();

        noteThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        playNote();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void setButtons() {
        int correctButtonID = buttonIdList.get(0);
        correctAnswerButton = buttons[correctButtonID];
        buttons[correctButtonID].setText(currentCorrectNote.getNoteName());
        buttons[buttonIdList.get(1)].setText(incorrectNotes[0].getNoteName());
        buttons[buttonIdList.get(2)].setText(incorrectNotes[1].getNoteName());
        buttons[buttonIdList.get(3)].setText(incorrectNotes[2].getNoteName());
    }

    /**
     * Sets the notes for the choices to the first 4 values of the IDList
     */
    private void setNotes() {
        currentCorrectNote = notesView.getNoteById(idList.get(0));
        incorrectNotes[0] = notesView.getNoteById(idList.get(1));
        incorrectNotes[1] = notesView.getNoteById(idList.get(2));
        incorrectNotes[2] = notesView.getNoteById(idList.get(3));

        currentNoteFrequency = currentCorrectNote.getFrequency();
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

    private void buttonClicked(Button clickedButton) {
        totalAnswered++;
        if (clickedButton.equals(correctAnswerButton)) {
            score++;
        }

        if (totalAnswered >= NUMBER_OF_QUESTIONS) {
            endGame();
        } else {
            nextQuestion();
        }
    }

    private void nextQuestion() {
        scoreDisplay.setText(score);
        questionDisplay.setText(totalAnswered + "/" + NUMBER_OF_QUESTIONS);
        isPlaying = false;
        for (int i = 0; i < BUTTON_NUMBER; i++) {
            buttons[i].setClickable(false);
        }
        resetArrayLists();
        setNotes();
        setButtons();
        for (int i = 0; i < BUTTON_NUMBER; i++) {
            buttons[i].setClickable(true);
        }
    }

    private void playNote() throws InterruptedException {
        if (isPlaying) {
            playSound(currentNoteFrequency, 44100);
            Thread.sleep(1000);
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

    private void endGame() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Finished").setMessage("Final Score: " + score + "/" + NUMBER_OF_QUESTIONS).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        closeActivity();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void closeActivity(){
        this.finish();
    }

}
