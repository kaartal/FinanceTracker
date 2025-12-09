package financeapp;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.swing.*;
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
            Document docTemplate = cursor.next();
            list.add(new Transaction(
                    docTemplate.getObjectId("_id").toHexString(),
                    docTemplate.getString("Vrsta"),
                    docTemplate.getDouble("Iznos"),
                    docTemplate.getString("Opis"),
                    docTemplate.getString("Kategorija")
            ));
        }
        return list;
    }


    // RETURN SUM OF ALL INCOME TRANSACTIONS
    public double getTotalIncome() {
        return getAllTransactions().stream()
                .filter(t -> t.getType().equalsIgnoreCase("Prihod"))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // RETURN SUM OF ALL EXPENSE TRANSACTION
    public double getTotalExpense() {
        return getAllTransactions().stream()
                .filter(t -> t.getType().equalsIgnoreCase("Rashod"))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }



    // EXPENSE LIST FOR EXPORT BY CATEGORIES
    public Map<String, Double> getExpenseByCategory() {

        String[] categories = {"Plata", "Hrana", "Racuni", "Zabava", "Prijevoz", "Ostalo"};
        Map<String, Double> result = new LinkedHashMap<>();

        for (String cat : categories) result.put(cat, 0.0);

        for (Transaction t : getAllTransactions()) {
            if ("Rashod".equalsIgnoreCase(t.getType())) {

                String cat = t.getCategory();
                if (cat == null || !result.containsKey(cat))
                    cat = "Ostalo";

                result.put(cat, result.get(cat) + t.getAmount());
            }
        }

        return result;
    }

    // INCOME LIST FOR EXPORT BY CATEGORIES
    public Map<String, Double> getIncomesByCategory() {

        Map<String, Double> result = new LinkedHashMap<>();
        String[] categories = {"Plata", "Hrana", "Racuni", "Zabava", "Prijevoz", "Ostalo"};


        for (String cat : categories) result.put(cat, 0.0);

        for (Transaction t : getAllTransactions()) {
            if ("Prihod".equalsIgnoreCase(t.getType())) {

                String cat = t.getCategory();
                if (cat == null || !result.containsKey(cat))
                    cat = "Ostalo";

                result.put(cat, result.get(cat) + t.getAmount());
            }
        }

        return result;
    }

    // UPDATE TRANSACTION, USER CAN INPUT NEW TYPE, AMOUNT, DESCRIPTION AND CATEGORY. ID SAME.
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

    // DELETE ALL MARKED TRANSACTION FROM PANEL
    public void deleteMarkedTransaction(String id) {
        collection.deleteOne(new Document("_id", new ObjectId(id)));
    }

    // DELETE ALL TRANSACTION FROM PANEL
    public void deleteAllTransactions() {
        collection.deleteMany(new Document());
    }
}
