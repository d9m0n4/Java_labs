package ru.vyatsu;

import java.sql.*;
import java.util.Scanner;

public class Main {
    private Connection conn;

    // Конструктор: создаём подключение к базе
    public Main() throws SQLException {
        Database db = new Database();
        this.conn = db.getConnection();
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

    public void deletePricesLowerThan(double threshold) throws SQLException {
        String sql = "DELETE FROM part_price WHERE price < ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, threshold);
            int deleted = stmt.executeUpdate();
            System.out.println("Удалено записей: " + deleted);
        }
    }

    public void increaseAllPricesByPercent(double percent) throws SQLException {
        String sql = "UPDATE part_price SET price = price * (1 + ? / 100.0)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, percent);
            int updated = stmt.executeUpdate();
            System.out.println("Обновлено записей: " + updated);
        }
    }


    public void close() throws SQLException {
        if (conn != null) conn.close();
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Main app = new Main();

            while (true) {
                System.out.println("\nВыберите действие:");
                System.out.println("1 - Добавить поставщика");
                System.out.println("2 - Добавить деталь");
                System.out.println("3 - Добавить цену детали у поставщика");
                System.out.println("4 - Зарегистрировать покупку");
                System.out.println("5 - Показать список покупок");
                System.out.println("6 - Показать все цены");
                System.out.println("7 - Показать цены выше заданного значения");
                System.out.println("8 - Показать цены в диапазоне");
                System.out.println("9 - Удалить цены ниже заданной");
                System.out.println("10 - Увеличить все цены на 5%");
                System.out.println("0 - Выход");
                System.out.print("Введите номер действия: ");

                int choice;
                try {
                    choice = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Пожалуйста, введите корректное число.");
                    continue;
                }

                if (choice == 0) {
                    System.out.println("Выход.");
                    break;
                }

                try {
                    switch (choice) {
                        case 1 -> {
                            System.out.print("Имя поставщика: ");
                            String name = scanner.nextLine();
                            System.out.print("Адрес: ");
                            String address = scanner.nextLine();
                            System.out.print("Телефон: ");
                            String phone = scanner.nextLine();
                            app.addSupplier(name, address, phone);
                        }
                        case 2 -> {
                            System.out.print("Артикул детали: ");
                            String article = scanner.nextLine();
                            System.out.print("Название детали: ");
                            String partName = scanner.nextLine();
                            app.addPart(article, partName);
                        }
                        case 3 -> {
                            System.out.print("ID поставщика: ");
                            int sid = Integer.parseInt(scanner.nextLine());
                            System.out.print("ID детали: ");
                            int pid = Integer.parseInt(scanner.nextLine());
                            System.out.print("Цена: ");
                            double price = Double.parseDouble(scanner.nextLine());
                            System.out.print("Дата начала действия цены (ГГГГ-ММ-ДД): ");
                            Date startDate = Date.valueOf(scanner.nextLine());
                            app.addPartPrice(sid, pid, price, startDate);
                        }
                        case 4 -> {
                            System.out.print("ID поставщика: ");
                            int sid = Integer.parseInt(scanner.nextLine());
                            System.out.print("ID детали: ");
                            int pid = Integer.parseInt(scanner.nextLine());
                            System.out.print("Дата покупки (ГГГГ-ММ-ДД): ");
                            Date purchaseDate = Date.valueOf(scanner.nextLine());
                            System.out.print("Количество: ");
                            int qty = Integer.parseInt(scanner.nextLine());
                            app.addPurchase(sid, pid, purchaseDate, qty);
                        }
                        case 5 -> app.listPurchases();
                        case 6 -> app.listAllPrices();
                        case 7 -> {
                            System.out.print("Введите минимальную цену: ");
                            double minPrice = Double.parseDouble(scanner.nextLine());
                            app.listPricesGreaterThan(minPrice);
                        }
                        case 8 -> {
                            System.out.print("Минимальная цена: ");
                            double min = Double.parseDouble(scanner.nextLine());
                            System.out.print("Максимальная цена: ");
                            double max = Double.parseDouble(scanner.nextLine());
                            app.listPricesInRange(min, max);
                        }
                        case 9 -> {
                            System.out.print("Удалить все цены ниже: ");
                            double threshold = Double.parseDouble(scanner.nextLine());
                            app.deletePricesLowerThan(threshold);
                        }
                        case 10 -> app.increaseAllPricesByPercent(5);
                        default -> System.out.println("Неверный выбор. Попробуйте снова.");
                    }
                } catch (SQLException e) {
                    System.out.println("Ошибка при работе с базой: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    System.out.println("Неверный формат даты. Используйте ГГГГ-ММ-ДД");
                }
            }

            app.close();

        } catch (SQLException e) {
            System.out.println("Ошибка подключения к базе: " + e.getMessage());
        }
    }
}
