package com.vts.samsung.labaccesscontrol.Fragment;



import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.vts.samsung.labaccesscontrol.Activity.LoginActivity;
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
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mRanks = new ArrayList<>();

    private ProgressDialog progressDialog;
    private Application app;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_active_members, container, false);

        MainActivity mainActivity = (MainActivity) getActivity();
        app = (Application) mainActivity.getApplication();
        sharedPreferences = getActivity().getSharedPreferences("аppSettings", Context.MODE_PRIVATE);

        sendAvilableUsersRequest();
        return v;
    }

    private void sendAvilableUsersRequest() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setProgressStyle(R.style.ProgressBar);
        progressDialog.setMessage("Ucitavam korisnike...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(getContext());

        CustomJsonObjectArrayRequest request = new CustomJsonObjectArrayRequest(Request.Method.POST, app.getRpiRouteAvailableUserInLab(), getRequestBody(), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                loadUsers(response);
                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
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
        JSONObject jsonObject;

        for (int i = 0; i < jSonList.length(); i++) {
            try {
                jsonObject = jSonList.getJSONObject(i);
                String fName = jsonObject.getString("ime");
                String lName = jsonObject.getString("prezime");
                String rank = jsonObject.getString("zvanje");

                mNames.add(fName+" "+lName);
                mRanks.add(rank);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        initRecycleView();
    }

    private void initRecycleView() {
        RecyclerView recyclerView = v.findViewById(R.id.recyclerActiveMembers);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(mNames, mRanks, getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}
