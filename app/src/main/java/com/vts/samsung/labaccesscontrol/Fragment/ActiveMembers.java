package com.vts.samsung.labaccesscontrol.Fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vts.samsung.labaccesscontrol.R;

public class ActiveMembers extends Fragment {


    public ActiveMembers() {
        // Required empty public constructor
    }
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_active_members, container, false);

        TextView textView = (TextView) v.findViewById(R.id.tvActiveMembersTest);
        sharedPreferences = getActivity().getSharedPreferences("аppSettings", Context.MODE_PRIVATE);
        String checkInStatus = sharedPreferences.getString("checkInStatus", "outsideLab");
        if(checkInStatus.equals("inLab")) {
            textView.setText("Trenutno se nalazite u laboratoriji");
            textView.setTextColor(Color.parseColor("#00FF00"));
        } else {
            textView.setText("Trenutni se NE nalazite u laboratoriji");
            textView.setTextColor(Color.parseColor("#FF0000"));
        }
        return v;
    }

}
