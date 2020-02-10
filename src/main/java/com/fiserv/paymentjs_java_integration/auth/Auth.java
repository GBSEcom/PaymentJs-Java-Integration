package com.fiserv.paymentjs_java_integration.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;

public class Auth {

    private static final String GATEWAY_AND_HOST_CREDENTIALS_FILE_PATH = "src/main/config.xml";

    private JsonNode loadConfig() throws IOException {
        ObjectMapper objectMapper = new XmlMapper();
        return objectMapper.readTree(new File(GATEWAY_AND_HOST_CREDENTIALS_FILE_PATH));
    }


    public ResponseEntity<String> exe() throws IOException {

        JsonNode config = this.loadConfig();
        System.out.println(config.toPrettyString());

        return ResponseEntity.ok("{\"response\": \"It works \"}");
    }

}
