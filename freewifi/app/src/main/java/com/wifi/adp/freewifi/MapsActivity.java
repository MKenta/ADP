package com.wifi.adp.freewifi;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private static Location mMyLocation = null;
    private static boolean mMyLocationCentering = false;
    private Polyline line = null;


    private static final int MENU_A = 0;
    private static final int MENU_B = 1;
    private static final int MENU_c = 2;

    public static String posinfo = "";
    public static String info_A = "";
    public static String info_B = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // TODO Auto-generated method stub
                    //test static route for demo
                    /*
                    LatLng ll = marker.getPosition();
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_VIEW);
                    i.setClassName("com.google.android.apps.maps", "com.google.android.maps.driveabout.app.NavigationActivity");

                    Uri uri = Uri.parse("google.navigation:///?ll=60.16736,24.946413&q=WLAN base station at Esplanadi");
                    i.setData(uri);
                    startActivity(i);
                    */
                    routeSearch(marker);
                return false;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
    //test limited date for demo
        mMap.addMarker(new MarkerOptions().position(new LatLng(60.16736, 24.946413)).title("WLAN base station at Esplanadi"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(60.217216, 24.887)).title("Riistavuori comprehensive service centre Service Centre"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(60.25529, 24.99727)).title("Northern activity centre for kin care"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(60.229687, 24.883745)).title("Western social work  Haaga"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(60.2235, 25.075596)).title("Myllypuro neighbourhood station"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(60.16822, 24.92685)).title("Kamppi service centre"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(60.189045, 24.889673)).title("Munkkiniemi service centre Meilahti recreation centre"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(60.201553, 24.876307)).title("Munkkiniemi service centre"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(60.188896, 24.962563)).title("Kinapori comprehensive service centre Service centre"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(60.212097, 25.07988)).title("Itäkeskus Library"));


        //json read
        /*
        try {
            InputStream input = new FileInputStream("./inout_wlan .json");
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();

            String json = new String(buffer);
            //JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = new JSONArray(json);

            Log.d("mytag", "jsonObject.length()");
        } catch (FileNotFoundException e) {
            Log.d("Mytag", String.valueOf(getFilesDir()));
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("Mytag","IOex");
            e.printStackTrace();
        } catch (JSONException e) {
            Log.d("Mytag","JSON ex");
            e.printStackTrace();
        }
        */
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                mMyLocation = location;
                if (mMyLocation != null && mMyLocationCentering == false) { // Getting device GPS and foucus
                    mMyLocationCentering = true;
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(mMyLocation.getLatitude(), mMyLocation.getLongitude()), 14.0f);
                    mMap.animateCamera(cameraUpdate);
                }
            }
        });
    }

    private void routeSearch(Marker marker){


        LatLng origin = new LatLng(mMyLocation.getLatitude(), mMyLocation.getLongitude());
        LatLng dest = marker.getPosition();


        String url = getDirectionsUrl(origin, dest);

        DownloadTask downloadTask = new DownloadTask();


        downloadTask.execute(url);

    }
    private String getDirectionsUrl(LatLng origin,LatLng dest){


        String str_origin = "origin="+origin.latitude+","+origin.longitude;


        String str_dest = "destination="+dest.latitude+","+dest.longitude;


        String sensor = "sensor=false";

        //パラメータ
        String parameters = str_origin+"&"+str_dest+"&"+sensor + "&language=ja" + "&mode=" + "walking";

        //JSON指定
        String output = "json";


        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        //get in AsyncTask

        @Override
        protected String doInBackground(String... url) {


            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }


        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    /*parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                parseJsonpOfDirectionAPI parser = new parseJsonpOfDirectionAPI();


                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        //ルート検索で得た座標を使って経路表示
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {


            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            if(result.size() != 0){

                for(int i=0;i<result.size();i++){
                    points = new ArrayList<LatLng>();
                    lineOptions = new PolylineOptions();


                    List<HashMap<String, String>> path = result.get(i);


                    for(int j=0;j<path.size();j++){
                        HashMap<String,String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }

                    //polyline
                    lineOptions.addAll(points);
                    lineOptions.width(10);
                    lineOptions.color(0x550000ff);

                }

                //draw and remove previous polyline
                if(line != null){
                    line.remove();
                }
                line = mMap.addPolyline(lineOptions);
            }else{
                mMap.clear();
                Toast.makeText(MapsActivity.this, "ルート情報を取得できませんでした", Toast.LENGTH_LONG).show();
            }


        }
    }
}
