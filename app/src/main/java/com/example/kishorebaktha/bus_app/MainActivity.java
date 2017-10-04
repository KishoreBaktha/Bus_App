package com.example.kishorebaktha.bus_app;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Button b,b2;
    private EditText t;
    Geocoder geocoder;
    DatabaseReference databaseReference;
    List<Address> addresses;
    Double Longitude, Latitude;
    RelativeLayout activity_main;
    private LocationManager locationManager;
    private static int SIGN_IN_CODE=1;
    private LocationListener listener;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity_main=(RelativeLayout)findViewById(R.id.Activity_main);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Buses");
        geocoder = new Geocoder(this, Locale.getDefault());
        t=(EditText)findViewById(R.id.busnumber);
        b = (Button) findViewById(R.id.start);
        b2 = (Button) findViewById(R.id.stop);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Longitude=location.getLongitude();
                Latitude=location.getLatitude();
                 String bus=t.getText().toString();
                databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Number").setValue(bus);
                databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Latitude").setValue(Latitude.toString());
                databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Longitude").setValue(Longitude.toString());
                Toast.makeText(getApplicationContext(),"Inserted",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }
            @Override
            public void onProviderEnabled(String s) {
            }
            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };
        check();
    }
    public void check()
    {
        if(FirebaseAuth.getInstance().getCurrentUser()==null)
        {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(),SIGN_IN_CODE);
        }
        else
        {
            Snackbar.make(activity_main,"Welcome"+FirebaseAuth.getInstance().getCurrentUser().getEmail(),Snackbar.LENGTH_SHORT).show();
            //load content
            configure_button();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==SIGN_IN_CODE&&resultCode==RESULT_OK)
        {
            Snackbar.make(activity_main,"Successfully signed in",Snackbar.LENGTH_SHORT).show();
            configure_button();
        }
        else
        {
            Snackbar.make(activity_main,"Couldn't sign in...try again later",Snackbar.LENGTH_SHORT).show();
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    void configure_button(){
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//noinspection MissingPermission
                locationManager.requestLocationUpdates("gps", 1000, 10, listener);
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                Toast.makeText(getApplicationContext(),"Stopped",Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.signout)
        {
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    // Snackbar.make(activity_main,"Successfully signed out",Snackbar.LENGTH_SHORT).show();
                    check();
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }
}

