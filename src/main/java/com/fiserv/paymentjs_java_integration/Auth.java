package com.fiserv.paymentjs_java_integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class Auth {

    private static final String PAYMENT_LOG_FILE_PATH = "src/main/payment-log";

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

    private String genHmac(String msg, String secret) {
        HmacAlgorithms algorithm = HmacAlgorithms.HMAC_SHA_256;
        HmacUtils hmacUtils = new HmacUtils(algorithm, secret);
        Hex hexEncoder = new Hex();

        byte[] binaryEncodedHash = hmacUtils.hmac(msg);
        byte[] hexEncodedHash = hexEncoder.encode(binaryEncodedHash);
        return Base64.encodeBase64String(hexEncodedHash);
    }

    private String stripBooleanQuotes(String input){
        return input.replaceAll("\"(?i)true\"","true").replaceAll("\"(?i)false\"","false");
    }

    private HashMap<String, String> prepareHeaders(HashMap<String, JsonNode> credentials) {
        HashMap<String, String> map = new HashMap<String, String>();

        //Json Payload
        String gateway_credentials = this.stripBooleanQuotes(credentials.get("gateway_credentials").toString());

        long timestamp = this.getTimestamp();
        long nonce = this.getNonce();

        JsonNode pjsv2_credentials = credentials.get("pjsv2_credentials");
        String api_key = pjsv2_credentials.findValue("api_key").asText();
        String api_secret_key = pjsv2_credentials.findValue("api_secret").asText();

        //message components
        String message = api_key + nonce + timestamp + gateway_credentials;

        String message_signature = this.genHmac(message, api_secret_key);

        map.put("Api-Key", api_key);
        map.put("Content-Type", "application/json");
        map.put("Content-Length", Integer.toString(gateway_credentials.length()));
        map.put("Message-Signature", message_signature);
        map.put("Nonce", Long.toString(nonce));
        map.put("Timestamp", Long.toString(timestamp));
        return map;

    }

    public HttpURLConnection post(HashMap<String, JsonNode> credentials) throws IOException {

        //API service URL
        String service_url = credentials.get("service_url").asText();

        //Json Payload
        String gateway_credentials = this.stripBooleanQuotes(credentials.get("gateway_credentials").toString());

        HashMap<String, String> headers = this.prepareHeaders(credentials);

        HttpURLConnection connection = (HttpURLConnection) new URL(service_url).openConnection();
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty( "charset", "utf-8");
        headers.forEach(connection::setRequestProperty);

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());

        wr.write(gateway_credentials.getBytes());
        return connection;
    }

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

    private void writeToLog(String callback_data) throws IOException {
        String date = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        File log_file = new File(PAYMENT_LOG_FILE_PATH+"/"+date+".log");
        if (log_file.createNewFile()) {
            System.out.println("File created: " + log_file.getName());
        }

        FileWriter wr = new FileWriter(log_file, true);
        wr.write(callback_data+"\n");
        wr.close();
    }

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

        //Write client token to log file
        String callback_data = this.getCallBackData(connection).toString();
        this.writeToLog(callback_data);

        connection.disconnect();

        //Return client token and publicKeyBase64 public rsa key view callback
        return ResponseEntity.ok(callback_data);
    }
}
