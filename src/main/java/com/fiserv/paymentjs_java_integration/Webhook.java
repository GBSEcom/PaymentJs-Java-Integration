package com.fiserv.paymentjs_java_integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class Webhook {

    /**
     *
     * Create new daily log if it does not already exist.
     * Write clientToken and json payload to payment log.
     *
     * @param file_path Path to payment log directory
     * @param client_token clientToken
     * @param body Json Payload from webhook
     */
    private boolean writeToLog(String file_path, String client_token, String body) {

        try {
            //Create new daily log file if not exists
            String date = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
            File log_file = new File(file_path + "/" + date + ".log");
            if (log_file.createNewFile()) {
                System.out.println("File created: " + log_file.getName());
            }

            //write clientToken and jso payload to daily log file
            String jsonString = "{\"Client-Token\":" + client_token + ",\"Payload\":" + body + "}";

            FileWriter wr = new FileWriter(log_file, true);
            wr.write(jsonString + "\n");
            wr.close();
            return true;
        } catch (Exception e){
            return false;
        }
    }

    /**
     *
     * Get payment-log file path. Write clientToken and json payload to payment log.
     *
     * @param client_token clientToken
     * @param body Json Payload from webhook
     * @throws IOException config.xml already validated in Auth.java
     */
    public ResponseEntity<?> exe(String client_token, String body) throws IOException {
        //Load credentials from config.xml
        HashMap<String, JsonNode> credentials = new ConfigLoader().loadCredentials();

        if(this.writeToLog(credentials.get("payment_log_filepath").asText(), client_token, body)){
            System.out.println("Webhook endpoint processing success! Client token and payment details written to log.");
            return ResponseEntity.ok("200");
        } else {
            System.out.println("Webhook endpoint processing failed");
            return null;
        }
    }
}
