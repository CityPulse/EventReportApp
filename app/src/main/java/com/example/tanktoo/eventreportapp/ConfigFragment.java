package com.example.tanktoo.eventreportapp;


import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConfigFragment extends Fragment implements View.OnClickListener{

    private final String host = "131.227.92.55";
    private final int port = 8007;
    View view;
    SharedPreferences sharedPreferences;

    public ConfigFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_config, container, false);

        MainActivity activity = ((MainActivity)getActivity());
        sharedPreferences = activity.getSharedPreferences(activity.getPreferences(), Context.MODE_PRIVATE);

        EditText editText_host = (EditText) view.findViewById(R.id.config_frag_mbusHost_lbl_value);
        editText_host.setText(sharedPreferences.getString("mbus_host", host));

        EditText editText_port = (EditText) view.findViewById(R.id.config_frag_mbusPort_lbl_value);
        editText_port.setText(String.valueOf(sharedPreferences.getInt("mbus_port", port)));

        Button upButton = (Button) view.findViewById(R.id.config_frag_mbusSave_button);
        upButton.setOnClickListener(this);

        int nrsensorevents = ((MainActivity)getActivity()).getNumberOfEvents(EventSourceType.SENSOR);
        TextView textView = (TextView) view.findViewById(R.id.eventListSize_lbl_value);
        textView.setText(String.valueOf(nrsensorevents));

        int nruserevents = ((MainActivity)getActivity()).getNumberOfEvents(EventSourceType.USER);
        TextView textView2 = (TextView) view.findViewById(R.id.userEventListSize_lbl_value);
        textView2.setText(String.valueOf(nruserevents));

        return view;
    }

    @Override
    public void onClick(View arg0) {
        EditText editText_host = (EditText) view.findViewById(R.id.config_frag_mbusHost_lbl_value);
        EditText editText_port = (EditText) view.findViewById(R.id.config_frag_mbusPort_lbl_value);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("mbus_host", editText_host.getText().toString());
        editor.putInt("mbus_port", Integer.parseInt(editText_port.getText().toString()));
        editor.commit();

        Snackbar.make(view, "Mbus config change to host " + sharedPreferences.getString("mbus_host", host) + " and port " + sharedPreferences.getInt("mbus_port", port), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        ((MainActivity) getActivity()).backToMain();
    }

}
