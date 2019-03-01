package edu.sjsu.seekers.appassist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import edu.sjsu.seekers.appassist.model.Apps;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.AppViewHolder>{

    public Context context;
    public List<Apps> lstApps;
    public int posToRemove = -1;
    public String source = "";

    public RVAdapter(List<Apps> lstApps, String source, int posToRemove){
        this.lstApps = lstApps;
        this.source = source;
        this.posToRemove = posToRemove;

    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view_layout, viewGroup, false);
        context = viewGroup.getContext();
        AppViewHolder pvh = new AppViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(@NonNull  AppViewHolder appViewHolder, int i) {
        appViewHolder.appName.setText(lstApps.get(i).brand);
        String imageUri = lstApps.get(i).imUrl.split(" ")[0];

        Picasso.with(context).load(imageUri).into(appViewHolder.appPhoto);
    }

    @Override
    public int getItemCount() {
        return lstApps.size();
    }



    public static class AppViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView appName;
        ImageView appPhoto;


        AppViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cv);
            appName = itemView.findViewById(R.id.app_name);
            appPhoto = itemView.findViewById(R.id.app_photo);

        }

    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }



}
