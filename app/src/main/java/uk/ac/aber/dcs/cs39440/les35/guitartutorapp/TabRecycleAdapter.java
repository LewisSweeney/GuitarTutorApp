package uk.ac.aber.dcs.cs39440.les35.guitartutorapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class TabRecycleAdapter extends RecyclerView.Adapter<TabRecycleAdapter.TabViewHolder> {

    private List<String> tabList;

    class TabViewHolder extends RecyclerView.ViewHolder {
        public TextView tabTextView;
        public TabViewHolder(@NonNull View itemView) {
            super(itemView);
             tabTextView = itemView.findViewById(R.id.tab_text_view);
        }
    }

    /**
     * Constructor for the WordListAdapter
     * @param context
     */
    public TabRecycleAdapter(Context context){LayoutInflater inflater = LayoutInflater.from(context);
    }

    /**
     * Method to create a new WordViewHolder
     * Required for extending RecyclerView.Adapter
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public TabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tab_layout, parent, false);
        return new TabViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TabViewHolder holder, int i) {
        if(tabList != null){
            holder.tabTextView.setText(tabList.get(i));
        }
        else{
            holder.tabTextView.setText("No Tab");
            notifyDataSetChanged();
        }
    }

    public void setTabList(List<String> tabs){
        tabList = tabs;

    }

    @Override
    public int getItemCount() {
        if(tabList!=null){
            System.out.println("TESTING TABLIST SIZE");
            return tabList.size();

        }
        else{
            return 0;
        }
    }

    public String getTab(int position){
        return tabList.get(position);
    }
}
