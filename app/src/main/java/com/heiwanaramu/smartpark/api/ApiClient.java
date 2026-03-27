package com.heiwanaramu.smartpark.api;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // If testing on a physical Android device, use your computer's local Wi-Fi IP address (e.g., "http://192.168.x.x:3000/")
    // Do NOT use "http://localhost:3000" as the phone will look for a server inside itself.
    private static final String BASE_URL = "https://your-live-nextjs-url.com/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
