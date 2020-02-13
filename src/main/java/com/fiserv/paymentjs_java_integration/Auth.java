package com.fiserv.paymentjs_java_integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.ResponseEntity;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Auth {

    private static final String GATEWAY_AND_HOST_CREDENTIALS_FILE_PATH = "src/main/config.xml";

    private JsonNode loadConfig() throws IOException {
        ObjectMapper objectMapper = new XmlMapper();
        return objectMapper.readTree(new File(GATEWAY_AND_HOST_CREDENTIALS_FILE_PATH));
    }

    private HashMap<String, JsonNode> loadCredentials() throws IOException {

        JsonNode credentials = this.loadConfig();
        HashMap<String, JsonNode> map = new HashMap<String, JsonNode>();

        JsonNode current_gateway = credentials.findValue("current_gateway");
        map.put("gateway", current_gateway);
        //map.put("host", credentials.findValue("host_name"));
        map.put("service_url", credentials.findValue("service_url"));
        map.put("pjsv2_credentials", credentials.findValue("credentials"));
        map.put("gateway_credentials", credentials.findValue(current_gateway.asText()));

        return map;
    }

    private String validateCredentials(HashMap<String, JsonNode> map){
        for (Map.Entry<String, JsonNode> entry : map.entrySet()) {
            if (null == entry.getValue()){
                return entry.getKey();
            }
        }
        return "Ok";
    }

    public String genHmac(String msg, String secret) {
        HmacAlgorithms algorithm = HmacAlgorithms.HMAC_SHA_256;
        HmacUtils hmacUtils = new HmacUtils(algorithm, secret);
        Hex hexEncoder = new Hex();

        byte[] binaryEncodedHash = hmacUtils.hmac(msg);
        byte[] hexEncodedHash = hexEncoder.encode(binaryEncodedHash);
        return Base64.encodeBase64String(hexEncodedHash);
    }

    private HashMap<String, String> prepareHeaders(HashMap<String, JsonNode> credentials) throws InvalidKeyException, NoSuchAlgorithmException, IOException {
        HashMap<String, String> map = new HashMap<String, String>();

        //Json Payload
        JsonNode gateway_credentials = credentials.get("gateway_credentials");

        long timestamp = System.currentTimeMillis();
        long nonce = timestamp + new Random().nextInt();

        JsonNode pjsv2_credentials = credentials.get("pjsv2_credentials");
        String api_key = pjsv2_credentials.findValue("api_key").asText();
        String api_secret_key = pjsv2_credentials.findValue("api_secret").asText();

        //message components
        String message = api_key + nonce + timestamp + gateway_credentials.toString();

        String message_signature = this.genHmac(message, api_secret_key);

        map.put("Api-Key", api_key);
        map.put("Content-Type", "application/json");
        map.put("Content-Length", Integer.toString(gateway_credentials.asText().length()));
        map.put("Message-Signature", message_signature);
        map.put("Nonce", Long.toString(nonce));
        map.put("Timestamp", Long.toString(timestamp));
        return map;

    }

    public HttpURLConnection post(HashMap<String, JsonNode> credentials) throws NoSuchAlgorithmException, InvalidKeyException, IOException {

        //API service URL
        String service_url = credentials.get("service_url").asText();

        //Json Payload
        JsonNode gateway_credentials = credentials.get("gateway_credentials");

        HashMap<String, String> headers = this.prepareHeaders(credentials);

        HttpURLConnection connection = (HttpURLConnection) new URL(service_url).openConnection();
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty( "charset", "utf-8");
        headers.forEach(connection::setRequestProperty);

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());

        wr.write(gateway_credentials.toString().getBytes());
        return connection;
    }

    public ResponseEntity<String> exe() throws IOException, InvalidKeyException, NoSuchAlgorithmException {

        //Load credentials from config.xml
        HashMap<String, JsonNode> credentials = this.loadCredentials();

        //Validate credentials
        String validation_response = this.validateCredentials(credentials);
        if(!"Ok".equals(validation_response)){
            System.out.println("Invalid credentials setup. Info: "+validation_response);
            return null;
        }
        //Send HTTP post request to payment.js server and check response
        HttpURLConnection postResponse = this.post(credentials);
        if (200 != postResponse.getResponseCode()){
            System.out.println("HTTP post request failed. Info: "+postResponse.getResponseCode()+": "+postResponse.getResponseMessage());
            return null;
        }

        //Get response headers from Payment.js server
        Map<String, List<String>> response_headers = postResponse.getHeaderFields();

        return ResponseEntity.ok("{\"response\": \"It works \"}");
    }

}
