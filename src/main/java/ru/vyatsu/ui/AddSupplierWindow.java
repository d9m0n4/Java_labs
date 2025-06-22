package ru.vyatsu.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import ru.vyatsu.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddSupplierWindow {

    private final Stage stage;

    public AddSupplierWindow() {
        stage = new Stage();
        stage.setTitle("Добавить поставщика");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setVgap(10);
        grid.setHgap(10);

        Label nameLabel = new Label("Имя:");
        TextField nameField = new TextField();

        Label addressLabel = new Label("Адрес:");
        TextField addressField = new TextField();

        Label phoneLabel = new Label("Телефон:");
        TextField phoneField = new TextField();

        Button addButton = new Button("Добавить");

        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(addressLabel, 0, 1);
        grid.add(addressField, 1, 1);
        grid.add(phoneLabel, 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(addButton, 1, 3);

        addButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String address = addressField.getText().trim();
            String phone = phoneField.getText().trim();

            if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Пожалуйста, заполните все поля");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO supplier(name, address, phone) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, name);
                    stmt.setString(2, address);
                    stmt.setString(3, phone);
                    stmt.executeUpdate();
                    showAlert(Alert.AlertType.INFORMATION, "Успех", "Поставщик добавлен!");
                    stage.close();
                }
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Ошибка при добавлении", ex.getMessage());
            }
        });

        Scene scene = new Scene(grid, 400, 250);
        stage.setScene(scene);
        stage.show();
    }

    public Stage getStage() {
        return stage;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
