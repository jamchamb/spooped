package edu.rutgers.jamchamb.spooped.api;

import android.content.Context;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.rutgers.jamchamb.spooped.items.Ghost;
import edu.rutgers.jamchamb.spooped.items.JSendResponse;

/**
 * Get ghosts from the server.
 */
public class SpiritRealm {

    private final static String TAG = "SpiritRealm";
    private final static String BASE_URL = "http://104.131.98.195/";

    private final static int CACHE_ONE_DAY = 1000 * 60 * 60 * 24;
    private final static int CACHE_NEVER = -1;

    private Context context;
    private AQuery aq;


    public SpiritRealm(Context context) {
        this.context = context;
        this.aq = new AQuery(context);
    }

    /**
     * Get all the ghosts!
     * @return Promise for list of ghosts
     */
    public Promise<List<Ghost>, Exception, Void> getGhosts() {
        final DeferredObject<List<Ghost>, Exception, Void> deferred = new DeferredObject<List<Ghost>, Exception, Void>();

        aq.ajax(BASE_URL+"ghosts.php", JSONObject.class, SpiritRealm.CACHE_NEVER, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject json, AjaxStatus ajaxStatus) {
                if(json == null) {
                    // Bad response, don't cache it
                    ajaxStatus.invalidate();
                    deferred.reject(new Exception(ajaxStatus.getMessage()));
                } else {
                    // Got a JSON response
                    try {
                        // Get JSend response status
                        String status = json.getString("status");
                        if(status.equals("success")) {
                            JSONArray ghosts = json.getJSONObject("data").getJSONArray("ghosts");
                            ArrayList<Ghost> ghostResults = new ArrayList<Ghost>(ghosts.length());

                            // Convert the ghost JSON array to a list of native ghost objects
                            Gson gson = new Gson();
                            for(int i = 0; i < ghosts.length(); i++) {
                                Ghost curGhost = gson.fromJson(ghosts.getJSONObject(i).toString(), Ghost.class);
                                ghostResults.add(curGhost);
                            }

                            // Resolve with the native ghost list
                            deferred.resolve(ghostResults);
                        } else {
                            deferred.reject(new Exception("server response: " + status));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        deferred.reject(e);
                    } catch (JsonSyntaxException e) {
                        Log.e(TAG, e.getMessage());
                        deferred.reject(e);
                    }
                }
            }
        });

        return deferred.promise();
    }

    public Promise<JSendResponse, Exception, Void> submitGhost(Ghost ghost) {
        final DeferredObject<JSendResponse, Exception, Void> deferred = new DeferredObject<JSendResponse, Exception, Void>();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", ghost.getName());
        params.put("user", ghost.getUser());
        params.put("drawable", ghost.getDrawable());
        params.put("longitude", ghost.getLocation().getLongitude());
        params.put("latitude", ghost.getLocation().getLatitude());

        aq.ajax(BASE_URL+"add_ghost.php", params, JSONObject.class, new AjaxCallback<JSONObject>() {

            @Override
            public void callback(String url, JSONObject json, AjaxStatus status){
                if(json == null) {
                    deferred.reject(new Exception(status.getMessage()));
                } else {
                    try {
                        JSendResponse response = new JSendResponse(json.getString("status"), json.getJSONObject("data").getString("message"));
                        deferred.resolve(response);
                    } catch (JSONException e) {
                        deferred.reject(e);
                    }
                }
            }

        });

        return deferred.promise();
    }

}
