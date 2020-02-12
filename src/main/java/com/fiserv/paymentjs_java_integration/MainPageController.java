package com.fiserv.paymentjs_java_integration;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Controller
public class MainPageController {

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping(value = "/auth", method = {RequestMethod.POST})
    public ResponseEntity<?> auth() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        return new Auth().exe();
    }
}