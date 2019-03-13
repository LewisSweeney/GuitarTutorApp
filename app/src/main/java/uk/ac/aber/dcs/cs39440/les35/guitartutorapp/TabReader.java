package uk.ac.aber.dcs.cs39440.les35.guitartutorapp;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.NotesViewModel;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Note;

public class TabReader {
    AssetManager am;
    InputStream is;
    String filename;
    BufferedReader reader;
    String line = "";
    Context context;
    String[] files;
    List<String> tabNames;
    List<Integer> tabNoteIds = new ArrayList<>();
    int[] openNoteIds = new int[6];
    List<String> tabFileNames = new ArrayList<>();


    public TabReader(Context context, int[] openNoteIds) throws IOException {
        files = context.getAssets().list("tabs");
        am = context.getAssets();
        this.context = context;
        this.openNoteIds = openNoteIds;
    }

    public String[] getTabNames() throws IOException {
        List<String> tabNames = new ArrayList<>();
        for (String file : files) {
            filename = "tabs/" + file;
            tabFileNames.add(filename);
            is = am.open(filename);
            reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));

            try {
                String name = reader.readLine();
                tabNames.add(name);
                System.out.println(name);
            } catch (IOException exception) {
                Log.e("CSV Reader", "Error " + line, exception);
                exception.printStackTrace();
            }
        }

        return tabNames.toArray(new String[tabNames.size()]);

    }

    public List<String> getTab(int position) throws IOException {
        String line = "";
        String[] tabStave;

        filename = tabFileNames.get(position);
        is = am.open(filename);
        reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));

        List<String> tab = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            if (line.charAt(0) == '[') {
                tabStave = constructTabStave(line);
                StringBuilder newTabStave = new StringBuilder();
                for(String tempLine : tabStave){
                    tempLine = tempLine.replace("[", "");
                    tempLine = tempLine.replace("]", "");
                    newTabStave.append(tempLine);
                }
                tab.add(newTabStave.toString());
            }
        }

        return tab;
    }

    private String[] constructTabStave(String line) {
        line = line.replace("[", "");
        line = line.replace("]", "");
        String[] stringsForTab = context.getResources().getStringArray(R.array.tab_string_notes);

        String[] stave = new String[6];
        String[] lineSplit = line.split(",");

        for (int i = 0; i < stave.length; i++) {
            stave[i] = "" + stringsForTab[i] + "|";
        }

        for (String split : lineSplit) {
            int guitarString = -1;
            if (!split.equals("-")) {
                String[] subSplit = split.split(":");
                guitarString = Integer.parseInt(subSplit[0]);
                int noteIdIncrement = Integer.parseInt(subSplit[1]);
                tabNoteIds.add(openNoteIds[guitarString - 1] + noteIdIncrement);
                stave[guitarString - 1] += subSplit[1] + "-";

                for (int i = 0; i < openNoteIds.length; i++) {
                    if(guitarString != -1 && i != guitarString -1 && subSplit[1].length() == 2){
                        stave[i] += "-";
                    }
                    if (guitarString != -1 && i != guitarString - 1) {
                        stave[i] += "--";
                    }
                }
            } else {
                for (int i = 0; i < stave.length; i++) {
                    stave[i] += "--";
                }
            }
        }

        for(int i = 0; i < stave.length; i++){
            stave[i] = stave[i] + "|" + "\n";
        }

        return stave;
    }


}
