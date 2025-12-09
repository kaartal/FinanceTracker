package financeapp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

public class FinanceTrackerForm {


    private JPanel mainPanel;
    private JTextField amountField, descriptionField;
    private JComboBox<String> typeDropdownMenu, categoryCombo;
    private JButton addTransactionButton, updateButton, deleteButton, deleteAllTransactionButton, exportButton;
    private JTable transactionTableSection;
    private JLabel incomeLabel, expenseLabel, titleSection, labelInputDescription, labelInputMoney, balanceLabel;

    private final TransactionManager manager;

    public FinanceTrackerForm() {

        manager = new TransactionManager();

        mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new
                EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);


        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(Color.WHITE);


        topPanel.add(Box.createVerticalStrut(15));

        typeDropdownMenu = new JComboBox<>(new String[]{"Prihod", "Rashod"});
        topPanel.add(createNewDisplay("Vrsta transakcije:", typeDropdownMenu));


        amountField = new JTextField();
        topPanel.add(createNewDisplay("Iznos:", amountField));


        descriptionField = new JTextField();
        topPanel.add(createNewDisplay("Opis:", descriptionField));


        categoryCombo = new JComboBox<>(new String[]{"Plata","Hrana","Racuni","Zabava","Prijevoz","Ostalo"});
        topPanel.add(createNewDisplay("Kategorija:", categoryCombo));

        topPanel.add(Box.createVerticalStrut(15));


        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        addTransactionButton = createModernButton("Dodaj", new Color(72, 201, 176));
        updateButton = createModernButton("Ažuriraj", new Color(52, 152, 219));
        deleteButton = createModernButton("Obriši označene", new Color(231, 76, 60));
        deleteAllTransactionButton = createModernButton("Obriši sve", new Color(241, 196, 15));
        exportButton = createModernButton("Eksportuj", new Color(155, 89, 182));

        buttonPanel.add(addTransactionButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(deleteAllTransactionButton);
        buttonPanel.add(exportButton);

        topPanel.add(buttonPanel);
        topPanel.add(Box.createVerticalStrut(15));


        transactionTableSection = new JTable();
        transactionTableSection.setFillsViewportHeight(true);
        transactionTableSection.setRowHeight(28);
        transactionTableSection.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane tableScroll = new JScrollPane(transactionTableSection);


        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 20, 10));
        summaryPanel.setBackground(Color.WHITE);

        incomeLabel = createSummaryLabel("Prihod: 0.0", new Color(39, 174, 96));
        expenseLabel = createSummaryLabel("Rashod: 0.0", new Color(192, 57, 43));
        balanceLabel = createSummaryLabel("Saldo: 0.0", new Color(41, 128, 185));

        summaryPanel.add(incomeLabel);
        summaryPanel.add(expenseLabel);
        summaryPanel.add(balanceLabel);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tableScroll, BorderLayout.CENTER);
        mainPanel.add(summaryPanel, BorderLayout.SOUTH);


        addTransactionButton.addActionListener(e -> addTransaction());
        updateButton.addActionListener(e -> updateSelectedTransaction());
        deleteButton.addActionListener(e -> deleteSelectedTransactions());
        deleteAllTransactionButton.addActionListener(e -> deleteAllTransactions());
        exportButton.addActionListener(e -> exportData());

        transactionTableSection.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedTransactionIntoFields();
        });

        loadDataIntoTable();
        updateSummary();
    }

    private JPanel createNewDisplay(String labelText, JComponent field) {
        JPanel rowData = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        rowData.setBackground(Color.WHITE);

        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(130, 28));
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        label.setForeground(new Color(44, 62, 80));
        rowData.add(label);

        field.setPreferredSize(new Dimension(200, 28));
        if(field instanceof JTextField) {
            field.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1, true));
        } else if(field instanceof JComboBox) {
            field.setBackground(Color.WHITE);
            field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }

        rowData.add(field);
        return rowData;
    }


    private JButton createModernButton(String text, Color bgColor) {
        JButton buttonDisplay = new JButton(text);
        buttonDisplay.setFocusPainted(false);
        buttonDisplay.setBackground(bgColor);
        buttonDisplay.setForeground(Color.WHITE);
        buttonDisplay.setFont(new Font("Segoe UI", Font.BOLD, 14));
        buttonDisplay.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
        buttonDisplay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));


        buttonDisplay.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonDisplay.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                buttonDisplay.setBackground(bgColor);
            }
        });
        return buttonDisplay;
    }


    private JLabel createSummaryLabel(String text, Color color) {
        JLabel labelDisplay = new JLabel(text);
        labelDisplay.setFont(new Font("Segoe UI", Font.BOLD, 16));
        labelDisplay.setForeground(color);
        return labelDisplay;
    }

