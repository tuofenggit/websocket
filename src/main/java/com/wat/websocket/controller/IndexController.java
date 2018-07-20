package com.wat.websocket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 主页
 */
@Controller
@RequestMapping("/")
public class IndexController {

    /**
     * 默认页面
     * @return
     */
    @RequestMapping("")
    public String toIndexHtml(){
        return "/view/index";
    }
}
