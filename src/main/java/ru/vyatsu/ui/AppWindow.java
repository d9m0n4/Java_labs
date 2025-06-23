package ru.vyatsu.ui;

import javafx.application.Application;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import ru.vyatsu.db.DBConnection;
import ru.vyatsu.model.Purchase;

import java.sql.*;
import java.util.Date;

public class AppWindow extends Application {

    private TableView<Purchase> table;
    private ObservableList<Purchase> allPurchases;
    private FilteredList<Purchase> filteredPurchases;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Автозапчасти");

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 900, 500);
        stage.setScene(scene);

        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("Действия");

        MenuItem addSupplier = new MenuItem("Добавить поставщика");
        addSupplier.setOnAction(e -> new AddSupplierWindow());

        MenuItem addPart = new MenuItem("Добавить деталь");
        addPart.setOnAction(e -> new AddPartWindow());

        MenuItem addPurchase = new MenuItem("Добавить покупку");
        addPurchase.setOnAction(e -> {
            AddPurchaseWindow w = new AddPurchaseWindow();
            w.getStage().setOnHidden(ev -> loadPurchases());
        });

        MenuItem addOrUpdatePrice = new MenuItem("Добавить цену");
        addOrUpdatePrice.setOnAction(e -> new AddOrUpdatePriceWindow());

        menu.getItems().addAll(addSupplier, addPart, addPurchase, addOrUpdatePrice);
        menuBar.getMenus().add(menu);
        root.setTop(menuBar);

        TextField searchField = new TextField();
        searchField.setPromptText("Поиск по поставщику или детали...");
        searchField.setMinHeight(30);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = newVal.toLowerCase();
            filteredPurchases.setPredicate(p ->
                    p.getSupplierName().toLowerCase().contains(lower) ||
                            p.getPartName().toLowerCase().contains(lower)
            );
        });

        VBox searchBox = new VBox(searchField);
        searchBox.setPadding(new Insets(10));
        root.setBottom(searchBox);

        table = new TableView<>();
        createTableColumns();

        allPurchases = FXCollections.observableArrayList();
        filteredPurchases = new FilteredList<>(allPurchases, p -> true);
        table.setItems(filteredPurchases);
        table.setPlaceholder(new Label("Покупок нет"));

        VBox center = new VBox();
        center.setSpacing(10);
        center.setPadding(new Insets(10));
        Label title = new Label("История покупок");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        center.getChildren().addAll(title, table);

        root.setCenter(center);

        stage.show();

        loadPurchases();
    }

    private void createTableColumns() {
        TableColumn<Purchase, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> data.getValue().idProperty());

        TableColumn<Purchase, String> supplierCol = new TableColumn<>("Поставщик");
        supplierCol.setCellValueFactory(data -> data.getValue().supplierNameProperty());

        TableColumn<Purchase, String> partCol = new TableColumn<>("Деталь");
        partCol.setCellValueFactory(data -> data.getValue().partNameProperty());

        TableColumn<Purchase, Date> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(data -> data.getValue().purchaseDateProperty());

        TableColumn<Purchase, Number> qtyCol = new TableColumn<>("Количество");
        qtyCol.setCellValueFactory(data -> data.getValue().quantityProperty());

        TableColumn<Purchase, Number> priceCol = new TableColumn<>("Цена");
        priceCol.setCellValueFactory(data -> data.getValue().priceAtPurchaseProperty());

        table.getColumns().addAll(idCol, supplierCol, partCol, dateCol, qtyCol, priceCol);
    }

    private void loadPurchases() {
        allPurchases.clear();

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
                Purchase p = new Purchase(
                        rs.getInt("id"),
                        rs.getString("supplier_name"),
                        rs.getString("part_name"),
                        rs.getDate("purchase_date"),
                        rs.getInt("quantity"),
                        rs.getDouble("price_at_purchase")
                );
                allPurchases.add(p);
            }

        } catch (SQLException e) {
            showError("Ошибка при загрузке покупок: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Произошла ошибка");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
