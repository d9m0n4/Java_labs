package ru.vyatsu.ui;

import ru.vyatsu.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddSupplierWindow extends JFrame {

    public AddSupplierWindow() {
        setTitle("Добавить поставщика");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(5, 2, 10, 10));

        JLabel nameLabel = new JLabel("Имя:");
        JTextField nameField = new JTextField();

        JLabel addressLabel = new JLabel("Адрес:");
        JTextField addressField = new JTextField();

        JLabel phoneLabel = new JLabel("Телефон:");
        JTextField phoneField = new JTextField();

        JButton addButton = new JButton("Добавить");

        add(nameLabel); add(nameField);
        add(addressLabel); add(addressField);
        add(phoneLabel); add(phoneField);
        add(new JLabel());
        add(addButton);

        addButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String address = addressField.getText().trim();
            String phone = phoneField.getText().trim();

            if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Пожалуйста, заполните все поля", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO supplier(name, address, phone) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, name);
                    stmt.setString(2, address);
                    stmt.setString(3, phone);
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Поставщик добавлен!");
                    dispose();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка при добавлении поставщика: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        setVisible(true);
    }
}
