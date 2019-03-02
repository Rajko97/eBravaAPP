package com.vts.samsung.labaccesscontrol.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.vts.samsung.labaccesscontrol.R;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mRanks = new ArrayList<>();
    private ArrayList<String> mImages = new ArrayList<>();
    private Context mContext;

    public RecyclerViewAdapter(Context context,ArrayList<String> userImages, ArrayList<String> usersNames, ArrayList<String> usersRanks) {
        mContext = context;
        mImages = userImages;
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
        try {
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.header_avatar);
            requestOptions.error(R.drawable.header_avatar);
            Glide.with(mContext)
                    .setDefaultRequestOptions(requestOptions)
                    .load(mImages.get(position))
                    .into(holder.imgAvatar);
            } catch (Exception e) {
            e.printStackTrace();
        }
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
        ImageView imgAvatar;

        public ViewHolder(View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgMemberItemHeader);
            tvName = itemView.findViewById(R.id.tvFNameLName);
            tvRank = itemView.findViewById(R.id.tvRank);
            Typeface typeface = Typeface.createFromAsset(itemView.getContext().getAssets(), "exo.ttf");
            tvName.setTypeface(typeface);
            tvRank.setTypeface(typeface);
        }
    }
}