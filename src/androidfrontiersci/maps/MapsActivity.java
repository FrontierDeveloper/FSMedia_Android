// MapsActivity.java

package androidfrontiersci.maps;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import androidfrontiersci.Download.Downloader;
import androidfrontiersci.Download.ResearchProject;
import androidfrontiersci.MainActivity;
import androidfrontiersci.research.ResearchActivity;

import frontsci.android.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

//API key is in AndroidManifest.xml, it is the same API key for youtube
// and is managed by developer@frontierscientists.com


public class MapsActivity extends Activity implements OnMapReadyCallback{

    private String new_center;
    public static Map<Marker,Integer>markerMap = new HashMap<Marker, Integer>();
    public static Map<Integer,Marker>RPtoMarker = new HashMap<Integer, Marker>();

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_maps);
        MainActivity.fromVideosOrMaps = true;
        init_map();
	}
public void init_map()
    {MapFragment map_frag = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map_frag.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap map) {
            set_center(map);
            set_markers(map);
            map.setOnCameraChangeListener(new OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                Log.e("maps","onCameraChange");
                    if(MainActivity.fromResearch){
                        RPtoMarker.get(MainActivity.index).showInfoWindow();
                        move_to_marker(RPtoMarker.get(MainActivity.index));
                        Log.e("maps", "if statement");
                        MainActivity.fromResearch = false;
                    }
                }
     });
    }

    public void set_center(GoogleMap map) {
        //Center of Alaska for tablet is: 62.89447956,-152.756170369
        CameraPosition init_position = new CameraPosition.Builder()
               .target(new LatLng(62.89447956, -152.756170369))
               .zoom(4)                       // Sets the zoom
               .build();                     // Creates a CameraPosition from the builder
            animate_camera(init_position);
    }

    public void animate_camera(CameraPosition cameraPosition){
        MapFragment map_frag = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        GoogleMap map = map_frag.getMap();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));}


    public void center_latlng(GoogleMap map) {
        new_center = map.getCameraPosition().target.toString();

    }

    public void set_markers(GoogleMap map) {
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        int i = 0;
        for (ResearchProject RP : Downloader.RPMap) {
            Double latitude = RP.mapData.lat;
            Double longitude = RP.mapData.lng;
            i++;
            Marker temp_marker = map.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                            .title(RP.title)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.diamond_blue))
                            .snippet("Tap to go to project description")
            );

            map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    MainActivity.index = markerMap.get(marker);
                    linkToResearch();
                }
            });
            markerMap.put(temp_marker, RP.index);
            RPtoMarker.put(RP.index,temp_marker);
        }
    }
    public void linkToResearch() {
        Intent intent;
        intent = new Intent(this, ResearchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Go to old instance of class if there is
                                                         // one. This disallows endless loops of new
                                                         // activities.
        startActivity(intent);
    }
    public void goto_center(){
        MapFragment map_frag = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        GoogleMap map = map_frag.getMap();
        set_center(map);
    }
    public void move_to_marker(Marker marker){
        LatLng target = marker.getPosition();
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(target)
                .zoom(8)
                .build();
        animate_camera(cameraPosition);

    }
    // When leaving the activity...
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                finish();
                MainActivity.fromVideosOrMaps = false;
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.Reset:
                goto_center();
        return super.onOptionsItemSelected(item);
    }
    return true;}

    //    public void build_info_windows(GoogleMap map, final Map.Entry map_entry, final String marker_title) {
//
//        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
//            @Override
//        public View getInfoWindow(Marker marker_title){
//                return null;
//            }
//            @Override
//        public View getInfoContents(Marker marker_title){
//
//
//                return null;}});
//        ;}

}
