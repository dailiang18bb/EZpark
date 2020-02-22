package ameya.com.maptest;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnMarkerDragListener {
    private static final String TAG = "MapsActivity";
    private static final String CHANNEL_ID = "u005";
    private GoogleMap mMap;
    private int ck_p=1,ck_ip=0,ck_g=0;
    private PlaceDetectionClient mPlaceDetectionClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private DatabaseReference mDatabase;
    private ArrayList<Recommendation> mrecommendations=new ArrayList<>();
    ListView listView;
    GarageListAdapter garageListAdapter;
    IGarageListAdapter migarageListAdapter;
    Button addparkbtn;
    ArrayList<Garage> mgarage=new ArrayList<>();
    ArrayList<Garage> mipark=new ArrayList<>();

    Dialog dialog;
    ArrayList<GarageParking> parkinglist=new ArrayList<>();
    ArrayList<IGarageParking> miparkinglist=new ArrayList<>();
    private int notificationId=1111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog=new Dialog(this);
        //createNotificationChannel();
        scheduleAlarm();
        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            CameraPosition mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MapsActivity.this, Login.class));
                    finish();
                }
            }
        };

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        // Construct a GeoDataClient.
        // The entry points to the Places API.
        GeoDataClient mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        final ValueEventListener postListener3 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                miparkinglist.clear();
                Log.d(TAG, "onDataChange: Parking called");
                Log.d(TAG, "onDataChange: Parking got checked?"+dataSnapshot.hasChildren());
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    IGarageParking gparking = postSnapshot.getValue(IGarageParking.class);
                    miparkinglist.add(gparking);
                    Log.d(TAG, "onDataChange:mi "+miparkinglist.isEmpty());
                }
                //populatemarkers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("MapsActivity:iGarage:", "loadPost:onCancelled", databaseError.toException());
            }
        };
        mDatabase.child("iparkings").addValueEventListener(postListener3);
        final ValueEventListener postListener4 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mipark.clear();
                Log.d(TAG, "onDataChange: iGarage called");
                Log.d(TAG, "onDataChange: iGarages got checked?"+dataSnapshot.hasChildren());
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Garage garage = postSnapshot.getValue(Garage.class);
                    mipark.add(garage);
                }
                populatemarkers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("MapsActivity:iGarage:", "loadPost:onCancelled", databaseError.toException());
            }
        };
        mDatabase.child("igarage").addValueEventListener(postListener4);
        final ValueEventListener postListener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                parkinglist.clear();
                Log.d(TAG, "onDataChange: Parking called");
                Log.d(TAG, "onDataChange: Parking got checked?"+dataSnapshot.hasChildren());
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    GarageParking gparking = postSnapshot.getValue(GarageParking.class);
                    parkinglist.add(gparking);
                    Log.d(TAG, "onDataChange: "+parkinglist.isEmpty());
                }
                //populatemarkers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("MapsActivity:Garage:", "loadPost:onCancelled", databaseError.toException());
            }
        };
        mDatabase.child("parkings").addValueEventListener(postListener2);
        final ValueEventListener postListener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mgarage.clear();
                Log.d(TAG, "onDataChange: Garage called");
                Log.d(TAG, "onDataChange: Garages got checked?"+dataSnapshot.hasChildren());
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Garage garage = postSnapshot.getValue(Garage.class);
                    mgarage.add(garage);
                }
                populatemarkers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("MapsActivity:Garage:", "loadPost:onCancelled", databaseError.toException());
            }
        };
        mDatabase.child("garage").addValueEventListener(postListener1);
        final ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mrecommendations.clear();
                Log.d(TAG, "onDataChange: Recommendation called");
                Log.d(TAG, "onDataChange: Recommendations got checked?"+dataSnapshot.hasChildren());
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Recommendation recommendation = postSnapshot.getValue(Recommendation.class);
                    mrecommendations.add(recommendation);
                }
                populatemarkers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("MapsActivity:Recommendation:", "loadPost:onCancelled", databaseError.toException());
            }
        };
        mDatabase.child("recommendation").addValueEventListener(postListener);
      /*  try {
            GeoJsonLayer layer = new GeoJsonLayer(mMap, R.raw.parkinglot, getApplicationContext());
            layer.addLayerToMap();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }*/



    }
    private void bookrecommendation(String s, String key) {
        //String key = mDatabase.child("recommendation").push().getKey();
        //Recommendation post = new Recommendation(key,desc, btime.toString(), etime.toString(), lat, lang, 1);
        Log.d(TAG, "bookrecommendation: "+key);
        mDatabase.child("recommendation").child(key).child("status").setValue(1);
        Map<String,Object> childupdates = new HashMap<>();
        childupdates.put(key,"Booked Recommendation at "+ s);
        mDatabase.child("Transactions").child(Objects.requireNonNull(auth.getUid())).updateChildren(childupdates);
        Map<String,Object> childupdates1 = new HashMap<>();
        childupdates1.put(key,"$12- Recommendation at "+s);
        mDatabase.child("Usage").child(Objects.requireNonNull(auth.getUid())).updateChildren(childupdates1);
    }

    private void writeNewrecommendation(String s, String desc, String btime, String etime, Double lat, Double lang) {
        String key = mDatabase.child("recommendation").push().getKey();
        Recommendation post = new Recommendation(s,key,desc, btime, etime, lat, lang, 0);
        Log.d(TAG, "writeNewrecommendation: "+key);
        Map<String, Object> postValues = post.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/recommendation/"+ key, postValues);
        mDatabase.updateChildren(childUpdates);

    }
    private void writeNewgarage(String toString, double latitude, double longitude) {
        String key = mDatabase.child("garage").push().getKey();
        Garage post = new Garage(0,toString,latitude, longitude,key,auth.getUid());
        Log.d(TAG, "writeNewGarage: "+key);
        Map<String, Object> postValues = post.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/garage/"+ key, postValues);
        mDatabase.updateChildren(childUpdates);
    }
    private void writeNewigarage(String toString, double latitude, double longitude) {
        String key = mDatabase.child("igarage").push().getKey();
        Garage post = new Garage(0,toString,latitude, longitude,key,auth.getUid());
        Log.d(TAG, "writeNewGarage: "+key);
        Map<String, Object> postValues = post.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/igarage/"+ key, postValues);
        mDatabase.updateChildren(childUpdates);
    }
    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Sets up the options menu.
     * @param menu The options menu.
     * @return Boolean.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.current_place_menu, menu);
        return true;
    }


    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }
    /**
     * Handles a click on the menu option to get a place.
     * @param item The menu item to handle.
     * @return Boolean.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.option_get_place) {
            showCurrentPlace();
        }
        else if (item.getItemId() == R.id.choose_parking) {
            chooseparking();
        }
        else if(item.getItemId()==R.id.add_garage){
            addgarage();
        }
        else if(item.getItemId()==R.id.add_igarage){
            addigarage();
        }
        else if (item.getItemId() == R.id.add_marker){
            addrecommendation();
        }
        else if (item.getItemId() == R.id.profile){
            startActivity(new Intent(MapsActivity.this,ProfileActivity.class));
        }
        else if (item.getItemId() == R.id.help){
            startActivity(new Intent(MapsActivity.this,Help_home.class));
        }
        else if(item.getItemId() == R.id.sign_out){
            auth.signOut();
            finish();
        }

        return true;
    }

    private void chooseparking() {
        final Dialog dg =new Dialog(MapsActivity.this);
        dg.setContentView(R.layout.choose_park);
        // Set up the input

        final CheckBox checkBox1=dg.findViewById(R.id.park_ck);
        final CheckBox checkBox2=dg.findViewById(R.id.gara_ck);
        final CheckBox checkBox3=dg.findViewById(R.id.ipark_ck);


        Button pos = dg.findViewById(R.id.sub_park);
        Button ng = dg.findViewById(R.id.negative);
        if (ck_p==1 ){
            checkBox1.setChecked(true);
        }
        if(ck_g==1)
            checkBox2.setChecked(true);
        if(ck_ip==1)
            checkBox3.setChecked(true);

        //final EditText input3 = new EditText(getApplicationContext());
        //input3.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        //input3.setHint("Enter Type Title");

        pos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkBox1.isChecked()&& !checkBox2.isChecked() && !checkBox3.isChecked()){
                    Toast.makeText(getApplicationContext(),"Please Select Type",Toast.LENGTH_SHORT).show();
                }
                else{
                    if(checkBox1.isChecked())
                        ck_p=1;
                    else
                        ck_p=0;
                    if (checkBox2.isChecked())
                        ck_g=1;
                    else
                        ck_g=0;
                    if (checkBox3.isChecked())
                        ck_ip=1;
                    else
                        ck_ip=0;
                    populatemarkers();
                    dg.cancel();
                }

            }
        });
        ng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dg.cancel();
            }
        });
        //AlertDialog alertDialog = builder.create();
        dg.show();

    }

    public void addrecommendation() {
        Log.e(TAG, "addrecommendation: Inside method");
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude()))
                .draggable(true)).setTag("Recommendation");
        Map<String,Object> childupdates = new HashMap<>();
        String key=mDatabase.child("Transactions").push().getKey();
        assert key != null;
        childupdates.put(key,"Added recommendation");
        mDatabase.child("Transactions").child(Objects.requireNonNull(auth.getUid())).updateChildren(childupdates);
        Map<String,Object> childupdates1 = new HashMap<>();
        childupdates1.put(key,"$5+ Recommendation added");
        mDatabase.child("Usage").child(Objects.requireNonNull(auth.getUid())).updateChildren(childupdates1);
    }
    public void addgarage() {
        Log.e(TAG, "addgarage: Inside method");
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude()))
                .draggable(true)).setTag("Garage");
        Map<String,Object> childupdates = new HashMap<>();
        String key=mDatabase.child("Transactions").push().getKey();
        assert key != null;
        childupdates.put(key,"Garage Added");
        mDatabase.child("Transactions").child(Objects.requireNonNull(auth.getUid())).updateChildren(childupdates);

    }
    public void addigarage() {
        Log.e(TAG, "addigarage: Inside method");
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude()))
                .draggable(true)).setTag("iGarage");
        Map<String,Object> childupdates = new HashMap<>();
        String key=mDatabase.child("Transactions").push().getKey();
        assert key != null;
        childupdates.put(key,"Added Individual Parking");
        mDatabase.child("Transactions").child(Objects.requireNonNull(auth.getUid())).updateChildren(childupdates);
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
    mMap.setOnMarkerDragListener(this);
        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {
                if(marker.getTag()=="Recommendation"){
                new AlertDialog.Builder(MapsActivity.this)
                        .setTitle("Book Parking")
                        .setMessage("Are you sure you want to book this parking?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                for(Recommendation r : mrecommendations)
                                    if(r.lang==marker.getPosition().longitude && r.lat==marker.getPosition().latitude) {
                                        bookrecommendation(r.key,marker.getTitle());
                                    }
                            }
                        })

                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();}
                        if(marker.getTag()=="Garage"){
                            dialog.setContentView(R.layout.garage_layout);
                            final TextView txtview;
                            listView =dialog.findViewById(R.id.garagelist);
                            txtview=dialog.findViewById(R.id.parking_title1);
                            TextView closebtn=dialog.findViewById(R.id.close_button);
                            closebtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });

                            addparkbtn=dialog.findViewById(R.id.add_parking_btn);
                            for(Garage r : mgarage)
                                if(r.lang==marker.getPosition().longitude && r.lat==marker.getPosition().latitude) {
                                    Log.d(TAG, "onInfoWindowClick:"+r.key+r.title);
                                    if(!(r.uid.equals(auth.getUid())))
                                        addparkbtn.setVisibility(View.INVISIBLE);
                                    txtview.setText(r.title);
                                    Log.d(TAG, "onInfoWindowClick: Step before loop"+parkinglist.isEmpty());
                                    for(GarageParking garageParking:parkinglist){
                                    {Log.d(TAG, "onInfoWindowClick: "+garageParking.gkey+" == "+r.key);
                                        if(garageParking.gkey.equals(r.key)){
                                            garageListAdapter=new GarageListAdapter(getBaseContext(),parkinglist,r.uid,r.title);
                                            listView.setAdapter(garageListAdapter);
                                        }
                                    }
                                    }
                                }

                            addparkbtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    for(Garage r : mgarage)
                                        if(r.lang==marker.getPosition().longitude && r.lat==marker.getPosition().latitude) {

                                            addgaragepark(r.key,r.title);
                                        }
                                }
                            });
                            dialog.show();
                        }
                if(marker.getTag()=="iGarage"){
                    dialog.setContentView(R.layout.garage_layout);
                    final TextView txtview;
                    listView =dialog.findViewById(R.id.garagelist);
                    txtview=dialog.findViewById(R.id.parking_title1);
                    TextView closebtn=dialog.findViewById(R.id.close_button);
                    closebtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    addparkbtn=dialog.findViewById(R.id.add_parking_btn);
                    for(Garage r : mipark)
                        if(r.lang==marker.getPosition().longitude && r.lat==marker.getPosition().latitude) {
                            Log.d(TAG, "onInfoWindowClick:"+r.key+r.title);
                            if(!(r.uid.equals(auth.getUid())))
                                addparkbtn.setVisibility(View.INVISIBLE);
                            txtview.setText(r.title);
                            Log.d(TAG, "onInfoWindowClick: Step before loop"+parkinglist.isEmpty());
                            for(IGarageParking garageParking:miparkinglist){
                                {Log.d(TAG, "onInfoWindowClick: "+garageParking.gkey+" == "+r.key);
                                    if(garageParking.gkey.equals(r.key)){
                                        migarageListAdapter=new IGarageListAdapter(getApplicationContext(),miparkinglist,r.uid,r.title);
                                        listView.setAdapter(migarageListAdapter);
                                    }
                                }
                            }
                        }

                    addparkbtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            for(Garage r : mipark)
                                if(r.lang==marker.getPosition().longitude && r.lat==marker.getPosition().latitude) {

                                    addigaragepark(r.key,r.title);
                                }
                        }
                    });
                    dialog.show();
                }

            }
        });
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.map), false);

                TextView title = infoWindow.findViewById(R.id.title);
                title.setText(marker.getTitle());

                TextView snippet = infoWindow.findViewById(R.id.snippet);
                snippet.setText(marker.getSnippet());
               // GeoJsonLayer layer = new GeoJsonLayer(getMap(), R.raw.geoJsonFile, getApplicationContext());
                return infoWindow;
            }
        });

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
        //String path = "android.resource://" + getPackageName() + "/" + R.raw.pkl;
        //loadKml();

    }

    private void addgaragepark(final String gkey, final String s) {
        final Dialog dg =new Dialog(MapsActivity.this);
        dg.setContentView(R.layout.radio_dialog);
        // Set up the input
        final String[] input3 = {""};
        RadioGroup rg = dg.findViewById(R.id.radio_group);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int childCount = group.getChildCount();
                for (int x = 0; x < childCount; x++) {
                    RadioButton btn = (RadioButton) group.getChildAt(x);
                    if (btn.getId() == checkedId) {
                        input3[0] =btn.getText().toString();

                    }
                }
            }
        });
        final EditText input = dg.findViewById(R.id.title_parking);
        final EditText input2 = dg.findViewById(R.id.dimen_parking);
        Button pos = dg.findViewById(R.id.postive);
        Button ng =dg.findViewById(R.id.negative);
        //final EditText input3 = new EditText(getApplicationContext());
        //input3.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        //input3.setHint("Enter Type Title");

        pos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(input2.getText().toString().equals("") ){
                    Toast.makeText(getApplicationContext(),"Please Enter Dimensions",Toast.LENGTH_SHORT).show();

                }
                else if(input3[0].equals("")){
                    Toast.makeText(getApplicationContext(),"Please Select Type",Toast.LENGTH_SHORT).show();
                }
                else{
                String key = mDatabase.child("garage").child(gkey).child("parking").push().getKey();
                GarageParking post = new GarageParking(0,input.getText().toString(),input2.getText().toString(),gkey , input3[0],key,"");
                Log.d(TAG, "writeNewGaragePark: "+gkey);
                Map<String, Object> postValues = post.toMap();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/parkings/"+key, postValues);
                mDatabase.updateChildren(childUpdates);
                if (garageListAdapter==null)
                garageListAdapter=new GarageListAdapter(getApplicationContext(),parkinglist,auth.getUid(),s);
                garageListAdapter.notifyDataSetChanged();
                populatemarkers();
                dg.cancel();
                }

            }
        });
        ng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dg.cancel();
            }
        });
        //AlertDialog alertDialog = builder.create();
        dg.show();
    }
    private void addigaragepark(final String gkey,final String s) {
        final Dialog dg =new Dialog(MapsActivity.this);
        dg.setContentView(R.layout.ipark_layout);
        // Set up the input
        final String[] input3 = {""};
        RadioGroup rg = dg.findViewById(R.id.radio_group);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int childCount = group.getChildCount();
                for (int x = 0; x < childCount; x++) {
                    RadioButton btn = (RadioButton) group.getChildAt(x);
                    if (btn.getId() == checkedId) {
                        input3[0] =btn.getText().toString();

                    }
                }
            }
        });
        final EditText input = dg.findViewById(R.id.title_parking);
        final EditText input4 = dg.findViewById(R.id.time_parking);
        final EditText input2 = dg.findViewById(R.id.dimen_parking);
        Button pos = dg.findViewById(R.id.postive);
        Button ng =dg.findViewById(R.id.negative);
        input4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                final int[] mHour = {c.get(Calendar.HOUR_OF_DAY)};
                final int[] mMinute = {c.get(Calendar.MINUTE)};

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(MapsActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                mHour[0] = hourOfDay;
                                mMinute[0] = minute;

                                input4.setText(hourOfDay + ":" + minute);
                            }
                        }, mHour[0], mMinute[0], false);
                timePickerDialog.show();
            }
        });
        //final EditText input3 = new EditText(getApplicationContext());
        //input3.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        //input3.setHint("Enter Type Title");

        pos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(input4.getText().toString().equals("") ){
                    Toast.makeText(getApplicationContext(),"Please Enter Time available",Toast.LENGTH_SHORT).show();

                }
                if(input2.getText().toString().equals("") ){
                    Toast.makeText(getApplicationContext(),"Please Enter Dimensions",Toast.LENGTH_SHORT).show();

                }
                else if(input3[0].equals("")){
                    Toast.makeText(getApplicationContext(),"Please Select Open or Closed",Toast.LENGTH_SHORT).show();
                }
                else{
                    String key = mDatabase.child("igarage").child(gkey).child("iparking").push().getKey();
                    IGarageParking post = new IGarageParking(0,input.getText().toString(),input2.getText().toString(),input4.getText().toString(),gkey , input3[0],key,"");
                    Log.d(TAG, "writeNewiGaragePark: "+gkey);
                    Map<String, Object> postValues = post.toMap();
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/iparkings/"+key, postValues);
                    mDatabase.updateChildren(childUpdates);
                    if (migarageListAdapter==null){
                        migarageListAdapter=new IGarageListAdapter(getApplicationContext(),miparkinglist,auth.getUid(),s);
                        listView.setAdapter(migarageListAdapter);
                    }
                    migarageListAdapter.notifyDataSetChanged();
                    populatemarkers();
                    dg.cancel();
                }

            }
        });
        ng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dg.cancel();
            }
        });
        //AlertDialog alertDialog = builder.create();
        dg.show();
    }
   /* public void loadKml() {

        try( InputStream inputstream = getApplicationContext().getResources().openRawResource(R.raw.pkl)) {

            // Set kmllayer to map
            // map is a GoogleMap, context is the Activity Context

            KmlLayer layer = new KmlLayer(mMap, inputstream, getApplicationContext());
            Log.d(TAG, "loadKml: done?");
            layer.addLayerToMap();



            // Handle these errors

        } catch (XmlPullParserException | IOException e) {

            e.printStackTrace();

        }

    }*/





    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    private void showCurrentPlace() {
        if (mMap == null) {
            return;
        }

        if (mLocationPermissionGranted) {
            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") final
            Task<PlaceLikelihoodBufferResponse> placeResult =
                    mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener
                    (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

                                // Set the count, handling cases where less than 5 entries are returned.
                                int count;
                                if (likelyPlaces.getCount() < M_MAX_ENTRIES) {
                                    count = likelyPlaces.getCount();
                                } else {
                                    count = M_MAX_ENTRIES;
                                }

                                int i = 0;
                                mLikelyPlaceNames = new String[count];
                                mLikelyPlaceAddresses = new String[count];
                                mLikelyPlaceAttributions = new String[count];
                                mLikelyPlaceLatLngs = new LatLng[count];

                                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                    // Build a list of likely places to show the user.
                                    mLikelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
                                    mLikelyPlaceAddresses[i] = (String) placeLikelihood.getPlace()
                                            .getAddress();
                                    mLikelyPlaceAttributions[i] = (String) placeLikelihood.getPlace()
                                            .getAttributions();
                                    mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                                    i++;
                                    if (i > (count - 1)) {
                                        break;
                                    }
                                }

                                // Release the place likelihood buffer, to avoid memory leaks.
                                likelyPlaces.release();

                                // Show a dialog offering the user the list of likely places, and add a
                                // marker at the selected place.
                                openPlacesDialog();

                            } else {
                                Log.e(TAG, "Exception: %s", task.getException());
                            }
                        }
                    });
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            mMap.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(mDefaultLocation)
                    .snippet(getString(R.string.default_info_snippet)));

            // Prompt the user for permission.
            getLocationPermission();
        }
    }

    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    private void openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The "which" argument contains the position of the selected item.
                LatLng markerLatLng = mLikelyPlaceLatLngs[which];
                String markerSnippet = mLikelyPlaceAddresses[which];
                if (mLikelyPlaceAttributions[which] != null) {
                    markerSnippet = markerSnippet + "\n" + mLikelyPlaceAttributions[which];
                }

                // Add a marker for the selected place, with an info window
                // showing information about that place.
                mMap.addMarker(new MarkerOptions()
                        .title(mLikelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet));

                // Position the map's camera at the location of the marker.
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                        DEFAULT_ZOOM));
            }
        };

        // Display the dialog.
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.pick_place)
                .setItems(mLikelyPlaceNames, listener)
                .show();
    }
  public void populatemarkers(){
      Log.d(TAG, "populatemarkers: inside pop markers"+mrecommendations.size());
      mMap.clear();
      int height = 115;
      int width = 115;
      BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.parking_icon);
      BitmapDrawable bitmapdraw1=(BitmapDrawable)getResources().getDrawable(R.drawable.bookedparkingicon);
      BitmapDrawable bitmapdraw2=(BitmapDrawable)getResources().getDrawable(R.drawable.garage);
      BitmapDrawable bitmapdraw3=(BitmapDrawable)getResources().getDrawable(R.drawable.inp);
      Bitmap b=bitmapdraw.getBitmap();
      Bitmap b1=bitmapdraw1.getBitmap();
      Bitmap b2=bitmapdraw2.getBitmap();
      Bitmap b3=bitmapdraw3.getBitmap();
      Bitmap smallMarker2 = Bitmap.createScaledBitmap(b2, width, height, false);
      Bitmap smallMarker3 = Bitmap.createScaledBitmap(b3, width, height, false);
      Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
      Bitmap smallMarker1 = Bitmap.createScaledBitmap(b1, width, height, false);
