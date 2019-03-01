package edu.sjsu.seekers.appassist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.sjsu.seekers.appassist.model.Apps;
import edu.sjsu.seekers.appassist.utils.DB;
import edu.sjsu.seekers.appassist.utils.Model;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AppAssist";

    RecyclerView rv;

    Model model;
    private DB mDBHelper;

    RVAdapter adapter;



    private static List<Apps> lstApps = null;



    private static float[][] inputData = null;
    private static float[][] outputData = null;

    protected static int COUNT_OF_APPS = 11045;
    public static final int MAX_APPS_DISPLAY = 30;

    public static List<Apps> getLstApps() {
        return lstApps;
    }

    public static float[][] getInputData() {
        return inputData;
    }

    public static float[][] getOutputData() {
        return outputData;
    }

    public static  HashMap<Integer, Float> mapRecommendations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rv = findViewById(R.id.rv);
        LinearLayoutManager llm = new LinearLayoutManager(this.getApplicationContext());
        rv.setLayoutManager(llm);


        model =  Model.getInstance();
        mDBHelper = DB.getInstance(this.getApplicationContext());

        mDBHelper.openDataBase();

        String callingActivity = getIntent().getStringExtra("callingActivity");
        Log.i(TAG, "Call to mainActivity from: " + callingActivity );

        if(callingActivity == null) {
            mapRecommendations = mDBHelper.getRecommendationApps();

            if (mapRecommendations != null && mapRecommendations.size() > 0) {
                Log.i(TAG, "Previously recommended apps in DB, getting apps accordingly");
                setArray(mapRecommendations);
                setOutputArray();
                model.performAction(this, inputData, outputData);
            } else {
                Log.i(TAG, "No previously recommended apps in DB, setting ratings to 0 and getting apps");
                setDefaultArray();
                setOutputArray();
                model.performAction(this, inputData, outputData);
            }
        }else {
            Log.i(TAG, "Updating model based on last selection");
            model.performAction(this, inputData, outputData);
        }

        displayApps();
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(),"Saving user ratings in db!!",Toast.LENGTH_LONG).show();
        mDBHelper.saveRecommendations(mapRecommendations);
        Toast.makeText(getApplicationContext(),"Saved user ratings in db!!",Toast.LENGTH_LONG).show();
        finish();
        return;
    }

    private void setAdapter() {
        adapter = new RVAdapter(lstApps, TAG, -1);
        rv.setAdapter(adapter);
        rv.addOnItemTouchListener(new RecyclerItemClickListener(this, rv, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.e(TAG, "clicked: " + lstApps.get(position));
                Intent intent = new Intent(MainActivity.this,AppSelectedActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("positionInList",position);
                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                Log.e(TAG, " item long clicked: " + lstApps.get(position));
            }
        }));
    }

    public void performAction(View v) {
        Log.e(TAG, "obj model: " + model);
        model.performAction(this, inputData, outputData);
        displayApps();
    }

    private void displayApps() {
        List<Integer> lstAppsINT = getTopResults();
        String appIDString = mDBHelper.getAppIdString(lstAppsINT);
        //TODO

        //        lstApps = mDBHelper.retierieveRandomApps();
        lstApps = mDBHelper.getAppsFromDB(appIDString);
        setAdapter();
    }

    public void setDefaultArray(){

        Log.i(TAG, "setting default input array");
        if(inputData == null)
            inputData = new float[1][COUNT_OF_APPS];

    }

    public void setArray(HashMap<Integer, Float> mapRecommendations){

        Log.i(TAG, "setting array");
        if(inputData == null)
            inputData = new float[1][COUNT_OF_APPS];

        for(Integer i: mapRecommendations.keySet()){
            inputData[0][i-1] = mapRecommendations.get(i);
        }
    }

    public void setOutputArray(){
        Log.i(TAG, "setting output array");
        outputData = new float[1][COUNT_OF_APPS];
    }

    public List<Integer> getTopResults(){
        Log.i(TAG, "getting top results");
        if(outputData == null)
            return null;
        else {
            List<Integer> lstAppIds = new ArrayList<>();
            Map<Integer,Float> mpTemp =  new HashMap<>();
            for(int i = 0; i < COUNT_OF_APPS; i++){
                mpTemp.put(i, outputData[0][i]);
            }
            entriesSortedByValues(mpTemp);
            int count = 0;
            for(Integer i : mpTemp.keySet()){
                lstAppIds.add(i);
                count++;
                if (count == MAX_APPS_DISPLAY)
                    break;
            }
            return lstAppIds;
        }

    }

    static <K,V extends Comparable<? super V>>
    SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
                new Comparator<Map.Entry<K,V>>() {
                    @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                        int res = e1.getValue().compareTo(e2.getValue());
                        return res != 1 ? res : 0;
                    }
                }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }


    public static void setRatings(int index, float rating){


        if(inputData != null)
            inputData[0][index - 1] = rating;


    }


}
