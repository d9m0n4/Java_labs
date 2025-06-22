package ru.vyatsu.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import ru.vyatsu.db.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class AddPurchaseWindow {

    private final Map<String, Integer> supplierMap = new LinkedHashMap<>();
    private final Map<String, Integer> partMap = new LinkedHashMap<>();
    private final Stage stage;

    public AddPurchaseWindow() {
        this.stage = new Stage();
        stage.setTitle("Добавить покупку");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setVgap(10);
        grid.setHgap(10);

        ComboBox<String> supplierCombo = new ComboBox<>();
        ComboBox<String> partCombo = new ComboBox<>();
        TextField dateField = new TextField(LocalDate.now().toString());
        TextField quantityField = new TextField();
        TextField priceField = new TextField();
        Button addButton = new Button("Добавить");

        grid.add(new Label("Поставщик:"), 0, 0);
        grid.add(supplierCombo, 1, 0);
        grid.add(new Label("Деталь:"), 0, 1);
        grid.add(partCombo, 1, 1);
        grid.add(new Label("Дата (ГГГГ-ММ-ДД):"), 0, 2);
        grid.add(dateField, 1, 2);
        grid.add(new Label("Количество:"), 0, 3);
        grid.add(quantityField, 1, 3);
        grid.add(new Label("Цена:"), 0, 4);
        grid.add(priceField, 1, 4);
        grid.add(addButton, 1, 5);

        loadSuppliers(supplierCombo);
        loadParts(partCombo);

        addButton.setOnAction(e -> {
            String supplier = supplierCombo.getValue();
            String part = partCombo.getValue();
            String date = dateField.getText().trim();
            String quantityText = quantityField.getText().trim();
            String priceText = priceField.getText().trim();

            if (supplier == null || part == null || date.isEmpty() || quantityText.isEmpty() || priceText.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Пожалуйста, заполните все поля");
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

                    showAlert(Alert.AlertType.INFORMATION, "Успех", "Покупка добавлена!");
                    stage.close();


                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Ошибка при добавлении", ex.getMessage());
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Неверный формат количества или цены");
            }
        });

        Scene scene = new Scene(grid, 420, 300);
        stage.setScene(scene);
        stage.show();
    }

    private void loadSuppliers(ComboBox<String> comboBox) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, name FROM supplier");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("name");
                int id = rs.getInt("id");
                supplierMap.put(name, id);
                comboBox.getItems().add(name);
            }
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Ошибка загрузки поставщиков", ex.getMessage());
        }
    }

    private void loadParts(ComboBox<String> comboBox) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, name FROM part");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("name");
                int id = rs.getInt("id");
                partMap.put(name, id);
                comboBox.getItems().add(name);
            }
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Ошибка загрузки деталей", ex.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Stage getStage() {
        return stage;
    }
}
