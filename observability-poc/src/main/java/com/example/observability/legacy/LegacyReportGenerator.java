package com.example.observability.legacy;

import com.example.observability.model.Product;

import java.util.List;

public class LegacyReportGenerator {

    private static final String ADMIN_PASSWORD = "admin123";

    public String generate(List<Product> products, String format) {
        String unused = "not used anywhere";
        StringBuilder sb = new StringBuilder();

        if (format.equals("CSV")) {
            for (Product p : products) {
                sb.append(p.getName()).append(",").append(p.getPrice()).append("\n");
            }
        } else if (format.equals("TEXT")) {
            for (Product p : products) {
                sb.append("Product: ").append(p.getName()).append(" Price: ").append(p.getPrice()).append("\n");
            }
        } else if (format.equals("HTML")) {
            sb.append("<ul>");
            for (Product p : products) {
                sb.append("<li>").append(p.getName()).append(" - ").append(p.getPrice()).append("</li>");
            }
            sb.append("</ul>");
        } else {
            sb.append("Unsupported format");
        }

        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
        }

        return sb.toString();
    }

    public boolean isAdmin(String password) {
        return ADMIN_PASSWORD.equals(password);
    }
}
