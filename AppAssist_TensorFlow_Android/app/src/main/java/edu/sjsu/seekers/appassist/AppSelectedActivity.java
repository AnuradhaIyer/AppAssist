package edu.sjsu.seekers.appassist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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

public class AppSelectedActivity extends AppCompatActivity {

    private static final String TAG = "SelectedAppActivity";

    RecyclerView rv1;

    Model model;
    private DB mDBHelper;

    RVAdapter adapter;
    TextView appName1;
    TextView appDescription1;
    RatingBar ratingbar;
    RVAdapter adapter1;
    ImageView imageView2;
    List<Apps> lstApps = null;
    List<Apps> lstAppsLocal = null;
    Apps selectedApp = null;
    int positionInList = 0;

    private float[][] inputData = null;
    private float[][] outputData = null;

    protected static int COUNT_OF_APPS = 11045;
    public static final int MAX_APPS_DISPLAY = 15;

    float rating = 1.0f;
    boolean ratingClicked = false;
    boolean firstTimeClicked = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Inside AppSelectedActivity ");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_app_selected);

        getDataFromMainActivity();
        getUIComponents();
        setDBAndModel();
        setInputArrayState();
        setOutputArray();
        model.performAction(this, inputData, outputData);
        displayApps();
        setAdapter();
    }

    private void getDataFromMainActivity() {
        Bundle bundle = getIntent().getExtras();
        lstApps = MainActivity.getLstApps();
        positionInList = bundle.getInt("positionInList");
        selectedApp = lstApps.get(positionInList);
    }

    private void setDBAndModel() {
        model =  Model.getInstance();
//        mDBHelper = new DB(this.getApplicationContext());
        mDBHelper = DB.getInstance(this.getApplicationContext());
        //mDBHelper.openDataBase();
    }

    private void getUIComponents() {
        appName1 = findViewById(R.id.txtAppName1);
        appDescription1 =  findViewById(R.id.txtAppDescription1);
        ratingbar = findViewById(R.id.ratingbar);
        rv1 = findViewById(R.id.rv1);
        LinearLayoutManager llm = new LinearLayoutManager(this.getApplicationContext());
        rv1.setLayoutManager(llm);
        ratingbar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {

            float rateValue = Float.parseFloat(String.valueOf(ratingbar.getRating()));
            System.out.println("***********Rating for this app is***********"+rateValue);
            MainActivity.getInputData()[0][selectedApp.appId-1] = rateValue/5;
            MainActivity.mapRecommendations.put(selectedApp.appId,rateValue/5);
            Log.i(TAG,"Rating changed for appId: " + selectedApp.appId + " - " +  MainActivity.getInputData()[0][selectedApp.appId-1]);
        });
        imageView2 = findViewById(R.id.imageView2);

    }

    private void setAdapter() {
        adapter = new RVAdapter(lstAppsLocal,TAG,positionInList);
        rv1.setAdapter(adapter);
        rv1.addOnItemTouchListener(new RecyclerItemClickListener(this, rv1, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

//                if(position >= adapter.posToRemove)
//                    position +=1;
                selectedApp = lstAppsLocal.get(position);
                Log.e(TAG, "clicked: " + lstAppsLocal.get(position));
                positionInList=position;
                firstTimeClicked = false;
                setInputArrayState();
                setOutputArray();
                model.performAction(AppSelectedActivity.this, inputData, outputData);
                displayApps();
                Apps x = searchAppInlist();
                if(x != null) {
                    Log.e(TAG, " found app in inner lst: " + selectedApp);
                    lstAppsLocal.remove(x);
                }else
                {
                    Log.e(TAG, " NOT found app in inner lst: " + selectedApp );
                    Log.e(TAG, " ------: " + lstAppsLocal.get(0) );
                }

                adapter = new RVAdapter(lstAppsLocal,TAG,positionInList);
                rv1.setAdapter(adapter);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                Log.e(TAG, " item long clicked: " + lstAppsLocal.get(position));
            }
        }));
        Apps x = searchAppInlist();
        if(x != null) {
            Log.e(TAG, " found app in outer lst: " + selectedApp);
            lstAppsLocal.remove(x);
        }
        else
        {
            Log.e(TAG, " NOT found app in outer lst: " + selectedApp);
        }
    }



    private void displayApps() {
        List<Integer> lstAppsINT = getTopResults();
        String appIDString = mDBHelper.getAppIdString(lstAppsINT);
        lstAppsLocal = mDBHelper.getAppsFromDB(appIDString);
        appName1.setText(selectedApp.brand);
        appDescription1.setText(selectedApp.desc_arr);
        String imageUri = selectedApp.imUrl.split(" ")[0];
        Picasso.with(getApplicationContext()).load(imageUri).into(imageView2);

    }

    public void setInputArrayState(){

        Log.i(TAG, "setting state for input array");
        inputData = MainActivity.getInputData();


        if(MainActivity.mapRecommendations !=null) {
            if(MainActivity.mapRecommendations.containsKey(selectedApp.appId)){
                ratingbar.setRating(MainActivity.mapRecommendations.get(selectedApp.appId)*5);
            }else{
                ratingbar.setRating(5);
                inputData[0][selectedApp.appId] = 1.0f;
            }


        }

    }


    public Apps searchAppInlist(){

        for(Apps app: lstAppsLocal){
            if(selectedApp.appId == app.appId)
                return app;
        }

        return null;
    }

    public void setArray(HashMap<Integer, Float> mapRecommendations){

        Log.i(TAG, "setting array");
        if(inputData == null)
            inputData = new float[1][COUNT_OF_APPS];

        for(Integer i: mapRecommendations.keySet()){
            inputData[0][i] = mapRecommendations.get(i);
        }
    }

    public void setOutputArray(){
        Log.i(TAG, "setting local output array");
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
                        return res != 0 ? res : 1;
                    }
                }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

    public void performBackAction(View v) {
        Log.e(TAG, "Going back to main activity " + model);
        MainActivity.setRatings(lstApps.get(positionInList).appId,rating);

        Intent intent = new Intent(AppSelectedActivity.this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("callingActivity","AppSelectedActivity");
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
