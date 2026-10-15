package com.capgemini.freebirds.presentation;

import com.capgemini.freebirds.model.Boat;
import com.capgemini.freebirds.model.BoatReservation;
import com.capgemini.freebirds.model.CancellationPolicy;
import com.capgemini.freebirds.model.Customer;
import com.capgemini.freebirds.model.CustomerReservation;
import com.capgemini.freebirds.service.ReservationService;
import com.toedter.calendar.JCalendar;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CustomerReservationDetails {

    static void createAndShowGUI(CustomerReservation customerReservation,
                                 ReservationService reservationService,
                                 Runnable onCancelled) {

        JFrame frame = new JFrame("Reservation Details");
        frame.setSize(800, 600);

        Customer customer = customerReservation.customer();

        CancellationPolicy policy = reservationService.getCancellationPolicy(customerReservation);
        double refund = reservationService.calculateRefund(customerReservation);

        JButton cancelButton = new JButton("Cancel Reservation");
        cancelButton.setEnabled(policy.isCancellable());
        cancelButton.addActionListener(e -> {
            String msg = policy.getFeePercent() == 0
                    ? "Are you sure you wish to cancel? You may cancel for no additional fee"
                    : String.format(
                    "Are you sure you wish to cancel?%n" +
                    "Policy: %s%n" +
                    "Refund: €%.2f",
                    policy.getDescription(), refund);

            int confirm = JOptionPane.showConfirmDialog(
                    null, msg, "Confirm Cancellation",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                reservationService.cancel(customerReservation);
                JOptionPane.showMessageDialog(null, "Reservation Cancelled and Deleted.");
                frame.dispose();
                onCancelled.run();
            }
        });

        JButton editButton = new JButton("Edit Reservation");
        editButton.addActionListener(e -> {
            JDialog editDialog = new JDialog(frame, "Edit Reservation", true);
            editDialog.setSize(800, 600);
            editDialog.setLayout(new BorderLayout());
            editDialog.setLocationRelativeTo(null);

            JPanel fieldsPanel = new JPanel(new GridLayout(3, 2, 10, 10));
            fieldsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JCalendar editCalendar = new JCalendar();
            editCalendar.setDate(java.util.Date.from(
                    customerReservation.beginDateTime().toLocalDate()
                            .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));

            JComboBox<String> editStartComboBox = new JComboBox<>(buildStartTimes());
            editStartComboBox.setSelectedItem(
                    customerReservation.beginDateTime().toLocalTime().toString().substring(0, 5));

            JComboBox<String> editEndComboBox = new JComboBox<>(buildStartTimes());
            editEndComboBox.setSelectedItem(
                    customerReservation.endDateTime().toLocalTime().toString().substring(0, 5));

            editStartComboBox.setMaximumSize(editStartComboBox.getPreferredSize());
            editEndComboBox.setMaximumSize(editEndComboBox.getPreferredSize());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            gbc.gridx = 0;
            gbc.gridy = 0;
            fieldsPanel.add(new JLabel("Starting time"), gbc);
            gbc.gridx = 1;
            fieldsPanel.add(editStartComboBox, gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            fieldsPanel.add(new JLabel("Finishing time"), gbc);
            gbc.gridx = 1;
            fieldsPanel.add(editEndComboBox, gbc);

            JButton opslaanButton = new JButton("Save");
            opslaanButton.addActionListener(e1 -> {
                java.time.LocalDate newDate = editCalendar.getDate().toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                String[] startParts = ((String) editStartComboBox.getSelectedItem()).split(":");
                String[] endParts = ((String) editEndComboBox.getSelectedItem()).split(":");

                java.time.LocalDateTime newBegin = newDate.atTime(
                        Integer.parseInt(startParts[0]), Integer.parseInt(startParts[1]));
                java.time.LocalDateTime newEnd = newDate.atTime(
                        Integer.parseInt(endParts[0]), Integer.parseInt(endParts[1]));

                if (!newEnd.isAfter(newBegin)) {
                    JOptionPane.showMessageDialog(null, "Invalid time slot: Starting time must be before Finishing time");
                    return;
                }

                for (BoatReservation br : customerReservation.boat()) {
                    Boat boat = br.boat();
                    if (!isBoatAvailableExcludingReservation(
                            reservationService,
                            customerReservation.uniqueReservationId(),
                            boat,
                            newBegin,
                            newEnd
                    )) {
                        JOptionPane.showMessageDialog(
                                null,
                                "Boot " + boat.uniqueId() + " has already been booked on this time slot",
                                "Boot al bezet!",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }
                }

                double totalPrice = 0;
                for (BoatReservation br : customerReservation.boat()) {
                    Boat boat = br.boat();
                    BoatReservation recalculated =
                            com.capgemini.freebirds.service.BoatReservationService.create(boat, newBegin, newEnd);
                    totalPrice += recalculated.price();
                }

                CustomerReservation gewijzigd = new CustomerReservation(
                        customerReservation.uniqueReservationId(),
                        customerReservation.boat(),
                        customerReservation.customer(),
                        newBegin,
                        newEnd,
                        totalPrice
                );

                reservationService.update(gewijzigd);
                JOptionPane.showMessageDialog(null, "Reservation changed!");
                editDialog.dispose();
                frame.dispose();
                onCancelled.run();
            });

            editDialog.add(opslaanButton, BorderLayout.SOUTH);
            editDialog.add(editCalendar, BorderLayout.NORTH);
            editDialog.add(fieldsPanel, BorderLayout.CENTER);
            editDialog.setVisible(true);
        });

        JButton editBoatsButton = new JButton("Change boats");
        editBoatsButton.addActionListener(ev -> {
            com.capgemini.freebirds.service.BoatService boatService = new com.capgemini.freebirds.service.BoatService();

            java.time.LocalDateTime begin = customerReservation.beginDateTime();
            java.time.LocalDateTime end = customerReservation.endDateTime();

            java.util.List<Boat> availableBoats = boatService.getAvailableBoats(begin, end);
            for (BoatReservation br : customerReservation.boat()) {
                Boat currentBoat = br.boat();
                if (availableBoats.stream().noneMatch(b -> b.uniqueId() == currentBoat.uniqueId())) {
                    availableBoats.add(currentBoat);
                }
            }

            Set<Integer> currentBoatIds = new HashSet<>();
            for (BoatReservation br : customerReservation.boat()) {
                currentBoatIds.add(br.boat().uniqueId());
            }

            String[] columns = {"Selected", "Boot ID", "Type", "Capacity"};
            DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 0 ? Boolean.class : super.getColumnClass(columnIndex);
                }

                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 0;
                }
            };

            for (Boat b : availableBoats) {
                boolean checked = currentBoatIds.contains(b.uniqueId());
                tableModel.addRow(new Object[]{checked, b.uniqueId(), b.type(), b.capacity()});
            }

            JTable boatTable = new JTable(tableModel);
            boatTable.getTableHeader().setReorderingAllowed(false);
            boatTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

            JScrollPane scrollPane = new JScrollPane(boatTable);
            scrollPane.setPreferredSize(new Dimension(650, 300));

            int result = JOptionPane.showConfirmDialog(
                    frame,
                    scrollPane,
                    "Select boats",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) return;

            java.util.List<Boat> selected = new java.util.ArrayList<>();
            for (int r = 0; r < tableModel.getRowCount(); r++) {
                Boolean checked = (Boolean) tableModel.getValueAt(r, 0);
                if (Boolean.TRUE.equals(checked)) {
                    int boatId = (int) tableModel.getValueAt(r, 1);
                    Boat boat = com.capgemini.freebirds.service.BoatService.read(boatId);
                    if (boat != null) selected.add(boat);
                }
            }

            if (selected.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Select at least one boat");
                return;
            }

            for (Boat b : selected) {
                if (!isBoatAvailableExcludingReservation(reservationService, customerReservation.uniqueReservationId(), b, begin, end)) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Boot " + b.uniqueId() + " has already been booked on this time slot",
                            "Boot al bezet!",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
            }

            java.util.List<BoatReservation> newBoatReservationsList = new java.util.ArrayList<>();
            double totalPrice = 0;

            for (Boat b : selected) {
                BoatReservation br = com.capgemini.freebirds.service.BoatReservationService.create(b, begin, end);
                newBoatReservationsList.add(br);
                totalPrice += br.price();
            }

            BoatReservation[] newBoatReservations = newBoatReservationsList.toArray(new BoatReservation[0]);

            CustomerReservation gewijzigd = new CustomerReservation(
                    customerReservation.uniqueReservationId(),
                    newBoatReservations,
                    customerReservation.customer(),
                    begin,
                    end,
                    totalPrice
            );

            reservationService.update(gewijzigd);
            JOptionPane.showMessageDialog(null, "Boats changed!");
            frame.dispose();
            onCancelled.run();
        });

        JPanel customerPanel = getJPanel(customer, cancelButton);
        JPanel reservationPanel = getJPanel(customerReservation, editButton);
        JPanel boatPanel = getJPanel(customerReservation.boat(), editBoatsButton);

        JScrollPane boatScrollPane = new JScrollPane(boatPanel);
        boatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        boatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JPanel mainPanel = new JPanel(new GridLayout(1, 3));
        mainPanel.add(customerPanel);
        mainPanel.add(reservationPanel);
        mainPanel.add(boatScrollPane);

        frame.setLayout(new BorderLayout());
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static JPanel getJPanel(Customer customer, JButton button) {
        JPanel customerPanel = new JPanel();
        customerPanel.setLayout(new BoxLayout(customerPanel, BoxLayout.Y_AXIS));
        customerPanel.setBorder(BorderFactory.createTitledBorder("Customer"));
        customerPanel.add(new JLabel("Customer ID: " + customer.uniqueCustomerId()));
        customerPanel.add(new JLabel("First name: " + customer.firstName()));
        customerPanel.add(new JLabel("Last name: " + customer.lastName()));
        customerPanel.add(new JLabel("Phonenumber: " + customer.phoneNumber()));
        customerPanel.add(new JLabel("Email: " + customer.mailAddress()));
        customerPanel.add(Box.createVerticalGlue());
        customerPanel.add(button);
        return customerPanel;
    }

    private static JPanel getJPanel(CustomerReservation customerReservation, JButton button) {
        JPanel reservationPanel = new JPanel();
        reservationPanel.setLayout(new BoxLayout(reservationPanel, BoxLayout.Y_AXIS));
        reservationPanel.setBorder(BorderFactory.createTitledBorder("Reservation"));
        reservationPanel.add(new JLabel("Reservation ID: " + customerReservation.uniqueReservationId()));
        reservationPanel.add(new JLabel("Date: " + customerReservation.beginDateTime().toLocalDate()));
        reservationPanel.add(new JLabel("Begintime: " + customerReservation.beginDateTime().toLocalTime()));
        reservationPanel.add(new JLabel("Endtime: " + customerReservation.endDateTime().toLocalTime()));
        reservationPanel.add(new JLabel("Duration: " + Duration.between(customerReservation.beginDateTime(), customerReservation.endDateTime()).toMinutes() + " Min"));
        reservationPanel.add(new JLabel("Total Price: " + customerReservation.totalPrice()));
        reservationPanel.add(Box.createVerticalGlue());
        reservationPanel.add(button);
        return reservationPanel;
    }

    private static JPanel getJPanel(BoatReservation[] customerReservations, JButton button) {
        JPanel boatsPanel = new JPanel();
        boatsPanel.setLayout(new BoxLayout(boatsPanel, BoxLayout.Y_AXIS));
        boatsPanel.setBorder(BorderFactory.createTitledBorder("Boats"));
        for (int i = 0; i < customerReservations.length; i++) {
            BoatReservation boatReservation = customerReservations[i];
            Boat boat = boatReservation.boat();
            boatsPanel.add(new JLabel(" "));
            boatsPanel.add(new JLabel("Boat " + (i + 1) + ":"));
            boatsPanel.add(new JLabel("Type: " + boat.type()));
            boatsPanel.add(new JLabel("Capacity: " + boat.capacity()));
            boatsPanel.add(new JLabel("Price: " + boatReservation.price() + " (" + boat.pricePer15Min() + " per 15 minutes) (" + boat.pricePer30Min() + " per 30 minutes)"));
//            boatsPanel.add(new JLabel("Price: " + boatReservation.price() + " (" + boat.pricePer15Min() + ") (" + boat.pricePer30Min() + ")"));
//            boatsPanel.add(new JLabel("Price: " + boatReservation.price()));


        }
        boatsPanel.add(Box.createVerticalGlue());
        boatsPanel.add(button);
        return boatsPanel;
    }

    private static boolean isBoatAvailableExcludingReservation(
            ReservationService reservationService,
            int excludeReservationId,
            Boat boat,
            java.time.LocalDateTime begin,
            java.time.LocalDateTime end
    ) {
        for (CustomerReservation existing : reservationService.getAll()) {
            if (existing == null) continue;
            if (existing.uniqueReservationId() == excludeReservationId) continue;

            for (BoatReservation br : existing.boat()) {
                if (br == null) continue;
                boolean sameBoat = br.boat().uniqueId() == boat.uniqueId();
                boolean overlaps = begin.isBefore(br.endDateTime()) && end.isAfter(br.beginDateTime());
                if (sameBoat && overlaps) return false;
            }
        }
        return true;
    }

    private static String[] buildStartTimes() {
        java.util.List<String> times = new java.util.ArrayList<>();
        for (int hour = 9; hour <= 22; hour++) {
            for (int min = 0; min < 60; min += 15) {
                if (hour == 22 && min > 0) break;
                times.add(String.format("%02d:%02d", hour, min));
            }
        }
        return times.toArray(new String[0]);
    }
}
