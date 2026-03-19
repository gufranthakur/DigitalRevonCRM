package com.prototype.controller;

import com.prototype.model.Client;
import com.prototype.service.ClientService;
import com.prototype.service.MetaAdsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MetaSyncController {

    @Autowired
    private MetaAdsService metaAdsService;

    @Autowired
    private ClientService clientService;

    @PostMapping("/clients/{id}/sync-meta")
    public String syncMeta(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Client client = clientService.getById(id);
            metaAdsService.syncCampaigns(client);
            redirectAttributes.addFlashAttribute("syncSuccess", "Meta Ads synced successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("syncError", "Sync failed: " + e.getMessage());
        }
        return "redirect:/clients/" + id;
    }
}