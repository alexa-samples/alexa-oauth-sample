/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Licensed under the Amazon Software License
 * http://aws.amazon.com/asl/
 */
package com.oauth.server.controller;

import static java.util.Arrays.asList;

import com.oauth.server.authentication.RoleEnum;
import com.oauth.server.dao.DynamoDBPartnerDetailsDAO;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.ClientRegistrationService;
import org.springframework.security.oauth2.provider.approval.Approval;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for login, logout, and approval management.
 *
 * @author Lucun Cai
 */
@Controller
public class OAuthManagementController {

    @Autowired
    private ClientRegistrationService clientRegistrationService;

    @Autowired
    private DynamoDBPartnerDetailsDAO partnerDetailsService;

    @Autowired
    private ApprovalStore approvalStore;

    @Autowired
    private TokenStore tokenStore;

    @RequestMapping("/")
    public ModelAndView root(HttpServletRequest request, Map<String, Object> model, Principal principal) {

        if (request.isUserInRole(RoleEnum.ROLE_USER_ADMIN.name())) {
            model.put("clientDetails", clientRegistrationService.listClientDetails());
            model.put("partners", partnerDetailsService.listPartners());
        } else {
            List<Approval> approvals = clientRegistrationService.listClientDetails().stream()
                .map(clientDetails -> approvalStore.getApprovals(principal.getName(), clientDetails.getClientId()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
            model.put("approvals", approvals);
        }

        return new ModelAndView("index", model);
    }

    /**
     * Method to revoke the OAuth approval.
     */
    @RequestMapping(value = "/approval/revoke", method = RequestMethod.POST)
    public String revokeApproval(@ModelAttribute Approval approval) {

        approvalStore.revokeApprovals(asList(approval));
        tokenStore.findTokensByClientIdAndUserName(approval.getClientId(), approval.getUserId())
            .forEach(tokenStore::removeAccessToken);
        return "redirect:/";
    }

    @RequestMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * Method to logout customer.
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login?logout";
    }
}
