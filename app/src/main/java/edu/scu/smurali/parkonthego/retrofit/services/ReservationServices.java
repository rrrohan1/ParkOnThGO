package edu.scu.smurali.parkonthego.retrofit.services;

import edu.scu.smurali.parkonthego.retrofit.reponses.ReservationCfnResponse;
import edu.scu.smurali.parkonthego.retrofit.reponses.ReservationDeleteResponse;
import edu.scu.smurali.parkonthego.retrofit.reponses.ReservationResponse;
import edu.scu.smurali.parkonthego.retrofit.reponses.ReservationUpdateResponse;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by chshi on 5/23/2016.
 */
public interface ReservationServices {

    @FormUrlEncoded
    @POST("reservation")
    Call<ReservationCfnResponse> createReservation(
            @Field("parkingid") Integer parkingId,
            @Field("userid") Integer userId,
            @Field("startingtime") String startingTime,
            @Field("endtime") String endTime,
            @Field("cost") Double cost);


    @GET("reservation/getUserReservations")
    Call<ReservationResponse> getUserReservations(
            @Query("userid") int id
    );

    @GET("reservation/getUserPastReservations")
    Call<ReservationResponse> getUserPastReservations(
            @Query("userid") int id
    );


    @FormUrlEncoded
    @POST("reservation/edit")
    Call<ReservationUpdateResponse> updateReservation(
            @Field("reservationid") Integer reservationId,
            @Field("parkingid") Integer parkingId,
            @Field("userid") Integer userId,
            @Field("startingtime") String startingTime,
            @Field("endtime") String endTime,
            @Field("cost") Double cost);


    @DELETE("reservation/{id}")
    Call<ReservationDeleteResponse> deleteReservation(
            @Path("id") String reservationId);

   
}
