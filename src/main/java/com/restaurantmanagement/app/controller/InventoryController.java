package com.restaurantmanagement.app.controller;

import com.restaurantmanagement.app.entity.InventoryLog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;

public class InventoryController {
    @FXML
    private ComboBox<String> transactionTypeFilterField;
    @FXML
    private TextField ingredientField, cancelIngredientField;
    @FXML
    private DatePicker dateFilterField;
    @FXML
    private TextField ingredientFilterField;
    @FXML
    private Button filterButton;
    @FXML
    private TextField cancelUnitField;
    @FXML
    private TextField unitField, supplierField, quantityField, priceField, cancelQuantityField, cancelNoteField;
    @FXML
    private TableView<InventoryLog> inventoryLogTable;
    @FXML
    private TableColumn<InventoryLog, String> logIngredientColumn, logTransactionTypeColumn, logNoteColumn, logUnitColumn, logDateColumn;
    @FXML
    private TableColumn<InventoryLog, Float> logQuantityColumn;
    @FXML
    private TableColumn<InventoryLog, Double> logPriceColumn;
    @FXML
    private Button addPurchaseButton, cancelIngredientButton;

    private Connection conn;

    public void initialize() {
        connectDB();
        loadTransactionTypes();
        loadIngredients();
        loadInventoryLog();

        logIngredientColumn.setCellValueFactory(cellData -> cellData.getValue().ingredientProperty());
        logTransactionTypeColumn.setCellValueFactory(cellData -> cellData.getValue().transactionTypeProperty());
        logQuantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        logPriceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        logNoteColumn.setCellValueFactory(cellData -> cellData.getValue().noteProperty());
        logUnitColumn.setCellValueFactory(cellData -> cellData.getValue().unitProperty());
        logDateColumn.setCellValueFactory(cellData -> cellData.getValue().logDateProperty());
    }

