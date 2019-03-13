package uk.ac.aber.dcs.cs39440.les35.guitartutorapp;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;



import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.NotesViewModel;

public class TabActivity extends AppCompatActivity {

    TabReader tabReader;
    List<String> fileNames;
    Spinner tabSpinner;
    TabRecycleAdapter adapter;
    RecyclerView recyclerView;
    int openNoteIds[] = new int[6];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        NotesViewModel notesViewModel = new NotesViewModel(this.getApplication());

        recyclerView = findViewById(R.id.tab_recycler);
        adapter = new TabRecycleAdapter(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        tabSpinner = findViewById(R.id.tab_spinner);



        openNoteIds[0] = notesViewModel.getNoteByName("E4").getId();
        openNoteIds[1] = notesViewModel.getNoteByName("B3").getId();
        openNoteIds[2] = notesViewModel.getNoteByName("G3").getId();
        openNoteIds[3] = notesViewModel.getNoteByName("D3").getId();
        openNoteIds[4] = notesViewModel.getNoteByName("A2").getId();
        openNoteIds[5] = notesViewModel.getNoteByName("E2").getId();

        try {
            tabReader = new TabReader(this.getApplicationContext(), openNoteIds);
            setupSpinners();
            List<String> tabStrings = new ArrayList<>(tabReader.getTab(tabSpinner.getSelectedItemPosition()));
            System.out.println(tabStrings.get(0));
            adapter.setTabList(tabStrings);
        } catch (IOException e) {
            e.printStackTrace();
        }

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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

    private void setupSpinners() throws IOException {

        SpinnerAdapter tabSpinnerAdapter = new SpinnerAdapter(this, tabReader.getTabNames());
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
            tabReader = new TabReader(this.getApplicationContext(), openNoteIds);
            tabReader.getTabNames();
            List<String> tabStrings = new ArrayList<>(tabReader.getTab(pos));
            System.out.println(tabStrings.get(0));
            adapter.setTabList(tabStrings);
        } catch (IOException e) {
            e.printStackTrace();
        }

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    private void convertTabToNotesList(){

    }
}
