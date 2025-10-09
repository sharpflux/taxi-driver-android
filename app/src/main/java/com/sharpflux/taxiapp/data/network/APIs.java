package com.sharpflux.taxiapp.data.network;

public class   APIs {
    public static final String Main_URL="https://tdm0f26m-7270.inc1.devtunnels.ms/api/";

    public static final String LoginURL = Main_URL + "Auth/login";
    public static final String RegisterURL = Main_URL + "Customers/CustomersInsertUpdate";
    public static final String GetCustomersURL = Main_URL + "Customers/CustomersGET";
    public static final String SaveBillURL = Main_URL + "CustomerBillRequest/insert-update";
}
