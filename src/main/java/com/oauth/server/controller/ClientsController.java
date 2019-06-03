/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Licensed under the Amazon Software License
 * http://aws.amazon.com/asl/
 */
package com.oauth.server.controller;

import com.oauth.server.controller.editor.AuthorityPropertyEditor;
import com.oauth.server.controller.editor.SplitCollectionEditor;
import com.oauth.server.dao.DynamoDBClientDetailsDAO;
import java.util.Collection;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
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
 * MVC Controller for OAuth {@link ClientDetails} add/edit/delete.
 *
 * @author Lucun Cai
 */
@Controller
@RequestMapping("clients")
public class ClientsController {

    @Autowired
    private DynamoDBClientDetailsDAO clientsDetailsService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Collection.class, new SplitCollectionEditor(Set.class, ","));
        binder.registerCustomEditor(GrantedAuthority.class, new AuthorityPropertyEditor());
    }

    @RequestMapping(value = "/form", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String showEditForm(@RequestParam(value = "client", required = false) String clientId, Model model) {

        ClientDetails clientDetails;
        if (clientId != null) {
            clientDetails = clientsDetailsService.loadClientByClientId(clientId);
        } else {
            clientDetails = new BaseClientDetails();
        }

        model.addAttribute("clientDetails", clientDetails);
        return "clientForm";
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String editClient(
        @ModelAttribute BaseClientDetails clientDetails) {

        clientsDetailsService.addOrUpdateClientDetails(clientDetails);

        if (!clientDetails.getClientSecret().isEmpty()) {
            clientsDetailsService.updateClientSecret(clientDetails.getClientId(), clientDetails.getClientSecret());
        }
        return "redirect:/";
    }

    @RequestMapping(value = "{client.clientId}/delete")
    public String deleteClient(@PathVariable("client.clientId") String id) {
        clientsDetailsService.removeClientDetails(clientsDetailsService.loadClientByClientId(id).toString());
        return "redirect:/";
    }
}