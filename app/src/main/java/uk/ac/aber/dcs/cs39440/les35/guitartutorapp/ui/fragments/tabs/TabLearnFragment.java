package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.ui.fragments.tabs;

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

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.R;
import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.adapters.TabSymbolAdapter;

/**
 * Fragment for displaying symbols for ASCII tablature
 * Displays said symbols along with a description in a recyclerview
 */
public class TabLearnFragment extends Fragment {

    RecyclerView symbolList;
    TabSymbolAdapter adapter;

    public TabLearnFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab_learn, container, false);

        symbolList = view.getRootView().findViewById(R.id.symbol_recycler);
        symbolList.setMotionEventSplittingEnabled(false);
        symbolList.addItemDecoration(new DividerItemDecoration(this.getActivity(), LinearLayout.VERTICAL));

        adapter = new TabSymbolAdapter(this.getActivity());

        symbolList.setAdapter(adapter);
        symbolList.setLayoutManager(new LinearLayoutManager(this.getContext()));

        return view;

    }

    /**
     * Required method for extending Fragment
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

}
