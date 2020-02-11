package com.fiserv.paymentjs_java_integration.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
        map.put("pjsv2_credentials", config.findValue("credentials"));
        map.put("gateway_config", config.findValue(current_gateway.toString().replaceAll("\"","")));

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

    public ResponseEntity<String> exe() throws IOException {

        HashMap<String, JsonNode> config = this.loadCredentials();
        String validation_response = this.validateCredentials(config);

        if(!"Ok".equals(validation_response)){
            System.out.println("Invalid credentials setup. Info: "+validation_response);
        }

        return ResponseEntity.ok("{\"response\": \"It works \"}");
    }

}
