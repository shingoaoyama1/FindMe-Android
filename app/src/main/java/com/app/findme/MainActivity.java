package com.app.findme;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.support.v4.widget.DrawerLayout;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private AdView adView;
    private GoogleMap mMap;
    private Marker mMarker;

    private Location mMyLocation;
    private double mLatitude;
    private double mLongitude;

    private TextView mMessageView;
    private CheckBox mDestinationCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMessageView = (TextView) super.findViewById(R.id.message_text);
        mDestinationCheckBox = (CheckBox) super.findViewById(R.id.destinationCheckBox);

        NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment)
                super.getFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        navigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) super.findViewById(R.id.drawer_layout));

        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public void onNavigationDrawerItemSelected(boolean traffic, int mapType) {
        if (mMap != null) {
            mMap.setTrafficEnabled(traffic);
            mMap.setMapType(mapType);
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link com.google.android.gms.maps.SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            MapFragment mapFragment = (MapFragment) super.getFragmentManager().findFragmentById(R.id.map);
            mMap = mapFragment.getMap();
            CameraPosition cameraPosition = mMap.getCameraPosition();
            if (cameraPosition.target.latitude == 0 && cameraPosition.target.longitude == 0) {
                View mapView = mapFragment.getView();
                if (mapView != null) {
                    mapView.setVisibility(View.INVISIBLE);
                }
            }
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        Location location = mMap.getMyLocation();
        if (location != null){
            locationChanged(location);
        }
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                locationChanged(location);
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mapClick();
            }
        });
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mapLongClick(latLng);
            }
        });
    }

    private void setAd(Location location) {
        if (adView == null) {
            adView = (AdView) this.findViewById(R.id.adView);
            // Create an ad request.
            AdRequest.Builder adRequestBuilder = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("AF4D184D6E5C3095CC0468E62D140721");
            if (location != null) {
                adRequestBuilder.setLocation(location);
            }
            // Start loading the ad in the background.
            adView.loadAd(adRequestBuilder.build());
        }
    }

    private void locationChanged(Location location) {
        mMyLocation = location;
        setAd(location);
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        if (mMarker == null || !mMarker.isVisible()) {
            mLatitude = latitude;
            mLongitude = longitude;
            mMessageView.setText(latitude + ", " + longitude);
        }
        if (mMap != null) {
            CameraPosition cameraPosition = mMap.getCameraPosition();
            if (cameraPosition.target.latitude == 0 && cameraPosition.target.longitude == 0) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
                View mapView = getFragmentManager().findFragmentById(R.id.map).getView();
                if (mapView != null) {
                    mapView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void mapClick()
    {
        if (mMarker != null && mMarker.isVisible()) {
            mMarker.setVisible(false);
            locationChanged(mMyLocation);
        }
    }

    private void mapLongClick(LatLng point)
    {
        if (mMarker == null) {
            mMarker = mMap.addMarker(new MarkerOptions().position(point).draggable(true));
        } else {
            mMarker.setVisible(true);
            mMarker.setPosition(point);
        }
        mLatitude = point.latitude;
        mLongitude = point.longitude;
        mMessageView.setText(mLatitude + ", " + mLongitude);
    }

    public void SendMessage(View view) {
        String encodedMessage = generateEncodedMessage();
        Uri data = Uri.parse("sms:?body=" + encodedMessage);
        Intent sendIntent = new Intent(Intent.ACTION_VIEW, data);
        try {
            super.startActivity(sendIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getBaseContext(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void SendEmail(View view) {
        String encodedMessage = generateEncodedMessage();
        Uri data = Uri.parse("mailto:?body=" + encodedMessage);
        Intent sendIntent = new Intent(Intent.ACTION_VIEW, data);
        try {
            super.startActivity(sendIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getBaseContext(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void SendText(View view) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        String messageText = generateMessageText();
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, messageText);
        try {
            super.startActivity(Intent.createChooser(sendIntent, null));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getBaseContext(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String generateEncodedMessage() {
        String messageText = generateMessageText();
        messageText = messageText.replace("+", "%2B");
        return Uri.encode(messageText);
    }

    private String generateMessageText() {
        StringBuilder builder = new StringBuilder(120);
        if (mDestinationCheckBox.isChecked()) {
            builder.append("Find me at: maps.google.com/maps?daddr=").append(mLatitude).append(',').append(mLongitude);
            builder.append(" or comgooglemaps://?daddr=").append(mLatitude).append(',').append(mLongitude);
            builder.append(" for ios");
        } else {
            builder.append("Find me at: maps.google.com/maps?q=").append(mLatitude).append('+').append(mLongitude);
            builder.append(" or comgooglemaps://?q=").append(mLatitude).append('+').append(mLongitude);
            builder.append(" for ios");
        }
        return builder.toString();
    }
}
