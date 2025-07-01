package ru.vyatsu.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import ru.vyatsu.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddPartWindow {

    public AddPartWindow() {
        Stage stage = new Stage();
        stage.setTitle("Добавить деталь");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setVgap(10);
        grid.setHgap(10);

        Label nameLabel = new Label("Название детали:");
        TextField nameField = new TextField();

        Label articleLabel = new Label("Артикул:");
        TextField articleField = new TextField();

        Button addButton = new Button("Добавить");

        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(articleLabel, 0, 1);
        grid.add(articleField, 1, 1);
        grid.add(addButton, 1, 2);

        addButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String article = articleField.getText().trim();

            if (name.isEmpty() || article.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Пожалуйста, заполните все поля");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO part(article_number, name) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, article);
                    stmt.setString(2, name);
                    stmt.executeUpdate();
                    showAlert(Alert.AlertType.INFORMATION, "Успех", "Деталь добавлена!");
                    stage.close();
                }
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Ошибка при добавлении", ex.getMessage());
            }
        });

        Scene scene = new Scene(grid, 400, 200);
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
