package com.capgemini.freebirds.presentation;

import com.capgemini.freebirds.model.Boat;
import com.capgemini.freebirds.model.BoatReservation;
import com.capgemini.freebirds.model.Customer;
import com.capgemini.freebirds.service.BoatService;
import com.capgemini.freebirds.service.CustomerService;
import com.capgemini.freebirds.service.ReservationService;
import com.capgemini.freebirds.service.UniqueIdCreator;
import com.toedter.calendar.JCalendar;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReservationForm implements iScreen {

    private final CustomerService customerService;
    private final ReservationService reservationService;
    private final BoatService boatService;

    List<BoatReservation> selectedBoatReservations = new ArrayList<>();
    ArrayList<Boat> selectedBoats = new ArrayList<>();

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField totalPrice;

    private JCalendar calendar;
    private JComboBox<String> startComboBox;
    private JComboBox<String> timeComboBox;

    private Customer selectedCustomer;
    private JLabel selectedCustomerLabel;
    private JLabel totalPriceLabel;
    private JDialog dialog;

    private JScrollPane boatListScrollPane;

    public ReservationForm(List<Boat> boats, CustomerService customerService, ReservationService reservationService) {
        this.customerService = customerService;
        this.reservationService = reservationService;
        this.boatService = new BoatService();
    }

    static private BoatReservation[] listToBoatList(List<BoatReservation> boatReservations) {
        return boatReservations.toArray(new BoatReservation[0]);
    }

    @Override
    public void show() {
        dialog = new JDialog();
        dialog.setTitle("Reservation form");
        dialog.setSize(800, 600);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(null);
        dialog.setModal(true);

        dialog.add(buildTopbar(), BorderLayout.NORTH);

        boatListScrollPane = buildBoatList(boatService.getBoatList());
        dialog.add(boatListScrollPane, BorderLayout.CENTER);

        dialog.setVisible(true);
    }

    private JPanel buildTopbar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setPreferredSize(new Dimension(800, 180));
        topBar.add(buildCustomerPanel(), BorderLayout.WEST);
        topBar.add(buildDatePicker(), BorderLayout.CENTER);
        topBar.add(buildTimeSlotPicker(), BorderLayout.EAST);
        return topBar;
    }

    private JPanel buildCustomerPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Customer"));
        panel.setPreferredSize(new Dimension(320, 180));

        selectedCustomerLabel = new JLabel("No customer selected");
        panel.add(selectedCustomerLabel, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(4, 2, 5, 5));
        fields.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        firstNameField = new JTextField();
        lastNameField = new JTextField();
        phoneField = new JTextField();
        emailField = new JTextField();

        fields.add(new JLabel("First name"));
        fields.add(firstNameField);
        fields.add(new JLabel("Surname"));
        fields.add(lastNameField);
        fields.add(new JLabel("Phone number"));
        fields.add(phoneField);
        fields.add(new JLabel("E-mail"));
        fields.add(emailField);

        panel.add(fields, BorderLayout.CENTER);

        JButton klantOpslaanButton = new JButton("Save customer");
        klantOpslaanButton.addActionListener(e -> {
            if (firstNameField.getText().isBlank()
                    || lastNameField.getText().isBlank()
                    || phoneField.getText().isBlank()
                    || emailField.getText().isBlank()) {
                JOptionPane.showMessageDialog(null, "Please fill in all fields.");
                return;
            }

            selectedCustomer = customerService.create(
                    UniqueIdCreator.getNextUniqueId(),
                    firstNameField.getText(),
                    lastNameField.getText(),
                    phoneField.getText(),
                    emailField.getText()
            );

            if (selectedCustomer != null) {
                selectedCustomerLabel.setText("Customer: " + selectedCustomer.firstName() + " " + selectedCustomer.lastName());
            }
        });

        panel.add(klantOpslaanButton, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildTimeSlotPicker() {
        JPanel timePanel = new JPanel();
        timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.Y_AXIS));
        timePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel startLabel = new JLabel("Start:");
        startLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        startComboBox = new JComboBox<>(buildStartTimes());
        startComboBox.setMaximumSize(startComboBox.getPreferredSize());
        startComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel endLabel = new JLabel("End:");
        endLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        timeComboBox = new JComboBox<>(buildStartTimes());
        timeComboBox.setMaximumSize(timeComboBox.getPreferredSize());
        timeComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        totalPriceLabel = new JLabel("Total Price: 0.0");
        totalPriceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//        totalPriceLabel.setMaximumSize(totalPriceLabel.getPreferredSize());

        startComboBox.addActionListener(e -> refreshBoatList());
        startComboBox.addActionListener(e1 -> updateTotalPriceLabel());
        timeComboBox.addActionListener(e -> refreshBoatList());
        timeComboBox.addActionListener(e1 -> updateTotalPriceLabel());

        JButton reserveringButton = createReservationButton();

        timePanel.add(startLabel);
        timePanel.add(startComboBox);
        timePanel.add(Box.createVerticalStrut(10));
        timePanel.add(endLabel);
        timePanel.add(timeComboBox);
        timePanel.add(Box.createVerticalStrut(10));
        timePanel.add(totalPriceLabel);
        timePanel.add(reserveringButton);

        return timePanel;
    }

    private void updateTotalPriceLabel() {

        LocalDate date = calendar.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String[] startParts = ((String) startComboBox.getSelectedItem()).split(":");
        String[] endParts = ((String) timeComboBox.getSelectedItem()).split(":");
        LocalDateTime begin = date.atTime(Integer.parseInt(startParts[0]), Integer.parseInt(startParts[1]));
        LocalDateTime end = date.atTime(Integer.parseInt(endParts[0]), Integer.parseInt(endParts[1]));

        totalPriceLabel.setText("Total Price: " + ReservationService.totalCost(selectedBoats, begin, end));

    }

    private void refreshBoatList() {
        if (dialog == null || calendar == null || startComboBox == null || timeComboBox == null) return;

        LocalDate date = calendar.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String[] startParts = ((String) startComboBox.getSelectedItem()).split(":");
        String[] endParts = ((String) timeComboBox.getSelectedItem()).split(":");
        LocalDateTime begin = date.atTime(Integer.parseInt(startParts[0]), Integer.parseInt(startParts[1]));
        LocalDateTime end = date.atTime(Integer.parseInt(endParts[0]), Integer.parseInt(endParts[1]));

        List<Boat> boats = end.isAfter(begin)
                ? boatService.getAvailableBoats(begin, end)
                : boatService.getBoatList();

        dialog.getContentPane().remove(boatListScrollPane);
        boatListScrollPane = buildBoatList(boats);
        dialog.getContentPane().add(boatListScrollPane, BorderLayout.CENTER);
        dialog.revalidate();
        dialog.repaint();
    }

    private JButton createReservationButton() {
        JButton reserveringButton = new JButton("Book Reservation");
        reserveringButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        reserveringButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        reserveringButton.addActionListener(e -> {
            if (selectedCustomer == null) {
                JOptionPane.showMessageDialog(null, "Please save customer first.");
                return;
            }

            if (selectedBoats == null || selectedBoats.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Select at least one boat.");
                return;
            }


            LocalDate date = calendar.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            String[] startParts = ((String) startComboBox.getSelectedItem()).split(":");
            String[] endParts = ((String) timeComboBox.getSelectedItem()).split(":");
            LocalDateTime begin = date.atTime(Integer.parseInt(startParts[0]), Integer.parseInt(startParts[1]));
            LocalDateTime eind = date.atTime(Integer.parseInt(endParts[0]), Integer.parseInt(endParts[1]));

            if (!eind.isAfter(begin)) {
                JOptionPane.showMessageDialog(null, "Invalid time slot: Starting time must be before Finishing time");
                return;
            }

            try {
                reservationService.create(selectedCustomer, selectedBoats, begin, eind);
                JOptionPane.showMessageDialog(null, "Reservation booked!");
                selectedBoats.clear();
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Boat already booked!", JOptionPane.WARNING_MESSAGE);
            }
        });

        return reserveringButton;
    }

    private String[] buildStartTimes() {
        java.util.List<String> times = new java.util.ArrayList<>();
        for (int hour = 9; hour <= 22; hour++) {
            for (int min = 0; min < 60; min += 15) {
                if (hour == 22 && min > 0) break;
                times.add(String.format("%02d:%02d", hour, min));
            }
        }
        return times.toArray(new String[0]);
    }

    private JPanel buildDatePicker() {
        JPanel datePanel = new JPanel();
        datePanel.setBorder(BorderFactory.createTitledBorder("Date"));
        datePanel.setLayout(new BoxLayout(datePanel, BoxLayout.Y_AXIS));

        calendar = new JCalendar();
        calendar.setPreferredSize(new Dimension(200, 130));
        calendar.setAlignmentX(Component.CENTER_ALIGNMENT);
        calendar.getDayChooser().addPropertyChangeListener("day", e -> refreshBoatList());

        JButton klantSelecterenButton = new JButton("Select customer");
        klantSelecterenButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        klantSelecterenButton.addActionListener(e -> {
            CustomerScreen customerScreen = new CustomerScreen(customerService.getAll(), customerService, klant -> {
                selectedCustomer = klant;
                selectedCustomerLabel.setText("Klant: " + klant.firstName() + " " + klant.lastName());
            });
            customerScreen.show();
        });

        datePanel.add(calendar);
        datePanel.add(klantSelecterenButton);
        return datePanel;
    }

    private JScrollPane buildBoatList(List<Boat> boats) {
        String[] columns = {"Selected", "Boat ID", "Type", "Capacity", "Price per 15 minutes", "Price per 30 minutes", "Properties"};

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : super.getColumnClass(columnIndex);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        Set<Integer> selectedIds = new HashSet<>();
        for (Boat b : selectedBoats) selectedIds.add(b.uniqueId());

        for (Boat b : boats) {
            boolean checked = selectedIds.contains(b.uniqueId());
            model.addRow(new Object[]{
                    checked,
                    b.uniqueId(),
                    b.type(),
                    b.capacity(),
                    b.pricePer15Min(),
                    b.pricePer30Min(),
                    String.join(", ", b.properties())
            });
        }

        JTable table = new JTable(model);
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        model.addTableModelListener(e -> {
            if (e.getColumn() != 0) return;
            if (e.getFirstRow() < 0) return;

            int row = e.getFirstRow();
            Boolean checked = (Boolean) model.getValueAt(row, 0);
            int boatId = (int) model.getValueAt(row, 1);

            Boat boat = BoatService.read(boatId);
            if (boat == null) return;

            if (Boolean.TRUE.equals(checked)) {
                boolean exists = selectedBoats.stream().anyMatch(b -> b.uniqueId() == boat.uniqueId());
                if (!exists) {selectedBoats.add(boat);
                    updateTotalPriceLabel();}
            } else {
                selectedBoats.removeIf(b -> b.uniqueId() == boat.uniqueId());
                updateTotalPriceLabel();
            }
        });

        return new JScrollPane(table);
    }
}