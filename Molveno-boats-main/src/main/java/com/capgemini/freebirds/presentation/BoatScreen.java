package com.capgemini.freebirds.presentation;

import com.capgemini.freebirds.data.BoatRepo;
import com.capgemini.freebirds.model.Boat;
import com.capgemini.freebirds.service.UpdateBoats;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.List;

public class BoatScreen {

    static public void Show() {
        JFrame botenOverzicht = new JFrame("Boat Overview");
        botenOverzicht.setSize(800, 600);
        botenOverzicht.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // sluit alleen dit venster
        JPanel topPanel = new JPanel();
        topPanel.setPreferredSize(new Dimension(800, 60));
        topPanel.setLayout(new GridBagLayout());
        JButton botenUpdatenKnop = new JButton("Update Boats");


        topPanel.add(botenUpdatenKnop);
        botenUpdatenKnop.setPreferredSize(new Dimension(200, 50));
        botenOverzicht.add(topPanel, BorderLayout.NORTH);
        var boatTable = buildBoatTable();
        var boatTableScrollPane = new JScrollPane(boatTable);

        botenUpdatenKnop.addActionListener(e -> {
            UpdateBoats.update();
            RefreshBoatList(boatTable);
            botenOverzicht.revalidate();
            botenOverzicht.repaint();
        });

        botenOverzicht.add(boatTableScrollPane, BorderLayout.CENTER);
        botenOverzicht.setLocationRelativeTo(null);
        botenOverzicht.setVisible(true);

    }

    private static TableModel buildBoatTableModel() {
        String[] columns = {"Boot ID", "Type", "Capacity", "Price per 15 minutes", "Price per 30 minutes", "Properties"};
        BoatRepo boatRepo = new BoatRepo();

        List<Boat> boats = boatRepo.load();

        Object[][] data = new Object[boats.size()][6];
        for (int i = 0; i < boats.size(); i++) {
            Boat b = boats.get(i);
            data[i] = new Object[]{b.uniqueId(), b.type(), b.capacity(), b.pricePer15Min(), b.pricePer30Min(), String.join(", ", b.properties())};
        }

        TableModel model = new DefaultTableModel(data, columns);
        return model;
    }

    private static JTable buildBoatTable() {
        TableModel model = buildBoatTableModel();

        JTable table = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        return table;
    }

    private static void RefreshBoatList(JTable boatTable) {
        var model = buildBoatTableModel();
        boatTable.setModel(model);
    }
}