package com.sharpflux.taxiapp.data.model;

import java.util.List;

public class PricingPlan {
    public int planId;
    public String planName;
    public double originalPrice;
    public double discountedPrice;
    public int discountPercent;
    public int freeMonths;
    public double total48MonthsPrice;
    public List<PricingFeature> features;
}
