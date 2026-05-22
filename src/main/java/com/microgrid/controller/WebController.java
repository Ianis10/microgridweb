package com.microgrid.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String root(HttpSession session) {
        Object role = session.getAttribute("role");
        if (role == null) {
            return "redirect:/login";
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        Object role = session.getAttribute("role");
        if (role == null) {
            return "redirect:/login";
        }
        return "dashboard";
    }
}