package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.ui;

import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import be.tarsos.dsp.synthesis.SineGenerator;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.R;

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

    int currentBeat = 99;

    double timeBetweenBeats;

    SineGenerator sineGen;

    int beatFreq = 440;
    int barFreq = 880;

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
        bpmPicker.setValue(100);
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
        beatPicker.setValue(4);
        beatPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                setBeatsPerBar();
            }
        });

        metronomeSwitch = findViewById(R.id.switch_metronome);
        metronomeBeat = findViewById(R.id.current_beat);

        vibrate = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        vibrateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        metronomeTick();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        metronomeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!vibrateThread.isAlive()) {
                    vibrateThread.start();
                }
                if (metronomeActive) {
                    deactivateMetronome();
                } else if (!metronomeActive) {
                    metronomeActive = true;
                }
            }


        });

        setTimeBetweenBeats();
        setBeatsPerBar();
        deactivateMetronome();
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

        if (metronomeActive) {

            if (currentBeat < beatsPerBar) {
                currentBeat++;
                vibrate.vibrate(150);
            } else if (currentBeat >= beatsPerBar) {
                currentBeat = 1;
                vibrate.vibrate(200);
            }
            metronomeBeat.post(new Runnable() {
                public void run() {
                    changeBeat();
                }
            });

            double toMicro = round(timeBetweenBeats, 3);
            int nanoseconds = (int) toMicro * 1000000;
            long milliseconds = 0;
            while(nanoseconds >= 1000000){
                nanoseconds = nanoseconds - 1000000;
                milliseconds++;
            }
            Thread.sleep(milliseconds,nanoseconds);

        }
    }

    private void setTimeBetweenBeats() {
        if (metronomeSwitch.isChecked()) {
            metronomeSwitch.toggle();
            deactivateMetronome();
        }
        int chosenBpmValue = bpmPicker.getValue();
        System.out.println(chosenBpmValue);
        double beatsPerSecond;
        beatsPerSecond = (double) chosenBpmValue / ONE_MINUTE_IN_SECONDS;
        System.out.println(beatsPerSecond);
        timeBetweenBeats = (double)1000 / beatsPerSecond;
        System.out.println(timeBetweenBeats);
    }

    private void setBeatsPerBar() {
        if (metronomeSwitch.isChecked()) {
            metronomeSwitch.toggle();
            deactivateMetronome();
        }
        beatsPerBar = beatPicker.getValue();
        System.out.println(beatsPerBar);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    private void changeBeat(){

        metronomeBeat.setText(String.valueOf(currentBeat));
        if(currentBeat == 99){
            metronomeBeat.setText("-");
        }
        if(metronomeBeat.getText().equals("1")){
            metronomeBeat.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        else{
            metronomeBeat.setTextColor(getResources().getColor(R.color.black));
        }
    }

    private void deactivateMetronome(){
        metronomeActive = false;
        metronomeBeat.setTextColor(getResources().getColor(R.color.black));
        currentBeat = 99;
        metronomeBeat.setText("-");

    }

    @Override
    public void onPause(){
        super.onPause();
        deactivateMetronome();
        if(metronomeSwitch.isChecked()){
            metronomeSwitch.toggle();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        deactivateMetronome();
        if(metronomeSwitch.isChecked()){
            metronomeSwitch.toggle();
        }
    }
}
