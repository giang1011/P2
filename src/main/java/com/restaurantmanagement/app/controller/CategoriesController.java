package com.restaurantmanagement.app.controller;

import com.restaurantmanagement.app.entity.Category;
import com.restaurantmanagement.app.service.CategoryService;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.util.List;
import java.util.Comparator;

public class CategoriesController {

    @FXML
    private TableView<Category> categoriesTable;
    @FXML
    private TableColumn<Category, Integer> colCategoryID;
    @FXML
    private TableColumn<Category, String> colCategoryName;

    @FXML
    private Button backButton;

    private CategoryService categoryService = new CategoryService();

    @FXML
    private void initialize() {
        loadCategories();
    }

    private void loadCategories() {
        List<Category> categoryList = categoryService.getAllCategories();
        categoryList.sort(Comparator.comparingInt(Category::getCategoryID));

        colCategoryID.setCellValueFactory(cellData
                -> cellData.getValue().categoryIDProperty().asObject());
        colCategoryName.setCellValueFactory(cellData
                -> cellData.getValue().nameProperty());

        categoriesTable.setItems(javafx.collections.FXCollections.observableArrayList(categoryList));
    }

    @FXML
    private void handleAddCategory(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Category");
        dialog.setHeaderText("Enter the category name:");

        dialog.showAndWait().ifPresent(categoryName -> {
            Category newCategory = new Category(0, categoryName);
            categoryService.addCategory(newCategory);
            loadCategories();
        });
    }

    @FXML
    private void handleEditCategory(ActionEvent event) {
        Category selectedCategory = categoriesTable.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            TextInputDialog dialog = new TextInputDialog(selectedCategory.getName());
            dialog.setTitle("Edit Category");
            dialog.setHeaderText("Edit the category name:");

            dialog.showAndWait().ifPresent(updatedName -> {
                selectedCategory.setName(updatedName);
                categoryService.updateCategory(selectedCategory);
                loadCategories();
            });
        } else {
            showAlert("No Category Selected", "Please select a category to edit.");
        }
    }

    @FXML
    private void handleDeleteCategory(ActionEvent event) {
        Category selectedCategory = categoriesTable.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Delete Category");
            confirmation.setHeaderText("Are you sure you want to delete this category?");
            confirmation.setContentText("This action cannot be undone.");

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    categoryService.deleteCategory(selectedCategory.getCategoryID());
                    loadCategories();
                }
            });
        } else {
            showAlert("No Category Selected", "Please select a category to delete.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
