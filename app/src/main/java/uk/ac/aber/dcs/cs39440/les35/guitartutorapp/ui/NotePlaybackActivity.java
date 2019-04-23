package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.R;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource.DataManager;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.NotesViewModel;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Note;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.StatType;

public class NotePlaybackActivity extends AppCompatActivity {

    // Notes 5 cents either way of the exact frequency
    final double noteLeeway = 5;

    // The number of questions a user is required to answer
    final int NUMBER_OF_QUESTIONS = 5;

    // The IDs for the notes that the first 12 frets on each string of the guitar can play
    int lowerBoundID = 28;
    int upperBoundID = 64;

    // Integer that determines how many "checks" of the current frequency must be in tune for
    // a note to be c;assed as correct
    final int noteCorrectLimit = 12;

    List<Boolean> correctnessList = new ArrayList<>();

    // boolean to determine whether the note detection algorithm should be listening out for notes
    boolean detectingNote = false;

    // View Model to access th notes table of the Room Database
    NotesViewModel notesViewModel;

    TextView playbackPromptNote;
    TextView score;
    TextView total;

    int userScore = 0;
    int totalAnswered = 0;

    Button nextQuestionButton;

    // TarsosDSP object to allow for detection of frequency
    AudioDispatcher dispatcher;

    // A thread to run the pitch detection algorithm
    Thread audioThread = new Thread();

    // Note that is currently selected for the user to play
    Note currentRequiredNote;

    // ArrayList to hold all notes for questions
    List<Note> requiredNotes = new ArrayList<>();

    int numberOfDetections = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_playback);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        notesViewModel = ViewModelProviders.of(this).get(NotesViewModel.class);

        playbackPromptNote = findViewById(R.id.playback_prompt_note);
        score = findViewById(R.id.score);
        total = findViewById(R.id.total);
        nextQuestionButton = findViewById(R.id.button_next);
        nextQuestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextQuestionButtonClick();
            }
        });

        for (int i = lowerBoundID; i <= upperBoundID; i++) {
            requiredNotes.add(notesViewModel.getNoteById(i));
        }

        setScores();
        setNextQuestion();
        startDetection();

    }

    private void startDetection() {
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
        detectingNote = true;
        audioThread = new Thread(dispatcher, "Detection Thread");
        audioThread.start();
    }

    /**
     * This method takes the current frequency being detected and compares it to the note that is
     * required for a correct answer
     *
     * @param pitchInHz This is the current pitch being picked up by the microphone
     */
    public void processPitch(float pitchInHz) throws IOException {
        System.out.println("DETECTING PITCH");
        if (detectingNote) {
            Note previousChromaticNote = notesViewModel.getNoteBefore(currentRequiredNote.getId());
            Note nextChromaticNote = notesViewModel.getNoteAfter(currentRequiredNote.getId());

            float frequencyDifferenceDown = currentRequiredNote.getFrequency() - previousChromaticNote.getFrequency();
            float frequencyDifferenceUp = nextChromaticNote.getFrequency() - currentRequiredNote.getFrequency();

            float centDown = frequencyDifferenceDown / 100;
            float centUp = frequencyDifferenceUp / 100;

            if (pitchInHz >= currentRequiredNote.getFrequency() - (noteLeeway * centDown) && pitchInHz <= currentRequiredNote.getFrequency() + (noteLeeway * centUp)) {
               correctnessList.add(true);
            } else if (pitchInHz > 0){
               correctnessList.add(false);
            }

            if(correctnessList.size() >= noteCorrectLimit){
                calculateCorrectness();
            }
        }
    }

    private void calculateCorrectness() throws IOException {
        int correct = 0;
        int incorrect = 0;

        for(boolean i : correctnessList){
            if(i){
                correct++;
            } else{
                incorrect++;
            }
        }

        if(correct >= incorrect){
            notePlayedIsCorrect(true);
        } else{
            notePlayedIsCorrect(false);
        }
    }

    private void notePlayedIsCorrect(boolean correct) throws IOException {
        detectingNote = false;
        nextQuestionButton.setClickable(true);
        if(correct){
            playbackPromptNote.setText(getString(R.string.correct_note_text));
            playbackPromptNote.setTextColor(Color.GREEN);
            userScore++;
            playSound(true);
        } else{
            playbackPromptNote.setText(getString(R.string.incorrect_note_text));
            playbackPromptNote.setTextColor(Color.RED);
            playSound(false);
        }
        totalAnswered++;

        if(totalAnswered >= NUMBER_OF_QUESTIONS){
            setScores();
            endGame();
        }

        correctnessList.clear();
    }

    private void endGame() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Finished").setMessage("Final Score: " + userScore + "/" + NUMBER_OF_QUESTIONS).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    closeActivity();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void closeActivity() throws IOException {
        DataManager dataManager = new DataManager(this);
        dataManager.writeStats(StatType.REPSCORE, userScore);
        dataManager.writeStats(StatType.REPTOT, 1);
        this.finish();
    }

    private void setScores() {
        score.setText(Integer.toString(userScore));
        total.setText(Integer.toString(totalAnswered) + "/" + Integer.toString(NUMBER_OF_QUESTIONS));
    }

    private void setNextQuestion() {
        System.out.println("BUTTON CLICK");
        if(totalAnswered < NUMBER_OF_QUESTIONS){
            Collections.shuffle(requiredNotes);
            currentRequiredNote = requiredNotes.get(0);
            playbackPromptNote.setTextColor(Color.BLACK);
            playbackPromptNote.setText(currentRequiredNote.getNoteName());
            nextQuestionButton.setClickable(false);
        } else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.final_score_title));
            builder.setMessage(getString(R.string.final_score_first) + Integer.toString(userScore) + "/" + Integer.toString(NUMBER_OF_QUESTIONS));
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }

    }

    private void onNextQuestionButtonClick() {
        setScores();
        setNextQuestion();
        detectingNote = true;
    }

    private void playSound(boolean correct) throws IOException {
        AssetFileDescriptor afd;
        if(correct){
            afd = getAssets().openFd(getString(R.string.sound_intune));
        } else{
            afd = getAssets().openFd(getString(R.string.sound_incorrect));
        }
        MediaPlayer mPlayer = new MediaPlayer();
        mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        mPlayer.prepare();
        mPlayer.start();
    }

    @Override
    public void onBackPressed(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.leave_game_activity));
        // Cancels the word entry if the user clicks this AlertDialog option
        // Replies intent that result of this activity is Canceled
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                finish();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();

    }

    @Override
    public void onPause() {
        super.onPause();
        dispatcher.stop();
        Toast toast = Toast.makeText(this, getString(R.string.toast_game_quit), Toast.LENGTH_SHORT);
        finish();
    }

    @Override
    public void onStop() {
        super.onStop();
        dispatcher.stop();
        Toast toast = Toast.makeText(this, getString(R.string.toast_game_quit), Toast.LENGTH_SHORT);
        finish();
    }
}
