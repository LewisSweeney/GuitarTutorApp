package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.MainActivity;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.NotesDAO;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.TuningDAO;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.MyApplication;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Note;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Tuning;

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
            Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        CsvReader reader = new CsvReader("notes.csv", MyApplication.getAppContext());
                        reader.readFile();
                        List<Note> notes = reader.getNotes();
                        getDatabase(MyApplication.getAppContext()).getNotesDao().insertNotes(notes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
        }
    };

    public void closeDb() {
        close();
    }
}
