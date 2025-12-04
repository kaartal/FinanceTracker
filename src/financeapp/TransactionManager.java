package financeapp;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;

public class TransactionManager {

    private final MongoCollection<Document> collection;

    public TransactionManager() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        collection = db.getCollection("transactions");
    }

    public void addTransaction(Transaction t) {
        collection.insertOne(t.toDocument());
    }

    public ArrayList<Transaction> getAllTransactions() {
        ArrayList<Transaction> list = new ArrayList<>();
        MongoCursor<Document> cursor = collection.find().iterator();

        while (cursor.hasNext()) {
            Document d = cursor.next();
            list.add(new Transaction(
                    d.getObjectId("_id").toHexString(),
                    d.getString("Vrsta"),
                    d.getDouble("Iznos"),
                    d.getString("Opis"),
                    d.getString("Kategorija")
            ));
        }

        return list;
    }

    public double getTotalIncome() {
        return getAllTransactions().stream()
                .filter(t -> t.getType().equalsIgnoreCase("Prihod"))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalExpense() {
        return getAllTransactions().stream()
                .filter(t -> t.getType().equalsIgnoreCase("Rashod"))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }


    // EXPENSE LIST BY CATEGORIES
    public Map<String, Double> getExpenseByCategory() {

        String[] categories = {"Plata", "Hrana", "Racuni", "Zabava", "Prijevoz", "Ostalo"};Map<String, Double> result = new LinkedHashMap<>();

        for (String cat : categories) result.put(cat, 0.0);

        for (Transaction t : getAllTransactions()) {
            if (t.getType() != null && t.getType().equalsIgnoreCase("Rashod")) {

                String cat = (t.getCategory() != null ? t.getCategory() : "Ostalo");

                if (result.containsKey(cat)) {
                    result.put(cat, result.get(cat) + t.getAmount());
                } else {
                    result.put("Ostalo", result.get("Ostalo") + t.getAmount());
                }
            }
        }

        return result;
    }



    // INCOME LIST BY CATEGORIES
    public Map<String, Double> getIncomesByCategory() {

        String[] categories = {"Plata", "Hrana", "Racuni", "Zabava", "Prijevoz", "Ostalo"};
        Map<String, Double> result = new LinkedHashMap<>();

        for (String cat : categories) result.put(cat, 0.0);

        for (Transaction t : getAllTransactions()) {
            if (t.getType() != null && t.getType().equalsIgnoreCase("Prihod")) {

                String cat = (t.getCategory() != null ? t.getCategory() : "Ostalo");

                if (result.containsKey(cat)) {
                    result.put(cat, result.get(cat) + t.getAmount());
                } else {
                    result.put("Ostalo", result.get("Ostalo") + t.getAmount());
                }
            }
        }

        return result;
    }




















    public void updateTransaction(Transaction t) {
        Document filter = new Document("_id", new ObjectId(t.getId()));

        Document updated = new Document("$set",
                new Document("Vrsta", t.getType())
                        .append("Iznos", t.getAmount())
                        .append("Opis", t.getDescription())
                        .append("Kategorija", t.getCategory())
        );

        collection.updateOne(filter, updated);
    }

    public void deleteTransaction(String id) {
        Document filter = new Document("_id", new ObjectId(id));
        collection.deleteOne(filter);
    }

    public void deleteAllTransactions() {
        collection.deleteMany(new Document());
    }
}
