package com.cat.fsai.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cat.fsai.cc.huobi.HuobiMarket;

@RestController
public class HelloController {
	
	@Autowired
	HuobiMarket market;

    @RequestMapping("/")
    public String index() throws Exception {
        return "Greetings from Spring Boot!";
    }

}