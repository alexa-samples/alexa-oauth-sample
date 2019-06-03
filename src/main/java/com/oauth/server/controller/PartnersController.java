/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Licensed under the Amazon Software License
 * http://aws.amazon.com/asl/
 */
package com.oauth.server.controller;

import com.oauth.server.controller.editor.SplitCollectionEditor;
import com.oauth.server.dto.OAuthPartner;
import com.oauth.server.dao.DynamoDBPartnerDetailsDAO;
import java.util.Collection;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * MVC Controller for {@link OAuthPartner} add/edit/delete.
 *
 * @author Lucun Cai
 */
@Controller
@RequestMapping("partners")
@Log4j2
public class PartnersController {

    @Autowired
    private DynamoDBPartnerDetailsDAO partnerDetailsRepository;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Collection.class, new SplitCollectionEditor(Set.class, ","));
    }

    @RequestMapping(value = "/form", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String showEditForm(@RequestParam(value = "partnerId", required = false) String partnerId, Model model) {

        OAuthPartner partner;
        if (partnerId != null) {
            partner = partnerDetailsRepository.loadPartnerByPartnerId(partnerId);
        } else {
            partner = OAuthPartner.builder().build();
        }

        model.addAttribute("partner", partner);
        return "partnerForm";
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String editPartner(@ModelAttribute OAuthPartner partner) {
        partnerDetailsRepository.savePartner(partner);
        return "redirect:/";
    }

    @RequestMapping(value = "{partner.partnerId}/delete")
    public String deletePartner(@PathVariable("partner.partnerId") String partnerId) {
        partnerDetailsRepository.deletePartnerByPartnerId(partnerId);
        return "redirect:/";
    }
}