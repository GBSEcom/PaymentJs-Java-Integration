package com.fiserv.paymentjs_java_integration.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Auth {

    private static final String GATEWAY_AND_HOST_CREDENTIALS_FILE_PATH = "src/main/config.xml";

    private JsonNode loadConfig() throws IOException {
        ObjectMapper objectMapper = new XmlMapper();
        return objectMapper.readTree(new File(GATEWAY_AND_HOST_CREDENTIALS_FILE_PATH));
    }

    private HashMap<String, JsonNode> loadCredentials() throws IOException {

        JsonNode config = this.loadConfig();
        HashMap<String, JsonNode> map = new HashMap<String, JsonNode>();

        JsonNode current_gateway = config.findValue("current_gateway");
        map.put("gateway", current_gateway);
        map.put("host", config.findValue("host_name"));
        map.put("service_url", config.findValue("service_url"));
        map.put("pjsv2_credentials", config.findValue("credentials"));
        map.put("gateway_config", config.findValue(current_gateway.asText()));

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

    private String hash_hmac(String message, String api_secret_key) throws NoSuchAlgorithmException, InvalidKeyException {
        String hmac = "";

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret = new SecretKeySpec(api_secret_key.getBytes(), "HmacSHA1");
        mac.init(secret);
        byte[] digest = mac.doFinal(message.getBytes());
        BigInteger hash = new BigInteger(1, digest);
        hmac = hash.toString(16);

        if (hmac.length() % 2 != 0) {
            hmac = "0" + hmac;
        }

        return hmac;
    }

    private JSONObject prepareHeaders(HashMap<String, JsonNode> config) throws InvalidKeyException, NoSuchAlgorithmException, IOException {
        HashMap<String, String> map = new HashMap<String, String>();

        //Json Payload
        JsonNode gateway_config = config.get("gateway_config");

        long timestamp = System.currentTimeMillis() * 1000;
        long nonce = timestamp + new Random().nextInt();

        JsonNode pjsv2_credentials = config.get("pjsv2_credentials");
        String api_key = pjsv2_credentials.findValue("api_key").asText();
        String api_secret_key = pjsv2_credentials.findValue("api_secret").asText();

        //message components
        String message = api_key + nonce + timestamp + gateway_config.toPrettyString();
        String message_signature = Base64.getEncoder().encodeToString(this.hash_hmac(message, api_secret_key).getBytes());

        map.put("Api-Key", api_key);
        map.put("Content-Type", "application/json");
        map.put("Content-Length", Integer.toString(gateway_config.toString().length()));
        map.put("Message-Signature", message_signature);
        map.put("Nonce", Long.toString(nonce));
        map.put("Timestamp", Long.toString(timestamp));
        return new JSONObject(map);
    }

    public ResponseEntity<String> exe() throws IOException, InvalidKeyException, NoSuchAlgorithmException {

        HashMap<String, JsonNode> config = this.loadCredentials();
        String validation_response = this.validateCredentials(config);

        if(!"Ok".equals(validation_response)){

            //TODO: Throw error properly

            System.out.println("Invalid credentials setup. Info: "+validation_response);
        }

        //API service URL
        String service_url = config.get("service_url").asText();
        String host = config.get("host").toString();

        JSONObject headers = this.prepareHeaders(config);

        return ResponseEntity.ok("{\"response\": \"It works \"}");
    }

}
