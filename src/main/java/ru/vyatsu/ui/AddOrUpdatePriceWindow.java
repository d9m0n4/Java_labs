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

public class AddOrUpdatePriceWindow {

    private final Map<String, Integer> supplierMap = new LinkedHashMap<>();
    private final Map<String, Integer> partMap = new LinkedHashMap<>();
    private final Stage stage;

    private ComboBox<String> supplierCombo;
    private ComboBox<String> partCombo;
    private TextField priceField;
    private TextField startDateField;

    public AddOrUpdatePriceWindow() {
        stage = new Stage();
        stage.setTitle("Установить цену на деталь");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setVgap(10);
        grid.setHgap(10);

        supplierCombo = new ComboBox<>();
        partCombo = new ComboBox<>();
        priceField = new TextField();
        startDateField = new TextField(LocalDate.now().toString());

        Button saveButton = new Button("Сохранить");

        grid.add(new Label("Поставщик:"), 0, 0);
        grid.add(supplierCombo, 1, 0);
        grid.add(new Label("Деталь:"), 0, 1);
        grid.add(partCombo, 1, 1);
        grid.add(new Label("Цена:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Дата начала действия (ГГГГ-ММ-ДД):"), 0, 3);
        grid.add(startDateField, 1, 3);
        grid.add(saveButton, 1, 4);

        loadSuppliers();

        supplierCombo.setOnAction(e -> {
            String selectedSupplier = supplierCombo.getValue();
            if (selectedSupplier != null) {
                loadAllParts();
            }
            partCombo.getSelectionModel().clearSelection();
        });

        saveButton.setOnAction(e -> savePrice());

        Scene scene = new Scene(grid, 450, 280);
        stage.setScene(scene);
        stage.show();
    }

    private void loadSuppliers() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, name FROM supplier ORDER BY name");
             ResultSet rs = stmt.executeQuery()) {

            supplierMap.clear();
            supplierCombo.getItems().clear();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                supplierMap.put(name, id);
                supplierCombo.getItems().add(name);
            }

        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Ошибка загрузки поставщиков", ex.getMessage());
        }
    }

    private void loadAllParts() {
        String sql = "SELECT id, name FROM part ORDER BY name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            partMap.clear();
            partCombo.getItems().clear();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                partMap.put(name, id);
                partCombo.getItems().add(name);
            }

        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Ошибка загрузки деталей", ex.getMessage());
        }
    }

    private void savePrice() {
        String supplier = supplierCombo.getValue();
        String part = partCombo.getValue();
        String priceText = priceField.getText().trim();
        String startDateText = startDateField.getText().trim();

        if (supplier == null || part == null || priceText.isEmpty() || startDateText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Пожалуйста, заполните все поля");
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            Date startDate = Date.valueOf(startDateText);
            int supplierId = supplierMap.get(supplier);
            int partId = partMap.get(part);

            // Можно добавить проверку, существует ли уже запись с таким supplier_id, part_id и start_date
            // Для простоты сейчас просто добавим новую запись

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO part_price (supplier_id, part_id, price, start_date) VALUES (?, ?, ?, ?)")) {
                stmt.setInt(1, supplierId);
                stmt.setInt(2, partId);
                stmt.setDouble(3, price);
                stmt.setDate(4, startDate);
                stmt.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Успех", "Цена успешно сохранена!");
                stage.close();

            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Ошибка при сохранении цены", ex.getMessage());
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Неверный формат цены");
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Неверный формат даты");
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
