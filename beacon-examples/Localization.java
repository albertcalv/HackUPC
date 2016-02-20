package com.blueloc.blueloc;

/**
 * This activity will show the map, the beacons placed at their position and the user or users
 * also placed at their positions.
 * This activity should be called passing an extra parameter with the map identifier.
 *
 * @author Albert Cerezo
 * @author Santiago Rogrigo
 */

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

public class Localization extends AppCompatActivity implements BeaconConsumer, RangeNotifier {

    private BeaconManager mBeaconManager;
    private String friendlyName;
    private double distance;
    private boolean searchingEnabled = false;


    private RelativeLayout relative;
    int widthScreenMap, heightScreenMap;


    Vector<BlueBeacon> beaconsPosition;
    Vector<Pair<String,ImageView>> users;

    ImageView map;
    ImageView myPos;

    String mapID;
    String user, pass;
    JSONObject map_description = null;
    Boolean json_downloaded = false;
    Boolean map_downloaded = false;

    String response = null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localization);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize graphic variables
        myPos = new ImageView(getApplicationContext());
        RelativeLayout.LayoutParams myPosParams = new RelativeLayout.LayoutParams(20,20);
        relative = (RelativeLayout)findViewById(R.id.container);
        relative.addView(myPos,myPosParams);
        map = (ImageView)findViewById(R.id.imageView4);
        user = Credentials.getCredentials(getApplicationContext(),"0","0");
        pass = Credentials.getCredentials(getApplicationContext(),"1","0");
        users = new Vector<Pair<String, ImageView>>();




        //Get the map id from the intent
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            mapID = bundle.getString("MAP_ID");
            getJson task = new getJson();
            task.execute(new String[]{user, pass, mapID});
        }
        else finish(); //If there is no map id, the activity should close

        //Register beacons receiver
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        //Start or stop button listener
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0c3e92")));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickButton();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_localization, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.log_out:
                logOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This function is used to remove all the user credentials and it also returns to the log
     * in activity.
     */

    private void logOut() {
        Credentials.clearCredentials(Localization.this);
        Intent intent = new Intent(Localization.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * This function will parse the map description coming from the JSON file,
     * such as size and the position of each beacon.
     * At the end, a request to download the map will be made.
     *
     * @param objectJSON is a JSON object that describes a map and this JSON is described
     *                   in the attached documentation.
     */

    private void initializeJson(JSONObject objectJSON) {
        try {

            JSONObject mapJSON = objectJSON.getJSONObject("map");

            //Map size
            int mapY = mapJSON.getInt("high");
            int mapX = mapJSON.getInt("width");
            BeaconPositioning.setMapCoordinates(mapX, mapY);

            //Position of each beacon
            JSONObject beacons = mapJSON.getJSONObject("beacons");
            JSONArray beaconsArray = beacons.getJSONArray("beacon");
            beaconsPosition = new Vector<BlueBeacon>();
            for(int i = 0; i < beaconsArray.length(); ++i) {
                JSONObject beacon = beaconsArray.getJSONObject(i);
                String id = beacon.getString("id");
                int posBeaconX = beacon.getInt("posx");
                int posBeaconY = beacon.getInt("posy");
                BlueBeacon b = new BlueBeacon(id, posBeaconX, posBeaconY, System.currentTimeMillis());
                beaconsPosition.add(b);
            }

            BeaconPositioning.setBeacons(beaconsPosition);

            //Initialize the size of the image for each smart phone
            widthScreenMap = map.getMeasuredWidth();
            heightScreenMap = widthScreenMap*(int)BeaconPositioning.getMapY()/(int)BeaconPositioning.getMapX();
            heightScreenMap = heightScreenMap - 20; // Fixed value to solve some pixels


            //Download map
            getMap task2 = new getMap();
            task2.execute(new String[]{user, pass, mapID});



        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Each beacon will be rendered on the map.
     */

    private void renderBeacons() {
        for(int i = 0; i < beaconsPosition.size(); i++) {
            ImageView image = new ImageView(getApplicationContext());
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(20,20);
            params.leftMargin = (int)beaconsPosition.get(i).getPosX()*widthScreenMap/(int)BeaconPositioning.getMapX();
            params.topMargin = heightScreenMap-((int)beaconsPosition.get(i).getPosY()*heightScreenMap/(int)BeaconPositioning.getMapY());
            relative.addView(image, params);
            image.setBackgroundColor(Color.rgb(0, 0, 255));
            image.setElevation(2);
        }
    }

    /**
     * Handle the start or stop button.
     */

    private void clickButton() {
        if(!(json_downloaded && map_downloaded)) { //Check if the map description and the map image have been downloaded
            Toast.makeText(getApplicationContext(), "Espere a que se descargue el mapa", Toast.LENGTH_SHORT).show();
        }
        else if (searchingEnabled) { //Localization will stop
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setImageResource(android.R.drawable.ic_media_play);
            searchingEnabled = false;
            bindBeaconManager(false);
            Toast.makeText(getApplicationContext(), "Beacon ranging stopped", Toast.LENGTH_SHORT).show();
        } else {
            if (BluetoothAdapter.getDefaultAdapter().isEnabled()) { //Bluetooth is not enabled
                bindBeaconManager(true);
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                fab.setImageResource(android.R.drawable.ic_media_pause);
                searchingEnabled = true;
                Toast.makeText(getApplicationContext(), "Now ranging beacons...", Toast.LENGTH_SHORT).show();
            } else { //The localization of the user will start
                Toast.makeText(getApplicationContext(), "Bluetooth needs to be enabled", Toast.LENGTH_SHORT).show();
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 1);
            }
        }
    }


    private void bindBeaconManager(boolean on) {
        if (on) mBeaconManager.bind(this);
        else mBeaconManager.unbind(this);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        // Detect the main Eddystone-UID frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"));
        mBeaconManager.setForegroundScanPeriod(300);
        if (searchingEnabled) mBeaconManager.bind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        Region region = new Region("all-beacons-region", null, null, null);
        // We could substitute the null values for a list of the IDs we want to follow
        try {
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mBeaconManager.setRangeNotifier(this);
    }

    /**
     * Handle each new beacon message and if necessary, the position is calculated and
     * the position of others users is also obtained.
     * @param beacons
     * @param region
     */

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        for (Beacon beacon: beacons) {
            if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x00) {
                // This is a Eddystone-UID frame
                // These two values of the beacon are not very interesting... (hexadecimal IDs...)
                // Next one is more readable
                friendlyName = beacon.getBluetoothName();
                distance = beacon.getDistance()*100;
                runOnUiThread(new Runnable () {
                    public void run() {

                        //The beacon information is updated
                        BeaconPositioning.updateDistance(friendlyName, distance, System.currentTimeMillis());

                        //The function that calculates the position is called
                        Pair<Double, Double> result = BeaconPositioning.recalculatePosition();

                        //Once the result is obtained, the image view of the user is repositioned
                        if(result != null) {
                            int newx = (int) (result.first*widthScreenMap/(int)BeaconPositioning.getMapX());
                            int newy = (int) heightScreenMap-(int)(result.second*heightScreenMap/(int)BeaconPositioning.getMapY());
                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) myPos.getLayoutParams();
                            params.leftMargin = newx;
                            params.topMargin = newy;
                            myPos.setLayoutParams(params);
                            myPos.setBackgroundColor(Color.rgb(0, 255, 0));
                            myPos.setElevation(3);
                            int onlinex = result.first.intValue();
                            int onliney = result.second.intValue();

                            //Finally, the result should be updated the remotely data and get the
                            //position from others that are currently updated in the remote data.
                            getOthers task = new getOthers();
                            task.execute(new String[]{user, pass, Integer.toString(onlinex), Integer.toString(onliney), mapID});
                        }

                    }
                });
            }
        }
    }


    /**
     * Private class to get the JSON that describes each map.
     * It has to be executed with these parameters: user, password, mapId.
     */
    private class getJson extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("action", "4" ));
            postParameters.add(new BasicNameValuePair("username", params[0] ));
            postParameters.add(new BasicNameValuePair("password", params[1] ));
            postParameters.add(new BasicNameValuePair("map_id", params[2] ));
            String res = null;
            try {
                response = CustomHttpClient.executeHttpPost("http://blueloc.iotech.es/blueloc/php/Blueloc_WebService.php", postParameters);
                res=response.toString();
                res= res.replaceAll("\\s+", "");
            }
            catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("2") || result.equals("3") || result.equals("4") || result.equals("5")) {
                Toast.makeText(getApplicationContext(), "Error al descargar la descripcion", Toast.LENGTH_SHORT).show();
            }
            else {
                try {
                    map_description = new JSONObject(result);
                    json_downloaded = true;
                    initializeJson(map_description);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Private class to get the map image.
     * It has to be executed with these parameters: user, password, mapId.
     */
    private class getMap extends AsyncTask<String, String, Bitmap> {

        protected Bitmap doInBackground(String... params) {
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("action", "1" ));
            postParameters.add(new BasicNameValuePair("username", params[0] ));
            postParameters.add(new BasicNameValuePair("password", params[1] ));
            postParameters.add(new BasicNameValuePair("map_id", params[2] ));
            Bitmap bmp = null;
            try {
                response = CustomHttpClient.executeHttpPost("http://blueloc.iotech.es/blueloc/php/Blueloc_WebService.php", postParameters);
                String res=response.toString();
                res= res.replaceAll("\\s+", "");
                res= res.replace("\\", "");
                res= res.replaceAll("[^\\x2C-\\x7F]", "");
                URL url = new URL(res);
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return bmp;
        }

        protected void onPostExecute(Bitmap result) {
            map_downloaded = true;
            map.setImageBitmap(result);

            renderBeacons();

        }
    }

    /**
     * Private class to get the position of the others users.
     * It has to be executed with these parameters: user, password, currentPositionX,
     * currentPositionY, mapId.
     */

    private class getOthers extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String url = "http://blueloc.iotech.es/blueloc/php/Blueloc_WebService.php?username=" + params[0] + "&pwd=" + params[1] + "&posx=" + params[2] + "&posy=" + params[3] + "&map_id=" + params[4];
            String res = null;
            try {
                response = CustomHttpClient.executeHttpGet(url);
                res=response.toString();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            JSONArray others = null;
            try {

                //Render of each user
                others = new JSONArray(result);

                for (int i = 0; i < others.length(); i++) {
                    JSONObject user = others.getJSONObject(i);
                    int user_posx = user.getInt("posx");
                    int user_posy = user.getInt("posy");
                    String name_user = user.getString("username");
                    ImageView tmp = null;

                    if (users.size() == 0) { //First user obtained
                        tmp = new ImageView(getApplicationContext());
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(20,20);
                        params.leftMargin = user_posx*widthScreenMap/(int)BeaconPositioning.getMapX();
                        params.topMargin = heightScreenMap-(user_posy*heightScreenMap/(int)BeaconPositioning.getMapY());
                        relative.addView(tmp, params);
                        tmp.setLayoutParams(params);
                        tmp.setBackgroundColor(Color.rgb(255, 0, 0));
                        tmp.setElevation(3);
                        users.add(new Pair<String, ImageView>(name_user, tmp));
                    }
                    else {

                        //Search for the user in the current printed users
                        Boolean found = false;
                        for (Pair<String,ImageView> pairUser : users) {
                            if (pairUser.first.equals(name_user)) {
                                tmp = pairUser.second;
                                found = true;
                            }
                        }
                        if(found) {
                            int newx = (int) (user_posx*widthScreenMap/(int)BeaconPositioning.getMapX());
                            int newy = (int) heightScreenMap-(int)(user_posy*heightScreenMap/(int)BeaconPositioning.getMapY());
                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tmp.getLayoutParams();
                            params.leftMargin = newx;
                            params.topMargin = newy;
                            tmp.setLayoutParams(params);
                            tmp.setBackgroundColor(Color.rgb(255, 0, 0));
                            tmp.setElevation(3);
                        }
                        else {
                            tmp = new ImageView(getApplicationContext());
                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(20,20);
                            params.leftMargin = user_posx*widthScreenMap/(int)BeaconPositioning.getMapX();
                            params.topMargin = heightScreenMap-(user_posy*heightScreenMap/(int)BeaconPositioning.getMapY());
                            relative.addView(tmp, params);
                            tmp.setLayoutParams(params);
                            tmp.setBackgroundColor(Color.rgb(255, 0, 0));
                            tmp.setElevation(3);
                            users.add(new Pair<String, ImageView>(name_user,tmp));
                        }
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }




    @Override
    public void onPause() {
        super.onPause();
        if (searchingEnabled) mBeaconManager.unbind(this);
    }


    @Override
    public void onDestroy() {
        // Unregister broadcast listeners
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

}
