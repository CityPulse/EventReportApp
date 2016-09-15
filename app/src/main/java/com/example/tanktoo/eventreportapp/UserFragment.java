package com.example.tanktoo.eventreportapp;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 */
public class UserFragment extends Fragment implements View.OnClickListener{

    View view;
    SharedPreferences sharedPreferences;

    public UserFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_user, container, false);

        //get preferences
        MainActivity activity = ((MainActivity)getActivity());
        sharedPreferences = activity.getSharedPreferences(activity.getPreferences(), Context.MODE_PRIVATE);


        EditText editText = (EditText) view.findViewById(R.id.user_frag_editText_username);
        editText.setText(sharedPreferences.getString("username", "testuser"));

        Button upButton = (Button) view.findViewById(R.id.user_frag_save_button);
        upButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View arg0) {
        EditText editText = (EditText) view.findViewById(R.id.user_frag_editText_username);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", editText.getText().toString());
        editor.commit();

        Snackbar.make(view, "Name changed to " + sharedPreferences.getString("username", "testuser"), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        ((MainActivity) getActivity()).backToMain();
    }

}
