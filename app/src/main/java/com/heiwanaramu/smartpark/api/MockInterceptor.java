package com.heiwanaramu.smartpark.api;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MockInterceptor implements Interceptor {

    private double totalRevenue = 1250.50;
    private int bookings = 42;

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        String uri = chain.request().url().uri().toString();
        String responseString = "";

        if (uri.contains("api/mobile-login")) {
            responseString = "{\"token\": \"fake-jwt-token\", \"user\": {\"id\": \"user123\", \"name\": \"John Doe\", \"email\": \"test@example.com\", \"role\": \"OWNER\"}}";
        } else if (uri.contains("api/parking-slots")) {
            responseString = "[" +
                    "{\"id\":\"1\", \"name\":\"Central Plaza A\", \"latitude\":37.7749, \"longitude\":-122.4194, \"isAvailable\":true, \"pricePerHour\":15.0, \"hasEVCharging\":true, \"trafficSafetyScore\":0.95}," +
                    "{\"id\":\"2\", \"name\":\"Green Valley Lot\", \"latitude\":37.7833, \"longitude\":-122.4167, \"isAvailable\":true, \"pricePerHour\":10.0, \"hasEVCharging\":false, \"trafficSafetyScore\":0.88}," +
                    "{\"id\":\"3\", \"name\":\"Sunset Blvd Parking\", \"latitude\":37.7500, \"longitude\":-122.4333, \"isAvailable\":false, \"pricePerHour\":20.0, \"hasEVCharging\":true, \"trafficSafetyScore\":0.70}," +
                    "{\"id\":\"4\", \"name\":\"Metro Station Side\", \"latitude\":37.7900, \"longitude\":-122.4000, \"isAvailable\":true, \"pricePerHour\":12.0, \"hasEVCharging\":false, \"trafficSafetyScore\":0.92}" +
                    "]";
        } else if (uri.contains("api/book-slot")) {
            // Simulate a payment delay
            try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
            totalRevenue += 15.0; // Simulate adding booking price
            bookings++;
            responseString = "{}";
        } else if (uri.contains("api/owner/dashboard")) {
            double platformShare = totalRevenue * 0.6;
            double ownerShare = totalRevenue * 0.4;
            responseString = "{\"totalRevenue\": " + totalRevenue + ", \"currentOccupancy\": 75}";
        } else if (uri.contains("api/register")) {
            responseString = "{\"id\": \"new_user\", \"name\": \"New User\", \"email\": \"new@test.com\"}";
        }

        return new Response.Builder()
                .code(200)
                .message("OK")
                .request(chain.request())
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(MediaType.parse("application/json"), responseString.getBytes()))
                .addHeader("content-type", "application/json")
                .build();
    }
}
