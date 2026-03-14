package com.mycompany.javasystem;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import java.util.Base64;

public class PayMongoService {

    private static final String SECRET_KEY = System.getenv("PAYMONGO_SECRET_KEY");
    private static final String BASE_URL = "https://api.paymongo.com/v1";
    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static String getAuthHeader() {
        String credentials = SECRET_KEY + ":";
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    public static String createPaymentLink(String refNumber, String paymentType, int amountInCentavos) throws Exception {
        JSONObject attributes = new JSONObject();
        attributes.put("amount", amountInCentavos);
        attributes.put("currency", "PHP");
        attributes.put("description", paymentType + " - " + refNumber);
        attributes.put("remarks", refNumber);

        JSONObject data = new JSONObject();
        data.put("attributes", attributes);

        JSONObject body = new JSONObject();
        body.put("data", data);

        RequestBody requestBody = RequestBody.create(body.toString(), JSON);

        Request request = new Request.Builder()
            .url(BASE_URL + "/links")
            .post(requestBody)
            .addHeader("Authorization", getAuthHeader())
            .addHeader("Content-Type", "application/json")
            .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);
            JSONObject dataObj = json.getJSONObject("data");
            JSONObject attr = dataObj.getJSONObject("attributes");
            return attr.getString("checkout_url") + "|" + dataObj.getString("id");
        }
    }

    public static String checkPaymentStatus(String linkId) throws Exception {
        Request request = new Request.Builder()
            .url(BASE_URL + "/links/" + linkId)
            .get()
            .addHeader("Authorization", getAuthHeader())
            .addHeader("Content-Type", "application/json")
            .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);
            JSONObject dataObj = json.getJSONObject("data");
            JSONObject attr = dataObj.getJSONObject("attributes");
            return attr.getString("status");
        }
    }
}