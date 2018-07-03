package com.wat.websocket.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 */
@Controller
@RequestMapping(value = "/home")
public class HomeController {

   static Logger logger = LoggerFactory.getLogger(HomeController.class);

    /**
     * @methdName: toHome
     * @param: [uid]
     * @return: java.lang.String
     * @Description: TODO
     * @author: chuangwang8
     * @date: 2018-07-03
     * @version: V1.0
     */
    @RequestMapping(value = "/toHome", method = RequestMethod.GET)
    public String toHome(@RequestParam(value = "uid") Long uid, Model model) {
        logger.info("uid :" + uid);
        model.addAttribute("uid",uid) ;
        model.addAttribute("uid1",uid);
        logger.info("123123");
        return "/view/index";
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String toHome(Model model) {
        return "view/thymeleaf";
    }
}
