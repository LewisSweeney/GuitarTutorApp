package uk.ac.aber.dcs.cs39440.les35.guitartutorapp;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.NumberPicker;

public class MetronomeActivity extends AppCompatActivity {

    NumberPicker bpmPicker;

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
        bpmPicker.setWrapSelectorWheel(true);
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
