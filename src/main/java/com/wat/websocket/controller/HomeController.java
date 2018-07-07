package com.wat.websocket.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${socket.url}")
    String socket;

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
    public ModelAndView  toHome() {
        ModelAndView view = new ModelAndView("/view/index");
        return view;
    }

    @RequestMapping(value = "/toSocket", method = RequestMethod.POST)
    public ModelAndView toSocket(@RequestParam(value = "uid") Long uid, @RequestParam(value = "userName") String userName, Model model) {
        model.addAttribute("uid", uid);
        model.addAttribute("userName", userName);
        model.addAttribute("socket", socket);
        logger.info("uid : " + uid + ", userName :" + userName );
        ModelAndView  redirectView = new ModelAndView ("/view/socket");
        return redirectView;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String toHome(Model model) {
        return "view/thymeleaf";
    }
}
