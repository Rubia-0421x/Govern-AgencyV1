package com.govagency;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.govagency.controller.LoginController;
import com.govagency.controller.MainController;
import com.govagency.model.Citizen;
import com.govagency.util.CustomDialog;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApp extends Application {

    private double xOffset = 0;
    private double yOffset = 0;

    private Stage primaryStage;
    private final Map<String, Citizen> citizenMap = new HashMap<>();
    private LocalDatabase database;

    private static final String DARK_BG = "#0d1117";
    private static final String ACCENT_CYAN = "#00ffff";
    private static final String ERROR_RED = "#ff5555";

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        loadCitizens();
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/com/govagency/govicon1.png")));
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        showLoginScreen();
    }

    private VBox createTinyTopBar() {
        VBox topBar = new VBox();
        topBar.setPadding(new Insets(5, 10, 5, 10));
        topBar.setStyle(
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-background-radius: 20 20 0 0;"
        );

        HBox buttonsBox = new HBox(5);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);

        Button minimizeBtn = createButton("—", ACCENT_CYAN);
        minimizeBtn.setPrefWidth(30);
        minimizeBtn.setOnAction(e -> primaryStage.setIconified(true));

        Button closeBtn = createButton("✕", ERROR_RED);
        closeBtn.setPrefWidth(30);
        closeBtn.setOnAction(e -> {
            CustomDialog dialog = new CustomDialog();
            dialog.showAndWait("Exit", "Are you sure you want to exit?", "/com/govagency/govicon1.png");
            if (dialog.isConfirmed()) {
                primaryStage.close();
            }
        });


        buttonsBox.getChildren().addAll(minimizeBtn, closeBtn);
        topBar.getChildren().add(buttonsBox);

        topBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        topBar.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });

        return topBar;
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;"
        );
        return btn;
    }

    private void loadCitizens() {
        citizenMap.clear();
        database = new LocalDatabase();
        for (JSONObject obj : database.getAllCitizens()) {
            try {
                String id = obj.getString("id");
                String name = obj.getString("name");
                String email = obj.getString("email");
                String number = obj.getString("number");
                String password = obj.optString("password", "password");

                Citizen c = new Citizen(id, name, number, email, password);
                citizenMap.put(id, c);
            } catch (org.json.JSONException e) {
                System.err.println("Error loading citizen: " + e.getMessage());
            }
        }
    }

    public void showLoginScreen() {
        loadCitizens();
        LoginController loginController = new LoginController(this, citizenMap);
        Parent loginView = (Parent) loginController.getView();

        VBox root = new VBox();
        root.setStyle(
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-background-radius: 20;" +
            "-fx-border-radius: 20;"
        );

        Rectangle clip = new Rectangle();
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        root.setClip(clip);
        root.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
            clip.setWidth(newVal.getWidth());
            clip.setHeight(newVal.getHeight());
        });

        root.getChildren().add(createTinyTopBar());

        VBox.setVgrow(loginView, Priority.ALWAYS);
        root.getChildren().add(loginView);

        Scene loginScene = new Scene(root, 1400, 900);
        loginScene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    public void showMainApp(boolean isAdmin, Citizen citizen) {
        MainController controller = new MainController(isAdmin, citizen, citizenMap, primaryStage, this);
        Parent mainView = (Parent) controller.getView();

        VBox root = new VBox();
        root.setStyle(
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-background-radius: 20;" +
            "-fx-border-radius: 20;"
        );

        Rectangle clip = new Rectangle();
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        root.setClip(clip);
        root.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
            clip.setWidth(newVal.getWidth());
            clip.setHeight(newVal.getHeight());
        });

        VBox.setVgrow(mainView, Priority.ALWAYS);
        root.getChildren().add(mainView);

        Scene mainScene = new Scene(root, 1400, 900);
        mainScene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(mainScene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
