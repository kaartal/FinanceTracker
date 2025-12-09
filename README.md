Finance Tracker Application

Overview
This project is a simple finance tracker application built with Java and MongoDB. It allows users to manage their transactions by adding, updating, deleting, and exporting them, while categorizing income and expenses.

MongoDBConnection.java
The MongoDBConnection class establishes a connection to the MongoDB database, specifying the path mongodb://localhost:27017 and the database name financeTrackerDB.

Transaction.java
The Transaction class defines fields for id, type, amount, description, and category, which correspond to the database table transaction. Two constructors are created: one with id for updating or deleting transactions, and one without id for adding new transactions, where the id auto-increments. The toDocument method saves all transaction attributes to the database. Getter methods like getId(), getType(), getAmount(), getDescription(), and getCategory() return specific values from the table.

TransactionManager.java
The addTransaction() method adds a new transaction to the table, while getTotalIncome() and getTotalExpense() calculate total income and expenses. getExpenseByCategory() and getIncomeByCategory() list transactions by category in a .txt or .csv file, filtering by type. updateTransaction() updates a transaction based on its id, and deleteMarkedTransaction() deletes selected transactions by id. deleteAllTransaction() removes all transactions from the table.

FinanceTrackerForm.java
This class calls all previously created methods, allowing users to add, update, delete, or export transactions. Transaction categories are stored as a string array rather than a separate table, though creating a dedicated table would be better. Additional features include deleting multiple transactions, exporting incomes by category, and validating descriptions between 3 and 35 characters.