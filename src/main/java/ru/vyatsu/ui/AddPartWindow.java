package ru.vyatsu.ui;

import ru.vyatsu.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddPartWindow extends JFrame {

    public AddPartWindow() {
        setTitle("Добавить деталь");
        setSize(400, 250);
        setLocationRelativeTo(null);

        setLayout(new GridLayout(5, 2, 10, 10));

        JPanel mainpanel = new JPanel(new GridLayout(5, 2, 10, 10));

        JLabel nameLabel = new JLabel("Название детали:");
        JTextField nameField = new JTextField();

        JLabel articleLabel = new JLabel("Артикул:");
        JTextField articleField = new JTextField();

        JButton addButton = new JButton("Добавить");

        mainpanel.add(nameLabel);
        mainpanel.add(nameField);
        mainpanel.add(articleLabel);
        mainpanel.add(articleField);
        mainpanel.add(new JLabel());
        mainpanel.add(addButton);

        JPanel paddingPanel = new JPanel(new BorderLayout());
        paddingPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        paddingPanel.add(mainpanel, BorderLayout.CENTER);

        setContentPane(paddingPanel);

        addButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String articleNumber = articleField.getText().trim();

            if (name.isEmpty() || articleNumber.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Пожалуйста, заполните все поля", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO part(article_number, name) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, articleNumber);
                    stmt.setString(2, name);
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Деталь добавлена!");
                    dispose();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Ошибка при добавлении детали: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        setVisible(true);
    }
}
