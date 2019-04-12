package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.ui.fragments;

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

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.BadgeAdapter;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.R;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.datasource.CsvReader;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.Badge;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.StatType;

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
            CsvReader csvReader = new CsvReader(this.getActivity());
            csvReader.readBadges();
            badgeStringItems.addAll(csvReader.getBadges());
            for(String badgeString : badgeStringItems){
                String[] stringSplit = badgeString.split(",");
                StatType type = StatType.RECSCORE;
                if(stringSplit[0].equals("rep")){
                    if(stringSplit[3].equals("g")){
                        type = StatType.REPTOT;
                    }
                    if(stringSplit[3].equals("S")){
                        type = StatType.REPSCORE;
                    }
                }
                if(stringSplit[0].equals("reC")){
                    if(stringSplit[3].equals("g")){
                        type = StatType.RECTOT;
                    }
                    if(stringSplit[3].equals("S")){
                        type = StatType.RECSCORE;
                    }
                }
                Badge badge = new Badge(stringSplit[1], stringSplit[2],Integer.parseInt(stringSplit[4]), type);
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
}
