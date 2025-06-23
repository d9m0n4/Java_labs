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

    private ComboBox<String> supplierCombo;
    private ComboBox<String> partCombo;
    private TextField dateField;
    private TextField quantityField;
    private TextField priceField;

    public AddPurchaseWindow() {
        this.stage = new Stage();
        stage.setTitle("Добавить покупку");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setVgap(10);
        grid.setHgap(10);

        supplierCombo = new ComboBox<>();
        partCombo = new ComboBox<>();
        dateField = new TextField(LocalDate.now().toString());
        quantityField = new TextField();
        priceField = new TextField();
        priceField.setEditable(false); // Цена подтягивается автоматически, вводить нельзя

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

        loadSuppliers();

        // При выборе поставщика загружаем детали для этого поставщика
        supplierCombo.setOnAction(e -> {
            String selectedSupplier = supplierCombo.getValue();
            if (selectedSupplier != null) {
                int supplierId = supplierMap.get(selectedSupplier);
                loadPartsForSupplier(supplierId);
            }
            priceField.clear(); // Очистить цену при смене поставщика
            partCombo.getSelectionModel().clearSelection();
        });

        // При выборе детали подгружаем цену из part_price
        partCombo.setOnAction(e -> {
            String selectedSupplier = supplierCombo.getValue();
            String selectedPart = partCombo.getValue();
            if (selectedSupplier != null && selectedPart != null) {
                int supplierId = supplierMap.get(selectedSupplier);
                int partId = partMap.get(selectedPart);
                loadPrice(supplierId, partId);
            } else {
                priceField.clear();
            }
        });

        addButton.setOnAction(e -> addPurchase());

        Scene scene = new Scene(grid, 450, 320);
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

    private void loadPartsForSupplier(int supplierId) {
        String sql = """
            SELECT DISTINCT pa.id, pa.name
            FROM part pa
            JOIN part_price pp ON pa.id = pp.part_id
            WHERE pp.supplier_id = ?
            ORDER BY pa.name
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, supplierId);

            try (ResultSet rs = stmt.executeQuery()) {
                partMap.clear();
                partCombo.getItems().clear();

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    partMap.put(name, id);
                    partCombo.getItems().add(name);
                }
            }

        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Ошибка загрузки деталей", ex.getMessage());
        }
    }

    private void loadPrice(int supplierId, int partId) {
        String sql = "SELECT price FROM part_price WHERE supplier_id = ? AND part_id = ? ORDER BY start_date DESC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, supplierId);
            stmt.setInt(2, partId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double price = rs.getDouble("price");
                    priceField.setText(String.valueOf(price));
                } else {
                    priceField.clear();
                    showAlert(Alert.AlertType.WARNING, "Внимание", "Цена для выбранной детали не найдена.");
                }
            }

        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Ошибка загрузки цены", ex.getMessage());
        }
    }

    private void addPurchase() {
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
