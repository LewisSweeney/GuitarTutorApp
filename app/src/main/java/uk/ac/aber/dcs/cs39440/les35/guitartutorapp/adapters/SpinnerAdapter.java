package uk.ac.aber.dcs.cs39440.les35.guitartutorapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import uk.ac.aber.dcs.cs39440.les35.guitartutorapp.R;

public class SpinnerAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private String[] spinnerNames;


    public SpinnerAdapter(Context mContext, String[] spinnerNames) {
        super(mContext, R.layout.spinner_item, spinnerNames);
        this.mContext = mContext;
        this.spinnerNames = spinnerNames;
    }


    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.spinner_item,null);
        TextView tvSpinnerText = row.findViewById(R.id.spinner_text);

        tvSpinnerText.setText(spinnerNames[position]);

        return row;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.spinner_item,null);
        TextView tvSpinnerText = row.findViewById(R.id.spinner_text);
        tvSpinnerText.setText(spinnerNames[position]);

        return row;
    }
}