package com.example.tanktoo.eventreportapp;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


/**
 * A simple {@link Fragment} subclass.
 */
public class ReportFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    View view;
    MapView mapView;
    private GoogleMap googleMap;
    private LatLng eventLocation;
    SharedPreferences sharedPreferences;
    String type = "";
    Marker eventMarker;

    public ReportFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_report, container, false);

        MainActivity activity = ((MainActivity)getActivity());
        sharedPreferences = activity.getSharedPreferences(activity.getPreferences(), Context.MODE_PRIVATE);

        Button upButton = (Button) view.findViewById(R.id.test_button);
        upButton.setOnClickListener(this);

        ImageButton trafficJam = (ImageButton) view.findViewById(R.id.imageButton_trafficjam);
        trafficJam.setOnClickListener(this);
        ImageButton congestion = (ImageButton) view.findViewById(R.id.imageButton_congestion);
        congestion.setOnClickListener(this);
        ImageButton parking = (ImageButton) view.findViewById(R.id.imageButton_parking);
        parking.setOnClickListener(this);

        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar_intensity);
        String[] intensities = getResources().getStringArray(R.array.level_array);
        seekBar.setMax(intensities.length-1);
        seekBar.setOnSeekBarChangeListener(this);
        TextView intensity = (TextView) view.findViewById(R.id.report_intensity);
        intensity.setText(intensities[seekBar.getProgress()]);

        eventLocation = ((MainActivity)getActivity()).getEventLocation();

        mapView = (MapView) view.findViewById(R.id.miniMapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        try{
            MapsInitializer.initialize((getActivity().getApplicationContext()));
        } catch(Exception e){
            e.printStackTrace();
        }

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                googleMap = map;
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 14));
                MarkerOptions markerOptions = new MarkerOptions().position(eventLocation);

                markerOptions.icon( BitmapDescriptorFactory.fromBitmap(
                        BitmapFactory.decodeResource( getResources(),
                                R.mipmap.new_event ) ) );
                eventMarker = googleMap.addMarker(markerOptions);
            }
        });

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
        switch (v.getId()) {
            case R.id.test_button: {
                if(this.type.equals("")){
                    Snackbar.make(view, "Select event type (icon) first", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    break;
                }

                Event event = new Event();
                event.setLatitude(this.eventLocation.latitude);
                event.setLongitude(this.eventLocation.longitude);
                event.setDate(Calendar.getInstance().getTime());
                event.setSource("USER_" + sharedPreferences.getString("username", "testuser"));
                SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar_intensity);
                seekBar.setOnSeekBarChangeListener(this);
                event.setLevel(seekBar.getProgress() + 1);
                event.setEventClass(this.type);
                event.setType("TransportationEvent");

                //UUID must not start with a number
                String uuid = UUID.randomUUID().toString();
                while (Character.isDigit(uuid.charAt(0)))
                    uuid = UUID.randomUUID().toString();

                event.setIdentifier("sao:" + uuid);

                EventParser eventParser = new EventParser();

                System.out.println("Send event: " + eventParser.parseEvent(event));
                this.type = "";

                ((MainActivity) getActivity()).sendNewEventMessage(eventParser.parseEvent(event));
                Snackbar.make(view, "Event sent", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                ((MainActivity) getActivity()).backToMain();
                break;
            }
            case R.id.imageButton_trafficjam: {
                ImageButton traffic = (ImageButton) view.findViewById(R.id.imageButton_trafficjam);
                traffic.setImageResource(R.mipmap.event_trafficjam);
                ImageButton congestion = (ImageButton) view.findViewById(R.id.imageButton_congestion);
                congestion.setImageResource(R.mipmap.event_congestion_deselected);
                ImageButton parking = (ImageButton) view.findViewById(R.id.imageButton_parking);
                parking.setImageResource(R.mipmap.event_parking_deselected);
                this.showIntensity(true);
                this.type = "TrafficJam";
                eventMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.event_trafficjam));
                break;
            }
            case R.id.imageButton_congestion: {
                ImageButton traffic = (ImageButton) view.findViewById(R.id.imageButton_trafficjam);
                traffic.setImageResource(R.mipmap.event_trafficjam_deselected);
                ImageButton congestion = (ImageButton) view.findViewById(R.id.imageButton_congestion);
                congestion.setImageResource(R.mipmap.event_congestion);
                ImageButton parking = (ImageButton) view.findViewById(R.id.imageButton_parking);
                parking.setImageResource(R.mipmap.event_parking_deselected);
                this.showIntensity(true);
                this.type = "Congestion";
                eventMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.event_congestion));
                break;
            }
            case R.id.imageButton_parking: {
                ImageButton traffic = (ImageButton) view.findViewById(R.id.imageButton_trafficjam);
                traffic.setImageResource(R.mipmap.event_trafficjam_deselected);
                ImageButton congestion = (ImageButton) view.findViewById(R.id.imageButton_congestion);
                congestion.setImageResource(R.mipmap.event_congestion_deselected);
                ImageButton parking = (ImageButton) view.findViewById(R.id.imageButton_parking);
                parking.setImageResource(R.mipmap.event_parking);
                this.showIntensity(false);
                this.type = "PublicParking";
                eventMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.event_parking));
                break;
            }
        }
    }

    private void showIntensity(boolean show){
        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar_intensity);
        TextView intensity = (TextView) view.findViewById(R.id.report_intensity);
        TextView intensityheader = (TextView) view.findViewById(R.id.intensity_header);
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
        TextView intensity = (TextView) view.findViewById(R.id.report_intensity);
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
