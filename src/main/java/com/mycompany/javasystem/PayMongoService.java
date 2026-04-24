package com.mycompany.javasystem;
 
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import java.util.Base64;
 
public class PayMongoService {
 
    private static final String SECRET_KEY = "sk_test_wxooREqwAJ3qSFcAhiP4PWas";
    private static final String BASE_URL = "https://api.paymongo.com/v1";
    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
 
    private static String getAuthHeader() {
        String credentials = SECRET_KEY + ":";
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }
 
    // Creates a checkout session and returns "checkoutUrl|sessionId"
    public static String createPaymentLink(String refNumber, String paymentType, int amountInCentavos) throws Exception {
 
        org.json.JSONArray lineItems = new org.json.JSONArray();
        JSONObject item = new JSONObject();
        item.put("name", paymentType);
        item.put("amount", amountInCentavos);
        item.put("currency", "PHP");
        item.put("quantity", 1);
        lineItems.put(item);
 
        org.json.JSONArray paymentMethods = new org.json.JSONArray();
        paymentMethods.put("card");
        paymentMethods.put("gcash");
        paymentMethods.put("grab_pay");
 
        JSONObject metadata = new JSONObject();
        metadata.put("ref_number", refNumber);
 
        JSONObject attributes = new JSONObject();
        attributes.put("line_items", lineItems);
        attributes.put("payment_method_types", paymentMethods);
        attributes.put("description", paymentType + " - " + refNumber);
        attributes.put("metadata", metadata);
 
        JSONObject data = new JSONObject();
        data.put("attributes", attributes);
        JSONObject body = new JSONObject();
        body.put("data", data);
 
        RequestBody requestBody = RequestBody.create(body.toString(), JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "/checkout_sessions")
                .post(requestBody)
                .addHeader("Authorization", getAuthHeader())
                .addHeader("Content-Type", "application/json")
                .build();
 
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            System.out.println("PayMongo Checkout Session response: " + responseBody);
 
            JSONObject json = new JSONObject(responseBody);
            if (!json.has("data")) {
                throw new Exception("PayMongo error: " + responseBody);
            }
 
            JSONObject dataObj = json.getJSONObject("data");
            JSONObject attr    = dataObj.getJSONObject("attributes");
            String checkoutUrl = attr.getString("checkout_url");
            String sessionId   = dataObj.getString("id");
 
            System.out.println("✅ Checkout URL: " + checkoutUrl);
            System.out.println("✅ Session ID:   " + sessionId);
 
            return checkoutUrl + "|" + sessionId;
        }
    }
 
    // Detects ID type and checks the right endpoint:
    //   cs_xxxx  → /checkout_sessions  → reads "payment_status"
    //   anything else → /links         → reads "status"
    // Always returns "succeeded" if paid, raw value otherwise.
    public static String checkPaymentStatus(String sessionId) throws Exception {
    if (sessionId == null || sessionId.isEmpty()) {
        return "pending";
    }

    Request request = new Request.Builder()
            .url(BASE_URL + "/checkout_sessions/" + sessionId)
            .get()
            .addHeader("Authorization", getAuthHeader())
            .addHeader("Content-Type", "application/json")
            .build();

    try (Response response = client.newCall(request).execute()) {
        String responseBody = response.body().string();
        System.out.println("📥 [FULL RESPONSE] " + responseBody);

        JSONObject json = new JSONObject(responseBody);
        if (!json.has("data")) {
            throw new Exception("PayMongo error: " + responseBody);
        }

        JSONObject attr = json.getJSONObject("data").getJSONObject("attributes");

        // Log ALL the important fields
        System.out.println("📋 [SESSION ATTRS] status: " + attr.optString("status", "N/A"));
        System.out.println("📋 [SESSION ATTRS] payment_status: " + attr.optString("payment_status", "N/A"));
        
        // Check if payment_intent exists
        if (attr.has("payment_intent")) {
            JSONObject paymentIntent = attr.getJSONObject("payment_intent");
            System.out.println("📋 [PAYMENT_INTENT] type: " + paymentIntent.optString("type", "N/A"));
            
            if (paymentIntent.has("attributes")) {
                JSONObject piAttr = paymentIntent.getJSONObject("attributes");
                String piStatus = piAttr.optString("status", "N/A");
                System.out.println("💳 [PAYMENT_INTENT STATUS] " + piStatus);
                
                // Log payments array too
                if (piAttr.has("payments")) {
                    System.out.println("💳 [PAYMENTS ARRAY] " + piAttr.get("payments"));
                }
                
                if ("succeeded".equals(piStatus)) {
                    System.out.println("✅ PAYMENT SUCCEEDED!");
                    return "paid";
                }
            }
        }

        // Fallback
        String paymentStatus = attr.optString("payment_status", "pending");
        System.out.println("💳 [FALLBACK] payment_status: " + paymentStatus);
        return paymentStatus;
    }
}
    // Returns the checkout URL for any payment ID (link or checkout session)
    public static String getCheckoutUrl(String paymentId) throws Exception {
        if (paymentId == null || paymentId.isEmpty()) return "";
 
        boolean isCheckoutSession = paymentId.startsWith("cs_");
        String url = isCheckoutSession
            ? BASE_URL + "/checkout_sessions/" + paymentId
            : BASE_URL + "/links/" + paymentId;
 
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", getAuthHeader())
                .addHeader("Content-Type", "application/json")
                .build();
 
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);
            if (!json.has("data")) return "";
 
            JSONObject attr = json.getJSONObject("data").getJSONObject("attributes");
 
            // checkout_url exists on both link and checkout session responses
            return attr.optString("checkout_url", "");
        }
    }
}
 