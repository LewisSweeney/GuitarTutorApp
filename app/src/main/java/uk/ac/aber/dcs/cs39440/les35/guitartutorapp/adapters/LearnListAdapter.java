package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.adapters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.R;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.ChordsViewModel;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.model.NotesViewModel;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.LearnItem;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.ui.ChordsActivity;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.ui.MetronomeActivity;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.ui.NotePlaybackActivity;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.ui.NoteRecognitionActivity;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.ui.TabActivity;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.ui.TuningActivity;

/**
 * Adapter to adapt settings data and display it in the settings recyclerview
 */
public class LearnListAdapter extends RecyclerView.Adapter<LearnListAdapter.LearnViewHolder> {

    // Static integers required as permission codes for requesting and checking permissions
    private static final int REQUEST_MICROPHONE = 1;


    private List<LearnItem> learnItemList;
    private Context context;
    private Activity activity;
    public boolean isClickable = true;

    NotesViewModel notesViewModel;
    ChordsViewModel chordsViewModel;


    /**
     * Sub-class LearnViewHolder extends on RecyclerView.VIewHolder
     * Contains the TextView views for displaying the text from each Learn object
     */
    class LearnViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        private final TextView learnOptionView;
        private final TextView learnDescriptionView;

        LearnViewHolder(@NonNull View itemView) {
            super(itemView);
            learnOptionView = itemView.findViewById(R.id.learn_item);
            learnDescriptionView = itemView.findViewById(R.id.learn_description);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        }
    }

    /**
     * Constructor for the LearnListAdapter
     *
     * @param context
     */
    public LearnListAdapter(Context context, Activity activity) {
        LayoutInflater inflater = LayoutInflater.from(context);
        this.activity = activity;
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
    public LearnViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new LearnViewHolder(view);
    }

    /**
     * Binds data to the list_item view
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull LearnViewHolder holder, final int position) {
        if (learnItemList != null) {
            LearnItem current = learnItemList.get(position);
            holder.learnOptionView.setText(current.getLearnItemName());
            holder.learnDescriptionView.setText(current.getLearnItemDescription());
            int itemPosition = position;
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClicked(position);
                }
            });
        } else {
            // Covers the case of data not being ready yet.
            holder.learnOptionView.setText("No Options");
        }
    }

    @Override
    public int getItemCount() {
        return learnItemList.size();
    }

    /**
     * Sets the list for the itemsList and notifies the app that the data set has changed
     *
     * @param items
     */
    public void setLearnItems(List<LearnItem> items) {
        learnItemList = items;
        notifyDataSetChanged();
    }

    /**
     * Takes the item clicked and launches an activty/checks permissions accordingly
     * @param position
     */
    private void itemClicked(int position) {
        Intent intent;
        if (isClickable) {
            Toast toast = Toast.makeText(context, "This feature is under development", Toast.LENGTH_SHORT);
            switch (position) {
                case 0:
                    if(checkPermissions()){
                        intent = new Intent(context, TuningActivity.class);
                        context.startActivity(intent);
                        isClickable = false;
                    } else{
                        showPermissionRequiredAlert();
                    }

                    break;
                case 1:
                    intent = new Intent(context, ChordsActivity.class);
                    context.startActivity(intent);
                    isClickable = false;
                    break;
                case 2:
                    intent = new Intent(context, TabActivity.class);
                    context.startActivity(intent);
                    isClickable = false;
                    break;
                case 3:
                    if(checkPermissions()){
                        intent = new Intent(context, NotePlaybackActivity.class);
                        context.startActivity(intent);
                        isClickable = false;
                    } else{
                        showPermissionRequiredAlert();
                    }
                    break;
                case 4:
                    intent = new Intent(context, NoteRecognitionActivity.class);
                    context.startActivity(intent);
                    isClickable = false;
                    break;
                case 5:
                    intent = new Intent(context, MetronomeActivity.class);
                    context.startActivity(intent);
                    isClickable = false;
                    break;
            }
        }
    }

    /**
     * Checks for the microphone recording permission and only allows the launching of specific features
     * if the permission check passes
     * @return
     */
    private boolean checkPermissions(){
        // Checks if the RECORD_AUDIO permission is granted, and if it is not it prompts the user to
        // allow this permission.
        // CURRENTLY app will not function without the microphone permsission, but there is the intent
        // to add a sound only tuner, allowing the user to match to a sound played on the device.
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE);
            }
        } else {
            return true;
        }
        return false;
    }

    /**
     * Alerts the user that they should allow the microphone permission from their settings so
     * they can use features locked by permissions
     */
    private void showPermissionRequiredAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.permission_alert_title));
        builder.setMessage(context.getString(R.string.permission_alert_message));
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

}
