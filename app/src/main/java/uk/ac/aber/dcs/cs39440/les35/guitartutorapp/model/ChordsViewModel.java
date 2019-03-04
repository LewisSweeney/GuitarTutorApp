package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource.ChordsRepository;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Chord;

public class ChordsViewModel extends AndroidViewModel {
    // Repository for DB access
    private ChordsRepository repo;
    // List that holds all currently stored words
    private Chord[] chordList;

    public ChordsViewModel(@NonNull Application application) {
        super(application);
        repo = new ChordsRepository(application);
        chordList = repo.getChordsList();
    }

    /**
     * Inserts notes into the DB
     *
     * @param chord
     */
    public void insert(Chord[] chord) {
        System.out.println("inserting at view model");
        repo.insert(chord);
    }

    public Chord[] getAllChordsAsList() {
        return chordList;
    }

    public Chord getChordByName(String name) {
        Chord chord = repo.getChordByName(name);
        return chord;
    }
}
