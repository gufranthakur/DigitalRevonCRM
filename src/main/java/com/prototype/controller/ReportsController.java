package com.prototype.controller;

import com.prototype.model.Campaign;
import com.prototype.model.Client;
import com.prototype.model.Invoice;
import com.prototype.repository.CampaignRepository;
import com.prototype.repository.ClientRepository;
import com.prototype.repository.InvoiceRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/reports")
public class ReportsController {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    // --- Reports Page ---
    @GetMapping
    public String reports(Model model) {
        model.addAttribute("rows", buildRows());
        return "reports";
    }

    // --- CSV Export ---
    @GetMapping("/export")
    public void exportCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"adcrm-report.csv\"");

        PrintWriter writer = response.getWriter();
        writer.println("Client,Project,Ad Spend (₹),Leads Generated,CPL (₹),Total Invoiced (₹),Total Paid (₹),Pending (₹)");

        for (ReportRow row : buildRows()) {
            writer.printf("%s,%s,%.2f,%d,%.2f,%.2f,%.2f,%.2f%n",
                    escapeCsv(row.clientName),
                    escapeCsv(row.projectName),
                    row.adSpend,
                    row.leadsGenerated,
                    row.cpl,
                    row.totalInvoiced,
                    row.totalPaid,
                    row.pending);
        }
        writer.flush();
    }

    // --- Shared row-building logic ---
    private List<ReportRow> buildRows() {
        List<Client> clients = clientRepository.findAll();
        List<ReportRow> rows = new ArrayList<>();

        for (Client client : clients) {
            List<Campaign> campaigns = campaignRepository.findByClient(client);
            List<Invoice> invoices = invoiceRepository.findByClient(client);

            double adSpend = campaigns.stream().mapToDouble(c -> c.getAdSpend() != null ? c.getAdSpend() : 0).sum();
            int leads = campaigns.stream().mapToInt(c -> c.getLeadsGenerated() != null ? c.getLeadsGenerated() : 0).sum();
            double cpl = leads > 0 ? adSpend / leads : 0;

            double totalInvoiced = invoices.stream().mapToDouble(i -> i.getTotalAmount() != null ? i.getTotalAmount() : 0).sum();
            double totalPaid = invoices.stream()
                    .filter(i -> i.getStatus() == Invoice.Status.PAID)
                    .mapToDouble(i -> i.getTotalAmount() != null ? i.getTotalAmount() : 0).sum();
            double pending = totalInvoiced - totalPaid;

            rows.add(new ReportRow(
                    client.getName(),
                    client.getProjectName(),
                    adSpend, leads, cpl,
                    totalInvoiced, totalPaid, pending
            ));
        }
        return rows;
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        return val.contains(",") ? "\"" + val + "\"" : val;
    }

    // --- Inner DTO ---
    public static class ReportRow {
        public String clientName, projectName;
        public double adSpend, cpl, totalInvoiced, totalPaid, pending;
        public int leadsGenerated;

        public ReportRow(String clientName, String projectName,
                         double adSpend, int leadsGenerated, double cpl,
                         double totalInvoiced, double totalPaid, double pending) {
            this.clientName = clientName;
            this.projectName = projectName;
            this.adSpend = adSpend;
            this.leadsGenerated = leadsGenerated;
            this.cpl = cpl;
            this.totalInvoiced = totalInvoiced;
            this.totalPaid = totalPaid;
            this.pending = pending;
        }
    }
}