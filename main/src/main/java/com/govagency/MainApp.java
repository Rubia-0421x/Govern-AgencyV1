package com. govagency;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.govagency.controller.LoginController;
import com.govagency.controller.MainController;
import com.govagency.model.Citizen;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx. scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Stage primaryStage;
    private final Map<String, Citizen> citizenMap = new HashMap<>();
    private LocalDatabase database;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.database = new LocalDatabase();

        for (JSONObject obj : database.getAllCitizens()) {
            try {
                String id = obj.getString("id");
                String name = obj.getString("name");
                String email = obj.getString("email");
                String number = obj.getString("number");
                String password = obj. optString("password", "password"); // Load password if exists

                Citizen c = new Citizen(id, name, number, email, password);
                citizenMap.put(id, c);
                
                System.out.println("Loaded citizen: " + email + " (ID: " + id + ")");
            } catch (Exception e) {
                System.err. println("Error loading citizen: " + e.getMessage());
            }
        }

        System.out. println("Total citizens loaded: " + citizenMap.size());

        primaryStage.setTitle("Government Agency Information System");
        primaryStage.setWidth(1400);
        primaryStage.setHeight(900);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);

        showLogin();
        primaryStage.show();
    }

    private void showLogin() {
        LoginController loginController = new LoginController(this, citizenMap);
        Scene loginScene = new Scene((Parent) loginController.getView(), 1000, 700);
        primaryStage.setScene(loginScene);
    }

    /**
     * Return to login screen (for logout)
     */
    public void showLoginScreen() {
        citizenMap.clear();
        database = new LocalDatabase();
        
        for (JSONObject obj : database. getAllCitizens()) {
            try {
                String id = obj.getString("id");
                String name = obj.getString("name");
                String email = obj. getString("email");
                String number = obj.getString("number");
                String password = obj.optString("password", "password");
                
                Citizen c = new Citizen(id, name, number, email, password);
                citizenMap.put(id, c);
            } catch (Exception e) {
                System.err. println("Error loading citizen: " + e.getMessage());
            }
        }
        
        LoginController loginController = new LoginController(this, citizenMap);
        Scene loginScene = new Scene((Parent) loginController.getView(), 1000, 700);
        primaryStage.setScene(loginScene);
    }

    /**
     * Show main app UI
     * @param isAdmin true if admin user
     * @param citizen logged in citizen or null if admin
     */
    public void showMainApp(boolean isAdmin, Citizen citizen) {
        MainController controller = new MainController(isAdmin, citizen, citizenMap, primaryStage, this);
        Scene mainScene = new Scene((Parent) controller.getView(), 1200, 800);
        primaryStage.setScene(mainScene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}