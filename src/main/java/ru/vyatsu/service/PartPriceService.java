package ru.vyatsu.service;

import java.sql.*;

public class PartPriceService {

    private final Connection conn;

    public PartPriceService(final Connection conn) {
        this.conn = conn;
    }

    // Добавление поставщика
    public void addSupplier(String name, String address, String phone) throws SQLException {
        String sql = "INSERT INTO supplier(name, address, phone) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, address);
            stmt.setString(3, phone);
            stmt.executeUpdate();
            System.out.println("Supplier added");
        }
    }

    // Добавление детали
    public void addPart(String articleNumber, String name) throws SQLException {
        String sql = "INSERT INTO part(article_number, name) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, articleNumber);
            stmt.setString(2, name);
            stmt.executeUpdate();
            System.out.println("Part added");
        }
    }

    // Добавление цены детали у поставщика (с датой начала действия)
    public void addPartPrice(int supplierId, int partId, double price, Date startDate) throws SQLException {
        String sql = "INSERT INTO part_price(supplier_id, part_id, price, start_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, supplierId);
            stmt.setInt(2, partId);
            stmt.setDouble(3, price);
            stmt.setDate(4, startDate);
            stmt.executeUpdate();
            System.out.println("Part price added");
        }
    }

    // Получение цены детали у поставщика на конкретную дату (последняя цена до даты)
    public Double getPriceAtDate(int supplierId, int partId, Date purchaseDate) throws SQLException {
        String priceSql = """
                SELECT price FROM part_price
                WHERE supplier_id = ? AND part_id = ? AND start_date <= ?
                ORDER BY start_date DESC LIMIT 1
                """;
        try (PreparedStatement priceStmt = conn.prepareStatement(priceSql)) {
            priceStmt.setInt(1, supplierId);
            priceStmt.setInt(2, partId);
            priceStmt.setDate(3, purchaseDate);
            ResultSet rs = priceStmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("price");
            } else {
                return null;
            }
        }
    }

    // Добавление покупки с сохранением цены на момент покупки
    public void addPurchase(int supplierId, int partId, Date purchaseDate, int quantity) throws SQLException {
        Double priceAtPurchase = getPriceAtDate(supplierId, partId, purchaseDate);
        if (priceAtPurchase == null) {
            System.out.println("Price not found for this part at the given date.");
            return;
        }

        String purchaseSql = """
                INSERT INTO purchase(supplier_id, part_id, purchase_date, quantity, price_at_purchase)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement purchaseStmt = conn.prepareStatement(purchaseSql)) {
            purchaseStmt.setInt(1, supplierId);
            purchaseStmt.setInt(2, partId);
            purchaseStmt.setDate(3, purchaseDate);
            purchaseStmt.setInt(4, quantity);
            purchaseStmt.setDouble(5, priceAtPurchase);
            purchaseStmt.executeUpdate();
            System.out.println("Purchase recorded");
        }
    }

    // Вывод списка всех покупок с деталями и ценой
    public void listPurchases() throws SQLException {
        String sql = """
                SELECT p.id, s.name AS supplier_name, pa.name AS part_name, p.purchase_date, p.quantity, p.price_at_purchase
                FROM purchase p
                JOIN supplier s ON p.supplier_id = s.id
                JOIN part pa ON p.part_id = pa.id
                ORDER BY p.purchase_date DESC
                """;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("Purchases:");
            while (rs.next()) {
                System.out.printf("ID: %d | Supplier: %s | Part: %s | Date: %s | Quantity: %d | Price: %.2f\n",
                        rs.getInt("id"),
                        rs.getString("supplier_name"),
                        rs.getString("part_name"),
                        rs.getDate("purchase_date"),
                        rs.getInt("quantity"),
                        rs.getDouble("price_at_purchase"));
            }
        }
    }

    // Вывод всех цен
    public void listAllPrices() throws SQLException {
        String sql = "SELECT * FROM part_price";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("Все цены деталей:");
            while (rs.next()) {
                System.out.printf("ID: %d | Supplier ID: %d | Part ID: %d | Price: %.2f | Start Date: %s\n",
                        rs.getInt("id"), rs.getInt("supplier_id"), rs.getInt("part_id"),
                        rs.getDouble("price"), rs.getDate("start_date"));
            }
        }
    }

    // Вывод цен больше чем
    public void listPricesGreaterThan(double threshold) throws SQLException {
        String sql = "SELECT * FROM part_price WHERE price > ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, threshold);
            ResultSet rs = stmt.executeQuery();
            System.out.println("Цены выше " + threshold + ":");
            while (rs.next()) {
                System.out.printf("Part ID: %d | Supplier ID: %d | Price: %.2f\n",
                        rs.getInt("part_id"), rs.getInt("supplier_id"), rs.getDouble("price"));
            }
        }
    }

    // Вывод цен в диапазоне
    public void listPricesInRange(double min, double max) throws SQLException {
        String sql = "SELECT * FROM part_price WHERE price BETWEEN ? AND ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, min);
            stmt.setDouble(2, max);
            ResultSet rs = stmt.executeQuery();
            System.out.println("Цены от " + min + " до " + max + ":");
            while (rs.next()) {
                System.out.printf("Part ID: %d | Supplier ID: %d | Price: %.2f\n",
                        rs.getInt("part_id"), rs.getInt("supplier_id"), rs.getDouble("price"));
            }
        }
    }

    // Удаление цен ниже чем
    public void deletePricesLowerThan(double threshold) throws SQLException {
        String sql = "DELETE FROM part_price WHERE price < ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, threshold);
            int deleted = stmt.executeUpdate();
            System.out.println("Удалено записей: " + deleted);
        }
    }

    // Увеличение цены на %
    public void increaseAllPricesByPercent(double percent) throws SQLException {
        String sql = "UPDATE part_price SET price = price * (1 + ? / 100.0)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, percent);
            int updated = stmt.executeUpdate();
            System.out.println("Обновлено записей: " + updated);
        }
    }
}
