package com.prototype.service;


import com.prototype.model.Campaign;
import com.prototype.model.Client;
import com.prototype.repository.CampaignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class MetaAdsService {

    @Value("${meta.access.token}")
    private String accessToken;

    private static final String API_VERSION = "v19.0";
    private static final String BASE_URL = "https://graph.facebook.com/" + API_VERSION;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CampaignRepository campaignRepository;

    /**
     * Fetches last 30 days of campaign insights from Meta for the given client
     * and upserts them into the campaigns table.
     */
    public void syncCampaigns(Client client) throws Exception {
        String adAccountId = client.getMetaAdAccountId();
        if (adAccountId == null || adAccountId.isBlank()) {
            throw new IllegalArgumentException("Client does not have a Meta Ad Account ID configured.");
        }

        String url = BASE_URL + "/act_" + adAccountId + "/insights"
                + "?fields=campaign_name,campaign_id,spend,impressions,clicks,actions,cost_per_action_type,date_start,date_stop"
                + "&level=campaign"
                + "&date_preset=last_30d"
                + "&access_token=" + accessToken;

        String response = restTemplate.getForObject(url, String.class);
        JsonNode root = objectMapper.readTree(response);
        JsonNode data = root.get("data");

        if (data == null || !data.isArray()) {
            throw new RuntimeException("Unexpected response from Meta API: " + response);
        }

        for (JsonNode insight : data) {
            String metaCampaignId = insight.path("campaign_id").asText();
            String campaignName   = insight.path("campaign_name").asText();
            double spend          = insight.path("spend").asDouble(0);
            long impressions      = insight.path("impressions").asLong(0);
            long clicks           = insight.path("clicks").asLong(0);
            String dateStart      = insight.path("date_start").asText();
            String dateStop       = insight.path("date_stop").asText();

            // Extract leads from actions array
            int leads = 0;
            JsonNode actions = insight.get("actions");
            if (actions != null && actions.isArray()) {
                for (JsonNode action : actions) {
                    if ("lead".equals(action.path("action_type").asText())) {
                        leads = action.path("value").asInt(0);
                    }
                }
            }

            // Extract CPL from cost_per_action_type array
            double cpl = leads > 0 ? spend / leads : 0;
            JsonNode costActions = insight.get("cost_per_action_type");
            if (costActions != null && costActions.isArray()) {
                for (JsonNode ca : costActions) {
                    if ("lead".equals(ca.path("action_type").asText())) {
                        cpl = ca.path("value").asDouble(0);
                    }
                }
            }

            // Upsert — find existing by metaCampaignId or create new
            Optional<Campaign> existing = campaignRepository.findByMetaCampaignId(metaCampaignId);
            Campaign campaign = existing.orElse(new Campaign());

            campaign.setMetaCampaignId(metaCampaignId);
            campaign.setName(campaignName);
            campaign.setAdSpend(spend);
            campaign.setLeadsGenerated(leads);
            campaign.setCpl(cpl);
            campaign.setImpressions(impressions);
            campaign.setClicks(clicks);
            campaign.setClient(client);

            if (!dateStart.isBlank()) campaign.setStartDate(LocalDate.parse(dateStart));
            if (!dateStop.isBlank())  campaign.setEndDate(LocalDate.parse(dateStop));

            campaignRepository.save(campaign);
        }
    }
}