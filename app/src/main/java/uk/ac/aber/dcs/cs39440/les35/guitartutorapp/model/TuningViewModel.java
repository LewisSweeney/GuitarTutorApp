package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import java.util.List;


import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource.TuningRepository;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Tuning;

public class TuningViewModel extends AndroidViewModel {
    // Repository for DB access
    private TuningRepository repo;
    // List that holds all currently stored words
    private List<Tuning> tuningList;

    public TuningViewModel(@NonNull Application application) {
        super(application);
        repo = new TuningRepository(application);
    }

    /**
     * Inserts tunings into the DB
     * @param tunings
     */
    public void insert(Tuning[] tunings){repo.insert(tunings);}
}
