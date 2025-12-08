    package financeapp;

    import javax.swing.*;
    import javax.swing.table.DefaultTableModel;
    import java.awt.*;
    import java.io.FileWriter;
    import java.io.PrintWriter;
    import java.util.ArrayList;
    import java.util.Map;

    public class FinanceTrackerForm {

        private JPanel mainPanel; private JLabel titleSection; private JLabel labelInputMoney; private JLabel labelInputDescription; private JTextField amountField; private JTextField descriptionField; private JComboBox<String> typeDropdownMenu; private JComboBox<String> categoryCombo; private JButton addTransactionButton; private JButton updateButton; private JButton deleteButton; private JButton deleteAllTransactionButton; private JButton exportButton; private JTable transactionTableSection; private JLabel incomeLabel; private JLabel expenseLabel; private JLabel balanceLabel;

        private TransactionManager manager;

        public FinanceTrackerForm() {

            manager = new TransactionManager();

            mainPanel = new JPanel(new BorderLayout(10, 10));


            JPanel topPanel = new JPanel();
            topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
            topPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

            JLabel title = new JLabel("Finance Tracker");
            title.setFont(new Font("Segoe UI", Font.BOLD, 22));
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            topPanel.add(title);

            topPanel.add(Box.createVerticalStrut(10));


            topPanel.add(createRow("Vrsta transakcije:", typeDropdownMenu = new JComboBox<>()));
            typeDropdownMenu.addItem("Prihod");
            typeDropdownMenu.addItem("Rashod");

            topPanel.add(createRow("Iznos:", amountField = new JTextField(15)));
            topPanel.add(createRow("Opis:", descriptionField = new JTextField(15)));

            categoryCombo = new JComboBox<>(new String[]{"Plata","Hrana","Racuni","Zabava","Prijevoz","Ostalo"});
            topPanel.add(createRow("Kategorija:", categoryCombo));


            JPanel btnPanel = new JPanel(new GridLayout(1, 5, 10, 10));
            addTransactionButton = new JButton("Dodaj");
            updateButton = new JButton("Ažuriraj");
            deleteButton = new JButton("Obriši označene");
            deleteAllTransactionButton = new JButton("Obriši sve");
            exportButton = new JButton("Eksportuj");

            btnPanel.add(addTransactionButton);
            btnPanel.add(updateButton);
            btnPanel.add(deleteButton);
            btnPanel.add(deleteAllTransactionButton);
            btnPanel.add(exportButton);

            btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            topPanel.add(Box.createVerticalStrut(10));
            topPanel.add(btnPanel);


            transactionTableSection = new JTable();
            JScrollPane tableScroll = new JScrollPane(transactionTableSection);


            JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 20, 10));
            incomeLabel = new JLabel("Prihod: 0.0");
            expenseLabel = new JLabel("Rashod: 0.0");
            balanceLabel = new JLabel("Saldo: 0.0");
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


        private JPanel createRow(String label, JComponent field) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            row.add(new JLabel(label));
            row.add(field);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            return row;
        }


        public JPanel getMainPanel() {
            return mainPanel;
        }


        private boolean checkIsThereTransaction() {
            if (manager.getAllTransactions().isEmpty()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Trenutno nema nijedne transakcije u bazi podataka.",
                        "Greška",
                        JOptionPane.ERROR_MESSAGE
                );
                return false;
            }
            return true;
        }

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

                manager.addTransaction(new Transaction(type, amount, description, category));

                loadDataIntoTable();
                updateSummary();

                amountField.setText("");
                descriptionField.setText("");

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Greška: niste unijeli tražene podatke.");
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
                        manager.deleteTransaction(id);
                    }
                }

                loadDataIntoTable();
                updateSummary();

                JOptionPane.showMessageDialog(null,
                        "Odabrane transakcije su uspješno obrisane!");
            }
        }

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
                    String path = chooser.getSelectedFile().getAbsolutePath();
                    if (!path.endsWith(".txt") && !path.endsWith(".csv")) path += ".txt";

                    PrintWriter writer = new PrintWriter(new FileWriter(path));

                    double income = manager.getTotalIncome();
                    double expense = manager.getTotalExpense();
                    double balance = income - expense;

                    writer.println("Ukupni prihod: " + income);
                    writer.println("Ukupni rashod: " + expense);
                    writer.println("Stanje računa: " + balance);
                    writer.println("\n------------------------------");
                    writer.println("Rashodi po kategorijama:");
                    for (Map.Entry<String, Double> e : manager.getExpenseByCategory().entrySet()) {
                        writer.println(e.getKey() + ": " + e.getValue());
                    }
                    writer.println("\n------------------------------");
                    writer.println("Prihodi po kategorijama:");
                    for (Map.Entry<String, Double> e : manager.getIncomesByCategory().entrySet()) {
                        writer.println(e.getKey() + ": " + e.getValue());
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
