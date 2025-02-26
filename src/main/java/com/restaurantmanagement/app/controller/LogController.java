package com.restaurantmanagement.app.controller;

import com.restaurantmanagement.app.entity.DailyStock;
import com.restaurantmanagement.database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LogController {

    @FXML private TableView<DailyStock> dailyStockTable;
    @FXML private TableColumn<DailyStock, Integer> stockIDColumn;
    @FXML private TableColumn<DailyStock, Integer> ingredientIDColumn;
    @FXML private TableColumn<DailyStock, String> nameColumn;
    @FXML private TableColumn<DailyStock, Integer> categoryIDColumn;
    @FXML private TableColumn<DailyStock, String> unitColumn;
    @FXML private TableColumn<DailyStock, Double> stockColumn;
    @FXML private TableColumn<DailyStock, Double> minStockColumn;
    @FXML private TableColumn<DailyStock, Double> priceColumn;
    @FXML private TableColumn<DailyStock, String> dateColumn;

    @FXML private TextField nameSearchField;
    @FXML private DatePicker dateSearchField;
    @FXML private Button refreshButton;

    private final ObservableList<DailyStock> dailyStockList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        stockIDColumn.setCellValueFactory(cellData
                -> cellData.getValue().stockIDProperty().asObject());
        ingredientIDColumn.setCellValueFactory(cellData
                -> cellData.getValue().ingredientIDProperty().asObject());
        nameColumn.setCellValueFactory(cellData
                -> cellData.getValue().nameProperty());
        categoryIDColumn.setCellValueFactory(cellData
                -> cellData.getValue().categoryIDProperty().asObject());
        unitColumn.setCellValueFactory(cellData
                -> cellData.getValue().unitProperty());
        stockColumn.setCellValueFactory(cellData
                -> cellData.getValue().stockProperty().asObject());
        minStockColumn.setCellValueFactory(cellData
                -> cellData.getValue().minStockProperty().asObject());
        priceColumn.setCellValueFactory(cellData
                -> cellData.getValue().pricePerUnitProperty().asObject());
        dateColumn.setCellValueFactory(cellData
                -> cellData.getValue().dateProperty());
        nameSearchField.setStyle("-fx-text-fill: black;");
        dateSearchField.getEditor().setStyle("-fx-text-fill: black;");
        loadDailyStockData();
    }

    private void loadDailyStockData() {
        dailyStockList.clear();
        String sql = "SELECT * FROM DailyStock";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                dailyStockList.add(new DailyStock(
                        rs.getInt("StockID"),
                        rs.getInt("IngredientID"),
                        rs.getString("Name"),
                        rs.getInt("CategoryID"),
                        rs.getString("Unit"),
                        rs.getDouble("Stock"),
                        rs.getDouble("MinStock"),
                        rs.getDouble("PricePerUnit"),
                        rs.getString("Date")
                ));
            }

            dailyStockTable.setItems(dailyStockList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        String nameFilter = nameSearchField.getText().toLowerCase();
        String dateFilter = dateSearchField.getValue() != null ? dateSearchField.getValue().toString() : "";

        ObservableList<DailyStock> filteredList = FXCollections.observableArrayList();

        for (DailyStock stock : dailyStockList) {
            boolean matchesName = stock.getName().toLowerCase().contains(nameFilter);
            boolean matchesDate = stock.getDate().contains(dateFilter);

            if (matchesName && matchesDate) {
                filteredList.add(stock);
            }
        }

        dailyStockTable.setItems(filteredList);
    }

    @FXML
    private void handleRefresh() {
        nameSearchField.clear();
        dateSearchField.setValue(null);
        loadDailyStockData();
    }
}
