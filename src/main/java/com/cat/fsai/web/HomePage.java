package com.cat.fsai.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomePage {
	
	
	
	@RequestMapping("/home")
	public String home(Model model){
		model.addAttribute("name", "wangbo");
		return "home";
	}
}
