package edu.neu.madcourse.metu.service;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CountryStateCityService {
    private static final String TAG = "CountryStateCityService";
    private final String USER_EMAIL = "alice@bob.com";
    private final String API_TOKEN = "WxKOzHVWlBswYGXt1zoDZFjyQpYtAHsICAS9Aal0zxQmvbofAF_wtXMZr0oyTsq-ZNk";
    private final String BASE_ROUTE = "https://www.universal-tutorial.com/api/";

    public String getAuthToken() throws JSONException {
        HttpClient httpclient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(BASE_ROUTE + "getaccesstoken");
        request.setHeader("api-token", API_TOKEN);
        request.setHeader("user-email", USER_EMAIL);
        request.addHeader("Content-Type", "application/json");

        String auth_token = "";
        try {
            HttpResponse response = httpclient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                JSONObject auth_token_json = new JSONObject(responseBody);
                auth_token = auth_token_json.getString("auth_token");
            } else {
                Log.e(TAG, "HTTP GET returned " + statusCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            request.releaseConnection();
        }

        return auth_token;
    }

    public List<String> getStates() throws JSONException {
        String country = "United States";
        JSONArray stateArray = null;
        try {
            JSONObject json = readJsonFromUrl(BASE_ROUTE + "states/" + country);
            stateArray = new JSONArray(json);
        } catch (JSONException | IOException e) {
            Log.e(TAG, "Failed to get list of states: " + Arrays.toString(e.getStackTrace()));
            return new ArrayList<>();
        }

        List<String> states = new ArrayList<>();
        for (int i = 0; i < stateArray.length(); ++i) {
            JSONObject record = stateArray.getJSONObject(i);
            String state = record.getString("state_name");
            states.add(state);
        }
        return states;
    }

    public List<String> getCities(String state) throws JSONException {
        JSONArray cityArray = null;
        try {
            JSONObject json = readJsonFromUrl(BASE_ROUTE + "cities/" + state);
            cityArray = new JSONArray(json);
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Failed to get list of cities: " + Arrays.toString(e.getStackTrace()));
            return new ArrayList<>();
        }

        List<String> states = new ArrayList<>();
        for (int i = 0; i < cityArray.length(); ++i) {
            JSONObject record = cityArray.getJSONObject(i);
            String city = record.getString("city_name");
            states.add(state);
        }
        return states;

    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is,
                    StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        } catch (IOException e) {
            e.getStackTrace();
            return null;
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}

