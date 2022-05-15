package org.ruizlab.phoni.kargamobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class GeneListAdapter extends RecyclerView.Adapter<GeneListAdapter.ViewHolder> {

    private ArrayList<String> mData;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // Data is passed into the constructor
    GeneListAdapter(Context context, ArrayList<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // Inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.gene_list_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String[] geneData = mData.get(position).split(",",3);
        String[] geneId = geneData[0].split("\\|",5);
        holder.geneId1.setText(String.format("%s. %s", position, geneId[0].substring(1)));
        holder.geneId2.setText(String.format("%s|%s|", geneId[1], geneId[2]));
        holder.geneId3.setText(String.format("%s|%s|", geneId[3], geneId[4]));
        holder.coveragePercent.setText(geneData[1]);
        float f = Float.parseFloat(geneData[2]);
        DecimalFormat dfZero = new DecimalFormat("0.0000");
        holder.averageDepth.setText(String.valueOf(dfZero.format(f)));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView geneId1, geneId2, geneId3, coveragePercent, averageDepth;

        ViewHolder(View itemView) {
            super(itemView);
            geneId1 = itemView.findViewById(R.id.tvGeneId1);
            geneId2 = itemView.findViewById(R.id.tvGeneId2);
            geneId3 = itemView.findViewById(R.id.tvGeneId3);
            coveragePercent = itemView.findViewById(R.id.tvCoveragePercent);
            averageDepth = itemView.findViewById(R.id.tvAverageDepth);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getBindingAdapterPosition());
        }
    }

    // Convenience method for getting data at click position
    String getItem(int id) {
        return mData.get(id);
    }

    // Allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // Parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    /*  Work in process:
        Add a coverage map to the geneList so we can update what genes to be shown in the list
    public void UpdateList (Float desiredCoverage){
        this.
    }
    */

}
