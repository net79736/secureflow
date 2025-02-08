package com.tdd.secureflow.interfaces.api.controller.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class URIPatternController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = {"/", "/index"})
    public String handleUriTemplate() {
        return "main/index";
    }

    @RequestMapping("/login")
    public String login() {
        return "user/login";
    }

    @RequestMapping("/signup")
    public String signup() {
        return "user/signup";
    }


}
