/*
 * 未来へのキオク API アクセスのサンプルコード for 東北 TECH 道場
 * 
 * This code was inspired from "Android Programming Nyumon 2nd edition".
 * http://www.amazon.co.jp/dp/4048860682/
 */

package org.ktaka.dojo.miraikioku;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import android.location.Location;
import android.app.Dialog;
import android.content.Intent;


public class MainActivity extends ListActivity {

	private List<KiokuItem> kiokuList;
	private KiokuArrayAdapter adapter;
	// キオク検索 API
	private static final String miraiKiokuUrl = "http://www.miraikioku.com/api/search/kioku";
	private ProgressDialog progressDialog;

    // location
    private LocationClient locationClient = null;
    private TextView locationStatus;
    private LocationCallback locationCallback = new LocationCallback();
    private Location lastLocation;
    public static boolean isAppForeground = false;
    private Dialog errorDialog;
    
    private static final String TAG = "MainActivity";
    private static final int LOCATION_UPDATES_INTERVAL = 60000; // Setting 60 sec interval for location updates
    private static final int ERROR_DIALOG_ON_CREATE_REQUEST_CODE = 4055;
    private static final int ERROR_DIALOG_ON_RESUME_REQUEST_CODE = 4056;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkGooglePlayServiceAvailability(ERROR_DIALOG_ON_CREATE_REQUEST_CODE);
        kiokuList = new ArrayList<KiokuItem>();
        adapter = new KiokuArrayAdapter(getApplicationContext(), 0, kiokuList);
        getListView().setAdapter(adapter);
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Getting data from server...");
        progressDialog.setCancelable(true);
//        progressDialog.show();
//        getData();
    }

    private void handleLocation(Location location) {
        // Update the mLocationStatus with the lat/lng of the location
        Log.v(MainActivity.TAG, "LocationChanged == @" +
            location.getLatitude() + "," + location.getLongitude());
        locationStatus.setText("Location changed @" + 
            location.getLatitude() + "," + location.getLongitude());
        lastLocation = location;
    progressDialog.show();
    getData(location);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, KiokuView.class);
        KiokuItem item = (KiokuItem)l.getItemAtPosition(position); // 追加
        intent.putExtra("ImageUrl", item.imageUrl); // 追加
        intent.putExtra("ThumbUrl", item.thumbUrl);
        intent.putExtra("location", item.location); // この行を追加
        startActivity(intent);
    }

//    private void getData() {
    	// API アクセスのための url を文字列として組み立てます。
    	// ここでは type と event-date のパラメータを指定しています。
    	// http://www.miraikioku.com/docs/api/search_kioku を参照して
    	// いろいろなパラメータを設定して試してみて下さい。
