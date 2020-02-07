package com.fiserv.paymentjs_java_integration.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class MainPageController {

    @RequestMapping("/")
    public String index() {
        return "index";
    }
}