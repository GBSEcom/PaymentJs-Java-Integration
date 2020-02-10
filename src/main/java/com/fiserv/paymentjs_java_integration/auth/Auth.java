package com.fiserv.paymentjs_java_integration.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Auth {

    private static final String GATEWAY_AND_HOST_CREDENTIALS_FILE_PATH = "src/main/config.xml";

    private JsonNode loadConfig() throws IOException {
        ObjectMapper objectMapper = new XmlMapper();
        return objectMapper.readTree(new File(GATEWAY_AND_HOST_CREDENTIALS_FILE_PATH));
    }

    private void loadCredentials() throws IOException {
        JsonNode config = this.loadConfig();

        String host_name = config.findValue("host_name").toString();

        String psjv2_api_key = config.findValue("pjsv2_api_key").toString();
        String psjv2_secret_key = config.findValue("pjsv2_api_secret").toString();

        String current_gateway = config.findValue("current_gateway").toString();

        switch (current_gateway){
            case "payeezy" :

                break;
            case "bluepay":

                break;
            case "card_connect":

                break;
            case "ipg":

                break;

            default:
                //error;
                break;
        }

    }


    public ResponseEntity<String> exe() throws IOException {

       loadCredentials();

        return ResponseEntity.ok("{\"response\": \"It works \"}");
    }

}
