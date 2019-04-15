package uk.ac.aber.dcs.cs39440.les35.guitartutorapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.LearnItem;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.ui.ChordsActivity;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.ui.MetronomeActivity;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.ui.NotePlaybackActivity;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.ui.NoteRecognitionActivity;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.ui.TabActivity;

public class LearnListAdapter extends RecyclerView.Adapter<LearnListAdapter.LearnViewHolder> {

    private List<LearnItem> learnItemList;
    private Context context;
    public boolean isClickable = true;


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
    public LearnListAdapter(Context context) {
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
    public LearnViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new LearnViewHolder(view);
    }

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

    private void itemClicked(int position) {
        Intent intent;
        if (isClickable) {
            Toast toast = Toast.makeText(context, "This feature is under development", Toast.LENGTH_SHORT);
            switch (position) {
                case 0:
                    intent = new Intent(context, ChordsActivity.class);
                    context.startActivity(intent);
                    isClickable = false;
                    break;
                case 1:
                    intent = new Intent(context, TabActivity.class);
                    context.startActivity(intent);
                    isClickable = false;
                    break;
                case 2:
                    intent = new Intent(context, NotePlaybackActivity.class);
                    context.startActivity(intent);
                    isClickable = false;
                    break;
                case 3:
                    intent = new Intent(context, NoteRecognitionActivity.class);
                    context.startActivity(intent);
                    isClickable = false;
                    break;
                case 4:
                    intent = new Intent(context, MetronomeActivity.class);
                    context.startActivity(intent);
                    isClickable = false;
                    break;
            }
        }
    }

}
