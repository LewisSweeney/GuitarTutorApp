package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.IOException;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.ChordDAO;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.NotesDAO;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Chord;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.MyApplication;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Note;

@Database(entities = {Note.class, Chord.class}, version = 2, exportSchema = false
)

/**
 * Room Database class that contains entities for Notes and Chords, and is the direct accessor for
 * the database data
 */
public abstract class GuitarRoomDatabase extends RoomDatabase {

    private static GuitarRoomDatabase INSTANCE;

    public abstract NotesDAO getNotesDao();

    public abstract ChordDAO getChordsDao();

    // public abstract TuningDAO getTuningDao();


    public static GuitarRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (GuitarRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), GuitarRoomDatabase.class, "guitar_database")
                            .allowMainThreadQueries().addMigrations(MIGRATION_1_2).addCallback(sRoomDatabaseCallback).build();

                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {

        public void onCreate(SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsync(INSTANCE).execute();

        }

        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
        }
    };

    public void closeDb() {
        close();
    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {
        private final NotesDAO notesDAO;
        private final ChordDAO chordDAO;

        PopulateDbAsync(GuitarRoomDatabase db) {
            notesDAO = db.getNotesDao();
            chordDAO = db.getChordsDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                DataManager reader = new DataManager(MyApplication.getAppContext());

                reader.readNotes();
                Note[] notes = reader.getNotes();

                reader.readChords();

                Chord[] chords = reader.getChords();

                notesDAO.insertNotes(notes);
                chordDAO.insertChords(chords);

            } catch (IOException e) {
                System.out.println("CAUGHT");
                e.printStackTrace();
            }
            return null;
        }
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `chords` (`id` INTEGER, " + "`name` TEXT, " + " `notes` TEXT, " + " `startFret` INTEGER, PRIMARY KEY(`id`))");
        }
    };
}
