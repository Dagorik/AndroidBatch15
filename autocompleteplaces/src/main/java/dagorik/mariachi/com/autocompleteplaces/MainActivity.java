package dagorik.mariachi.com.autocompleteplaces;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private GoogleMap mMap;
    private int REQUESY_CODE = 1;

    private TextView mPlaceDetailsText;
    private TextView mPlaceAttribution;
    Place place;
    boolean skipMethod = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button openButton = (Button) findViewById(R.id.open_butoon);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAutocompleteActivity();
            }
        });
        mPlaceDetailsText = (TextView) findViewById(R.id.place_details);
        mPlaceAttribution = (TextView) findViewById(R.id.place_attribution);
        skipMethod = true;
    }

    private void openAutocompleteActivity() {
        try {
            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                    .setTypeFilter(Place.TYPE_COUNTRY)
                    .setCountry("MX")
                    .build();
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .setFilter(typeFilter)
                    .build(this);
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
        } catch (GooglePlayServicesRepairableException e) {

            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    0).show();

        } catch (GooglePlayServicesNotAvailableException e) {

            String message = "Google Play Services is not available: " +
                    GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

            Log.e("MyLog", message);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_AUTOCOMPLETE){
            if (resultCode == RESULT_OK){

                place = PlaceAutocomplete.getPlace(this,data);
                Log.e("MyLOg","Lugar Seleccionado " + place+"");

                mPlaceDetailsText.setText("Place id: " + place.getId() + "\nAddress: " + place.getAddress() +
                        "\nLatitude Longitud: " + place.getLatLng() + "\nName: " + place.getName());

                CharSequence charSequence = place.getAttributions();
                if(!TextUtils.isEmpty(charSequence)){
                    mPlaceAttribution.setText(charSequence.toString());
                }else{
                    mPlaceAttribution.setText("");
                }

            } else if(resultCode == PlaceAutocomplete.RESULT_ERROR){
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.e("MyLog", "Error: Status = " + status.toString());
            } else if (resultCode == RESULT_CANCELED){
                Log.e("MyLog","Candelado");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!skipMethod){
            onMapReady(mMap);
        }
        skipMethod = false;

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.clear();
        if (place != null){
            Log.e("MyLog","Entre");
            LatLng marker = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
            googleMap.addMarker(new MarkerOptions().position(marker).title(place.getName() + ""));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(marker)
                    .zoom(16).build();

            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        }

        else {
            Log.e("MyLog","ELSE");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUESY_CODE);
            }
        }

    }
}