public JPanel getMainPanel() {
        return mainPanel;
    }

        // CHECKING IS THERE SOME TRANSACTION IN PANEL
        private boolean checkIsThereTransaction() {
            if (manager.getAllTransactions().isEmpty()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Trenutno nema transakcija u bazi podataka.",
                        "Greška",
                        JOptionPane.ERROR_MESSAGE
                );
                return false;
            }
            return true;
        }

        // ADD NEW TRANSACTION TO DB, USER ENTER TYPE, AMOUNT, DESCRIBE, CATEGORY. ID INCREMENT FOR NEW TRANSACTION
        private void addTransaction() {
            try {
                String type = (String) typeDropdownMenu.getSelectedItem();
                double amount = Double.parseDouble(amountField.getText());
                String description = descriptionField.getText();
                String category = (String) categoryCombo.getSelectedItem();

                if (description.length() < 3) {
                    JOptionPane.showMessageDialog(null, "Opis mora imati minimalno 3 karaktera!");
                    return;
                }

                if (description.length() > 40) {
                    JOptionPane.showMessageDialog(null, "Opis transakcije se treba smanjiti!");
                    return;
                }

                manager.addTransaction(new Transaction(type, amount, description, category));

                loadDataIntoTable();
                updateSummary();

                amountField.setText("");
                descriptionField.setText("");

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Greška: niste unijeli tražene podatke.");
            }
        }


        // UPDATE SELECTED TRANSACTION, USER CAN ENTER NEW VALUES TYPE, AMOUNT, DESCRIBE, CATEGORY, ID SAME
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

        //    DELETE SELECT TRANSACTIONS FROM PANEL
        private void deleteSelectedTransactions() {
            int rowCount = transactionTableSection.getRowCount();
            boolean anyChecked = false;

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
                    if (checked) {
                        String id = all.get(i).getId();
                        manager.deleteMarkedTransaction(id);
                    }
                }

                loadDataIntoTable();
                updateSummary();

                JOptionPane.showMessageDialog(null,
                        "Odabrane transakcije su uspješno obrisane!");
            }
        }

        // DELETE ALL TRANSACTION FROM DB
        private void deleteAllTransactions() {
            if(checkIsThereTransaction())
                if (JOptionPane.showConfirmDialog(null,
                        "Izbrisati sve?", "Potvrda",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                {
                    manager.deleteAllTransactions();
                    loadDataIntoTable();
                    updateSummary();
                }
        }

        private void exportData() {
            if (manager.getAllTransactions().isEmpty()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Trenutno nema nijedne transakcije za eksportovati.",
                        "Greška",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Sačuvaj kao TXT ili CSV");

            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                try {
                    String fileExstension = chooser.getSelectedFile().getAbsolutePath();
                    if (!fileExstension.endsWith(".txt") && !fileExstension.endsWith(".csv"))
                        fileExstension += ".txt";

                    PrintWriter writerTransaction = new PrintWriter(new FileWriter(fileExstension));

                    double income = manager.getTotalIncome();
                    double expense = manager.getTotalExpense();
                    double balance = income - expense;

                    writerTransaction.println("Ukupni prihod: " + income);
                    writerTransaction.println("Ukupni rashod: " + expense);
                    writerTransaction.println("Stanje racuna: " + balance);
                    writerTransaction.println("\n-----------------------------------");

                    // INCOME EXPORT BY CATEGORIES
                    writerTransaction.println("Prihodi po kategorijama:");
                    for (Map.Entry<String, Double> export : manager.getIncomesByCategory().entrySet()) {
                        writerTransaction.println(export.getKey() + ": " + export.getValue());
                    }
                    //EXPENSES EXPORT BY CATEGORIES
                    writerTransaction.println("\n-----------------------------------");
                    writerTransaction.println("Rashodi po kategorijama:");
                    for (Map.Entry<String, Double> export : manager.getExpenseByCategory().entrySet()) {
                        writerTransaction.println(export.getKey() + ": " + export.getValue());
                    }

                    writerTransaction.close();

                    JOptionPane.showMessageDialog(null, "Podaci su uspješno eksportovani!");

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Greška pri eksportu: " + ex.getMessage());
                }
            }
        }

        // LOAD ALL TRANSACTIONS FROM DB TO USING PANEL
        private void loadDataIntoTable() {
            ArrayList<Transaction> list = manager.getAllTransactions();

            DefaultTableModel model = new DefaultTableModel() {
                public boolean isCellEditable(int row, int col) { return col == 0; }
                public Class<?> getColumnClass(int col) { return col == 0 ? Boolean.class : String.class; }
            };

            model.addColumn("");
            model.addColumn("Vrsta");
            model.addColumn("Iznos");
            model.addColumn("Opis");
            model.addColumn("Kategorija");

            for (Transaction t : list) {
                model.addRow(new Object[]{false, t.getType(), t.getAmount(), t.getDescription(), t.getCategory()});
            }

            transactionTableSection.setModel(model);
            transactionTableSection.getColumnModel().getColumn(0).setMinWidth(35);
            transactionTableSection.getColumnModel().getColumn(0).setMaxWidth(35);
            transactionTableSection.getColumnModel().getColumn(0).setPreferredWidth(35);
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



    }
