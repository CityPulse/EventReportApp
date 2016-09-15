package com.example.tanktoo.eventreportapp;


import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventDetailsFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener{

    View view;
    MapView mapView;
    private Event event;
    private GoogleMap googleMap;

    public EventDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_event_details, container, false);

        event = ((MainActivity)getActivity()).getSelectedEvent();
        if(event == null) {
            ((MainActivity) getActivity()).backToMain();
            return view;
        }

        mapView = (MapView) view.findViewById(R.id.details_miniMapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        try{
            MapsInitializer.initialize((getActivity().getApplicationContext()));
        } catch(Exception e){
            e.printStackTrace();
        }

        LatLng eventLocation = new LatLng(event.getLatitude(), event.getLongitude());

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                googleMap = map;
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(event.getLocation(), 14));
                MarkerOptions markerOptions = new MarkerOptions().position(event.getLocation());

                if(event.getEventClass().equals("TrafficJam"))
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.event_trafficjam));
                else if(event.getEventClass().equals("PublicParking"))
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.event_parking));
                else if(event.getEventClass().equals("Congestion"))
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.event_congestion));
                googleMap.addMarker(markerOptions);
            }
        });

        Button deleteButton = (Button) view.findViewById(R.id.details_frag_button_change);
        deleteButton.setOnClickListener(this);
        Button changeButton = (Button) view.findViewById(R.id.details_frag_button_delete);
        changeButton.setOnClickListener(this);

        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar_intensity);
        String[] intensities = getResources().getStringArray(R.array.level_array);
        seekBar.setMax(intensities.length-1);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setProgress(event.getLevel()-1);
        TextView intensity = (TextView) view.findViewById(R.id.details_frag_text_level);
        intensity.setText(intensities[seekBar.getProgress()]);

        //check if its auto generated event, no changes allowed then! (hide buttons)
        if(this.event.getEventSourceType().equals(EventSourceType.SENSOR)){
            deleteButton.setVisibility(View.GONE);
            changeButton.setVisibility(View.GONE);
            seekBar.setVisibility(View.GONE);
        }
        if(this.event.getEventClass().equals("PublicParking"))
            this.showIntensity(false);
        TextView textView_class = (TextView) view.findViewById(R.id.details_frag_textView_class_value);
        textView_class.setText(event.getEventClass());
        TextView textView_time = (TextView) view.findViewById(R.id.details_frag_textView_time_value);
        textView_time.setText(event.getStringDate());

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        if(mapView != null)
            mapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mapView != null)
            mapView.onDestroy();
    }

    @Override
    public void onClick(View v) {
        EventParser eventParser = new EventParser();

        switch (v.getId()) {
            case  R.id.details_frag_button_change: {
                SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar_intensity);
                this.event.setLevel(seekBar.getProgress()+1);

                //send message
                ((MainActivity)getActivity()).sendMessage(eventParser.parseEvent(event));
                Snackbar.make(view, "Event changed", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                ((MainActivity) getActivity()).backToMain();
                break;
            }

            case R.id.details_frag_button_delete: {
                this.event.setLevel(0);     //set level to 0 for deletion of event
                this.event.setDate(Calendar.getInstance().getTime());
                ((MainActivity)getActivity()).sendMessage(eventParser.parseEvent(event));
                Snackbar.make(view, "Event deleted", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                ((MainActivity) getActivity()).backToMain();
                break;
            }

        }
    }

    private void showIntensity(boolean show){
        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar_intensity);
        TextView intensity = (TextView) view.findViewById(R.id.details_frag_text_level);
        TextView intensityheader = (TextView) view.findViewById(R.id.details_frag_intensity_header);
        if(!show) {
            seekBar.setVisibility(View.GONE);
            intensity.setVisibility(View.GONE);
            intensityheader.setVisibility(View.GONE);
        }else {
            seekBar.setVisibility(View.VISIBLE);
            intensity.setVisibility(View.VISIBLE);
            intensityheader.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
        // TODO Auto-generated method stub
        TextView intensity = (TextView) view.findViewById(R.id.details_frag_text_level);
        intensity.setText("" + (progress+1));
        String[] intensities = getResources().getStringArray(R.array.level_array);
        intensity.setText(intensities[progress]);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
    }
}
