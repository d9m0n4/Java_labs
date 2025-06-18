package ru.vyatsu;

import ru.vyatsu.ui.AddPartWindow;
import ru.vyatsu.ui.AddPurchaseWindow;
import ru.vyatsu.ui.AddSupplierWindow;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppWindow extends JFrame {

    private JPanel contentPanel;
    private Connection conn;

    public AppWindow () {

        setTitle("Автозапчасти");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Действия");

        JMenuItem supplierItem = new JMenuItem("Добавить поставщика");
        supplierItem.addActionListener(e -> new AddSupplierWindow());

        JMenuItem partItem = new JMenuItem("Добавить деталь");
        partItem.addActionListener(e -> new AddPartWindow());

        JMenuItem purchaseItem = new JMenuItem("Добавить покупку");
        purchaseItem.addActionListener(e -> new AddPurchaseWindow(this));

        menu.add(supplierItem);
        menu.add(partItem);
        menu.add(purchaseItem);

        menuBar.add(menu);
        setJMenuBar(menuBar);

        contentPanel = new JPanel(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);

        showPurchases();

        setVisible(true);

    }

    public void showPurchases() {
        List<Purchase> purchases = fetchPurchases();

        contentPanel.removeAll();

        if (purchases.isEmpty()) {
            JLabel noDataLabel = new JLabel("Покупок нет", SwingConstants.CENTER);
            noDataLabel.setFont(new Font("Arial", Font.BOLD, 18));
            contentPanel.add(noDataLabel, BorderLayout.CENTER);
        } else {
            String[] columns = {"ID", "Поставщик", "Деталь", "Дата", "Количество", "Цена"};
            Object[][] data = new Object[purchases.size()][columns.length];

            for (int i = 0; i < purchases.size(); i++) {
                Purchase p = purchases.get(i);
                data[i][0] = p.id;
                data[i][1] = p.supplierName;
                data[i][2] = p.partName;
                data[i][3] = p.purchaseDate;
                data[i][4] = p.quantity;
                data[i][5] = p.priceAtPurchase;
            }

            JTable table = new JTable(data, columns);
            JScrollPane scrollPane = new JScrollPane(table);

            JPanel panelWithLabel = new JPanel(new BorderLayout());
            JLabel titleLabel = new JLabel("История покупок", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

            panelWithLabel.add(titleLabel, BorderLayout.NORTH);
            panelWithLabel.add(scrollPane, BorderLayout.CENTER);

            contentPanel.add(panelWithLabel, BorderLayout.CENTER);
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private List<Purchase> fetchPurchases() {
        List<Purchase> list = new ArrayList<>();
        String sql = """
                SELECT p.id, s.name AS supplier_name, pa.name AS part_name, p.purchase_date, p.quantity, p.price_at_purchase
                FROM purchase p
                JOIN supplier s ON p.supplier_id = s.id
                JOIN part pa ON p.part_id = pa.id
                ORDER BY p.purchase_date DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Purchase p = new Purchase();
                p.id = rs.getInt("id");
                p.supplierName = rs.getString("supplier_name");
                p.partName = rs.getString("part_name");
                p.purchaseDate = rs.getDate("purchase_date");
                p.quantity = rs.getInt("quantity");
                p.priceAtPurchase = rs.getDouble("price_at_purchase");
                list.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при загрузке покупок: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    private static class Purchase {
        int id;
        String supplierName;
        String partName;
        Date purchaseDate;
        int quantity;
        double priceAtPurchase;
    }
}
