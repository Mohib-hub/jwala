package com.cerner.jwala.web.controller;

import com.siemens.cto.security.saml.service.SamlIdentityProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Z003BPEJ on 6/18/14.
 */
@Controller
public class SamlController {

    @Autowired
    private SamlIdentityProviderService samlIdentityProviderService;
    
    // for test injection
    protected void setSamlIdentityProviderService(SamlIdentityProviderService samlIdentityProviderService) {
        this.samlIdentityProviderService = samlIdentityProviderService;
    }

    @RequestMapping(value = "/idp")
    public String idProvider(HttpServletRequest request, HttpServletResponse response) {
        samlIdentityProviderService.createSamlResponse(request, response);
        return "saml/post";
    }
}