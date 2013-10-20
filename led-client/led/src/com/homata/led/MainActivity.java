package com.homata.led;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.ParseException;
import android.net.Uri;
//import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
//import android.view.Menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


/*
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
*/

public class MainActivity extends FragmentActivity {

    private static final String TAG_MAP_FRAGMENT = "MAP_FRAGMENT";
    //private static final LatLng TOKYO = new LatLng(35.681382, 139.766084);
    private static final LatLng SABAE = new LatLng(35.943187, 136.188701); // Sabae Station

    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        /* for activity_main.xml
         * setContentView(R.layout.activity_main);
         */
        
        // 登録したタグから MapFragment を取得する（デバイス回転などの再生成対策）
        mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_MAP_FRAGMENT);
        if (mMapFragment == null) {
            // MapFragment がなければ作成する
            mMapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, mMapFragment, TAG_MAP_FRAGMENT)
                    .commit();
        }
        
        // Gingerbreadの新機能の一つとして「StrictMode」(since API Level 9 )がある
        // メール通信など、処理時間が不明なものや、 時間経過が必要な処理は、メインスレッドで実行すると、ブロックされるらしい
        // DefaultHttpClient to AndroidHttpClient
        // <http://stackoverflow.com/questions/8706464/defaulthttpclient-to-androidhttpclient>
        //   StrictMode.ThreadPolicy was introduced since API Level 9 and the default thread policy had been 
        //   changed since API Level 11, which in short, does not allow network operation 
        //   (include HttpClient and HttpUrlConnection) get executed on UI thread. 
        //   if you do this, you get NetworkOnMainThreadException.
        //      :
        //   Add the above code into your main activity's onCreate() method.
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
          }

        getData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
 
        if (mMap == null) {
            // MapFragment から GoogleMap を取得する
            mMap = mMapFragment.getMap();
            if (mMap != null) {
                // マップをハイブリッド表示にする
                //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
 
                // 屋内マップ表示を無効にする（標準は true）
                mMap.setIndoorEnabled(false);
 
                // 現在地表示ボタンを有効にする
                mMap.setMyLocationEnabled(true);
                // UiSettings にボタン表示設定があるが標準は true なので設定不要
                // mMap.getUiSettings().setMyLocationButtonEnabled(true);
 
                // 鯖江駅にマーカーをつける
                mMap.addMarker(new MarkerOptions()
                        //.position(TOKYO)
                        .position(SABAE)
                        .title("鯖江駅")
                        .snippet("ほげー")
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
 
                // カメラの位置を東京駅に変える
                this.moveCameraToTokyo(false);
 
                // 地図の長押しでカメラを東京駅まで移動する
                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng point) {
                        moveCameraToTokyo(true);
                    }
                });
            }
        }
    }
 
    /**
     * カメラを東京駅に移動する
     * 
     * @param isAnimation
     *            アニメーション移動するかの判定。true でアニメーション移動。
     */
    private void moveCameraToTokyo(boolean isAnimation) {
        // カメラの位置情報を作成する
        CameraUpdate camera = CameraUpdateFactory
                .newCameraPosition(new CameraPosition.Builder()
                        //.target(TOKYO)
                        .target(SABAE)
                        .zoom(15.0f).build());
        if (isAnimation) {
            // アニメーション移動する
            mMap.animateCamera(camera);
        } else {
            // 瞬間移動する
            mMap.moveCamera(camera);
        }
    }
    
    // ：Tips 　Android で JSON を使おう　～ 前編 ～  | http://d.hatena.ne.jp/androidprogram/20100621/1277075384
    // ：Tips 　Android で JSON を使おう　～ 後編 ～ | http://d.hatena.ne.jp/androidprogram/20100622/1277229166
    // http://json.parser.online.fr/
    private void getData() {
    	// const URLXML = "http://www3.city.sabae.fukui.jp/xml/toilet/toiletinformation.xml";
    	String scheme = "http";
        String authority = "www3.city.sabae.fukui.jp";
        String path = "/xml/toilet/toiletinformation.xml";
            
        Uri.Builder uriBuilder = new Uri.Builder();
            
        uriBuilder.scheme(scheme);
        uriBuilder.authority(authority);
        uriBuilder.path(path);
            
        String uri = uriBuilder.toString();
            
        HttpClient httpClient = new DefaultHttpClient();
        HttpParams params = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, 1000);
        HttpConnectionParams.setSoTimeout(params, 1000);
            
        //HttpUriRequest httpRequest = new HttpGet(uri);
        HttpGet httpRequest = new HttpGet(uri);
        HttpResponse httpResponse = null;
        
        try {
        	httpResponse = httpClient.execute(httpRequest);
        } catch (ClientProtocolException e) {
            Log.d("MainActivity", "Error ClientProtocolException");
        	//例外処理
        } catch (IOException e){
            Log.d("MainActivity", "Error IOException");
        	//例外処理
        } catch (Exception e) {
            Log.d("MainActivity", "Error Exception");
        	e.printStackTrace();
        }
        String json = null;
        
        int status = httpResponse.getStatusLine().getStatusCode();
        /*
        ステータスコード
        SC_OK	200
        SC_MOVED_PERMANENTLY	301
        SC_MOVED_TEMPORARILY	302
        SC_NOT_FOUND	404
        SC_INTERNAL_SERVER_ERROR	500
        SC_SERVICE_UNAVAILABLE	503
        */
        if (httpResponse != null && HttpStatus.SC_OK == status) {
            try {
                HttpEntity httpEntity = httpResponse.getEntity();
                try {
                    json = EntityUtils.toString(httpEntity);
                }
                catch (ParseException e) {
                    //例外処理
                }
                catch (IOException e) {
                    //例外処理
                }
                finally {
                    try {
                        httpEntity.consumeContent();
                    }
                    catch (IOException e) {
                        //例外処理
                    }
                }
            } catch (Exception e) {
                Log.d("MainActivity", "Error");
            }
        } else {
            Log.d("MainActivity", "Status" + status);
        }
        httpClient.getConnectionManager().shutdown();
        
        // 表示用のテキストバッファ
        StringBuffer stringBuffer = new StringBuffer();
        
        //  var data = jsondata.dataroot.toiletinformation;
        try {
            JSONObject rootObject = new JSONObject(json);
            
            JSONObject dataObject = rootObject.getJSONObject("toiletinformation");
            JSONArray  dataArray  = dataObject.getJSONArray("toiletinformation"); 
            int count0 = dataArray.length();
            int count1 = dataObject.length();
            for (int ii=0; ii<count0; ii++) {
            	//bookObject[i] = itemArray.getJSONObject(i);
            	//String localname = dataObject[ii].getString("locaname");
                //Log.d("MainActivity", "localname" + localname);
            }
            
/*
            for (int i=0; i<count; i++){
            	bookObject[i] = itemArray.getJSONObject(i);
            }
            
            for (int i=0; i<bookObject.length; i++){
                  // 書籍の売り上げランキング
            	int rank = i + 1 ;
                  // 書籍名のデータを取得
            	String title = bookObject[i].getString("title");
            	// 「売り上げランキング」＋「書籍名」をテキストに追加
            	stringBuffer.append(rank + "位" + "\n");
            	stringBuffer.append(title + "\n");
            }
*/
        } catch (JSONException e) {
            // 例外処理
            Log.d("MainActivity", "JSONException Exception");
        	e.printStackTrace();
        }
    }
}
