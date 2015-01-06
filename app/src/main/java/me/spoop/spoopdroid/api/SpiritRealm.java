package me.spoop.spoopdroid.api;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.spoop.spoopdroid.Config;
import me.spoop.spoopdroid.items.Ghost;
import me.spoop.spoopdroid.items.JSendResponse;

/**
 * Server API
 * @author James Chambers
 */
public class SpiritRealm {

    private final static String TAG = "SpiritRealm";

    private final static int CACHE_NEVER = -1; // refreshes cache
    private final static int CACHE_ONE_MINUTE = 1000 * 60;
    private final static int CACHE_ONE_HOUR = CACHE_ONE_MINUTE * 60;
    private final static int CACHE_ONE_DAY = CACHE_ONE_HOUR * 24;

    private Context mContext;
    private OkHttpClient mHttpClient;
    private Gson mGson;

    public SpiritRealm(Context context) {
        mContext = context;
        mHttpClient = new OkHttpClient();
        mGson = new Gson();
    }

    /**
     * Get all the ghosts!
     * @return Promise for list of ghosts
     */
    public Promise<List<Ghost>, Exception, Void> getGhosts() {
        final DeferredObject<List<Ghost>, Exception, Void> deferred = new DeferredObject<List<Ghost>, Exception, Void>();

        Request request = new Request.Builder()
                .url(Config.BASE_URL + "ghosts.php")
                .build();

        mHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                deferred.reject(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    JSONObject json = new JSONObject(response.body().string());

                    // Get JSend response status
                    String status = json.getString("status");
                    if (status.equals("success")) {
                        JSONArray ghosts = json.getJSONObject("data").getJSONArray("ghosts");
                        ArrayList<Ghost> ghostResults = new ArrayList<Ghost>(ghosts.length());

                        // Convert the ghost JSON array to a list of native ghost objects
                        for (int i = 0; i < ghosts.length(); i++) {
                            Ghost curGhost = mGson.fromJson(ghosts.getJSONObject(i).toString(), Ghost.class);
                            ghostResults.add(curGhost);
                        }

                        // Resolve with the native ghost list
                        deferred.resolve(ghostResults);
                    } else {
                        deferred.reject(new Exception("server response: " + status));
                    }
                } catch (JSONException | JsonSyntaxException e) {
                    Log.e(TAG, e.getMessage());
                    deferred.reject(e);
                }
            }
        });

        return deferred.promise();
    }

    /**
     * Get all ghosts within 25 meters of a specific location.
     * @param longitude Longitude
     * @param latitude Latitude
     * @return Promise for list of ghosts near given location
     */
    public Promise<List<Ghost>, Exception, Void> getGhosts(double longitude, double latitude) {
        final DeferredObject<List<Ghost>, Exception, Void> deferred = new DeferredObject<List<Ghost>, Exception, Void>();

        // Set up POST parameters
        RequestBody params = new FormEncodingBuilder()
                .add("longitude", Double.toString(longitude))
                .add("latitude", Double.toString(latitude))
                .build();

        Request request = new Request.Builder()
                .url(Config.BASE_URL + "loc_query.php")
                .post(params)
                .build();

        // Make the POST request and get JSend response
        mHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                deferred.reject(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    JSONObject json = new JSONObject(response.body().string());
                    // Get JSend response status
                    String status = json.getString("status");
                    if (status.equals("success")) {
                        JSONArray ghosts = json.getJSONObject("data").getJSONArray("ghosts");
                        ArrayList<Ghost> ghostResults = new ArrayList<Ghost>(ghosts.length());

                        // Convert the ghost JSON array to a list of native ghost objects
                        for (int i = 0; i < ghosts.length(); i++) {
                            Ghost curGhost = mGson.fromJson(ghosts.getJSONObject(i).toString(), Ghost.class);
                            ghostResults.add(curGhost);
                        }

                        // Resolve with the native ghost list
                        deferred.resolve(ghostResults);
                    } else {
                        deferred.reject(new Exception("server response: " + status));
                    }
                } catch (JSONException | JsonSyntaxException e) {
                    Log.e(TAG, e.getMessage());
                    deferred.reject(e);
                }
            }
        });

        return deferred.promise();
    }

    /**
     * Submit a new ghost to the server.
     * @param ghost Ghost to submit
     * @return Promise for a JSend response with success or fail status
     */
    public Promise<JSendResponse, Exception, Void> submitGhost(Ghost ghost) {
        final DeferredObject<JSendResponse, Exception, Void> deferred = new DeferredObject<JSendResponse, Exception, Void>();

        // Set up POST parameters
        RequestBody params = new FormEncodingBuilder()
            .add("name", ghost.getName())
            .add("user", ghost.getUser())
            .add("drawable", ghost.getDrawable())
            .add("longitude", Double.toString(ghost.getLocation().getLongitude()))
            .add("latitude", Double.toString(ghost.getLocation().getLatitude()))
            .build();

        Request request = new Request.Builder()
                .url(Config.BASE_URL + "add_ghost.php")
                .post(params)
                .build();

        // Make the POST request and get JSend response
        mHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                deferred.reject(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    JSONObject json = new JSONObject(response.body().string());
                    JSendResponse jSendResponse = new JSendResponse(json.getString("status"), json.getJSONObject("data").getString("message"));
                    deferred.resolve(jSendResponse);
                } catch (JSONException e) {
                    deferred.reject(e);
                }
            }
        });

        return deferred.promise();
    }

}
