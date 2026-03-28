package com.heiwanaramu.smartpark.api;

import com.heiwanaramu.smartpark.models.LoginResponse;
import com.heiwanaramu.smartpark.models.ParkingSlot;
import com.heiwanaramu.smartpark.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @FormUrlEncoded
    @POST("api/mobile-login")
    Call<LoginResponse> loginUser(
            @Field("email") String email,
            @Field("password") String password
    );

    @POST("api/register")
    Call<User> registerUser(@Body User user);

    @GET("api/parking-slots")
    Call<List<ParkingSlot>> getParkingSlots();

    @FormUrlEncoded
    @POST("api/book-slot")
    Call<Void> bookSlot(
            @Field("slotId") String slotId,
            @Field("userId") String userId,
            @Field("amount") double amount
    );

    @GET("api/owner/dashboard/{ownerId}")
    Call<DashboardData> getOwnerDashboard(@Path("ownerId") String ownerId);

    class DashboardData {
        public double totalRevenue;
        public int currentOccupancy;
    }
}
