package com.heiwanaramu.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.tabs.TabLayout;
import com.heiwanaramu.smartpark.api.ApiClient;
import com.heiwanaramu.smartpark.api.ApiService;
import com.heiwanaramu.smartpark.models.ParkingSlot;
import com.heiwanaramu.smartpark.ui.LoginActivity;
import com.heiwanaramu.smartpark.ui.ParkingSlotAdapter;
import com.heiwanaramu.smartpark.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements ParkingSlotAdapter.OnSlotClickListener, OnMapReadyCallback {

    private RecyclerView rvParkingSlots;
    private View mapContainer;
    private ProgressBar progressBar;
    private TabLayout tabLayout;
    private ParkingSlotAdapter adapter;
    private List<ParkingSlot> slotList = new ArrayList<>();
    private ApiService apiService;
    private GoogleMap mMap;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        rvParkingSlots = findViewById(R.id.rvParkingSlots);
        mapContainer = findViewById(R.id.map);
        progressBar = findViewById(R.id.progressBar);
        tabLayout = findViewById(R.id.tabLayout);

        apiService = ApiClient.getClient().create(ApiService.class);
        adapter = new ParkingSlotAdapter(slotList, this);
        rvParkingSlots.setAdapter(adapter);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    rvParkingSlots.setVisibility(View.VISIBLE);
                    mapContainer.setVisibility(View.GONE);
                } else {
                    rvParkingSlots.setVisibility(View.GONE);
                    mapContainer.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
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
                    slotList.clear();
                    slotList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateMapMarkers();
                } else {
                    Toast.makeText(MainActivity.this, R.string.failed_to_load_slots, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ParkingSlot>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMapMarkers() {
        if (mMap == null) return;
        mMap.clear();
        for (ParkingSlot slot : slotList) {
            LatLng location = new LatLng(slot.getLatitude(), slot.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(slot.getName())
                    .snippet(slot.isAvailable() ? "Available" : "Occupied"));
        }
        if (!slotList.isEmpty()) {
            ParkingSlot first = slotList.get(0);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(first.getLatitude(), first.getLongitude()), 12));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        updateMapMarkers();
    }

    @Override
    public void onBookClick(ParkingSlot slot) {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        apiService.bookSlot(slot.getId(), userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, R.string.booking_successful, Toast.LENGTH_SHORT).show();
                    fetchParkingSlots();
                } else {
                    Toast.makeText(MainActivity.this, R.string.booking_failed, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            sessionManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
