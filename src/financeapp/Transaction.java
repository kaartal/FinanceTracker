package financeapp;

import org.bson.Document;

public class Transaction {

    private String id;
    private String type;
    private double amount;
    private String description;
    private String category;

    public Transaction(String id, String type, double amount, String description, String category) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.category = category;
    }

    public Transaction(String type, double amount, String description, String category) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.category = category;
    }

    public Document toDocument() {
        return new Document("Vrsta", type)
                .append("Iznos", amount)
                .append("Opis", description)
                .append("Kategorija", category);
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
}
