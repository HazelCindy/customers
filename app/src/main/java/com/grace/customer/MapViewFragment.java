package com.grace.customer;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import com.bdhobare.mpesa.network.NetworkHandler;
import com.bdhobare.mpesa.utils.Pair;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.libraries.places.api.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grace.customer.utils.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class MapViewFragment extends Fragment implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {

    private static final int LOCATION_REQUEST = 100;
    private static final int REQUEST_USER_TO_ENABLE_LOCATION_REQUEST = 300;

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest mLocationRequest;

    private String string_latitude;
    private String string_longitude;

    private FragmentActivity activity;
    private Context context;

    private EditText pickup;
    private EditText destination;

    int AUTOCOMPLETE_PICKUP_REQUEST_CODE = 400;
    int AUTOCOMPLETE_DEST_REQUEST_CODE = 500;

    Place sourcePlace = null;
    Place destPlace = null;

    MaterialDialog dialog;

    private DatabaseReference root;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private String latitude;
    private String longitude;

    public MapViewFragment() {
        // Required empty public constructor
    }

    public static MapViewFragment newInstance() {
        MapViewFragment fragment = new MapViewFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity =  getActivity();
        context= getActivity();

        // Initialize Places.
        Places.initialize(getActivity().getApplicationContext(), getResources().getString(R.string.google_maps_key));

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_view, container, false);

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    LOCATION_REQUEST);
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        FragmentManager fragmentManager = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment)fragmentManager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        string_latitude = getArguments().getString("latitude");
        string_longitude = getArguments().getString("longitude");


        Toast.makeText(context, "Vehicle Latitude is:"+string_latitude, Toast.LENGTH_SHORT).show();
        Toast.makeText(context, "Vehicle Longitude is:"+string_longitude, Toast.LENGTH_SHORT).show();


        pickup = (EditText) view.findViewById(R.id.pickup);
        destination = (EditText) view.findViewById(R.id.destination);


        pickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Set the fields to specify which types of place data to
                // return after the user has made a selection.
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

                // Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(getActivity());
                startActivityForResult(intent, AUTOCOMPLETE_PICKUP_REQUEST_CODE);
            }
        });

        destination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Set the fields to specify which types of place data to
                // return after the user has made a selection.
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

                // Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(getActivity());
                startActivityForResult(intent, AUTOCOMPLETE_DEST_REQUEST_CODE);
            }
        });


        return view;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            final Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);


            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(5000); //5 seconds
            mLocationRequest.setFastestInterval(3000); //3 seconds
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, (LocationListener) this);

            if (lastLocation != null)
                showCurrentLocationMarker(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));

            if (googleApiClient != null){
                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                        .addLocationRequest(mLocationRequest);

                //**************************
                builder.setAlwaysShow(true); //this is the key ingredient
                //**************************

                PendingResult<LocationSettingsResult> result =
                        LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
                result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                    @Override
                    public void onResult(LocationSettingsResult result) {
                        final Status status = result.getStatus();
                        final LocationSettingsStates state = result.getLocationSettingsStates();
                        switch (status.getStatusCode()) {
                            case LocationSettingsStatusCodes.SUCCESS:
                                if (lastLocation != null)
                                    showCurrentLocationMarker(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
                                break;
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                // Location settings are not satisfied. But could be fixed by showing the user
                                // a dialog.
                                try {
                                    // Show the dialog by calling startResolutionForResult(),
                                    // and check the result in onActivityResult().
                                    status.startResolutionForResult(
                                            activity, REQUEST_USER_TO_ENABLE_LOCATION_REQUEST);
                                } catch (IntentSender.SendIntentException e) {
                                    // Ignore the error.
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                // Location settings are not satisfied. However, we have no way to fix the
                                // settings so we won't show the dialog.
                                break;
                        }
                    }
                });
            }
            if (lastLocation != null)
                showCurrentLocationMarker(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        zoomToLocation(new LatLng(location.getLatitude(),location.getLongitude()));
        showCurrentLocationMarker(new LatLng(location.getLatitude(),location.getLongitude()));

    }

    private void zoomToLocation(LatLng location){
        if (mMap != null && location!=null){
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(location).zoom(14).build();

            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        final Interpolator interpolator = new BounceInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed / duration), 0);
                marker.setAnchor(0.5f, 1.0f + 2 * t);

                if (t > 0.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setContentDescription("Map");

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            buildGoogleApiClient();

            googleApiClient.connect();
        }
        mMap.setOnMapClickListener(new OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                showCurrentLocationMarker(latLng);
            }
        });

    }
    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    private void showCurrentLocationMarker(LatLng location) {
        if (location != null) {
            refreshMap();
            zoomToLocation(location);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(location);
            mMap.addMarker(markerOptions);
            mMap.setOnMarkerClickListener(this);

            string_latitude = getArguments().getString("latitude");
            string_longitude = getArguments().getString("longitude");

            double parsed_latitude = Double.parseDouble(string_latitude);
            double parsed_longitude = Double.parseDouble(string_longitude);

            mMap.addMarker(new MarkerOptions().position(new LatLng(parsed_latitude, parsed_longitude))
                    .title("Vehicle Location").icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(parsed_latitude, parsed_longitude), 8));
        }

    }
    private void  refreshMap(){
        mMap.clear();
    }
    @Override
    public void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }
    @Override
    public void onStop() {
        if (googleApiClient != null)
            googleApiClient.disconnect();
        super.onStop();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }

                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_PICKUP_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                pickup.setText(place.getName());
                sourcePlace = place;
                Log.i("MapViewFragment", "Place: " + place.getName() + ", " + place.getId());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("MapViewFragment", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }else if(requestCode == AUTOCOMPLETE_DEST_REQUEST_CODE){
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                destination.setText(place.getName());
                destPlace = place;
                if (sourcePlace != null)
                    estimateDistance(sourcePlace, destPlace);
                Log.i("MapViewFragment", "Place: " + place.getName() + ", " + place.getId());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("MapViewFragment", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
    private void estimateDistance(Place first, Place last){
        String src = "origins="+first.getLatLng().latitude + "," + first.getLatLng().longitude;
        String dest = "destinations="+last.getLatLng().latitude + "," + last.getLatLng().longitude;
        String api_key = "AIzaSyDYm-VSWmddV6RA1j1AXUksiq4kQBWYRsY";
        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?"+src+"&"+dest+"&key="+api_key;
        new EstimateDistance().execute(url);
        Log.d("URL", url);
    }


    @Override
    public void onInfoWindowClick(Marker marker) {

    }
    private class EstimateDistance extends AsyncTask<String, Void, Pair<Integer, String>>{

        protected void onPreExecute(){
            dialog = Utils.configureProgessDialog(getActivity(), "Estimating price", "Please wait as we estimate the price.");
            dialog.show();
        }
        @Override
        protected Pair<Integer, String> doInBackground(String... strings) {
            return NetworkHandler.doGet(strings[0], new HashMap<String, String>());
        }
        @Override
        protected void onPostExecute(Pair<Integer, String> result){
            dialog.dismiss();
            if(result != null && result.message != null){
                JsonParser jsonParser = new JsonParser();JsonObject jo = (JsonObject) jsonParser.parse(result.message).getAsJsonObject();
                if (result.code/100 != 2){
                    dialog = Utils.configureDialog(getActivity(), "Error", "Error occurred estimating distance", "OK",null);
                    dialog.show();
                    return;
                }else{
                   String status = jo.get("status").getAsString();
                   if (status.equals("OK")){
                       JsonArray rows = jo.get("rows").getAsJsonArray();
                       if (rows.size() > 0){
                           JsonObject first = rows.get(0).getAsJsonObject();
                           JsonArray elements = first.get("elements").getAsJsonArray();
                           JsonObject firstElements = elements.get(0).getAsJsonObject();
                           JsonObject distanceObject = firstElements.get("distance").getAsJsonObject();
                           Double distance = distanceObject.get("value").getAsDouble();
                           double fare = (distance/1000) * 4.17;


                           ((HomeActivity)getActivity()).updateEstimate("Fare Estimate: " + (int)fare);

                       }
                   }
                }
            }else{
                dialog = Utils.configureDialog(getActivity(), "Connection error", "Check internet","OK", null);
                dialog.show();
            }
        }
    }

}