    private void connectDB() {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Restaurant", "root", "123456");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Connection Error", "Unable to connect to the database!", Alert.AlertType.ERROR);
        }
    }

    private void loadTransactionTypes() {
        String query = "SELECT TypeName FROM TransactionTypes";
        ObservableList<String> transactionTypes = FXCollections.observableArrayList();

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                transactionTypes.add(rs.getString("TypeName"));
            }
            transactionTypeFilterField.setItems(transactionTypes);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadIngredients() {
        String query = "SELECT Name FROM Ingredients";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                // Gán giá trị mặc định cho TextField
                ingredientField.setText(rs.getString("Name"));
                cancelIngredientField.setText(rs.getString("Name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void loadInventoryLog() {
        ObservableList<InventoryLog> logList = FXCollections.observableArrayList();
        String query = """
        SELECT i.Name AS Ingredient, t.TypeName, l.Quantity, l.Price, l.Note, l.TransactionDate, l.Unit
        FROM InventoryTransactions l
        JOIN Ingredients i ON l.IngredientID = i.IngredientID
        JOIN TransactionTypes t ON l.TransactionTypeID = t.TransactionTypeID
        ORDER BY l.TransactionDate DESC
    """;

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                logList.add(new InventoryLog(
                        rs.getString("Ingredient"),
                        rs.getString("TypeName"),
                        rs.getFloat("Quantity"),
                        rs.getString("Unit"),
                        rs.getDouble("Price"),
                        rs.getString("Note"),
                        rs.getString("TransactionDate")
                ));
            }
            inventoryLogTable.setItems(logList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddPurchase() {
        String supplier = supplierField.getText();
        String ingredientName = ingredientField.getText().trim();
        String quantityText = quantityField.getText();
        String priceText = priceField.getText();
        String unit = unitField.getText().trim();

        if (ingredientName.isEmpty() || quantityText.isEmpty() || priceText.isEmpty() || unit.isEmpty()) {
            showAlert("Error", "Please fill in all information, including the unit!", Alert.AlertType.ERROR);
            return;
        }

        try {
            float quantity = Float.parseFloat(quantityText);
            double price = Double.parseDouble(priceText);

            if (quantity <= 0 || price < 0) {
                showAlert("Error", "Quantity and price must be greater than 0!", Alert.AlertType.ERROR);
                return;
            }

            String getIngredientIdQuery = "SELECT IngredientID FROM Ingredients WHERE Name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(getIngredientIdQuery)) {
                stmt.setString(1, ingredientName);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    showAlert("Error", "Ingredient not found!", Alert.AlertType.ERROR);
                    return;
                }
                int ingredientId = rs.getInt("IngredientID");

                String getTransactionTypeIdQuery = "SELECT TransactionTypeID FROM TransactionTypes WHERE LOWER(TypeName) = LOWER(?)";
                try (PreparedStatement stmt2 = conn.prepareStatement(getTransactionTypeIdQuery)) {
                    stmt2.setString(1, "Stock In");
                    ResultSet rs2 = stmt2.executeQuery();

                    if (!rs2.next()) {
                        showAlert("Error", "Transaction type 'Stock In' does not exist!", Alert.AlertType.ERROR);
                        return;
                    }
                    int transactionTypeId = rs2.getInt("TransactionTypeID");

                    String insertTransactionQuery = """
                INSERT INTO InventoryTransactions (TransactionTypeID, SupplierName, IngredientID, Quantity, Price, Note, Unit)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
                    try (PreparedStatement stmt3 = conn.prepareStatement(insertTransactionQuery)) {
                        stmt3.setInt(1, transactionTypeId);
                        stmt3.setString(2, supplier);
                        stmt3.setInt(3, ingredientId);
                        stmt3.setFloat(4, quantity);
                        stmt3.setDouble(5, price);
                        stmt3.setString(6, "Stock In from supplier: " + supplier);
                        stmt3.setString(7, unit);
                        stmt3.executeUpdate();
                    }
                }
            }
            showAlert("Success", "Stock in successful!", Alert.AlertType.INFORMATION);
            loadInventoryLog();

            supplierField.clear();
            ingredientField.clear();
            quantityField.clear();
            priceField.clear();
            unitField.clear();
        } catch (SQLException | NumberFormatException e) {
            showAlert("Error", "Invalid data or connection error!", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelIngredient() {
        String ingredientName = cancelIngredientField.getText().trim();
        String quantityText = cancelQuantityField.getText();
        String note = cancelNoteField.getText();
        String unit = cancelUnitField.getText().trim();

        if (ingredientName.isEmpty() || quantityText.isEmpty() || note.isEmpty() || unit.isEmpty()) {
            showAlert("Error", "Please fill in all information, including the unit!", Alert.AlertType.ERROR);
            return;
        }

        try {
            float quantity = Float.parseFloat(quantityText);
            if (quantity <= 0) {
                showAlert("Error", "Quantity must be greater than 0!", Alert.AlertType.ERROR);
                return;
            }

            String getIngredientIdQuery = "SELECT IngredientID FROM Ingredients WHERE Name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(getIngredientIdQuery)) {
                stmt.setString(1, ingredientName);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    showAlert("Error", "Ingredient not found!", Alert.AlertType.ERROR);
                    return;
                }
                int ingredientId = rs.getInt("IngredientID");

                String getTransactionTypeIdQuery = "SELECT TransactionTypeID FROM TransactionTypes WHERE LOWER(TypeName) = LOWER(?)";
                try (PreparedStatement stmt2 = conn.prepareStatement(getTransactionTypeIdQuery)) {
                    stmt2.setString(1, "Cancel Ingredient");
                    ResultSet rs2 = stmt2.executeQuery();

                    if (!rs2.next()) {
                        showAlert("Error", "Transaction type 'Cancel Ingredient' does not exist!", Alert.AlertType.ERROR);
                        return;
                    }
                    int transactionTypeId = rs2.getInt("TransactionTypeID");

                    String insertTransactionQuery = """
                INSERT INTO InventoryTransactions (TransactionTypeID, IngredientID, Quantity, Price, Note, Unit)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
                    try (PreparedStatement stmt3 = conn.prepareStatement(insertTransactionQuery)) {
                        stmt3.setInt(1, transactionTypeId);
                        stmt3.setInt(2, ingredientId);
                        stmt3.setFloat(3, quantity);
                        stmt3.setDouble(4, 0.0);
                        stmt3.setString(5, note);
                        stmt3.setString(6, unit);
                        stmt3.executeUpdate();
                    }
                }
            }

            showAlert("Success", "Ingredient cancellation successful!", Alert.AlertType.INFORMATION);
            loadInventoryLog();

            cancelIngredientField.clear();
            cancelQuantityField.clear();
            cancelNoteField.clear();
            cancelUnitField.clear();
        } catch (SQLException | NumberFormatException e) {
            showAlert("Error", "Invalid data or connection error!", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFilter() {
        String ingredientFilter = ingredientFilterField.getText().trim();
        String dateFilter = (dateFilterField.getValue() != null) ? dateFilterField.getValue().toString() : "";
        String transactionTypeFilter = (transactionTypeFilterField.getValue() != null) ? transactionTypeFilterField.getValue() : "";

        loadInventoryLog(ingredientFilter, dateFilter, transactionTypeFilter);
    }

    private void loadInventoryLog(String ingredientFilter, String dateFilter, String transactionTypeFilter) {
        ObservableList<InventoryLog> logList = FXCollections.observableArrayList();

        StringBuilder query = new StringBuilder("""
            SELECT i.Name AS Ingredient, t.TypeName, l.Quantity, l.Price, l.Note, l.TransactionDate, l.Unit
            FROM InventoryTransactions l
            JOIN Ingredients i ON l.IngredientID = i.IngredientID
            JOIN TransactionTypes t ON l.TransactionTypeID = t.TransactionTypeID
            WHERE 1=1
        """);

        if (!ingredientFilter.isEmpty()) {
            query.append(" AND i.Name LIKE ?");
        }
        if (!dateFilter.isEmpty()) {
            query.append(" AND l.TransactionDate = ?");
        }
        if (!transactionTypeFilter.isEmpty()) {
            query.append(" AND t.TypeName = ?");
        }

        query.append(" ORDER BY l.TransactionDate DESC");

        try (PreparedStatement stmt = conn.prepareStatement(query.toString())) {
            int paramIndex = 1;

            if (!ingredientFilter.isEmpty()) {
                stmt.setString(paramIndex++, "%" + ingredientFilter + "%");
            }
            if (!dateFilter.isEmpty()) {
                stmt.setString(paramIndex++, dateFilter);
            }
            if (!transactionTypeFilter.isEmpty()) {
                stmt.setString(paramIndex++, transactionTypeFilter);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logList.add(new InventoryLog(
                            rs.getString("Ingredient"),
                            rs.getString("TypeName"),
                            rs.getFloat("Quantity"),
                            rs.getString("Unit"),
                            rs.getDouble("Price"),
                            rs.getString("Note"),
                            rs.getString("TransactionDate")
                    ));
                }
                inventoryLogTable.setItems(logList);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}