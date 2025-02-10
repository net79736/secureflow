package com.tdd.secureflow.interfaces.api.controller.view;

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

    @RequestMapping("/user/user.do")
    public String userPage() {
        System.out.println("여기 유저 페이지로 이동");
        return "user/userPage";
    }

    @RequestMapping("/member/member.do")
    public String memberPage() {
        System.out.println("멤버 페이지 입니다.");
        return "member/memberPage";
    }

    @RequestMapping("/admin/admin.do")
    public String adminPage() {
        System.out.println("여기 관리자 페이지로 이동");
        return "admin/adminPage";
    }

}
