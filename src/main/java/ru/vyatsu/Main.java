package ru.vyatsu;

import ru.vyatsu.service.PartPriceService;

import java.sql.*;
import java.util.Scanner;

public class Main {
    private Connection conn;
    private final PartPriceService partPriceService;

    public Main() throws SQLException {
        Database db = new Database();
        this.conn = db.getConnection();
        this.partPriceService = new PartPriceService(conn);
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
                            app.partPriceService.addSupplier(name, address, phone);
                        }
                        case 2 -> {
                            System.out.print("Артикул детали: ");
                            String article = scanner.nextLine();
                            System.out.print("Название детали: ");
                            String partName = scanner.nextLine();
                            app.partPriceService.addPart(article, partName);
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
                            app.partPriceService.addPartPrice(sid, pid, price, startDate);
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
                            app.partPriceService.addPurchase(sid, pid, purchaseDate, qty);
                        }
                        case 5 -> app.partPriceService.listPurchases();
                        case 6 -> app.partPriceService.listAllPrices();
                        case 7 -> {
                            System.out.print("Введите минимальную цену: ");
                            double minPrice = Double.parseDouble(scanner.nextLine());
                            app.partPriceService.listPricesGreaterThan(minPrice);
                        }
                        case 8 -> {
                            System.out.print("Минимальная цена: ");
                            double min = Double.parseDouble(scanner.nextLine());
                            System.out.print("Максимальная цена: ");
                            double max = Double.parseDouble(scanner.nextLine());
                            app.partPriceService.listPricesInRange(min, max);
                        }
                        case 9 -> {
                            System.out.print("Удалить все цены ниже: ");
                            double threshold = Double.parseDouble(scanner.nextLine());
                            app.partPriceService.deletePricesLowerThan(threshold);
                        }
                        case 10 -> app.partPriceService.increaseAllPricesByPercent(5);
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
