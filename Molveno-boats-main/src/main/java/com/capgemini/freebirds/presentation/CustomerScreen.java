package com.capgemini.freebirds.presentation;

import com.capgemini.freebirds.model.Customer;
import com.capgemini.freebirds.service.CustomerService;
import com.capgemini.freebirds.service.UniqueIdCreator;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CustomerScreen implements iScreen {

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JDialog dialog;
    private JTable table; // class field zodat knoppen er bij kunnen
    private DefaultTableModel tableModel; // voor verwijderen
    private List<Customer> customers;
    private final CustomerService customerService;
    private final Consumer<Customer> onSelectCallback;

    // CustomerScreen gebruikt voor ReservationForm - Callback
    public CustomerScreen(List<Customer> customers, CustomerService customerService, Consumer<Customer> onSelectCallback) {
        this.customerService = customerService;
        this.customers = customers;
        this.onSelectCallback = onSelectCallback;
    }

    // CustomerScreen gebruikt voor SWING UI
    public CustomerScreen(List<Customer> customers, CustomerService customerService) {
        this.customerService = customerService;
        this.customers = customers;
        this.onSelectCallback = null;
    }

    @Override
    public void show() {
        showCustomer(null);
    }

    private void showCustomer(Customer c) {
        dialog = new JDialog();
        dialog.setSize(800, 600);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(null);
        dialog.setModal(true);
        dialog.setTitle(c != null ? "Select existing customer" : "Add new customer");
        dialog.add(buildTopbar(c), BorderLayout.NORTH);
        dialog.add(buildGuestList(), BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // balk boven
    private JPanel buildTopbar(Customer c) {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setPreferredSize(new Dimension(800, 150));
        topBar.add(buildInputFields(c), BorderLayout.WEST);
        topBar.add(buildButtonsPanel(), BorderLayout.EAST);
        return topBar;
    }

    // knoppen
    private JPanel buildButtonsPanel() {
        JPanel buttonsPanel = new JPanel(new GridLayout(3, 1, 5, 5));

        JButton opslaanButton = new JButton("Save new customer");
        opslaanButton.addActionListener(e -> {
            Customer newCustomer = customerService.create(
                    UniqueIdCreator.getNextUniqueId(),
                    firstNameField.getText(),
                    lastNameField.getText(),
                    phoneField.getText(),
                    emailField.getText()
            );
            if (newCustomer != null) {
                tableModel.addRow(new Object[]{newCustomer.uniqueCustomerId(), newCustomer.firstName(), newCustomer.lastName(), newCustomer.phoneNumber(), newCustomer.mailAddress()});
                firstNameField.setText("");
                lastNameField.setText("");
                phoneField.setText("");
                emailField.setText("");
            }
        });

        JButton selecterenButton = new JButton("Select customer");
        selecterenButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(null, "Select a customer first");
                return;
            }
            if (onSelectCallback != null) {
                int customerId = (int) tableModel.getValueAt(row, 0);
                customers.stream()
                        .filter(c -> c.uniqueCustomerId() == customerId)
                        .findFirst()
                        .ifPresent(c -> {
                            onSelectCallback.accept(c);
                            dialog.dispose();
                        });
            }
        });

        JButton deleteButton = new JButton("Delete customer data");
        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(null, "Select a customer first");
                return;
            }
            int customerId = (int) tableModel.getValueAt(row, 0);
            customers.stream()
                    .filter(c -> c.uniqueCustomerId() == customerId)
                    .findFirst()
                    .ifPresent(customerService::delete);
            customers.removeIf(c -> c.uniqueCustomerId() == customerId);
            tableModel.removeRow(row); // verwijderd rij uit tabel
        });

        buttonsPanel.add(opslaanButton);
        buttonsPanel.add(selecterenButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return buttonsPanel;
    }

    // input velden
    private JPanel buildInputFields(Customer c) {
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 10, 10));

        firstNameField = new JTextField(c != null ? c.firstName() : "");
        lastNameField = new JTextField(c != null ? c.lastName() : "");
        phoneField = new JTextField(c != null ? c.phoneNumber() : "");
        emailField = new JTextField(c != null ? c.mailAddress() : "");

        inputPanel.add(new JLabel("First name"));
        inputPanel.add(firstNameField);
        inputPanel.add(new JLabel("Surname"));
        inputPanel.add(lastNameField);
        inputPanel.add(new JLabel("Phone number"));
        inputPanel.add(phoneField);
        inputPanel.add(new JLabel("E-mail address"));
        inputPanel.add(emailField);

        return inputPanel;
    }

    private JScrollPane buildGuestList() {
        String[] columns = {"ID", "First name", "Surname", "Phone number", "E-mail"};

        tableModel = new DefaultTableModel(columns, 0);
        for (Customer c : customers) {
            tableModel.addRow(new Object[]{c.uniqueCustomerId(), c.firstName(), c.lastName(), c.phoneNumber(), c.mailAddress()});
        }

        table = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.getTableHeader().setReorderingAllowed(false);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && onSelectCallback != null) {
                    int row = table.getSelectedRow();
                    if (row == -1) return;
                    int customerId = (int) tableModel.getValueAt(row, 0);
                    customers.stream()
                            .filter(c -> c.uniqueCustomerId() == customerId)
                            .findFirst()
                            .ifPresent(c -> {
                                onSelectCallback.accept(c);
                                dialog.dispose();
                            });
                }
            }
        });
        return new JScrollPane(table);
    }
}
