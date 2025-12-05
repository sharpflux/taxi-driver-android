package com.sharpflux.taxiapp.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.sharpflux.logomobility.R;
import com.sharpflux.taxiapp.data.model.PricingFeature;
import com.sharpflux.taxiapp.data.model.PricingPlan;
import com.sharpflux.taxiapp.ui.activities.PaymentActivity;

import java.util.List;

public class PricingAdapter extends RecyclerView.Adapter<PricingAdapter.ViewHolder> {

    Context context;
    List<PricingPlan> list;
    private String userName;
    private String userPhone;
    private String userEmail;
    private int userId;

    public PricingAdapter(Context context, List<PricingPlan> list,
                          String userName, String userPhone, String userEmail, int userId) {
        this.context = context;
        this.list = list;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userEmail = userEmail;
        this.userId = userId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_pricing_plan, parent, false);
        return new ViewHolder(view);
    }

    private int getThemeColor(int attr) {
        android.util.TypedValue typedValue = new android.util.TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);

        if (typedValue.resourceId != 0) {
            return androidx.core.content.ContextCompat.getColor(context, typedValue.resourceId);
        } else {
            return typedValue.data;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        PricingPlan p = list.get(position);

        h.txtPlanName.setText(p.planName);

        if (p.discountPercent > 0) {

            // SHOW DISCOUNT BADGE
            h.txtDiscount.setVisibility(View.VISIBLE);
            h.txtDiscount.setText(p.discountPercent + "% OFF");

            // SHOW ORIGINAL PRICE (Strike-through)
            h.txtOriginalPrice.setVisibility(View.VISIBLE);
            h.txtOriginalPrice.setText("₹" + p.originalPrice);
            h.txtOriginalPrice.setPaintFlags(
                    h.txtOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
            );

            // SHOW PRICE ROW
            h.txtCurrency.setVisibility(View.VISIBLE);
            h.txtDiscountedPrice.setVisibility(View.VISIBLE);
            h.txtPerMonth.setVisibility(View.VISIBLE);

            h.txtDiscountedPrice.setText(String.valueOf(p.discountedPrice));

            // SHOW FREE MONTHS
            if (p.freeMonths > 0) {
                h.txtFreeMonths.setVisibility(View.VISIBLE);
                h.txtFreeMonths.setText("+ " + p.freeMonths + " months free");
            } else {
                h.txtFreeMonths.setVisibility(View.GONE);
            }

        } else {

            // NO DISCOUNT → SO HIDE DISCOUNT AREA
            h.txtDiscount.setVisibility(View.GONE);

            // SHOW ORIGINAL PRICE IN BOLD (NO strike-through)
            h.txtOriginalPrice.setVisibility(View.VISIBLE);
            h.txtOriginalPrice.setPaintFlags(0);
            h.txtOriginalPrice.setText("₹" + p.originalPrice);
            h.txtOriginalPrice.setTextSize(22);
            h.txtOriginalPrice.setTextColor(getThemeColor(R.attr.text_primary_color));
            //h.txtOriginalPrice.setTypeface(null, android.graphics.Typeface.BOLD);

            // HIDE DISCOUNT PRICE ROW COMPLETELY
            h.txtCurrency.setVisibility(View.GONE);
            h.txtDiscountedPrice.setVisibility(View.GONE);
            h.txtPerMonth.setVisibility(View.GONE);

            // HIDE FREE MONTHS
            h.txtFreeMonths.setVisibility(View.GONE);
        }

        // Features
        h.layoutFeatures.removeAllViews();
        for (PricingFeature f : p.features) {
            TextView tv = new TextView(context);
            tv.setText("• " + f.featureText);
            tv.setTextSize(14);
            tv.setTextColor(getThemeColor(R.attr.text_primary_color));
            tv.setPadding(0, 6, 0, 0);
            h.layoutFeatures.addView(tv);
        }
        h.btnChoose.setOnClickListener(v -> {
            Intent i = new Intent(context, PaymentActivity.class);

            i.putExtra("planId", p.planId);
            i.putExtra("planName", p.planName);
            i.putExtra("originalPrice", p.originalPrice);
            i.putExtra("discountPercent", p.discountPercent);
            i.putExtra("discountedPrice", p.discountedPrice);
            i.putExtra("freeMonths", p.freeMonths);

            // Decide final payable amount
            double finalAmount = (p.discountPercent > 0) ? p.discountedPrice : p.originalPrice;
            i.putExtra("finalAmount", finalAmount);

            i.putExtra("userName", userName);
            i.putExtra("userPhone", userPhone);
            i.putExtra("userEmail", userEmail);
            i.putExtra("userId", userId);

            context.startActivity(i);
        });

    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtPlanName, txtDiscount, txtDiscountedPrice, txtFreeMonths,txtOriginalPrice,txtCurrency,txtPerMonth;
        LinearLayout layoutFeatures;
        MaterialButton btnChoose;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtPlanName = itemView.findViewById(R.id.txtPlanName);
            txtDiscount = itemView.findViewById(R.id.txtDiscount);
            txtOriginalPrice = itemView.findViewById(R.id.txtOriginalPrice);
            txtDiscountedPrice = itemView.findViewById(R.id.txtDiscountedPrice);
            txtFreeMonths = itemView.findViewById(R.id.txtFreeMonths);
            layoutFeatures = itemView.findViewById(R.id.layoutFeatures);
            txtPerMonth = itemView.findViewById(R.id.txtPerMonth);
            txtCurrency = itemView.findViewById(R.id.txtCurrency);
            btnChoose = itemView.findViewById(R.id.btnChoose);
        }
    }
}

