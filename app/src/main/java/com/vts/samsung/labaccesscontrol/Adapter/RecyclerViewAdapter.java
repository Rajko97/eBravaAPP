package com.vts.samsung.labaccesscontrol.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vts.samsung.labaccesscontrol.R;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mRanks = new ArrayList<>();
    private Context mContext;

    public RecyclerViewAdapter(ArrayList<String> usersNames, ArrayList<String> usersRanks, Context context) {
        mNames = usersNames;
        mRanks = usersRanks;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_active_member_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvName.setText(mNames.get(position));
        holder.tvRank.setText(mRanks.get(position));
    }

    @Override
    public int getItemCount() {
        return mNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        TextView tvRank;

        public ViewHolder(View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvFNameLName);
            tvRank = itemView.findViewById(R.id.tvRank);
        }
    }
}