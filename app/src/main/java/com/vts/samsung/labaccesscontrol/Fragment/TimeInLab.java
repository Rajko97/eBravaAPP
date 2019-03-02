package com.vts.samsung.labaccesscontrol.Fragment;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
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
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.vts.samsung.labaccesscontrol.Activity.LoginActivity;
import com.vts.samsung.labaccesscontrol.Adapter.CustomJsonObjectArrayRequest;
import com.vts.samsung.labaccesscontrol.R;
import com.vts.samsung.labaccesscontrol.Utils.Application;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TimeInLab extends Fragment implements View.OnClickListener {

    public TimeInLab() {
        // Required empty public constructor
    }

    private ImageButton btnCalendarStart, btnCalendarEnd;
    private TextView tvDateStart, tvDateEnd;
    private DatePickerDialog datePickerDialog;
    private FloatingActionButton btnSearch;
    private String dateStartFormatedForRequest;
    private String dateEndFormatedForRequest;
    private ProgressDialog progressDialog;
    private BarChart barChart;
    private Application application;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_time_in_lab, container, false);

        application = (Application) getActivity().getApplication();
        sharedPreferences = getActivity().getSharedPreferences("аppSettings", Context.MODE_PRIVATE);

        initializeViews(v);
        setCustomFont(v);
        initBarChart();
        return v;
    }

    private void initializeViews(View v) {
        btnCalendarStart = (ImageButton) v.findViewById(R.id.imgBtnCalendarStart);
        btnCalendarEnd = (ImageButton) v.findViewById(R.id.imgBtnCalendarEnd);
        tvDateStart = (TextView) v.findViewById(R.id.tvCalendarStart);
        tvDateEnd = (TextView) v.findViewById(R.id.tvCalendarEnd);
        btnSearch = (FloatingActionButton) v.findViewById(R.id.fAbtnUpdateTime);

        barChart = (BarChart) v.findViewById(R.id.barChart);

        btnCalendarStart.setOnClickListener(this);
        btnCalendarEnd.setOnClickListener(this);
        tvDateStart.setOnClickListener(this);
        tvDateEnd.setOnClickListener(this);

        btnSearch.setOnClickListener(searchOnClick());
    }

    private View.OnClickListener searchOnClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int errorCode = 0;
                if(tvDateStart.getText().toString().isEmpty())
                    errorCode++;
                if(tvDateEnd.getText().toString().isEmpty())
                    errorCode+=2;
                switch (errorCode) {
                    case 0: sendGetTimeRequest(); break;
                    case 1:
                        Toast.makeText(getActivity(), "Unesite datum pocetka", Toast.LENGTH_SHORT).show();break;
                    case 2:
                        Toast.makeText(getActivity(), "Unesite datum zavrsetka", Toast.LENGTH_SHORT).show(); break;
                    case 3:
                        Toast.makeText(getActivity(), "Unesite pocetni i krajnji datum", Toast.LENGTH_SHORT).show();break;
                    default:
                }
            }
        };
    }

    private void sendGetTimeRequest() {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(R.style.ProgressBar);
        progressDialog.setMessage(getResources().getString(R.string.dialogMessageFetchTimeInLab));
        progressDialog.setCancelable(false);
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(getContext());

        CustomJsonObjectArrayRequest request = new CustomJsonObjectArrayRequest(Request.Method.POST, "http://rajk0.000webhostapp.com/eBrava/time.php", getRequestBody(), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if(response.length() == 0) {
                    barChart.setNoDataText("Nema pronadjenih podataka za unete datume.");
                    return;
                }
                ArrayList<BarEntry> barEntries = new ArrayList<>();
                ArrayList<String> dates = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    try {
                        String time = response.getJSONObject(i).getString("totalTimeForDay");
                        dates.add(reformatDateFromRequest(response.getJSONObject(i).getString("dateCheckInUnique")));
                        //barEntries.add(new BarEntry(Float.parseFloat(time), i, reformatTimeFromRequest(time)));
                        barEntries.add(new BarEntry(Float.parseFloat(time), i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                int colorBlue = ContextCompat.getColor(getActivity(), R.color.colorThemeBlue);
                BarDataSet barDataSet = new BarDataSet(barEntries, "Sati");
                barDataSet.setColor(colorBlue);
                barDataSet.setValueTextColor(ContextCompat.getColor(getActivity(), R.color.colorTextWhite));
                barDataSet.setValueTypeface( Typeface.createFromAsset(getActivity().getAssets(), "exo.ttf"));
                barDataSet.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                        //0-1 <=> 60
                         int sati = (int) value;
                         int x = (int) (value * 100) % 100;
                         int min = Math.round(6*((float)x/10));
                         return sati+(min<10?":0":":")+min;
                    }
                });
                BarData barData = new BarData(dates, barDataSet);
                barChart.setData(barData);
                barChart.animateY(3000 , Easing.EasingOption.EaseOutBack );
                barChart.invalidate();
                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;

                if (error instanceof TimeoutError || error instanceof NoConnectionError)
                    barChart.setNoDataText(getResources().getString(R.string.errConnectionFailure));
                else
                    barChart.setNoDataText(getResources().getString(R.string.errOnSrv)+"("+response.statusCode+")");
                progressDialog.dismiss();
            }
        });
        queue.add(request);
    }

    private JSONObject getRequestBody() {
        JSONObject body = new JSONObject();
        try {
            body.put("uniqueToken", sharedPreferences.getString("uniqueToken", ""));
            body.put("macAddressDevice", application.getDeviceMac());
            body.put("dateStart", dateStartFormatedForRequest);
            body.put("dateEnd", dateEndFormatedForRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return body;
    }

    @Override
    public void onClick(View view) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        if (view.getId() == R.id.imgBtnCalendarStart || view.getId() == R.id.tvCalendarStart) {
            if (datePickerDialog == null || (datePickerDialog != null && !datePickerDialog.isShowing())) {
                datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        dateStartFormatedForRequest = year + "-" + (month + 1) + "-" + day;
                        tvDateStart.setText(day + "." + (month + 1) + "." + year);
                    }
                }, year, month, dayOfMonth);
            }

        } else if (view.getId() == R.id.imgBtnCalendarEnd || view.getId() == R.id.tvCalendarEnd){
            if(datePickerDialog == null || (datePickerDialog != null && !datePickerDialog.isShowing())) {
                datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        dateEndFormatedForRequest = year + "-" + (month + 1) + "-" + day;
                        tvDateEnd.setText(day + "." + (month + 1) + "." + year);
                    }
                }, year, month, dayOfMonth);
            }
        }
        datePickerDialog.show();
    }

    private void initBarChart() {
        int colorWhite = ContextCompat.getColor(getActivity(), R.color.colorTextWhite);
        //barChart.setDescriptionColor(Color.parseColor("#ffff"));// menja samo na description
        //barChart.setBackgroundColor(color); pozadina layout-a
        //barChart.setDrawingCacheBackgroundColor(color); nista??

        barChart.setTouchEnabled(false);
        barChart.setDragEnabled(false);
        barChart.setScaleEnabled(false);

        barChart.setDescription("");
        barChart.setNoDataText("Odaberite datume i kliknite na dugme za pretragu.");

        barChart.getLegend().setTextColor(colorWhite);
        barChart.getAxisLeft().setTextColor(colorWhite);
        barChart.getAxisRight().setTextColor(colorWhite);
        barChart.getXAxis().setTextColor(colorWhite);
    }

    private void setCustomFont(View v) {
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "exo.ttf");
        int ids[] = {R.id.tvChooseTimeHeader, R.id.tvTextBegin, R.id.tvTextEnd, R.id.tvCalendarStart, R.id.tvCalendarEnd};
        for (int id : ids) {
            ((TextView) v.findViewById(id)).setTypeface(typeface);
        }
        barChart.getAxisLeft().setTypeface(typeface);
        barChart.getAxisRight().setTypeface(typeface);
        barChart.getXAxis().setTypeface(typeface);
        barChart.getLegend().setTypeface(typeface);
    }

    private String reformatDateFromRequest(String date) {
        SimpleDateFormat existingUTCFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat requiredFormat = new SimpleDateFormat("dd");
        Date getDate = null;
        try {
            getDate = existingUTCFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return requiredFormat.format(getDate);
    }
}