package com.capgemini.freebirds.presentation;

import com.capgemini.freebirds.model.BoatReservation;
import com.capgemini.freebirds.model.CustomerReservation;
import com.capgemini.freebirds.service.BoatService;
import com.capgemini.freebirds.service.CustomerService;
import com.capgemini.freebirds.service.ReservationService;
import com.toedter.calendar.JCalendar;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;

public class SwingUi implements UserInterface {

    private final BoatService boatService;
    private final CustomerService customerService;
    private final ReservationService reservationService = new ReservationService();
    private JCalendar calendar;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> dropdown;

    public SwingUi(BoatService boatService, CustomerService customerService) {
        this.boatService = boatService;
        this.customerService = customerService;
    }

    @Override
    public void start() {
        JFrame frame = new JFrame("Molveno Boat Reservation System");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);
        frame.add(buildTopBar(), BorderLayout.NORTH);

        // maakt de reserveringen tabel met inhoud
        var table = buildTable();
        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setPreferredSize(new Dimension(800, 170));
        topBar.add(buildLogo(), BorderLayout.WEST);
        topBar.add(buildSearchPanel(), BorderLayout.CENTER);
        topBar.add(buildButtonsPanel(), BorderLayout.EAST);
        return topBar;
    }

    private JPanel buildLogo() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        try {
            URL imgUrl = getClass().getResource("/Logo.png");
            ImageIcon icon = new ImageIcon(imgUrl);
            Image scaled = icon.getImage().getScaledInstance(-1, 150, Image.SCALE_SMOOTH);
            wrapper.add(new JLabel(new ImageIcon(scaled)));
        } catch (Exception e) {
            wrapper.add(new JLabel("LOGO"));
        }
        return wrapper;
    }

    private JPanel buildButtonsPanel() {
        JPanel buttonsPanel = new JPanel(new GridLayout(3, 1, 5, 5));

        JButton reservationButton = new JButton("Book Reservation");
        reservationButton.addActionListener(e -> {
            new ReservationForm(boatService.getBoatList(), customerService, reservationService).show();
            refreshTable();
        });

        JButton customerButton = new JButton("Customer database");
        customerButton.addActionListener(e -> new CustomerScreen(customerService.getCustomerList(), customerService).show());



        JButton boatButton = new JButton("Boat database");

        boatButton.addActionListener(e -> {
            System.out.println("Button pressed!");
            BoatScreen.Show();
        });



        buttonsPanel.add(reservationButton);
        buttonsPanel.add(customerButton);
        buttonsPanel.add(boatButton);



        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return buttonsPanel;
    }

    private JPanel buildSearchPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout(10, 5));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // zoekbalk links - compact
        JPanel zoekPanel = new JPanel(new GridLayout(2, 1, 3, 3));
        zoekPanel.setPreferredSize(new Dimension(150, 80));

        JPanel dropdownRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        dropdownRow.add(new JLabel("Search:"));
        dropdown = new JComboBox<>(new String[]{"Reservation number", "Surname", "Phone number"});
        dropdown.setPreferredSize(new Dimension(120, 24));
        dropdownRow.add(dropdown);

        searchField = new JTextField();
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                refreshTable();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                refreshTable();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
            }
        });

        zoekPanel.add(dropdownRow);
        zoekPanel.add(searchField);

        // datumkiezer - standaard vandaag
        calendar = new JCalendar();
        calendar.setCalendar(Calendar.getInstance());
        calendar.setPreferredSize(new Dimension(250, 150));
        calendar.getDayChooser().addPropertyChangeListener("day", e -> refreshTable());

        JPanel zoekWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        zoekWrapper.add(zoekPanel);
        searchPanel.add(zoekWrapper, BorderLayout.WEST);
        searchPanel.add(calendar, BorderLayout.CENTER);

        return searchPanel;
    }

    private JTable buildTable() {
        String[] columns = {"ID", "Customer", "Date", "Starting time", "Finishing time", "Boats"};
        tableModel = new DefaultTableModel(columns, 0);
        LocalDate today = LocalDate.now();
        for (CustomerReservation r : reservationService.getAll()) {
            if (!r.beginDateTime().toLocalDate().equals(today))
                continue;
            String klant = r.customer().firstName() + " " + r.customer().lastName();
            BoatReservation[] boats = r.boat();

            String boot;
            if (boats != null && boats.length > 0) {
                boot = boats[0].boat().type();
            } else {
                boot = "-";
            }
            tableModel.addRow(new Object[]{r.uniqueReservationId(), klant, r.beginDateTime().toLocalDate().toString(), r.beginDateTime().toLocalTime().toString(), r.endDateTime().toLocalTime().toString(), boot});
        }

        JTable table = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.getTableHeader().setReorderingAllowed(false);

        //Wat er gebeurt bij een dubbelklik
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int viewRow = table.getSelectedRow();
                    int value = (int) table.getValueAt(viewRow, 0);
                    CustomerReservation customerReservation = reservationService.read(value);
                    CustomerReservationDetails.createAndShowGUI(customerReservation, reservationService, SwingUi.this::refreshTable);
                }
            }
        });
        return table;
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        LocalDate selectedDate = calendar.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String zoekterm = searchField.getText().toLowerCase().trim();
        String criteria = (String) dropdown.getSelectedItem();

        for (CustomerReservation r : reservationService.getAll()) {
            if (!r.beginDateTime().toLocalDate().equals(selectedDate))
                continue;

            if (!zoekterm.isEmpty()) {
                boolean match;
                if (criteria.equals("Surname")) {
                    match = r.customer().lastName().toLowerCase().contains(zoekterm);
                } else if (criteria.equals("Phone number")) {
                    match = r.customer().phoneNumber().toLowerCase().contains(zoekterm);
                } else {
                    match = String.valueOf(r.uniqueReservationId()).contains(zoekterm);
                }
                if (!match)
                    continue;
            }

            String klant = r.customer().firstName() + " " + r.customer().lastName();
            String boot = r.boat().length > 0 ? r.boat()[0].boat().type() : "-";
            tableModel.addRow(new Object[]{r.uniqueReservationId(), klant, r.beginDateTime().toLocalDate().toString(), r.beginDateTime().toLocalTime().toString(), r.endDateTime().toLocalTime().toString(), boot});
        }
    }
}