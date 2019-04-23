package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.R;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource.DataManager;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder> {

    private String[] titleList;
    private String[] descriptionList;
    Context context;


    /**
     * Sub-class LearnViewHolder extends on RecyclerView.VIewHolder
     * Contains the TextView views for displaying the text from each Learn object
     */
    class SettingsViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        private final TextView titleView;
        private final TextView descriptionView;

        SettingsViewHolder(@NonNull View itemView) {
            super(itemView);
            titleList = itemView.getResources().getStringArray(R.array.settings_titles);
            descriptionList = itemView.getResources().getStringArray(R.array.settings_description);
            titleView = itemView.findViewById(R.id.learn_item);
            descriptionView = itemView.findViewById(R.id.learn_description);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        }
    }

    /**
     * Constructor for the TabSymbolAdapter
     * @param context
     */
    public SettingsAdapter(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        this.context = context;
    }


    /**
     * Method to create a new LearnViewHolder
     * Required for extending RecyclerView.Adapter
     *
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public SettingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new SettingsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsViewHolder holder, final int position) {
        try {
            DataManager dataManager = new DataManager(context);
            dataManager.readStats();

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (titleList != null && descriptionList != null) {
            holder.titleView.setText(titleList[position]);
            holder.descriptionView.setText(descriptionList[position]);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClicked(position);
                }
            });

            int itemPosition = position;
        } else {
            // Covers the case of data not being ready yet.
            holder.titleView.setText("No Options");
        }
    }

    private void itemClicked(int position){
        switch(position){
            case 0:
                clearData();
                break;
            default:
                break;
        }
    }

    private void clearData(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getResources().getString(R.string.clear_game_data));
        // Cancels the word entry if the user clicks this AlertDialog option
        // Replies intent that result of this activity is Canceled
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                DataManager dataManager;
                try {
                    System.out.println();
                    dataManager = new DataManager(context);
                    dataManager.clearStats();
                    Toast.makeText(context, "Data Cleared", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                dialog.dismiss();

            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    @Override
    public int getItemCount() {
        return 1;
    }
}
