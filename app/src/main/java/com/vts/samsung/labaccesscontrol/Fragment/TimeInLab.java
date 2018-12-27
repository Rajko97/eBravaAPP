package com.vts.samsung.labaccesscontrol.Fragment;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vts.samsung.labaccesscontrol.R;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class TimeInLab extends Fragment {


    public TimeInLab() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_time_in_lab, container, false);
    }

}
