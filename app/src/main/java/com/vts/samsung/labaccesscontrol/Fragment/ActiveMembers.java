package com.vts.samsung.labaccesscontrol.Fragment;



import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.vts.samsung.labaccesscontrol.Utils.Application;
import com.vts.samsung.labaccesscontrol.Activity.MainActivity;
import com.vts.samsung.labaccesscontrol.Adapter.RecyclerViewAdapter;
import com.vts.samsung.labaccesscontrol.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vts.samsung.labaccesscontrol.Adapter.CustomJsonObjectArrayRequest;

import java.util.ArrayList;

public class ActiveMembers extends Fragment {
    private View v;

    public ActiveMembers() {
        // Required empty public constructor
    }
    private Application app;
    private SharedPreferences sharedPreferences;
    private RecyclerView recyclerView;
    private ShimmerFrameLayout shimmerFrameLayout;
    private TextView tvError;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_active_members, container, false);

        MainActivity mainActivity = (MainActivity) getActivity();
        app = (Application) mainActivity.getApplication();
        sharedPreferences = getActivity().getSharedPreferences("аppSettings", Context.MODE_PRIVATE);

        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "exo.ttf");
        recyclerView = v.findViewById(R.id.recyclerActiveMembers);
        shimmerFrameLayout = (ShimmerFrameLayout) v.findViewById(R.id.shimmer_view_container);
        swipeRefreshLayout =(SwipeRefreshLayout) v.findViewById(R.id.swipe);
        tvError = (TextView) v.findViewById(R.id.tvErrorEmptyLab);
        tvError.setTypeface(typeface);

        swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#29ABE1"));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                sendAvailableUsersRequest();
                shimmerFrameLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                tvError.setVisibility(View.INVISIBLE);
            }
        });
        sendAvailableUsersRequest();
        return v;
    }

    private void sendAvailableUsersRequest() {
        shimmerFrameLayout.startShimmer();
        RequestQueue queue = Volley.newRequestQueue(getContext());

        CustomJsonObjectArrayRequest request = new CustomJsonObjectArrayRequest(Request.Method.GET, "http://rajk0.000webhostapp.com/eBrava/members.php", getRequestBody(), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                loadUsers(response);
                shimmerFrameLayout.stopShimmer();
                swipeRefreshLayout.setRefreshing(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                shimmerFrameLayout.stopShimmer();
                swipeRefreshLayout.setRefreshing(false);
                shimmerFrameLayout.setVisibility(View.GONE);
                //tvError.setText("Greska na serveru");
                //tvError.setVisibility(View.VISIBLE);

                NetworkResponse response = error.networkResponse;

                if (error instanceof TimeoutError || error instanceof NoConnectionError)
                    Toast.makeText(getContext(), getResources().getString(R.string.errConnectionFailure), Toast.LENGTH_SHORT).show();
                else if (response != null && response.data != null) {
                    switch (response.statusCode) {
                        /*case 400:
                            break;
                        case 500:
                            break;*/
                        default:
                            Toast.makeText(getContext(),getResources().getString(R.string.errUnhandled)+" ("+response.statusCode+")", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        queue.add(request);
    }

    private JSONObject getRequestBody() {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("uniqueToken", sharedPreferences.getString("uniqueToken", ""));
            requestBody.put("zahtev", "prisutni_u_laboratoriji");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return requestBody;
    }

    private void loadUsers(JSONArray jSonList) {
        ArrayList<String> mNames = new ArrayList<>();
        ArrayList<String> mRanks = new ArrayList<>();
        ArrayList<String> mImages = new ArrayList<>();

        JSONObject jsonObject;

        for (int i = 0; i < jSonList.length(); i++) {
            try {
                jsonObject = jSonList.getJSONObject(i);
                String fName = jsonObject.getString("ime");
                String lName = jsonObject.getString("prezime");
                String rank = jsonObject.getString("zvanje");
                try {
                    String img = jsonObject.getString("slika");
                    mImages.add(img);
                } catch (JSONException e) {
                    mImages.add("");
                }
                mNames.add(fName+" "+lName);
                mRanks.add(rank);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(app, getResources().getText(R.string.errWrongParameters), Toast.LENGTH_SHORT).show();
            }
        }
        initRecycleView(mImages, mNames, mRanks);
    }

    private void initRecycleView(ArrayList<String> mImages, ArrayList<String> mNames, ArrayList<String> mRanks) {
        recyclerView.setVisibility(View.VISIBLE);
        shimmerFrameLayout.setVisibility(View.GONE);
        tvError.setVisibility(mNames.size()!=0?View.INVISIBLE:View.VISIBLE);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(getActivity(),mImages, mNames, mRanks);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);//todo ispitati ovu komandu
    }
}
