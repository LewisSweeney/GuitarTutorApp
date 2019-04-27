package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.R;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource.DataManager;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Badge;


/**
 * Adapter class for displaying achievements/badges to the user. Takes input from the DataManager
 * class and displays them for each achievement
 */
public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    private List<Badge> badgeItemList;
    Context context;


    /**
     * Sub-class BadgeViewHolder extends on RecyclerView.VIewHolder
     * Contains the TextView views for displaying the text from each Badge Object, as well as
     * a rating bar that functions as an indicator for whether a user has achieved the specified
     * achievement
     */
    class BadgeViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        private final TextView badgeTitleView;
        private final TextView badgeDescriptionView;
        private final RatingBar badgeCompleteView;
        private final TextView badgeProgressView;

        BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            badgeTitleView = itemView.findViewById(R.id.badge_item);
            badgeDescriptionView = itemView.findViewById(R.id.badge_description);
            badgeCompleteView = itemView.findViewById(R.id.badge_graphic);
            badgeProgressView = itemView.findViewById(R.id.progress);
            itemView.setOnCreateContextMenuListener(this);
        }

        // Empty required method
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        }
    }

    /**
     * Constructor for the LearnListAdapter
     *
     * @param context
     */
    public BadgeAdapter(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        this.context = context;
    }


    /**
     * Method to create a new BadgeViewHolder
     * Required for extending RecyclerView.Adapter
     *
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.badge_item, parent, false);
        return new BadgeViewHolder(view);
    }

    /** Binds each badge data to a badge_item layout item
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, final int position) {
        int[][] stats = new int[2][2];
        try {
            DataManager dataManager = new DataManager(context);
            dataManager.readStats();
            stats = dataManager.getStats();

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (badgeItemList != null) {
            Badge current = badgeItemList.get(position);
            holder.badgeTitleView.setText(current.getName());
            holder.badgeDescriptionView.setText(current.getDescription());

            switch(current.getType()){
                case REPSCORE:
                    if(stats[0][0] >= current.getAchievementLimit()){
                        holder.badgeCompleteView.setRating(1);
                    }
                    else{
                        holder.badgeCompleteView.setRating(0);
                    }
                    holder.badgeProgressView.setText(stats[0][0] + "/" + String.valueOf(current.getAchievementLimit()));
                    break;
                case RECSCORE:
                    if(stats[1][0] >= current.getAchievementLimit()){
                        holder.badgeCompleteView.setRating(1);
                    }
                    else{
                        holder.badgeCompleteView.setRating(0);
                    }
                    holder.badgeProgressView.setText(String.valueOf(stats[1][0]) + "/" + String.valueOf(current.getAchievementLimit()));
                    break;
                case REPTOT:
                    if(stats[0][1] >= current.getAchievementLimit()){
                        holder.badgeCompleteView.setRating(1);
                    }
                    else{
                        holder.badgeCompleteView.setRating(0);
                    }
                    holder.badgeProgressView.setText(String.valueOf(stats[0][1]) + "/" + String.valueOf(current.getAchievementLimit()));
                    break;
                case RECTOT:
                    if(stats[1][1] >= current.getAchievementLimit()){
                        holder.badgeCompleteView.setRating(1);
                    }
                    else{
                        holder.badgeCompleteView.setRating(0);
                    }
                    holder.badgeProgressView.setText(String.valueOf(stats[1][1]) + "/" + String.valueOf(current.getAchievementLimit()));
                    break;
            }

            int itemPosition = position;
        } else {
            // Covers the case of data not being ready yet.
            holder.badgeTitleView.setText("No Options");
        }
    }

    /**
     * Gets number of badge items
     * @return
     */
    @Override
    public int getItemCount() {
        return badgeItemList.size();
    }

    /**
     * Sets the list for the itemsList and notifies the app that the data set has changed
     *
     * @param items
     */
    public void setBadges(List<Badge> items) {
        badgeItemList = items;
        notifyDataSetChanged();
    }
}
