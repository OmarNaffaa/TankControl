package com.tankControl.naffaa.tankcontrol;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    @NonNull

    private ArrayList<String> mTitles = new ArrayList<>();
    private ArrayList<String> mData = new ArrayList<>();
    private Context mContext;

    public RecyclerViewAdapter(@NonNull ArrayList<String> mTitles, ArrayList<String> mData, Context mContext) {
        this.mTitles = mTitles;
        this.mData = mData;
        this.mContext = mContext;
    }

    // constructor used for the system select recycler view
    public RecyclerViewAdapter(@NonNull ArrayList<String> mTitles, Context mContext){
        this.mTitles = mTitles;
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;

        if(!mData.isEmpty()){
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_listitem, viewGroup, false);
        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.system_listview, viewGroup, false);
        }

        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        if(!mData.isEmpty()) viewHolder.dataText.setText(mData.get(i));
        viewHolder.titleText.setText(mTitles.get(i));
    }

    @Override
    public int getItemCount() {
        return mTitles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CardView titleCard;
        CardView dataCard;
        TextView titleText;
        TextView dataText;
        ConstraintLayout parent_layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleCard = itemView.findViewById(R.id.card_one);
            dataCard = itemView.findViewById(R.id.card_two);
            titleText = itemView.findViewById(R.id.genericTitle);
            dataText = itemView.findViewById(R.id.genericText);
            parent_layout = itemView.findViewById(R.id.parent_layout);
        }

    }

}