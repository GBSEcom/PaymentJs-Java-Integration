package com.fiserv.paymentjs_java_integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigLoader {

    private JsonNode loadConfig() throws IOException {
        ObjectMapper objectMapper = new XmlMapper();
        return objectMapper.readTree(ResourceUtils.getFile("classpath:config.xml"));
    }

    public HashMap<String, JsonNode> loadCredentials() throws IOException {

        JsonNode credentials = this.loadConfig();
        HashMap<String, JsonNode> map = new HashMap<String, JsonNode>();

        JsonNode current_gateway = credentials.findValue("current_gateway");

        map.put("payment_log_filepath", credentials.findValue("file_path"));
        map.put("gateway", current_gateway);
        map.put("service_url", credentials.findValue("service_url"));
        map.put("pjsv2_credentials", credentials.findValue("credentials"));
        map.put("gateway_credentials", credentials.findValue(current_gateway.asText()));

        return map;
    }

    public String validateCredentials(HashMap<String, JsonNode> map){
        for (Map.Entry<String, JsonNode> entry : map.entrySet()) {
            if (null == entry.getValue()){
                return entry.getKey();
            }
        }

        File payment_log = new File(map.get("payment_log_filepath").asText());
        if (!payment_log.exists()){
            return "Log file \""+payment_log.toString()+"\" not found.";
        }

        return "200";
    }
}
