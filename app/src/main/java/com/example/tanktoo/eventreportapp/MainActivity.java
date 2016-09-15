package com.example.tanktoo.eventreportapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener {

    static final LatLng AARHUS = new LatLng(56.15, 10.22);  //using Aarhus as starting location for map
    private GoogleMap map;
    private MapFragment mapFragment;
    private MessageBus messageBus;
    private EventParser eventParser = new EventParser();
    private Map<String, Event> eventMap = new HashMap<String, Event>();
    private FloatingActionButton fab_newevent;
    private FloatingActionButton fab_info;
    private Marker eventMarker;
    private Marker selectedEventMarker = null;
    private Event selectedEvent;
    private Map<Marker, Event> markerEventMap = new HashMap<Marker, Event>();
    private SharedPreferences sharedPreferences;
    private static final String ApplicationPreferences = "ApplicationPreferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = getSharedPreferences(ApplicationPreferences, Context.MODE_PRIVATE);

        fab_info = (FloatingActionButton) findViewById(R.id.fab_info);
        fab_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Select an event first", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                if(selectedEventMarker != null) {
                    selectedEvent = markerEventMap.get(selectedEventMarker);
                    Snackbar.make(view, "Selected: " + selectedEvent, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    selectedEventMarker = null;
                    fab_newevent.hide();
                    fab_info.hide();
                    setFragment(new EventDetailsFragment());
                }
            }
        });


        fab_newevent = (FloatingActionButton) findViewById(R.id.fab);
        fab_newevent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (eventMarker == null)
                    Snackbar.make(view, "Place a marker first to add an event", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                else{
                    Snackbar.make(view, "Adding new event", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    fab_newevent.hide();
                    fab_info.hide();
                    setFragment(new ReportFragment());
                }
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //initialise message bus
        String host = sharedPreferences.getString("mbus_host", "131.227.92.55");
        int port = sharedPreferences.getInt("mbus_port", 8007);
        this.messageBus = new MessageBus(host, port);
        this.messageBus.publishToAMQP();

        //initialise fragments
        this.mapFragment = MapFragment.newInstance();
        this.mapFragment.getMapAsync(this);
        this.setFirstFragment(this.mapFragment);


        final Handler incomingMessageHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String message = msg.getData().getString("msg");
                System.out.println("##### Message received: " + message);
                Event e = eventParser.parseEvent(message);

                //check if it is a level 0 message and we need to delete an existing event (works only for user generated events at the moment
                if(e.getLevel() == 0){
                    String id = e.getIdentifier();
                    if(eventMap.containsKey(id))
                        eventMap.remove(id);
                }else
                    eventMap.put(e.getIdentifier(), e);
                eventsToMap();
            }
        };
        this.messageBus.subscribe(incomingMessageHandler);
    }



    public String getPreferences(){
        return this.ApplicationPreferences;
    }

    @Override
    public void onResume(){
        super.onResume();
        System.out.println("####################on resume#################");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        this.messageBus.destroy();
    }

    Event getSelectedEvent(){
        return this.selectedEvent;
    }

    void sendNewEventMessage(String message){
        this.sendMessage(message);
        this.eventMarker.remove();
        this.eventMarker = null;
        this.fab_newevent.show();
    }

    void sendMessage(String message){
        this.messageBus.publishMessage(message);
    }


    int getNumberOfEvents(EventSourceType eventSourceType){
        int count = 0;
        for(Map.Entry<String, Event> entry : eventMap.entrySet()){
            if (entry.getValue().getEventSourceType().equals(eventSourceType))
                count++;
        }
        return count;
    }

    LatLng getEventLocation() {
        if (this.eventMarker != null)
            return this.eventMarker.getPosition();
        else
            return AARHUS;
    }

    @Override
    public void onMapReady(GoogleMap googleMap){
        System.out.println("#####ready#####");
        this.map = googleMap;
        this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(AARHUS, 10));
        this.map.setOnMapLongClickListener(this);
        this.map.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker){
        marker.showInfoWindow();
        this.selectedEventMarker = marker;
        Event e = this.markerEventMap.get(marker);
        return true;
    }

    @Override
    public void onMapLongClick(LatLng latLng){
        if(this.eventMarker != null)
            this.eventMarker.remove();
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        markerOptions.draggable(true);
        markerOptions.icon( BitmapDescriptorFactory.fromBitmap(
                BitmapFactory.decodeResource( getResources(),
                        R.mipmap.new_event ) ) );
        this.eventMarker = this.map.addMarker(markerOptions);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            int fragmentCount = getFragmentManager().getBackStackEntryCount();
            if (fragmentCount == 0) {
                this.messageBus.destroy();
                this.finish();
            }else
            if(fragmentCount > 0){
                this.backToMain();
            }
        }
    }




    private void eventsToMap(){
        System.out.println("#####eventsToMap");
        this.map.clear();
        this.markerEventMap.clear();

        for(Map.Entry<String, Event> entry : eventMap.entrySet()) {
            Event event = entry.getValue();
//            System.out.println("#### add event at lat " + event.getLatitude() + " lon " + event.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(event.getLatitude(), event.getLongitude()))
                    .title("Event: " + event.getIdentifier())
                    .snippet("This is a " + event.getType() + " event");
            if(event.getEventSourceType().equals(EventSourceType.USER)) {
                if (event.getEventClass().equals("TrafficJam"))
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.event_traffic_user));
                else if (event.getEventClass().equals("PublicParking"))
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.event_parking_user));
                else if (event.getEventClass().equals("Congestion"))
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.event_slow_user));
            }
            else{
                if(event.getEventClass().equals("TrafficJam"))
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.event_trafficjam));
                else if(event.getEventClass().equals("PublicParking"))
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.event_parking));
                else if(event.getEventClass().equals("Congestion"))
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.event_congestion));
            }
            Marker marker = this.map.addMarker(markerOptions);
            this.markerEventMap.put(marker, event);
        }
    }

    private void setFragment(Fragment fragment){
        if(fragment != null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.mainFrame, fragment);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    //first fragment isn't set to stack to avoid blank page when emptying backstack
    private void setFirstFragment(Fragment fragment){
        if(fragment != null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.mainFrame, fragment);
            ft.commit();
        }
    }

    void backToMain(){
        //delete whole back stack, first/main fragment will stay as it is not on stack
        this.getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        this.fab_info.show();
        this.fab_newevent.show();
        if(this.eventMarker != null) {
            this.eventMarker.remove();
            this.eventMarker = null;
        }
        this.selectedEventMarker = null;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        fab_newevent.hide();
        fab_info.hide();

        if (id == R.id.nav_eventmap) {
            this.backToMain();
        } else if (id == R.id.nav_config) {
            fragment = new ConfigFragment();
            this.setFragment(fragment);
        } else if (id == R.id.nav_user) {
            fragment = new UserFragment();
            this.setFragment(fragment);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
