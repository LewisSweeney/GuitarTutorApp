package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.ui;

import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.R;

import static java.lang.Thread.sleep;

public class MetronomeActivity extends AppCompatActivity {

    final static int ONE_MINUTE_IN_SECONDS = 60;

    NumberPicker bpmPicker;
    NumberPicker beatPicker;

    Switch metronomeSwitch;

    Vibrator vibrate;

    Thread vibrateThread;

    Boolean metronomeActive = false;

    TextView metronomeBeat;

    int beatsPerBar;

    int currentBeat = 1;

    long timeBetweenBeats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metronome);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        bpmPicker = findViewById(R.id.bpm_picker);
        bpmPicker.setMaxValue(getResources().getInteger(R.integer.max_bpm_metronome));
        bpmPicker.setMinValue(getResources().getInteger(R.integer.min_bpm_metronome));
        bpmPicker.setWrapSelectorWheel(false);
        bpmPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                setTimeBetweenBeats();
            }
        });

        beatPicker = findViewById(R.id.beat_picker);
        beatPicker.setMaxValue(getResources().getInteger(R.integer.max_beat_value));
        beatPicker.setMinValue(getResources().getInteger(R.integer.min_beat_value));
        beatPicker.setWrapSelectorWheel(false);

        metronomeSwitch = findViewById(R.id.switch_listen);
        metronomeBeat = findViewById(R.id.current_beat);

        vibrate = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        vibrateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        metronomeTick();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        metronomeSwitch.setOnClickListener(new View.OnClickListener() {
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

    private void metronomeTick() throws InterruptedException {
        if(metronomeActive) {
            vibrate.vibrate(150);
            if(currentBeat < beatsPerBar){
                currentBeat++;
            }
            else if(currentBeat == beatsPerBar ){
                currentBeat = 1;
            }
            metronomeBeat.setText(currentBeat);
            sleep(250);

        }
    }

    private void setTimeBetweenBeats(){
        if(metronomeSwitch.isChecked()){
            metronomeSwitch.toggle();
        }
        int chosenBpmValue = bpmPicker.getValue();
        long beatsPerSecond = chosenBpmValue / ONE_MINUTE_IN_SECONDS;
        timeBetweenBeats = 1000 / beatsPerSecond;
    }

    private void setBeatsPerBar(){
        beatsPerBar = beatPicker.getValue();
    }
}
