package com.prototype.controller;

import com.prototype.model.Campaign;
import com.prototype.model.Client;
import com.prototype.model.Invoice;
import com.prototype.repository.CampaignRepository;
import com.prototype.repository.ClientRepository;
import com.prototype.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        // --- Stat Cards ---
        List<Invoice> allInvoices = invoiceRepository.findAll();

        double totalRevenue = allInvoices.stream()
                .filter(i -> i.getStatus() == Invoice.Status.PAID)
                .mapToDouble(Invoice::getTotalAmount)
                .sum();

        double pendingPayments = allInvoices.stream()
                .filter(i -> i.getStatus() == Invoice.Status.UNPAID)
                .mapToDouble(Invoice::getTotalAmount)
                .sum();

        model.addAttribute("totalClients", clientRepository.count());
        model.addAttribute("activeClients", clientRepository.findByStatus(Client.Status.ACTIVE).size());
        model.addAttribute("totalRevenue", String.format("%.0f", totalRevenue));
        model.addAttribute("pendingPayments", String.format("%.0f", pendingPayments));

        // --- Last 6 months labels ---
        List<String> monthLabels = new ArrayList<>();
        List<LocalDate> monthStarts = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate month = today.minusMonths(i).withDayOfMonth(1);
            monthStarts.add(month);
            monthLabels.add(month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        }

        // --- Monthly Revenue (PAID invoices grouped by month) ---
        Map<String, Double> revenueByMonth = allInvoices.stream()
                .filter(i -> i.getStatus() == Invoice.Status.PAID && i.getIssueDate() != null)
                .collect(Collectors.groupingBy(
                        i -> i.getIssueDate().withDayOfMonth(1).toString(),
                        Collectors.summingDouble(Invoice::getTotalAmount)
                ));

        List<Double> revenueData = monthStarts.stream()
                .map(m -> revenueByMonth.getOrDefault(m.toString(), 0.0))
                .collect(Collectors.toList());

        // --- Monthly Leads (campaigns grouped by startDate month) ---
        List<Campaign> allCampaigns = campaignRepository.findAll();

        Map<String, Integer> leadsByMonth = allCampaigns.stream()
                .filter(c -> c.getStartDate() != null && c.getLeadsGenerated() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getStartDate().withDayOfMonth(1).toString(),
                        Collectors.summingInt(Campaign::getLeadsGenerated)
                ));

        List<Integer> leadsData = monthStarts.stream()
                .map(m -> leadsByMonth.getOrDefault(m.toString(), 0))
                .collect(Collectors.toList());

        model.addAttribute("monthLabels", monthLabels);
        model.addAttribute("revenueData", revenueData);
        model.addAttribute("leadsData", leadsData);

        return "dashboard";
    }
}