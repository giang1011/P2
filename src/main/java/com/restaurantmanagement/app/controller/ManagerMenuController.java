package com.restaurantmanagement.app.controller;

import com.restaurantmanagement.app.entity.MenuItems;
import com.restaurantmanagement.app.entity.Category;
import com.restaurantmanagement.app.service.MenuService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

import static com.restaurantmanagement.app.utils.AlertUtils.showAlert;

public class ManagerMenuController {

    @FXML
    private TextField searchField;
    @FXML
    private Button categoriesButton;
    @FXML
    private TableView<MenuItems> menuItemsTable;
    @FXML
    private TableColumn<MenuItems, Integer> colId;
    @FXML
    private TableColumn<MenuItems, String> colName;
    @FXML
    private TableColumn<MenuItems, String> colCategory;
    @FXML
    private TableColumn<MenuItems, String> colDescription;
    @FXML
    private TableColumn<MenuItems, Double> colPrice;

    private MenuService managerMenuService = new MenuService();
    private ObservableList<MenuItems> menuItemsList = FXCollections.observableArrayList();
    private ObservableList<Category> categoriesList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        colName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        colCategory.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        colDescription.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        colPrice.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());

        loadMenuItems();
        categoriesList.setAll(managerMenuService.getAllCategories());
    }

    private void loadMenuItems() {
        menuItemsList.setAll(managerMenuService.getAllMenuItems());
        menuItemsTable.setItems(menuItemsList);
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String searchQuery = searchField.getText();
        if (!searchQuery.isEmpty()) {
            menuItemsList.setAll(managerMenuService.searchMenuItems(searchQuery));
        } else {
            loadMenuItems();
        }
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add New Menu Item");
        dialog.setHeaderText("Enter the name and price of the new menu item");
        dialog.setContentText("Menu item name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String itemName = result.get();

            TextInputDialog priceDialog = new TextInputDialog();
            priceDialog.setTitle("Add Menu Item Price");
            priceDialog.setHeaderText("Enter the price of the menu item");
            priceDialog.setContentText("Price:");

            Optional<String> priceResult = priceDialog.showAndWait();
            if (priceResult.isPresent()) {
                String priceString = priceResult.get();
                double price;

                try {
                    price = Double.parseDouble(priceString);
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Price Error", "Invalid price. Please enter a number.");
                    return;
                }

                TextInputDialog descriptionDialog = new TextInputDialog();
                descriptionDialog.setTitle("Add Menu Item Description");
                descriptionDialog.setHeaderText("Enter the description of the menu item");
                descriptionDialog.setContentText("Description:");

                Optional<String> descriptionResult = descriptionDialog.showAndWait();
                if (descriptionResult.isPresent()) {
                    String description = descriptionResult.get();

                    ChoiceDialog<Category> categoryDialog = new ChoiceDialog<>(null, categoriesList);
                    categoryDialog.setTitle("Select Menu Item Category");
                    categoryDialog.setHeaderText("Select Menu Item Category");
                    categoryDialog.setContentText("Category:");

                    categoryDialog.setResultConverter(dialogButton -> {
                        if (dialogButton == ButtonType.OK) {
                            return categoryDialog.getSelectedItem();
                        }
                        return null;
                    });

                    Optional<Category> categoryResult = categoryDialog.showAndWait();
                    if (categoryResult.isPresent()) {
                        Category selectedCategory = categoryResult.get();
                        int categoryId = selectedCategory.getCategoryID();

                        managerMenuService.addMenuItem(itemName, price, description, categoryId);
                        loadMenuItems();
                    }
                }
            }
        }
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        MenuItems selectedItem = menuItemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            TextInputDialog nameDialog = new TextInputDialog(selectedItem.getName());
            nameDialog.setTitle("Edit Menu Item");
            nameDialog.setHeaderText("Enter the new name");
            nameDialog.setContentText("Menu item name:");

            Optional<String> nameResult = nameDialog.showAndWait();
            if (nameResult.isPresent()) {
                String newName = nameResult.get();

                TextInputDialog priceDialog = new TextInputDialog(String.valueOf(selectedItem.getPrice()));
                priceDialog.setTitle("Edit Menu Item Price");
                priceDialog.setHeaderText("Enter the new price");
                priceDialog.setContentText("Price:");

                Optional<String> priceResult = priceDialog.showAndWait();
                if (priceResult.isPresent()) {
                    String priceString = priceResult.get();
                    double newPrice;

                    try {
                        newPrice = Double.parseDouble(priceString);
                    } catch (NumberFormatException e) {
                        showAlert(Alert.AlertType.ERROR, "Price Error", "Invalid price. Please enter a number.");
                        return;
                    }

                    TextInputDialog descriptionDialog = new TextInputDialog(selectedItem.getDescription());
                    descriptionDialog.setTitle("Edit Menu Item Description");
                    descriptionDialog.setHeaderText("Enter the new description");
                    descriptionDialog.setContentText("Description:");

                    Optional<String> descriptionResult = descriptionDialog.showAndWait();
                    if (descriptionResult.isPresent()) {
                        String newDescription = descriptionResult.get();

                        ChoiceDialog<Category> categoryDialog = new ChoiceDialog<>(null, categoriesList);
                        categoryDialog.setTitle("Select Menu Item Category");
                        categoryDialog.setHeaderText("Select Menu Item Category");
                        categoryDialog.setContentText("Category:");

                        categoryDialog.setResultConverter(dialogButton -> {
                            if (dialogButton == ButtonType.OK) {
                                return categoryDialog.getSelectedItem();
                            }
                            return null;
                        });

                        Optional<Category> categoryResult = categoryDialog.showAndWait();
                        if (categoryResult.isPresent()) {
                            Category selectedCategory = categoryResult.get();
                            int categoryId = selectedCategory.getCategoryID();

                            managerMenuService.updateMenuItem(selectedItem.getId(), newName, newPrice, newDescription, categoryId);
                            loadMenuItems();
                        }
                    }
                }
            }
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Notification", "Please select a menu item to edit.");
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        MenuItems selectedItem = menuItemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Deletion");
            confirmationAlert.setHeaderText("Are you sure you want to delete this menu item?");
            confirmationAlert.setContentText("Menu Item: " + selectedItem.getName());

            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                managerMenuService.deleteMenuItems(selectedItem.getId());
                loadMenuItems();
            }
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Notification", "Please select a menu item to delete.");
        }
    }

    @FXML
    private void handleCategories(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/restaurantmanagement/fxml/categories.fxml"));
            AnchorPane categoriesPane = loader.load();
            Stage popupStage = new Stage();
            popupStage.setTitle("Manage Categories");
            Scene scene = new Scene(categoriesPane);
            popupStage.setScene(scene);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Unable to open category management");
            alert.setContentText("An error occurred while opening the category popup.");
            alert.showAndWait();
        }
    }
}
