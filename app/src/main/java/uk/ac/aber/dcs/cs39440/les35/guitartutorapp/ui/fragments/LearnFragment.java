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

import java.util.ArrayList;
import java.util.List;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.LearnListAdapter;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.R;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.objects.LearnItem;

/**
 *
 */
public class LearnFragment extends Fragment {

    LearnListAdapter adapter;
    RecyclerView listLearn;

    public LearnFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_learn, container, false);

        String[] learnItemNames = getResources().getStringArray(R.array.learning_options_array);
        String[] learnDescriptionNames = getResources().getStringArray(R.array.learning_descriptions_array);
        List<LearnItem> learnListItems = new ArrayList<>(0);
        System.out.println("LEARN ITEM NAME SIZE IS: " + learnItemNames.length);
        // Populate the List for use with the RecyclerView
        for(int i = 0; i < learnDescriptionNames.length; i++){
            LearnItem l = new LearnItem(learnItemNames[i], learnDescriptionNames[i]);
            learnListItems.add(l);
        }

        listLearn = view.getRootView().findViewById(R.id.learn_list);
        listLearn.setMotionEventSplittingEnabled(false);
        listLearn.addItemDecoration(new DividerItemDecoration(this.getActivity(), LinearLayout.VERTICAL));

        adapter = new LearnListAdapter(this.getActivity());
        adapter.setLearnItems(learnListItems);

        listLearn.setAdapter(adapter);
        listLearn.setLayoutManager(new LinearLayoutManager(this.getContext()));

        return view;

    }

    @Override
    public void onResume(){
        super.onResume();
        adapter.isClickable = true;
    }

    /**
     * Required method for extending Fragment
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
