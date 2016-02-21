package com.hackupc.cardview;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.DefaultHttpClientConnection;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.DefaultClientConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;

public class CardViewActivity extends ActionBarActivity {

    private Card[] datos;
    private ListView lstCards;
    private Activity thisClass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        thisClass = this;

        setContentView(R.layout.activity_card_view);
/*
        =
        new Card[]{
                new CardRoute(10),
                new CardSite("Sagrada Familia", "site1"),
                new CardRoute(40),
                new CardSite("Hard Rock Cafe", "site2")};
*/


        AdapterCard adapter =
                new AdapterCard(this, new Card[]{});
        lstCards = (ListView) findViewById(R.id.LstCards);
        lstCards.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menucardview, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.action_plus:
                updateCardList();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void updateCardList() {
        new CallAPI().execute("");
    }

    private class CallAPI extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                String url = "http://ec2-54-187-231-186.us-west-2.compute.amazonaws.com:8000/beaconserver";
                url = url + "?date_ini=" + URLEncoder.encode("25/02/2016 15:00","UTF-8") + "&"
                          + "date_end="+ URLEncoder.encode("25/02/2016 20:00", "UTF-8") + "&"
                          + "money=50" + "&"
                          + "preferences="+URLEncoder.encode("['gaudi','museum','religion']", "UTF-8");
                Log.d("A", url);
                HttpGet request = new HttpGet(url);
                HttpResponse response = httpClient.execute(request);
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                StringBuilder builder = new StringBuilder();
                String aux;
                aux = rd.readLine();
                while(aux!=null){
                    builder.append(aux);
                    aux = rd.readLine();
                }

                return builder.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return "WAT";
        }

        protected void onPostExecute(String result) {
            try {
                JSONObject obj = new JSONObject(result);
                JSONArray results = obj.getJSONArray("beacons");
                JSONObject beacons_by_id = obj.getJSONObject("beacons_by_id");
                datos = new Card[results.length() -1];
                for (int i = 1; i < results.length(); ++i) {
                    if((i -1)%2 == 1) {
                        JSONObject aux = beacons_by_id.getJSONObject(results.get(i).toString());
                        datos[i - 1] = new CardSite(aux.getString("name").toString(), aux.getString("img"));
                    }
                    else datos[i-1] = new CardRoute(Integer.parseInt(results.get(i).toString()));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            AdapterCard adapter = new AdapterCard(thisClass, datos);
            lstCards = (ListView) findViewById(R.id.LstCards);
            lstCards.setAdapter(adapter);

            lstCards.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                    if (position % 2 == 1) {
                        CardView selectedCard = (CardView) v.findViewById(R.id.cardbeacon);
                        Integer widthCard = 800; //size expanded
                        if (datos[position].changeExpanded()) widthCard = 400;  //size non expanded
                        selectedCard.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, widthCard));
                    } else {
                        CardView selectedCard = (CardView) v.findViewById(R.id.cardroute);
                        Integer widthCard = 800; //size expanded
                        Log.d("A",datos.toString());
                        Log.d("A",Integer.toString(position));
                        if (datos[position].changeExpanded()) widthCard = 400;  //Size non expanded
                        selectedCard.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, widthCard));
                    }
                }
            });

        }
    }


}