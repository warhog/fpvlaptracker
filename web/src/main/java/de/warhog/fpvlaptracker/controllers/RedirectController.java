package de.warhog.fpvlaptracker.controllers;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RedirectController {

    private static final Logger LOG = LoggerFactory.getLogger(RedirectController.class);

//    @RequestMapping(value = "/{[path:[^\\.]*}")
    // exclude /websocketEndpoint
//    @RequestMapping(value = "/{[path:^(?:(?!websocketEndpoint).)*}")
    @RequestMapping(value = {"/home", "/login", "/nodesetup/**", "/scan", "/settings", "/pilots", "/nodes", "/race"})
    public String redirect(HttpServletRequest request) {
        LOG.debug("catching redirect: " + request.getRequestURI());
        return "forward:/";
    }

}
