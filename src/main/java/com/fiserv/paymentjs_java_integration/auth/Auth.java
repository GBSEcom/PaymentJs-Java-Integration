package com.fiserv.paymentjs_java_integration.auth;

import org.springframework.http.ResponseEntity;

public class Auth {

    public ResponseEntity<String> exe(){
        return ResponseEntity.ok("{\"response\": \"It works \"}");
    }

}
