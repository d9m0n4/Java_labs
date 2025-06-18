package ru.vyatsu.ui;

import ru.vyatsu.AppWindow;
import ru.vyatsu.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;


public class AddPurchaseWindow extends JFrame {
    private JComboBox<String> supplierCombo;
    private JComboBox<String> partCombo;
    private JTextField dateField;
    private JTextField quantityField;
    private JTextField priceField;

    private AppWindow appWindow;

    private Map<String, Integer> supplierMap = new LinkedHashMap<>();
    private Map<String, Integer> partMap = new LinkedHashMap<>();

    public AddPurchaseWindow(AppWindow appWindow) {
        this.appWindow = appWindow;
        setTitle("Добавить покупку");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 2, 10, 10));

        JLabel supplierLabel = new JLabel("Поставщик:");
        supplierCombo = new JComboBox<>();
        loadSuppliers();

        JLabel partLabel = new JLabel("Деталь:");
        partCombo = new JComboBox<>();
        loadParts();

        JLabel dateLabel = new JLabel("Дата (ГГГГ-ММ-ДД):");
        dateField = new JTextField(LocalDate.now().toString());

        JLabel quantityLabel = new JLabel("Количество:");
        quantityField = new JTextField();

        JLabel priceLabel = new JLabel("Цена:");
        priceField = new JTextField();

        JButton addButton = new JButton("Добавить");

        add(supplierLabel); add(supplierCombo);
        add(partLabel); add(partCombo);
        add(dateLabel); add(dateField);
        add(quantityLabel); add(quantityField);
        add(priceLabel); add(priceField);
        add(new JLabel()); add(addButton);

        addButton.addActionListener(e -> addPurchase());

        setVisible(true);
    }

    private void loadSuppliers() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, name FROM supplier");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("name");
                int id = rs.getInt("id");
                supplierMap.put(name, id);
                supplierCombo.addItem(name);
            }
        } catch (SQLException ex) {
            showError("Ошибка загрузки поставщиков: " + ex.getMessage());
        }
    }

    private void loadParts() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, name FROM part");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("name");
                int id = rs.getInt("id");
                partMap.put(name, id);
                partCombo.addItem(name);
            }
        } catch (SQLException ex) {
            showError("Ошибка загрузки деталей: " + ex.getMessage());
        }
    }

    private void addPurchase() {
        String supplier = (String) supplierCombo.getSelectedItem();
        String part = (String) partCombo.getSelectedItem();
        String date = dateField.getText().trim();
        String quantityText = quantityField.getText().trim();
        String priceText = priceField.getText().trim();

        if (supplier == null || part == null || date.isEmpty() || quantityText.isEmpty() || priceText.isEmpty()) {
            showError("Пожалуйста, заполните все поля");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityText);
            double price = Double.parseDouble(priceText);
            int supplierId = supplierMap.get(supplier);
            int partId = partMap.get(part);

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO purchase(supplier_id, part_id, purchase_date, quantity, price_at_purchase) VALUES (?, ?, ?, ?, ?)")) {
                stmt.setInt(1, supplierId);
                stmt.setInt(2, partId);
                stmt.setDate(3, Date.valueOf(date));
                stmt.setInt(4, quantity);
                stmt.setDouble(5, price);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Покупка добавлена!");
                appWindow.showPurchases();
                dispose();
            }

        } catch (NumberFormatException ex) {
            showError("Неверный формат количества или цены");
        } catch (SQLException ex) {
            showError("Ошибка при добавлении покупки: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }
}
