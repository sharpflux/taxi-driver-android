package com.sharpflux.taxiapp.data.network;

public class   APIs {
    public static final String Main_URL="https://6kt492jn-7270.inc1.devtunnels.ms/api/";
    public static final String serverUrl ="https://6kt492jn-7270.inc1.devtunnels.ms";

    public static final String LoginURL = Main_URL + "Auth/Driverlogin";
    public static final String RegisterURL = Main_URL + "Customers/CustomersInsertUpdate";
    public static final String GetCustomersURL = Main_URL + "DriversController1/GetById";
    public static final String SaveBillURL = Main_URL + "CustomerBillRequest/insert-update";
//    public static final String DriverRegisterURL = Main_URL + "DriversController1/DriverInsertUpdate";
    public static final String DriverRegisterURL = Main_URL + "Driver/DriverInsertUpdate";
    public static final String GenerateBillPdfURL = Main_URL + "CustomerBillRequest/GenerateCustomerBillPdf";
    public static final String GetBillRequestURL = Main_URL + "CustomerBillRequest/GetByCustomerId";
    public static final String UpdateBillApprovalURL = Main_URL + "CustomerBillRequest/insert-update";
    public static final String GetDriversURL = Main_URL + "DriversController1/GetById/";
    public static final String QR_URL = Main_URL + "QRCode/generate/{driverId}";
    public static final String GetDocumentURL = Main_URL + "DocumentTypes/GetDocumentTypes";
    public static final String SendSMSURL = Main_URL + "DriverOtp/send";
    public static final String VerifySMSURL = Main_URL + "DriverOtp/verify";


    // For SignalR Hub
    public static final String SIGNALR_HUB_URL = serverUrl ;

    // RazorPay
    public static final String RazorPayCreateOrder = Main_URL + "Razorpay/create-order";
    public static final String RazorPayVerification = Main_URL + "Razorpay/verify";
    public static final String GetPaymentDetails = Main_URL + "Razorpay/verify";
}
