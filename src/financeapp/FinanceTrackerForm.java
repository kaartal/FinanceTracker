package financeapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

public class FinanceTrackerForm {

    private JPanel mainPanel;
    private JLabel titleSection;
    private JLabel labelInputMoney;
    private JLabel labelInputDescription;

    private JTextField amountField;
    private JTextField descriptionField;

    private JComboBox<String> typeDropdownMenu;
    private JComboBox<String> categoryCombo;

    private JButton addTransactionButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton deleteAllTransactionButton;
    private JButton exportButton;

    private JTable transactionTableSection;
    private JLabel incomeLabel;
    private JLabel expenseLabel;
    private JLabel balanceLabel;

    private TransactionManager manager;

    public FinanceTrackerForm() {

        manager = new TransactionManager();

        categoryCombo.addItem("Plata");
        categoryCombo.addItem("Hrana");
        categoryCombo.addItem("Racuni");
        categoryCombo.addItem("Zabava");
        categoryCombo.addItem("Prijevoz");
        categoryCombo.addItem("Ostalo");

        loadDataIntoTable();
        updateSummary();

        addTransactionButton.addActionListener(e -> addTransaction());
        updateButton.addActionListener(e -> updateSelectedTransaction());
        deleteButton.addActionListener(e -> deleteSelectedTransactions());
        deleteAllTransactionButton.addActionListener(e -> deleteAll());
        exportButton.addActionListener(e -> exportData());

        transactionTableSection.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedTransactionIntoFields();
        });
    }

    private void deleteAll() {
        if (JOptionPane.showConfirmDialog(null,
                "Izbrisati sve?", "Potvrda",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
        {
            manager.deleteAllTransactions();
            loadDataIntoTable();
            updateSummary();
        }
    }

    private void addTransaction() {
        try {
            String type = (String) typeDropdownMenu.getSelectedItem();
            double amount = Double.parseDouble(amountField.getText());
            String description = descriptionField.getText();
            String category = (String) categoryCombo.getSelectedItem();

            if (description.length() < 3) {
                JOptionPane.showMessageDialog(null, "Opis mora imati min. 3 karaktera!");
                return;
            }

            manager.addTransaction(new Transaction(type, amount, description, category));

            loadDataIntoTable();
            updateSummary();

            amountField.setText("");
            descriptionField.setText("");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Greška: " + e.getMessage());
        }
    }

    private void updateSelectedTransaction() {
        int row = transactionTableSection.getSelectedRow();
        if (row == -1) return;

        String id = manager.getAllTransactions().get(row).getId();
        String type = (String) typeDropdownMenu.getSelectedItem();
        double amount = Double.parseDouble(amountField.getText());
        String desc = descriptionField.getText();
        String category = (String) categoryCombo.getSelectedItem();

        manager.updateTransaction(new Transaction(id, type, amount, desc, category));

        loadDataIntoTable();
        updateSummary();
    }








    private void deleteSelectedTransactions() {
        int rowCount = transactionTableSection.getRowCount();

        boolean anyChecked = false;

        // Provjera da li je išta označeno
        for (int i = 0; i < rowCount; i++) {
            Boolean checked = (Boolean) transactionTableSection.getValueAt(i, 0);
            if (checked != null && checked) {
                anyChecked = true;
                break;
            }
        }

        if (!anyChecked) {
            JOptionPane.showMessageDialog(null,
                    "Niste označili nijednu transakciju!",
                    "Greška",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        ArrayList<Transaction> all = manager.getAllTransactions();

        int choose = JOptionPane.showConfirmDialog(
                null,
                "Jeste li sigurni da želite izbrisati odabrane transakcije?",
                "Potvrda brisanja",
                JOptionPane.YES_NO_OPTION
        );

        if (choose == JOptionPane.YES_OPTION) {

            for (int i = rowCount - 1; i >= 0; i--) {
                Boolean checked = (Boolean) transactionTableSection.getValueAt(i, 0);

                if (checked != null && checked) {
                    String id = all.get(i).getId();
                    manager.deleteTransaction(id);
                }
            }

            loadDataIntoTable();
            updateSummary();

            JOptionPane.showMessageDialog(null,
                    "Odabrane transakcije su uspješno obrisane!");
        }
    }

    private void exportData() {

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Sačuvaj kao TXT ili CSV");

        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {

            try {
                String path = chooser.getSelectedFile().getAbsolutePath();
                if (!path.endsWith(".txt") && !path.endsWith(".csv")) {
                    path += ".txt";
                }

                PrintWriter writer = new PrintWriter(new FileWriter(path));

                double income = manager.getTotalIncome();
                double expense = manager.getTotalExpense();
                double balance = income - expense;

                writer.println("Ukupni prihod: " + income);
                writer.println("Ukupni rashod: " + expense);
                writer.println("Stanje računa: " + balance);
                writer.println();
                writer.println("------------------------------");
                writer.println("Rashodi po kategorijama:");

                Map<String, Double> exportCategoriesForExpense = manager.getExpenseByCategory();
                for (String exportExpense : exportCategoriesForExpense.keySet()) {
                    writer.println(exportExpense + ": " + exportCategoriesForExpense.get(exportExpense));
                }



                writer.println("------------------------------");
                writer.println("Prihodi po kategorijama:");
                Map<String, Double> exportCategoriesForIncome = manager.getIncomesByCategory();
                for (String exportIncome : exportCategoriesForIncome.keySet()) {
                    writer.println(exportIncome + ": " + exportCategoriesForIncome.get(exportIncome));
                }

                writer.close();

                JOptionPane.showMessageDialog(null, "Podaci su uspješno eksportovani!");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Greška pri eksportu: " + ex.getMessage());
            }
        }
    }

    private void loadDataIntoTable() {
        ArrayList<Transaction> list = manager.getAllTransactions();

        DefaultTableModel model = new DefaultTableModel() {
            public boolean isCellEditable(int row, int col) { return col == 0; }
            public Class<?> getColumnClass(int col) { return col == 0 ? Boolean.class : String.class; }
        };

        model.addColumn("Označi");
        model.addColumn("Vrsta");
        model.addColumn("Iznos");
        model.addColumn("Opis");
        model.addColumn("Kategorija");

        for (Transaction t : list) {
            model.addRow(new Object[]{
                    false, t.getType(), t.getAmount(), t.getDescription(), t.getCategory()
            });
        }

        transactionTableSection.setModel(model);
    }

    private void loadSelectedTransactionIntoFields() {
        int row = transactionTableSection.getSelectedRow();
        if (row == -1) return;

        typeDropdownMenu.setSelectedItem(transactionTableSection.getValueAt(row, 1).toString());
        amountField.setText(transactionTableSection.getValueAt(row, 2).toString());
        descriptionField.setText(transactionTableSection.getValueAt(row, 3).toString());
        categoryCombo.setSelectedItem(transactionTableSection.getValueAt(row, 4).toString());
    }

    private void updateSummary() {
        double income = manager.getTotalIncome();
        double expense = manager.getTotalExpense();
        double balance = income - expense;

        incomeLabel.setText("Prihod: " + income);
        expenseLabel.setText("Rashod: " + expense);
        balanceLabel.setText("Saldo: " + balance);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
