package com.fiserv.paymentjs_java_integration.controller;

import com.fiserv.paymentjs_java_integration.auth.Auth;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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