//   	String apiUrl = miraiKiokuUrl + "?" + "type=photo" + "&" + "location=38.431645,141.30943"  + "&" + "location-radius=1";
//       new AccessAPItask().execute(apiUrl);
//    }
    
    private void getData(Location location) {
        // API アクセスのための url を文字列として組み立てます。
        // ここでは type と event-date のパラメータを指定しています。
        // http://www.miraikioku.com/docs/api/search_kioku を参照して
        // いろいろなパラメータを設定して試してみて下さい。
        String apiUrl = miraiKiokuUrl + "?" + "type=photo" + "&" + "thumb-size=100c" + "&" +
                "location-radius=40" + "&" + "location=" + String.valueOf(location.getLatitude()) + "," +
                 String.valueOf(location.getLongitude());
        new AccessAPItask().execute(apiUrl);
    }
    
    private class KiokuItem {
    	String title;
    	String thumbUrl;
        String imageUrl; // 追加
        String location; // この行を追加
    }
    
    private class KiokuArrayAdapter extends ArrayAdapter<KiokuItem> {
    	private LayoutInflater inflater;
    	
		public KiokuArrayAdapter(Context context, int textViewResourceId,
				List<KiokuItem> objects) {
			super(context, textViewResourceId, objects);
			inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		private class ViewHolder {
			TextView title;
			ImageView thumbnail;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			if(convertView == null) {
				convertView = inflater.inflate(R.layout.row, null, false);
				holder = new ViewHolder();
				holder.title = (TextView)convertView.findViewById(R.id.title);
				holder.thumbnail = (ImageView)convertView.findViewById(R.id.thumbnail);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			KiokuItem item = getItem(position);
			holder.title.setText(item.title);
			String thumbUrl = item.thumbUrl;
			holder.thumbnail.setTag(thumbUrl);
			Bitmap b = ImageMap.getImage(thumbUrl);
			if(b != null) {
				holder.thumbnail.setImageBitmap(b);
			} else {
				holder.thumbnail.setImageDrawable(null);
				new SetImageTask(thumbUrl, holder.thumbnail).execute((Void)null);
			}
			return convertView;
		}
    }
    
    private class AccessAPItask extends AsyncTask<String, Void, JSONObject> {
    	private DefaultHttpClient httpClient;
    	
    	public AccessAPItask() {
    		httpClient = new DefaultHttpClient();
    	}

		@Override
		protected JSONObject doInBackground(String... args) {
			execAPI(args[0]);
			return null;
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			progressDialog.dismiss();
			adapter.notifyDataSetChanged();
		}
		
		private void execAPI(String url) {
			try {
		    	Log.d("MiraiKiokuAPIsample", "execAPI=" + url);
		    	// 文字列として組み立てた url で http の GET リクエストをサーバーに送ります。
		    	// これが「API を呼び出す」ことになります。
				HttpGet request = new HttpGet(url);
				HttpResponse response = executeRequest(request);
				
				// サーバーからのステータスを取得します。
				int statusCode = response.getStatusLine().getStatusCode();
				StringBuilder buf = new StringBuilder();
				InputStream in = response.getEntity().getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String l = null;
				
				// サーバーからのレスポンスを行単位に読み込みます。
				while((l = reader.readLine()) != null) {
					buf.append(l);
//					Log.d("MiraiKiokuAPISample", l);
				}
				if(statusCode == 200) {
					// ステータスが成功ならレスポンスのパース（解析）を行います。
					parseResponse(buf.toString());
				}
			} catch(IOException e) {
				Log.e("MiraiKiokuAPISample", "IO error", e);
			} catch(JSONException e) {
				Log.e("MiraiKiokuAPISample", "JSON error", e);
			}
		}
		
		private void parseResponse(String buf) throws JSONException {
			// レスポンスは JSON フォーマットとしてパースします。
			JSONObject rootObj = new JSONObject(buf);
			// アイテムの件数を取得
			int count = rootObj.getInt("count");
			Log.d("MiraiKiokuAPISample", String.valueOf(count));
			
			// アイテムを配列として取得
			JSONArray results = rootObj.getJSONArray("results");
			count = results.length();
			kiokuList.clear();
			for(int i = 0; i < count; i++) {
				JSONObject item = results.getJSONObject(i);
				Log.d("MiraiKiokuAPISample", item.getString("title"));
				Log.d("MiraiKiokuAPISample", item.getString("url"));
				Log.d("MiraiKiokuAPISample", item.getString("thumb-url"));
				
				// ListView に表示するためのアイテムとして登録します。
				KiokuItem kioku = new KiokuItem();
				kioku.title = item.getString("title");
				kioku.thumbUrl = item.getString("thumb-url");
	            kioku.imageUrl = item.getString("image-url"); // 追加
                kioku.location = item.getString("location"); // この行を追加
                kiokuList.add(kioku);
			}
		}
		
		private HttpResponse executeRequest(HttpRequestBase base) throws IOException {
			try {
				return httpClient.execute(base);
			} catch(IOException e) {
				base.abort();
				throw e;
			}
		}
    }

    private void init() {
        // Initialize Location Client
        locationStatus = (TextView) findViewById(R.id.locationText);
        if(locationClient == null) {
            locationClient = new LocationClient(this, locationCallback, locationCallback);
            Log.v(MainActivity.TAG, "Location Client connect");
                    if(!(locationClient.isConnected() || locationClient.isConnecting())) {
                locationClient.connect();
            }
        }
    }

    private void checkGooglePlayServiceAvailability(int requestCode) {
        // Query for the status of Google Play services on the device
        int statusCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getBaseContext());

        if (statusCode == ConnectionResult.SUCCESS) {
            init();
        } else {
            if (GooglePlayServicesUtil.isUserRecoverableError(statusCode)) {
                errorDialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                        this, requestCode);
                errorDialog.show();
            } else {
                // Handle unrecoverable error
            }
        }
    }

    private class LocationCallback implements 
    ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
//    ConnectionCallbacks, OnConnectionFailedListener {

        @Override
        public void onConnected(Bundle connectionHint) {
            Log.v(MainActivity.TAG, "Location Client Connected");
            Location currentLocation = locationClient.getLastLocation();
            if(currentLocation != null) {
                handleLocation(currentLocation);
            }
            LocationRequest request = LocationRequest.create();
            request.setInterval(LOCATION_UPDATES_INTERVAL);
            request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            // より高い精度が必要な場合は下記の方を使用します
            // request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationClient.requestLocationUpdates(request, locationCallback);
        }

        @Override
        public void onDisconnected() {
            Log.v(MainActivity.TAG, "Location Client Disconnected");
        }

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.v(MainActivity.TAG, "Location Client connection failed");
        }

        @Override
        public void onLocationChanged(Location location) {
            if (location == null) {
                Log.v(MainActivity.TAG, "onLocationChanged: location == null");
                return;
            }
            if (lastLocation != null &&
                    lastLocation.getLatitude() == location.getLatitude() &&
                    lastLocation.getLongitude() == location.getLongitude()) {
                return;
            }
            handleLocation(location);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
            case ERROR_DIALOG_ON_CREATE_REQUEST_CODE:
                init();
                break;
            case ERROR_DIALOG_ON_RESUME_REQUEST_CODE:
                restartLocationClient();
                break;
            }
        }
    }

    private void restartLocationClient() {
        if (!(locationClient.isConnected() || locationClient.isConnecting())) {
            locationClient.connect(); // Somehow it becomes connected here
            return;
        }
        
        LocationRequest request = LocationRequest.create();
        request.setInterval(LOCATION_UPDATES_INTERVAL);
        request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        // より高い精度が必要な場合は下記の方を使用します
        // request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationClient.requestLocationUpdates(request, locationCallback);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Indicate the application is in background
        isAppForeground = false;
        
        if (locationClient.isConnected()) {
            locationClient.removeLocationUpdates(locationCallback);
            locationClient.disconnect();
        }
    }

    @Override
    public void onResume() {
    	super.onResume();
        isAppForeground = true;
        checkGooglePlayServiceAvailability(ERROR_DIALOG_ON_RESUME_REQUEST_CODE);
        init();
        restartLocationClient();
    }
}
