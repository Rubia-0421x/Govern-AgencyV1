package com.govagency.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class CustomDialog {

    private boolean confirmed = false;
    private double xOffset = 0;
    private double yOffset = 0;

    public void showAndWait(String title, String message, String iconPath) {
        Stage stage = new Stage(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle(
            "-fx-background-color: #0d1117;" +
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-border-color: #58a6ff;" +
            "-fx-border-width: 2;"
        );

        // Draggable
        root.setOnMousePressed((MouseEvent event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged((MouseEvent event) -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        if (iconPath != null && !iconPath.isEmpty()) {
            ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
            icon.setFitWidth(24);
            icon.setFitHeight(24);
            header.getChildren().add(icon);
        }

        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font(16));
        header.getChildren().add(titleLabel);

        Label messageLabel = new Label(message);
        messageLabel.setTextFill(Color.WHITE);
        messageLabel.setFont(Font.font(14));
        messageLabel.setWrapText(true);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button okButton = new Button("OK");
        okButton.setStyle(
            "-fx-background-color: #3a3a3a; -fx-text-fill: white; -fx-background-radius: 6;"
        );
        okButton.setOnAction(e -> {
            confirmed = true;
            stage.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle(
            "-fx-background-color: #2b2b2b; -fx-text-fill: white; -fx-background-radius: 6;"
        );
        cancelButton.setOnAction(e -> {
            confirmed = false;
            stage.close();
        });

        buttonBox.getChildren().addAll(okButton, cancelButton);

        root.getChildren().addAll(header, messageLabel, buttonBox);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.showAndWait();
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
