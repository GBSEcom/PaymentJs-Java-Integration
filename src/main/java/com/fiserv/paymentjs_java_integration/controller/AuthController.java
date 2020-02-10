package com.fiserv.paymentjs_java_integration.controller;

import org.springframework.http.ResponseEntity;

public class AuthController {

    public ResponseEntity<String> exe(){
        return ResponseEntity.ok("{\"response\": \"It works \"}");
    }

}
