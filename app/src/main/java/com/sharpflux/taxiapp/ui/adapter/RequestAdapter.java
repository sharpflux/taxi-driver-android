package com.sharpflux.taxiapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sharpflux.taxiapp.R;
import com.sharpflux.taxiapp.data.model.CustomerRequest;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    public interface OnActionClickListener {
        void onAccept(CustomerRequest request);
        void onReject(CustomerRequest request);
    }

    private List<CustomerRequest> requestList;
    private OnActionClickListener listener;

    public RequestAdapter(List<CustomerRequest> requestList, OnActionClickListener listener) {
        this.requestList = requestList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomerRequest request = requestList.get(position);
        holder.txtPickFrom.setText("From: " + request.getPickFrom());
        holder.txtDropAt.setText("To: " + request.getDropAt());
        holder.txtFare.setText("Fare: ₹" + request.getFare());

        holder.btnAccept.setOnClickListener(v -> listener.onAccept(request));
        holder.btnReject.setOnClickListener(v -> listener.onReject(request));
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtPickFrom, txtDropAt, txtFare;
        Button btnAccept, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPickFrom = itemView.findViewById(R.id.txtPickFrom);
            txtDropAt = itemView.findViewById(R.id.txtDropAt);
            txtFare = itemView.findViewById(R.id.txtFare);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}

