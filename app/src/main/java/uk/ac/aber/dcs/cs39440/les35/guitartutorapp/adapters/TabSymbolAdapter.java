package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.R;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource.DataManager;

public class TabSymbolAdapter extends RecyclerView.Adapter<TabSymbolAdapter.TabSymbolViewHolder> {

    private String[] symbolList;
    private String[] descriptionList;
    Context context;


    /**
     * Sub-class LearnViewHolder extends on RecyclerView.VIewHolder
     * Contains the TextView views for displaying the text from each Learn object
     */
    class TabSymbolViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        private final TextView symbolView;
        private final TextView descriptionView;

        TabSymbolViewHolder(@NonNull View itemView) {
            super(itemView);
            symbolList = itemView.getResources().getStringArray(R.array.tab_symbols);
            descriptionList = itemView.getResources().getStringArray(R.array.tab_symbol_descriptions);
            symbolView = itemView.findViewById(R.id.learn_item);
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
    public TabSymbolAdapter(Context context) {
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
    public TabSymbolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new TabSymbolViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TabSymbolViewHolder holder, final int position) {
        try {
            DataManager dataManager = new DataManager(context);
            dataManager.readStats();

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (symbolList != null && descriptionList != null) {
            holder.symbolView.setText(symbolList[position]);
            holder.descriptionView.setText(descriptionList[position]);

            int itemPosition = position;
        } else {
            // Covers the case of data not being ready yet.
            holder.symbolView.setText("No Options");
        }
    }

    @Override
    public int getItemCount() {
        return 24;
    }
}
