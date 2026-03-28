package com.heiwanaramu.smartpark.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.heiwanaramu.smartpark.R;
import com.heiwanaramu.smartpark.models.ParkingSlot;

import java.util.List;

public class ParkingSlotAdapter extends RecyclerView.Adapter<ParkingSlotAdapter.ViewHolder> {

    private List<ParkingSlot> slots;
    private OnSlotClickListener listener;

    public interface OnSlotClickListener {
        void onBookClick(ParkingSlot slot);
    }

    public ParkingSlotAdapter(List<ParkingSlot> slots, OnSlotClickListener listener) {
        this.slots = slots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parking_slot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParkingSlot slot = slots.get(position);
        holder.tvSlotName.setText(slot.getName());
        holder.tvPrice.setText(String.format("Price: $%.2f/hr", slot.getPricePerHour()));
        holder.tvStatus.setText(slot.isAvailable() ? "Available" : "Occupied");
        holder.tvStatus.setTextColor(slot.isAvailable() ?
            holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark) :
            holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
        
        holder.btnBook.setEnabled(slot.isAvailable());
        holder.btnBook.setOnClickListener(v -> listener.onBookClick(slot));
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSlotName, tvPrice, tvStatus;
        MaterialButton btnBook;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSlotName = itemView.findViewById(R.id.tvSlotName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnBook = itemView.findViewById(R.id.btnBook);
        }
    }
}
