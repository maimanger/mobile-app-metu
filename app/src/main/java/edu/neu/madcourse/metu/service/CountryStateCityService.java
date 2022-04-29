package edu.neu.madcourse.metu.service;

import android.util.Log;

import org.apache.http.HttpConnection;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CountryStateCityService {
    private static final String TAG = "CountryStateCityService";
    private static final String USER_EMAIL = "alice@bob.com";
    private static final String API_TOKEN =
            "WxKOzHVWlBswYGXt1zoDZFjyQpYtAHsICAS9Aal0zxQmvbofAF_wtXMZr0oyTsq-ZNk";
    private static final String BASE_ROUTE = "https://www.universal-tutorial.com/api/";

    private static CountryStateCityService singleton_instance = null;

    private CountryStateCityService() {
    }

    public static CountryStateCityService getInstance() {
        if (singleton_instance == null) {
            singleton_instance = new CountryStateCityService();
        }
        return singleton_instance;
    }

    private static String getAuthToken() throws JSONException {
        String auth_token = "";
        try {
            String jsonStr = readJsonStrFromUrl(BASE_ROUTE + "getaccesstoken",
                    "", new HashMap<String, String>() {{
                        put("Accept", "application/json");
                        put("api-token", API_TOKEN);
                        put("user-email", USER_EMAIL);
                    }});
            auth_token = (new JSONObject(jsonStr)).getString("auth_token");
//            Log.e(TAG, "auth token = " + auth_token);
        } catch (JSONException | IOException e) {
            Log.e(TAG, "Failed to get auth token");
            e.printStackTrace();
        }

        return auth_token;
    }

    public static List<String> getStates() throws JSONException {
        String country = "United States";
        JSONArray stateArray = null;
        try {
            String jsonStr = readJsonStrFromUrl(BASE_ROUTE + "states/" + country,
                    "Bearer " + getAuthToken(), new HashMap<String, String>() {{
                        put("Accept", "application/json");
                    }});
            stateArray = new JSONArray(jsonStr);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException: Failed to get list of states");
            e.printStackTrace();
            return new ArrayList<>();
        } catch (IOException e) {
            Log.e(TAG, "IOException: Failed to get list of states");
            e.printStackTrace();
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

    public static List<String> getCities(String state) throws JSONException {
        JSONArray cityArray = null;
        try {
            String jsonStr = readJsonStrFromUrl(BASE_ROUTE + "cities/" + state,
                    "Bearer " + getAuthToken(), new HashMap<String, String>() {{
                        put("Accept", "application/json");
                    }});
            cityArray = new JSONArray(jsonStr);
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Failed to get list of cities");
            e.printStackTrace();
            return new ArrayList<>();
        }

        List<String> cities = new ArrayList<>();
        for (int i = 0; i < cityArray.length(); ++i) {
            JSONObject record = cityArray.getJSONObject(i);
            String city = record.getString("city_name");
            cities.add(city);
        }
        return cities;

    }

    public static String readJsonStrFromUrl(String url, String authToken,
                                            Map<String, String> headerMap) throws IOException,
            JSONException {
        Log.e(TAG, "url: " + url);
        URL webUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) webUrl.openConnection();
        conn.setUseCaches(true);
        conn.setDoInput(true);

        if (authToken != null && !authToken.isEmpty()) {
            conn.setRequestProperty("Authorization", authToken);
        }

        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            conn.setRequestProperty(entry.getKey(), entry.getValue());
        }

        conn.setRequestMethod("GET");
//        Log.e(TAG, conn.getRequestProperties().toString());

        int respCode = conn.getResponseCode(); // New items get NOT_FOUND on PUT
//        Log.e(TAG, "response code: " + respCode);

        if (respCode == HttpURLConnection.HTTP_OK || respCode == HttpURLConnection.HTTP_NOT_FOUND) {
            InputStream is = conn.getInputStream();

            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(is,
                        StandardCharsets.UTF_8));
                return readAll(rd);
            } catch (MalformedURLException e) {
                Log.e(TAG, "MalformedURLException");
                e.getStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "IOException");
                e.getStackTrace();
            } finally {
                conn.disconnect();
                is.close();
            }
        }
        return "";
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

