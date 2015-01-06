package me.spoop.spoopdroid.api;

import java.util.List;

import me.spoop.spoopdroid.items.Ghost;
import me.spoop.spoopdroid.items.JSendResponse;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Server API
 * @author James Chambers
 */
public interface SpoopedAPI {

    /** Get all the ghosts! */
    @GET("/ghosts.php")
    void getGhosts(Callback<List<Ghost>> cb);

    /** Get all ghosts within 25 meters of a specific location. */
    @GET("/loc_query.php")
    void getGhosts(@Query("longitude") String longitude, @Query("latitude") String latitude, Callback<List<Ghost>> cb);

    /** Submit a new ghost to the server. */
    @POST("/add_ghost.php")
    void submitGhost(@Body Ghost ghost, Callback<JSendResponse> cb);

}
