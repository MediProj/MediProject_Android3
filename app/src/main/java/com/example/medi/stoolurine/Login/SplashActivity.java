package com.example.medi.stoolurine.Login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.medi.stoolurine.BaseActivity;
import com.example.medi.stoolurine.MediValues;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;


public class SplashActivity extends BaseActivity {
    private RequestQueue queue;
    private String authToken;

    public static final String TAG = "MainTAG";

    private String urlToken ="http://54.202.222.14/api-token-auth/";
    private String urlData ="http://54.202.222.14/dashboard/patients/api/patients-dashboard/";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(this);

        authToken = "";

        Map<String, String> params = new HashMap<>();
        params.put("username", MediValues.USERNAME);//put your parameters here
        params.put("password", MediValues.PASSWORD);

        MediTokenRequest jsObjRequest = new MediTokenRequest(
                Request.Method.POST, urlToken, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response: ", response.toString());
                        StringTokenizer tokens = new StringTokenizer(response.toString(), "\"");
                        tokens.nextToken();
                        tokens.nextToken();
                        tokens.nextToken();
                        MediValues.ACCESS_TOKEN = authToken = tokens.nextToken();

                        getPatientData();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError response) {
                        Log.d("Response: Error", response.toString());
                        Toast.makeText(getApplicationContext(),"인터넷 연결 후 재실행 하세요.", Toast.LENGTH_LONG).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 3000);
                    }
                }
        );

        jsObjRequest.setTag(TAG);
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        queue.add(jsObjRequest);
    }


    protected void getPatientData() {
        MediDataRequest jsRequest = new MediDataRequest(urlData,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Log.d("Response: ", response.toString());
                        parsePatientJSON(response);

                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError response) {
                        Toast.makeText(getApplicationContext(),response.toString(), Toast.LENGTH_LONG).show();

                        Log.d("Response: Error", response.toString());
                    }
                }
        );
        jsRequest.setTag(TAG);

        queue.add(jsRequest);
    }

    protected void parsePatientJSON(JSONArray response) {
        MediValues.patientData = new HashMap<>();

        for(int i = 0; i < response.length(); i++) {
            try {
                JSONObject entry = response.getJSONObject(i);
                String pid = entry.getString("pid");
                String name = entry.getString("name");
                String birth = entry.getString("birth");
                String pk = entry.getString("pk");

                Map<String, String> temp = new HashMap<>();
                temp.put("name", name);
                temp.put("birth", birth);
                temp.put("pk", pk);
                MediValues.patientData.put(pid, temp);
            } catch (JSONException je){
            }
        }
    }

}
