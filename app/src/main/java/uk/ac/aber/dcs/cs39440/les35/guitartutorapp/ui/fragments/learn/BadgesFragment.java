package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.ui.fragments.learn;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.adapters.BadgeAdapter;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.R;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource.DataManager;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Badge;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.StatType;

/**
 * Fragment for displaying the badges/achievements a user can work towards. Displays these badges
 * in a recyclerview, and is used as a tab within the Home/Learn activity
 */
public class BadgesFragment extends Fragment {

    BadgeAdapter adapter;
    RecyclerView listBadge;

    public BadgesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_badges, container, false);

        List<String> badgeStringItems = new ArrayList<>();
        List<Badge> badges = new ArrayList<>();

        try {
            DataManager dataManager = new DataManager(this.getActivity());
            dataManager.readBadges();
            badgeStringItems.addAll(dataManager.getBadges());
            for(String badgeString : badgeStringItems){
                String[] stringSplit = badgeString.split(",");
                StatType type = StatType.RECSCORE;
                if(stringSplit[0].equals("rep")){
                    if(stringSplit[3].equals("g")){
                        type = StatType.REPTOT;

                        System.out.println("CREATED REPTOT BADGE " + stringSplit[1]);
                    }
                    if(stringSplit[3].equals("s")){
                        type = StatType.REPSCORE;
                        System.out.println("CREATED REPSCORE BADGE" + stringSplit[1]);
                    }
                }
                if(stringSplit[0].equals("rec")){
                    if(stringSplit[3].equals("g")){
                        type = StatType.RECTOT;
                        System.out.println("CREATED RECTOT BADGE" + stringSplit[1]);
                    }
                    if(stringSplit[3].equals("s")){
                        type = StatType.RECSCORE;
                        System.out.println("CREATED RECSCORE BADGE" + stringSplit[1]);
                    }
                }
                String stringScore = stringSplit[4];
                int score = Integer.parseInt(stringScore);
                Badge badge = new Badge(String.valueOf(stringSplit[1]), String.valueOf(stringSplit[2]),score, type);
                badges.add(badge);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        listBadge = view.getRootView().findViewById(R.id.learn_list);
        listBadge.setMotionEventSplittingEnabled(false);
        listBadge.addItemDecoration(new DividerItemDecoration(this.getActivity(), LinearLayout.VERTICAL));

        adapter = new BadgeAdapter(this.getActivity());
        adapter.setBadges(badges);

        listBadge.setAdapter(adapter);
        listBadge.setLayoutManager(new LinearLayoutManager(this.getContext()));

        return view;

    }


    /**
     * Required method for extending Fragment
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }
}
