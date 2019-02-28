package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.IOException;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.NotesDAO;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.MyApplication;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Note;

@Database(entities = {Note.class/*, Tuning.class*/}, version = 1)

public abstract class GuitarRoomDatabase extends RoomDatabase {

    private static GuitarRoomDatabase INSTANCE;

    public abstract NotesDAO getNotesDao();

    // public abstract TuningDAO getTuningDao();


    public static GuitarRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (GuitarRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            GuitarRoomDatabase.class, "guitar_database").allowMainThreadQueries().addCallback(sRoomDatabaseCallback).build();
                     /* Do the following when migrating
                     to a new version of the database
                     INSTANCE =
                     Room.databaseBuilder(
                     context.getApplicationContext(),
                     FaaRoomDatabase.class,
                     "faa_database").addMigrations(MIGRATION_1_2, G
                     MIGRATION_2_3) H
                     .build();
*/
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {

        public void onCreate(SupportSQLiteDatabase db) {
            super.onCreate(db);
            System.out.println("TRYING TO ADD");
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

        PopulateDbAsync(GuitarRoomDatabase db){
            notesDAO = db.getNotesDao();
        }
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                CsvReader reader = new CsvReader("csv/notes.csv", MyApplication.getAppContext());
                reader.readNotes();
                Note[] notes = reader.getNotes();
                notesDAO.insertNotes(notes);

            } catch (IOException e) {
                System.out.println("CAUGHT");
                e.printStackTrace();
            }
            return null;
        }
    }
}
