package com.wanhar.routingexample;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.wanhar.routingexample.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    boolean isFromPlacePicker = false;
    MarkerOptions markerOptionFrom = null;
    MarkerOptions markerOptionTo= null;
    PolylineOptions polylineOptions = null;


    int PLACE_PICKER_REQ = 30;


    ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this,R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        binding.fromText.setOnClickListener(this);
        binding.toText.setOnClickListener(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng bandung = new LatLng(-6.903429,107.5030708);
        mMap.addMarker(new MarkerOptions().position(bandung).title("Marker in Bandung"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(bandung));
    }

    private void loadPlaceAutoCompleteIntent() throws GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {

        Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(this);

        startActivityForResult(intent, PLACE_PICKER_REQ);

    }

    private void loadBound(){

        mMap.clear();
        mMap.addMarker(markerOptionFrom);
        mMap.addMarker(markerOptionTo);
        LatLngBounds.Builder latlongBounds = new LatLngBounds.Builder().include(markerOptionFrom.getPosition()).include(markerOptionTo.getPosition());
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latlongBounds.build(), 60));

    }

    private void drawRoute(){
        GoogleDirection.withServerKey(getResources().getString(R.string.google_maps_key))
                .from(markerOptionFrom.getPosition())
                .to(markerOptionTo.getPosition())
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        if (direction.isOK()){
                            Leg leg = direction.getRouteList().get(0).getLegList().get(0);

                            PolylineOptions polylineOption = DirectionConverter.createPolyline(getApplication(), leg.getDirectionPoint(),5, Color.RED);

                            mMap.addPolyline(polylineOption);

                            Log.d( "onDirectionSuccess: ",leg.getDistance().getText() + "-->" + leg.getDistance().getText());

                            binding.distanceText.setVisibility(View.VISIBLE);
                            binding.distanceText.setText(String.format("distance = %s , duration = %s"
                                    , leg.getDistance().getText(), leg.getDistance().getText()));
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {

                        Toast.makeText(MapsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == PLACE_PICKER_REQ){
            if (resultCode == Activity.RESULT_OK){
                Place place = PlaceAutocomplete.getPlace(this,data);
                if (isFromPlacePicker){
                    binding.fromText.setText(place.getName());
                    markerOptionFrom = new MarkerOptions().title(place.getName().toString()).position(place.getLatLng());
                }else {
                    binding.toText.setText(place.getName());
                    markerOptionTo = new MarkerOptions().title(place.getName().toString()).position(place.getLatLng());
                }
                if (markerOptionFrom != null && markerOptionTo != null){
                    loadBound();
                    drawRoute();
                }
            }
            if (resultCode == PlaceAutocomplete.RESULT_ERROR ){
                Status status = PlaceAutocomplete.getStatus(this,data);
                Toast.makeText(this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case  R.id.fromText:
                isFromPlacePicker = true;
                try {
                    loadPlaceAutoCompleteIntent();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                }
                break;
            case  R.id.toText:
                isFromPlacePicker = false;
                try {
                    loadPlaceAutoCompleteIntent();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
