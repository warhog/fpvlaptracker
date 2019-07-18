package de.warhog.fpvlaptracker.controllers;

import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RedirectController {

    private static final Logger LOG = LoggerFactory.getLogger(RedirectController.class);

    @RequestMapping(value = "/{[path:[^\\.]*}")
    public String redirect() {
        LOG.debug("catching redirect");
        return "forward:/";
    }

    @RequestMapping("/user")
    @ResponseBody
    public Principal user(Principal user) {
        return user;
    }

}
