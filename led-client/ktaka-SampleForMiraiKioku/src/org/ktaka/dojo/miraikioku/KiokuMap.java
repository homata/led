package org.ktaka.dojo.miraikioku;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.os.Bundle;
//import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

public class KiokuMap extends FragmentActivity {

    private GoogleMap mMap; // この行を追加
    LatLng location;        // この行を追加

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kioku_map);
        // ここから
        Intent intent = getIntent();
        String locationStr[] = intent.getStringExtra("location").split(",");
        double lat = Double.parseDouble(locationStr[0]);
        double lon = Double.parseDouble(locationStr[1]);
        location = new LatLng(lat, lon);
        setUpMapIfNeeded();
        // ここまでを追加
	}

	// ここから
    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            if (mMap != null) {
                mMap.moveCamera(CameraUpdateFactory.
                           newCameraPosition(CameraPosition.fromLatLngZoom(
                            location,
                                (float) 16.0)));
            addMarker(location);
            }
        }
    }
// ここまでを追加

// ここから
    void addMarker(LatLng location) {
        MarkerOptions options = new MarkerOptions();
        // 緯度・経度
        options.position(location);
        mMap.addMarker(options);
    }
// ここまでを追加
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.kioku_map, menu);
		return true;
	}

}
