package edu.scu.smurali.parkonthego.retrofit.services;

import edu.scu.smurali.parkonthego.retrofit.reponses.LocationResponse;
import edu.scu.smurali.parkonthego.retrofit.reponses.SearchResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by chshi on 5/23/2016.
 */
public interface LocationServices {

    @GET("search")
    Call<LocationResponse> getLocationDetails(
            @Query("id") String id);

    @GET("search/getLocationsNearMe")
    Call<SearchResponse> getLocationsNearMe(
            @Query("userId") int id,
            @Query("lat") Double lat,
            @Query("long") Double lng,
            @Query("dis") Integer distance,
            @Query("startdatetime") String startDateTime,
            @Query("enddatetime") String endDateTime);
}
