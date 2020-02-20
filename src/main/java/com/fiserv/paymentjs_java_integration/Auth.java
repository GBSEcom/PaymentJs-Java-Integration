package com.fiserv.paymentjs_java_integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Auth {

    private long timestamp;
    private long nonce;

    private void setTimestampAndNonce(){
        this.timestamp = System.currentTimeMillis();
        this.nonce = this.getTimestamp() + new Random().nextInt();
    }

    private long getTimestamp(){
        return this.timestamp;
    }

    private long getNonce(){
        return this.nonce;
    }

    /**
     * Create message signature and create post request headers
     *
     * @param credentials HashMap of credentials
     * @return Headers paired with appropriate Json strings
     */
    private HashMap<String, String> prepareHeaders(HashMap<String, JsonNode> credentials) {
        HashMap<String, String> map = new HashMap<String, String>();

        //Json Payload
        String gateway_credentials = StringUtil.formatBooleans(credentials.get("gateway_credentials").toString());

        long timestamp = this.getTimestamp();
        long nonce = this.getNonce();

        JsonNode pjsv2_credentials = credentials.get("pjsv2_credentials");
        String api_key = pjsv2_credentials.findValue("api_key").asText();
        String api_secret_key = pjsv2_credentials.findValue("api_secret").asText();

        //message components
        String message = api_key + nonce + timestamp + gateway_credentials;

        //generate hmac
        String message_signature = StringUtil.genHmac(message, api_secret_key);

        map.put("Api-Key", api_key);
        map.put("Content-Type", "application/json");
        map.put("Content-Length", Integer.toString(gateway_credentials.length()));
        map.put("Message-Signature", message_signature);
        map.put("Nonce", Long.toString(nonce));
        map.put("Timestamp", Long.toString(timestamp));
        return map;
    }

    /**
     * Connect to Payment.js server, set headers, and post json payload
     *
     * @param credentials HashMap of credentials
     * @return Connection to Payment.js server
     * @throws IOException Post request failed
     */
    public HttpURLConnection post(HashMap<String, JsonNode> credentials) throws IOException {

        //API service URL
        String service_url = credentials.get("service_url").asText();

        //Json Payload
        String gateway_credentials = StringUtil.formatBooleans(credentials.get("gateway_credentials").toString());

        HashMap<String, String> headers = this.prepareHeaders(credentials);

        HttpURLConnection connection = (HttpURLConnection) new URL(service_url).openConnection();
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty( "charset", "utf-8");

        //set headers paired with Json string
        headers.forEach(connection::setRequestProperty);

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());

        wr.write(gateway_credentials.getBytes());
        return connection;
    }

    /**
     * Receive clientToken and publicKeyBase64 rsa key from Payment.js server
     *
     * @param connection Connection to Payment.js server
     * @return JsonObject containing clientToken and publicKeyBase64 rsa key
     * @throws IOException Request failed
     * @throws JSONException Invalid Json string
     */
    private JSONObject getCallBackData(HttpURLConnection connection) throws IOException, JSONException {

        //Get client token from response header
        String client_token = connection.getHeaderField("Client-Token");

        //Parse response body for publicKetBase64 value
        Map<String, List<String>> map = connection.getHeaderFields();
        String json_response = "";
        InputStreamReader in = new InputStreamReader(connection.getInputStream());
        BufferedReader br = new BufferedReader(in);
        String text = "";
        while ((text = br.readLine()) != null) {
            json_response += text;
        }
        String publicKeyBase64 = new JSONObject(json_response).getString("publicKeyBase64");

        HashMap<String, String> response_map = new HashMap<String, String>();
        response_map.put("clientToken", client_token);
        response_map.put("publicKeyBase64", publicKeyBase64);

        return new JSONObject(response_map);
    }

    /**
     * Execute Payment.js process flow
     *
     * @return ResponseEntity.ok() | null
     * @throws IOException File not found/Request failed
     * @throws JSONException Invalid Json string
     */
    public ResponseEntity<String> exe() throws IOException, JSONException {

        //Initialize timestamp and nonce values
        this.setTimestampAndNonce();

        //Load credentials from config.xml
        ConfigLoader config = new ConfigLoader();
        HashMap<String, JsonNode> credentials = config.loadCredentials();

        //Validate credentials
        String validation_response = config.validateCredentials(credentials);
        if(!"200".equals(validation_response)){
            System.out.println("Invalid credentials setup. Info: "+validation_response);
            return null;
        }

        //Send HTTP post request to payment.js server and check response
        HttpURLConnection connection = this.post(credentials);
        if (200 != connection.getResponseCode()){
            System.out.println("HTTP post request failed. Info: "+connection.getResponseCode()+": "+connection.getResponseMessage());
            return null;
        }
        System.out.println("HTTP post request Successful "+connection.getResponseCode()+": "+connection.getResponseMessage());

        //Nonce validation
        if (!String.valueOf(this.getNonce()).equals(connection.getHeaderField("Nonce"))){
            System.out.println("Nonce validation failed.");
            return null;
        }

        //Get callback data to return in response entity
        String callback_data = this.getCallBackData(connection).toString();

        //Disconnect and return callback data
        connection.disconnect();
        return ResponseEntity.ok(callback_data);
    }
}
