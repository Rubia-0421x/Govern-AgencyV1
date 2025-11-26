package com.govagency.controller;

import com.govagency.model.Document;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class DocumentReviewDialog {
    
    private static final String DARK_BG = "#0d1117";
    private static final String CARD_BG = "#161b22";
    private static final String ACCENT_CYAN = "#58a6ff";
    private static final String SUCCESS_GREEN = "#3fb950";
    private static final String ERROR_RED = "#f85149";
    private static final String TEXT_WHITE = "#c9d1d9";
    private static final String TEXT_GRAY = "#8b949e";

    public static void show(
            String docId,
            String requestId,
            String filePath,
            String citizenId,
            Document.Status currentStatus,
            String currentComment,
            ReviewCallback callback) {
        
        Stage dialog = new Stage();
        dialog.setTitle("Document Review - " + docId);
        dialog.setWidth(600);
        dialog.setHeight(500);
        
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + DARK_BG + ";");

        Label titleLabel = new Label("📄 Document Review");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(ACCENT_CYAN));

        VBox infoBox = createInfoBox(docId, requestId, filePath, citizenId);

        Label statusLabel = new Label("📋 Decision:");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        statusLabel.setTextFill(Color.web(ACCENT_CYAN));

        ComboBox<Document.Status> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(Document.Status.PENDING, Document.Status.APPROVED, Document.Status.REJECTED);
        statusCombo.setValue(currentStatus);
        statusCombo.setStyle(
            "-fx-font-size: 12;" +
            "-fx-padding: 8;" +
            "-fx-background-color: " + CARD_BG + ";" +
            "-fx-text-fill: " + TEXT_WHITE + ";" +
            "-fx-border-color: " + ACCENT_CYAN + ";" +
            "-fx-border-radius: 4;" +
            "-fx-border-width: 1;"
        );
        statusCombo.setPrefHeight(35);

        Label commentLabel = new Label("💬 Officer's Comment (Optional):");
        commentLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        commentLabel.setTextFill(Color.web(ACCENT_CYAN));

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Enter reason for approval/rejection or additional notes...");
        commentArea.setText(currentComment);
        commentArea.setWrapText(true);
        commentArea.setPrefRowCount(5);
        commentArea.setStyle(
            "-fx-font-size: 11;" +
            "-fx-padding: 8;" +
            "-fx-background-color: " + CARD_BG + ";" +
            "-fx-text-fill: " + TEXT_WHITE + ";" +
            "-fx-prompt-text-fill: " + TEXT_GRAY + ";" +
            "-fx-border-color: " + ACCENT_CYAN + ";" +
            "-fx-border-radius: 4;" +
            "-fx-border-width: 1;" +
            "-fx-font-family: 'Segoe UI';"
        );
        VBox.setVgrow(commentArea, Priority.ALWAYS);

        Button approveButton = createButton("✅ APPROVE", SUCCESS_GREEN);
        approveButton.setOnAction(e -> {
            callback.onReview(docId, Document.Status.APPROVED, commentArea.getText());
            dialog.close();
        });

        Button rejectButton = createButton("❌ REJECT", ERROR_RED);
        rejectButton.setOnAction(e -> {
            callback.onReview(docId, Document.Status.REJECTED, commentArea.getText());
            dialog.close();
        });

        Button cancelButton = createButton("🔙 CANCEL", TEXT_GRAY);
        cancelButton.setOnAction(e -> dialog.close());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(cancelButton, rejectButton, approveButton);

        root.getChildren().addAll(
            titleLabel,
            infoBox,
            statusLabel,
            statusCombo,
            commentLabel,
            commentArea,
            buttonBox
        );

        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.show();
    }

    private static VBox createInfoBox(String docId, String requestId, String filePath, String citizenId) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(12));
        box.setStyle(
            "-fx-background-color: " + CARD_BG + ";" +
            "-fx-border-color: " + ACCENT_CYAN + ";" +
            "-fx-border-radius: 4;" +
            "-fx-border-width: 1;"
        );

        addInfoRow(box, "Document ID:", docId);
        addInfoRow(box, "Request ID:", requestId);
        addInfoRow(box, "Citizen ID:", citizenId);
        addInfoRow(box, "File Path:", filePath);

        return box;
    }

    private static void addInfoRow(VBox box, String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label labelWidget = new Label(label);
        labelWidget.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        labelWidget.setTextFill(Color.web(ACCENT_CYAN));
        labelWidget.setPrefWidth(100);

        Label valueWidget = new Label(value);
        valueWidget.setFont(Font.font("Segoe UI", 11));
        valueWidget.setTextFill(Color.web(TEXT_WHITE));
        valueWidget.setWrapText(true);
        HBox.setHgrow(valueWidget, Priority.ALWAYS);

        row.getChildren().addAll(labelWidget, valueWidget);
        box.getChildren().add(row);
    }

    private static Button createButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefHeight(35);
        button.setStyle(
            "-fx-font-size: 11;" +
            "-fx-font-weight: bold;" +
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: #ffffff;" +
            "-fx-border-radius: 4;" +
            "-fx-padding: 8;" +
            "-fx-cursor: hand;" +
            "-fx-font-family: 'Segoe UI';"
        );
        return button;
    }

    @FunctionalInterface
    public interface ReviewCallback {
        void onReview(String docId, Document.Status newStatus, String comment);
    }
}