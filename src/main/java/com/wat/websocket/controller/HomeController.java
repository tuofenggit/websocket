package com.wat.websocket.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

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
    public String toHome() {
        return "/view/index";
    }

    @RequestMapping(value = "/toSocket", method = RequestMethod.POST)
    public ModelAndView toSocket(@RequestParam(value = "uid") Long uid, @RequestParam(value = "userName") String userName, Model model) {
        model.addAttribute("uid", uid);
        model.addAttribute("userName", userName);
        logger.info("uid : " + uid + ", userName :" + userName );
        ModelAndView  redirectView = new ModelAndView ("/view/socket");
        return redirectView;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String toHome(Model model) {
        return "view/thymeleaf";
    }
}
