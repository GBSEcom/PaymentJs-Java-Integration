package com.fiserv.paymentjs_java_integration;

import org.json.JSONException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
public class MainPageController {

    /**
     *
     * Load template on localhost:7000 or otherwise specified host
     *
     * @return Main Page
     */
    @RequestMapping("/")
    public String index() {
        return "index";
    }

    /**
     * Execute Auth.exe()
     *
     * @return ResponseEntity 200 from payment.js server | null
     * @throws IOException File not found (config.xml)
     * @throws JSONException Malformed Json
     */
    @RequestMapping(value = "/auth", method = {RequestMethod.POST})
    public ResponseEntity<?> auth() throws IOException, JSONException {
        return new Auth().exe();
    }

    /**
     * Get Json response from webhook. Post client token + payload to log file.
     *
     * @return Refresh homepage
     * @throws IOException config.xml already validated in Auth.java
     */
    @RequestMapping(value = "/webhook", method = {RequestMethod.POST})
    public String webhook(@RequestHeader(value = "Client-Token") String client_token, @RequestBody String body) throws IOException {
        Webhook webhook = new Webhook();
        webhook.exe(client_token,body);
        return "index";
    }
}