package com.heiwanaramu.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.heiwanaramu.smartpark.api.ApiClient;
import com.heiwanaramu.smartpark.api.ApiService;
import com.heiwanaramu.smartpark.models.ParkingSlot;
import com.heiwanaramu.smartpark.ui.ParkingSlotAdapter;
import com.heiwanaramu.smartpark.ui.SettingsActivity;
import com.heiwanaramu.smartpark.ui.SmartParkActivity;
import com.heiwanaramu.smartpark.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends SmartParkActivity implements ParkingSlotAdapter.OnSlotClickListener, OnMapReadyCallback {

    private View cardExplore, cardMap, cardInsights;
    private RecyclerView rvParkingSlots;
    private ProgressBar progressBar;
    private TabLayout tabLayout;
    private ParkingSlotAdapter adapter;
    private List<ParkingSlot> fullSlotList = new ArrayList<>();
    private List<ParkingSlot> displayedSlotList = new ArrayList<>();
    private ApiService apiService;
    private GoogleMap mMap;
    private SessionManager sessionManager;
    private TextView btnSettings;
    private ChipGroup chipGroupFilters;

    private TextView tvTotalRevenue, tvOwnerShare, tvPlatformShare, tvOccupancy;
    private CircularProgressIndicator occupancyProgress;
    private Marker destinationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient().create(ApiService.class);

        cardExplore = findViewById(R.id.cardExplore);
        cardMap = findViewById(R.id.cardMap);
        cardInsights = findViewById(R.id.cardInsights);
        
        rvParkingSlots = findViewById(R.id.rvParkingSlots);
        progressBar = findViewById(R.id.progressBar);
        tabLayout = findViewById(R.id.tabLayout);
        btnSettings = findViewById(R.id.btnSettings);
        chipGroupFilters = findViewById(R.id.chipGroupFilters);

        // Dashboard Views
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvOwnerShare = findViewById(R.id.tvOwnerShare);
        tvPlatformShare = findViewById(R.id.tvPlatformShare);
        tvOccupancy = findViewById(R.id.tvOccupancy);
        occupancyProgress = findViewById(R.id.occupancyProgress);

        adapter = new ParkingSlotAdapter(displayedSlotList, this);
        rvParkingSlots.setAdapter(adapter);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                cardExplore.setVisibility(View.GONE);
                cardMap.setVisibility(View.GONE);
                cardInsights.setVisibility(View.GONE);

                switch (tab.getPosition()) {
                    case 0:
                        cardExplore.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        cardMap.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        cardInsights.setVisibility(View.VISIBLE);
                        loadDashboardData();
                        break;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            applyFilters();
        });

        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        fetchParkingSlots();
    }

    private void fetchParkingSlots() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getParkingSlots().enqueue(new Callback<List<ParkingSlot>>() {
            @Override
            public void onResponse(@NonNull Call<List<ParkingSlot>> call, @NonNull Response<List<ParkingSlot>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    fullSlotList.clear();
                    fullSlotList.addAll(response.body());
                    applyFilters();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<ParkingSlot>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void applyFilters() {
        displayedSlotList.clear();
        displayedSlotList.addAll(fullSlotList);

        int checkedId = chipGroupFilters.getCheckedChipId();
        if (checkedId == R.id.chipCheapest) {
            Collections.sort(displayedSlotList, (s1, s2) -> Double.compare(s1.getPricePerHour(), s2.getPricePerHour()));
        } else if (checkedId == R.id.chipSafest) {
            Collections.sort(displayedSlotList, (s1, s2) -> Double.compare(s2.getTrafficSafetyScore(), s1.getTrafficSafetyScore()));
        } else { // Recommended
            for (ParkingSlot slot : displayedSlotList) slot.setRecommended(false);
            Collections.sort(displayedSlotList, (s1, s2) -> {
                double score1 = (1.0 / (s1.getPricePerHour() + 1)) * 0.4 + (s1.getTrafficSafetyScore() * 0.4) + (s1.isHasEVCharging() ? 0.2 : 0);
                double score2 = (1.0 / (s2.getPricePerHour() + 1)) * 0.4 + (s2.getTrafficSafetyScore() * 0.4) + (s2.isHasEVCharging() ? 0.2 : 0);
                return Double.compare(score2, score1);
            });
            if (!displayedSlotList.isEmpty()) displayedSlotList.get(0).setRecommended(true);
        }

        adapter.notifyDataSetChanged();
        updateMapMarkers();
    }

    private void updateMapMarkers() {
        if (mMap == null) return;
        mMap.clear();
        for (ParkingSlot slot : displayedSlotList) {
            LatLng location = new LatLng(slot.getLatitude(), slot.getLongitude());
            float color = slot.isRecommended() ? BitmapDescriptorFactory.HUE_AZURE : 
                         (slot.isAvailable() ? BitmapDescriptorFactory.HUE_GREEN : BitmapDescriptorFactory.HUE_RED);
            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(slot.getName())
                    .snippet(String.format(Locale.getDefault(), "Safety: %d%% | $%.2f/hr", (int)(slot.getTrafficSafetyScore()*100), slot.getPricePerHour()))
                    .icon(BitmapDescriptorFactory.defaultMarker(color)));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        updateMapMarkers();
        mMap.setOnMapLongClickListener(latLng -> {
            if (destinationMarker != null) destinationMarker.remove();
            destinationMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Target")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
            
            ParkingSlot nearest = findNearestSlot(latLng);
            if (nearest != null) {
                Toast.makeText(this, "Nearest: " + nearest.getName(), Toast.LENGTH_LONG).show();
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        });
    }

    private ParkingSlot findNearestSlot(LatLng point) {
        if (fullSlotList.isEmpty()) return null;
        ParkingSlot closest = fullSlotList.get(0);
        double minDistance = Double.MAX_VALUE;
        for (ParkingSlot slot : fullSlotList) {
            double dist = Math.sqrt(Math.pow(slot.getLatitude() - point.latitude, 2) + Math.pow(slot.getLongitude() - point.longitude, 2));
            if (dist < minDistance) {
                minDistance = dist;
                closest = slot;
            }
        }
        return closest;
    }

    private void loadDashboardData() {
        String userId = sessionManager.getUserId();
        apiService.getOwnerDashboard(userId).enqueue(new Callback<ApiService.DashboardData>() {
            @Override
            public void onResponse(@NonNull Call<ApiService.DashboardData> call, @NonNull Response<ApiService.DashboardData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.DashboardData data = response.body();
                    tvTotalRevenue.setText(String.format(Locale.getDefault(), "$%.2f", data.totalRevenue));
                    tvOwnerShare.setText(String.format(Locale.getDefault(), "$%.2f", data.totalRevenue * 0.9));
                    tvPlatformShare.setText(String.format(Locale.getDefault(), "$%.2f", data.totalRevenue * 0.1));
                    tvOccupancy.setText(String.format(Locale.getDefault(), "%d%%", data.currentOccupancy));
                    occupancyProgress.setProgress(data.currentOccupancy);
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiService.DashboardData> call, @NonNull Throwable t) {}
        });
    }

    @Override
    public void onBookClick(ParkingSlot slot) {
        apiService.bookSlot(slot.getId(), sessionManager.getUserId(), slot.getPricePerHour()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Booked!", Toast.LENGTH_SHORT).show();
                    fetchParkingSlots();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {}
        });
    }
}
