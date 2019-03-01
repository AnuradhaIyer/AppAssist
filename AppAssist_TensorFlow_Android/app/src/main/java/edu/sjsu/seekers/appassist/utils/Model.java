package edu.sjsu.seekers.appassist.utils;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;
import android.util.Log;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;


public class Model {

    private static final String TAG = "AppAssist";
    private static final String MODEL_PATH = "optimized_graph.lite";

    private Interpreter tflite;




    private Model(){}
    private static Model model;
    public static Model getInstance(){
        if(model == null)
            model = new Model();

        return model;
    }





    public void performAction(Activity activity, float[][] inputData, float[][] outputData ) {

        Log.i(TAG, "performing action to run model");
        if(isModelNull()) {
            try {
                tflite = new Interpreter(loadModelFile(activity));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        boolean modelResult = runModel(inputData, outputData);

        if(!modelResult) Log.i(TAG, "Issue running model");
        else Log.i(TAG, "Model ran successfully");

    }

    public void close() {
        tflite.close();
        tflite = null;
    }

    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_PATH);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    boolean runModel(float[][] inputData, float[][] outputData ) {
        //if (isModelNull()) return false;
        long startTime = SystemClock.uptimeMillis();
        tflite.run(inputData, outputData);
        long endTime = SystemClock.uptimeMillis();
        Log.d(TAG, "Time taken to run model: " + Long.toString(endTime - startTime));
        return true;
    }

    private boolean isModelNull() {
        if (tflite == null) {
            Log.e(TAG, "tflite NULL");
            return true;
        }
        return false;
    }





}
