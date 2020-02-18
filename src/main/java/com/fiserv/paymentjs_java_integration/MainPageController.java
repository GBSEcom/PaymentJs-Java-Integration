package com.fiserv.paymentjs_java_integration;

import org.json.JSONException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;

@Controller
public class MainPageController {

    /**
     *
     * Load template on localhost:8080
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
}