if(ck_g==1)
for(Garage g:mgarage){
    mMap.addMarker(new MarkerOptions().title(g.title).icon(BitmapDescriptorFactory.fromBitmap(smallMarker2)).position(new LatLng(g.lat,g.lang)).snippet("Garage:"+g.title)).setTag("Garage");
}
if(ck_ip==1)
      for(Garage g:mipark){
          mMap.addMarker(new MarkerOptions().title(g.title).icon(BitmapDescriptorFactory.fromBitmap(smallMarker3)).position(new LatLng(g.lat,g.lang)).snippet("IndividualParking:"+g.title)).setTag("iGarage");
      }
if(ck_p==1)
      for (Recommendation r:mrecommendations) {
          if (r.status==1)
              mMap.addMarker(new MarkerOptions().title(r.desc).icon(BitmapDescriptorFactory.fromBitmap(smallMarker1)).position(new LatLng(r.lat,r.lang)).snippet("Begin time is:"+r.btime+" \n and end time is:"+r.etime+"\nDimensions are: "+r.dimen)).setTag("Recommendation");
          else
          mMap.addMarker(new MarkerOptions().title(r.desc).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)).position(new LatLng(r.lat,r.lang)).snippet("Begin time is:"+r.btime+"\n and end time is:"+r.etime+"\nDimensions are: "+r.dimen)).setTag("Recommendation");
      }
  }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }

    @Override
    public void onMarkerDragEnd(final Marker marker) {
        if(marker.getTag()=="Recommendation")
        {
        Log.d(TAG, "onMarkerDragEnd: This is it");
                //LayoutInflater li = LayoutInflater.from(getApplicationContext());
                //View promptsView = li.inflate(R.layout.add_rec_menu, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setTitle("Parking Recommendation");

                LinearLayout layout = new LinearLayout(getApplicationContext());
                layout.setOrientation(LinearLayout.VERTICAL);
                // Set up the input
            final SimpleDateFormat simpleDateFormat= new SimpleDateFormat("HH:mm");
                final EditText input = new EditText(getApplicationContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                input.setHint("Enter Description");
                layout.addView(input);

                final EditText input4 = new EditText(getApplicationContext());
                input4.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                input4.setHint("Enter Dimensions");
                layout.addView(input4);

                final EditText input2 = new EditText(getApplicationContext());
                input2.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME );
                input2.setHint("Enter Begin Time");
                input2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Calendar c = Calendar.getInstance();
                        final int[] mHour = {c.get(Calendar.HOUR_OF_DAY)};
                        final int[] mMinute = {c.get(Calendar.MINUTE)};

                        // Launch Time Picker Dialog
                        TimePickerDialog timePickerDialog = new TimePickerDialog(MapsActivity.this,
                                new TimePickerDialog.OnTimeSetListener() {

                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                        mHour[0] = hourOfDay;
                                        mMinute[0] = minute;

                                        input2.setText(hourOfDay + ":" + minute);
                                    }
                                }, mHour[0], mMinute[0], false);
                        timePickerDialog.show();

                    }
                });
                layout.addView(input2);
                final EditText input3 = new EditText(getApplicationContext());
                input3.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME);
                input3.setHint("Enter End Time");
                input3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                final int[] mHour = {c.get(Calendar.HOUR_OF_DAY)};
                final int[] mMinute = {c.get(Calendar.MINUTE)};

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(MapsActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                mHour[0] = hourOfDay;
                                mMinute[0] = minute;

                                input3.setText(hourOfDay + ":" + minute);
                            }
                        }, mHour[0], mMinute[0], false);
                timePickerDialog.show();

            }
        });

                layout.addView(input3);
                builder.setView(layout);

                builder.setPositiveButton("Add marker", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String bt="";
                        String et="";
                        try {
                            et=String.format("%s:%s",input2.getText().toString().substring(0,2),input2.getText().toString().substring(3));
                            bt=String.format("%s:%s",input2.getText().toString().substring(0,2),input2.getText().toString().substring(3));

                        if(et.matches("\\d{2}:\\d{2}") || bt.matches("\\d{2}:\\d{2}")){
                            if(input.getText().toString().equals("")||input2.getText().toString().equals("")||input3.getText().toString().equals("")||input4.getText().toString().equals(""))
                                Toast.makeText(getApplicationContext(),"All fields are mandatory",Toast.LENGTH_LONG).show();
                                else{
                            writeNewrecommendation(input4.getText().toString(),input.getText().toString(),bt,et,marker.getPosition().latitude,marker.getPosition().longitude);
                            populatemarkers();
                            }
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"Time is not valid",Toast.LENGTH_LONG).show();
                        }

                        Log.d(TAG, "onClick: Parsing Dates:"+bt+" "+et);
                        }catch (Exception e)
                        {
                            Toast.makeText(getApplicationContext(),"Time is not valid",Toast.LENGTH_LONG).show();
                        }

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                //AlertDialog alertDialog = builder.create();

                builder.show();
            }
        else if(marker.getTag()=="iGarage"){
            Log.d(TAG, "onMarkerDragEnd: This is Individual Parking");
            //LayoutInflater li = LayoutInflater.from(getApplicationContext());
            //View promptsView = li.inflate(R.layout.add_rec_menu, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setTitle("Individual Parking");

            LinearLayout layout = new LinearLayout(getApplicationContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            // Set up the input
            final EditText input = new EditText(getApplicationContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            input.setHint("Enter Individual Parking Title");
            layout.addView(input);
            builder.setView(layout);

            builder.setPositiveButton("Add marker", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(input.getText().toString().equals(""))
                        Toast.makeText(getApplicationContext(),"Enter title",Toast.LENGTH_LONG).show();
                    else
                    {
                        writeNewigarage(input.getText().toString(),marker.getPosition().latitude,marker.getPosition().longitude);
                        populatemarkers();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            //AlertDialog alertDialog = builder.create();
            builder.show();
        }
            else if(marker.getTag()=="Garage"){
            Log.d(TAG, "onMarkerDragEnd: This is Garage Parking");
            //LayoutInflater li = LayoutInflater.from(getApplicationContext());
            //View promptsView = li.inflate(R.layout.add_rec_menu, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setTitle("Garage Parking");

            LinearLayout layout = new LinearLayout(getApplicationContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            // Set up the input
            final EditText input = new EditText(getApplicationContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            input.setHint("Enter Garage Title");
            layout.addView(input);
            builder.setView(layout);

            builder.setPositiveButton("Add marker", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(input.getText().toString().equals(""))
                        Toast.makeText(getApplicationContext(),"Enter title",Toast.LENGTH_LONG).show();
                    else
                    {
                        writeNewgarage(input.getText().toString(),marker.getPosition().latitude,marker.getPosition().longitude);
                    populatemarkers();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            //AlertDialog alertDialog = builder.create();
            builder.show();
        }
    }
    private void createNotificationChannel() {
        Intent intent = new Intent(this, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.parking_icon)
                .setContentTitle("My notification")
                .setContentText("Hello World!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            NotificationManagerCompat notificationManager1 = NotificationManagerCompat.from(this);

            // notificationId is a unique int for each notification that you must define
            notificationManager1.notify(notificationId, builder.build());
        }
    }
    public void scheduleAlarm() {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), NotificationReciever.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, NotificationReciever.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every every half hour from this point onwards
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                50, pIntent);
    }
    public void cancelAlarm() {
        Intent intent = new Intent(getApplicationContext(), NotificationReciever.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, NotificationReciever.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }
}