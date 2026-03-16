package com.sharpflux.taxiapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.sharpflux.logomobility.R;
import com.sharpflux.taxiapp.data.model.BillRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BillRequestAdapter extends RecyclerView.Adapter<BillRequestAdapter.ViewHolder> {

    private List<BillRequest> billRequests;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(BillRequest billRequest);
    }

    public BillRequestAdapter(List<BillRequest> billRequests, OnItemClickListener listener) {
        this.billRequests = billRequests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bill_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BillRequest billRequest = billRequests.get(position);

        holder.tvCustomerName.setText(billRequest.getCustomerName());
        holder.tvBillDate.setText(formatDate(billRequest.getBillDate()));
        holder.tvTotal.setText(String.format("₹ %.2f", billRequest.getTotal()));
        holder.tvRoute.setText(billRequest.getPickFrom() + " → " + billRequest.getDropAt());

        // Click listener for the entire card
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(billRequest);
            }
        });
    }

    @Override
    public int getItemCount() {
        return billRequests.size();
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateString;
        }
    }

    public void updateData(List<BillRequest> newBillRequests) {
        this.billRequests = newBillRequests;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvCustomerName, tvBillDate, tvTotal, tvRoute;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvBillDate = itemView.findViewById(R.id.tvBillDate);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvRoute = itemView.findViewById(R.id.tvRoute);
        }
    }
}