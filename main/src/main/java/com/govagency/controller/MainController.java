package com.govagency.controller;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.json.JSONException;
import org.json.JSONObject;

import com.govagency.LocalDatabase;
import com.govagency.MainApp;
import com.govagency.model.Citizen;
import com.govagency.model.Document;
import com.govagency.model.ServiceRequest;
import com.govagency.util.CitizenIdGenerator;
import com.govagency.util.CustomDialog;
import com.govagency.util.Validator;

import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainController {

    private final boolean isAdmin;
    private final Citizen loggedInCitizen;
    private final Map<String, Citizen> citizenMap;
    private final LocalDatabase database;
    private final MainApp mainApp;
    private final Stage primaryStage;

    private TextArea citizenRequestsStatusArea;
    private TextArea citizenDocumentsStatusArea;
    private TextArea adminCitizensStatusArea;
    private TextArea archiveStatusArea;
    private TextArea adminRequestsStatusArea;
    private TextArea adminDocumentsStatusArea;

    private double xOffset = 0;
    private double yOffset = 0;

    private static final String DARK_BG = "#0d1117";
    private static final String CARD_BG = "#161b22";
    private static final String PRIMARY_BLUE = "#1f6feb";
    private static final String ACCENT_CYAN = "#58a6ff";
    private static final String SUCCESS_GREEN = "#3fb950";
    private static final String ERROR_RED = "#f85149";
    private static final String TEXT_WHITE = "#ffffffff";
    private static final String TEXT_GRAY = "#8b949e";
    private static final String INPUT_BG = "#0d1117";
    private static final String BORDER_COLOR = "#30363d";

    private final List<ServiceRequest> serviceRequests = new ArrayList<>();
    private final List<Document> documents = new ArrayList<>();

    public MainController(boolean isAdmin, Citizen citizen, Map<String, Citizen> citizenMap, 
                         Stage primaryStage, MainApp mainApp) {
        this.isAdmin = isAdmin;
        this. loggedInCitizen = citizen;
        this.citizenMap = citizenMap != null ? citizenMap : new HashMap<>();
        this.database = new LocalDatabase();
        this.primaryStage = primaryStage;
        this.mainApp = mainApp;

        loadDataFromDatabase();
    }

    private void loadDataFromDatabase() {
        try {
            for (JSONObject obj : database.getAllRequests()) {
                try {
                    String id = obj.getString("id");
                    String citizenId = obj.getString("citizenId");
                    String type = obj.getString("type");
                    String description = obj.optString("description", "");
                    String statusStr = obj.optString("status", "REQUESTED");
                    String adminNote = obj.optString("adminNote", "");

                    ServiceRequest sr = new ServiceRequest(id, citizenId, type, description);
                    sr.setStatus(ServiceRequest.Status.valueOf(statusStr));
                    sr.setAdminNote(adminNote);
                    serviceRequests.add(sr);
                } catch (JSONException | IllegalArgumentException | NullPointerException e) {
                    System.err.println("Error loading individual request: " + e.getMessage());
                }
            }
        } catch (JSONException | IllegalArgumentException | NullPointerException e) {
            System.err.println("Error loading service requests: " + e.getMessage());
        }

        try {
            for (JSONObject obj : database.getAllDocuments()) {
                try {
                    String id = obj.getString("id");
                    String requestId = obj.optString("requestId", obj.optString("attachedRequestId", ""));
                    String citizenId = obj.optString("citizenId", "");
                    String filePath = obj.optString("filePath", "");
                    String statusStr = obj.optString("status", "PENDING");

                    if (id.isEmpty() || requestId.isEmpty() || citizenId.isEmpty() || filePath.isEmpty()) {
                        System.err.println("Skipping document - missing required fields: " + id);
                        continue;
                    }

                    Document doc = new Document(id, requestId, filePath, citizenId);
                    doc.setStatus(Document.Status.valueOf(statusStr));
                    documents.add(doc);
                    
                    System.out.println("✓ Loaded document: " + id);
                } catch (JSONException | IllegalArgumentException | NullPointerException e){
                    System. err.println("Error loading individual document: " + e.getMessage());
                }
            }
        } catch (JSONException | IllegalArgumentException | NullPointerException e) {
            System.err.println("Error loading documents collection: " + e.getMessage());
        }
    }

    public Node getView() {
        StackPane root = new StackPane();
        root.setStyle(
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-padding: 0;"
        );

        BorderPane mainPane = new BorderPane();
        mainPane.setStyle("-fx-background-color: " + DARK_BG + ";");

        mainPane.setTop(createHeaderPane());

        if (isAdmin) {
            mainPane.setCenter(createAdminPortal());
        } else {
            mainPane.setCenter(createCitizenPortal());
        }

        root.getChildren().add(mainPane);
        return root;
    }

    private VBox createHeaderPane() {
        VBox header = new VBox(5);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setStyle(
            "-fx-background-color: " + CARD_BG + ";" +
            "-fx-border-color: " + ACCENT_CYAN + ";" +
            "-fx-border-width: 0 0 1 0;" +
            "-fx-background-radius: 20 20 0 0;"
        );

        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setSpacing(20);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        VBox titleSection = new VBox(5);
        Label title = new Label(isAdmin ? "🔐 ADMIN DASHBOARD" : "👤 CITIZEN PORTAL");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.web(ACCENT_CYAN));

        Label subtitle = new Label(isAdmin ? "System Administrator" : "Welcome, " + loggedInCitizen.getName());
        subtitle.setFont(Font.font("Segoe UI", 12));
        subtitle.setTextFill(Color.web(TEXT_GRAY));

        titleSection.getChildren().addAll(title, subtitle);

        Button logoutBtn = createButton("🚪 LOG OUT", ERROR_RED);
        logoutBtn.setPrefWidth(120);
        logoutBtn.setOnAction(e -> handleLogout());

        Button minimizeBtn = createButton("—", ACCENT_CYAN);
        minimizeBtn.setPrefWidth(40);
        minimizeBtn.setOnAction(e -> {
            Stage stage = (Stage) header.getScene().getWindow();
            stage.setIconified(true);
        });

        Button closeBtn = createButton("✕", ERROR_RED);
        closeBtn.setPrefWidth(40);
        closeBtn.setOnAction(e -> {
            CustomDialog dialog = new CustomDialog();
            dialog.showAndWait("Exit", "Are you sure you want to exit?", "/com/govagency/govicon1.png");
            if (dialog.isConfirmed()) {
                Stage stage = (Stage) header.getScene().getWindow();
                stage.close();
            }
        });

        HBox windowButtons = new HBox(5, minimizeBtn, closeBtn);
        windowButtons.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleBox.getChildren().addAll(titleSection, spacer, logoutBtn, windowButtons);

        header.getChildren().add(titleBox);

        header.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        header.setOnMouseDragged(event -> {
            Stage stage = (Stage) header.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        return header;
    }

    private void handleLogout() {
        CustomDialog dialog = new CustomDialog();
        dialog.showAndWait("Logout", "Are you sure you want to log out?", "/com/govagency/govicon1.png");
        if (dialog.isConfirmed()) {
            System.out.println("User logged out successfully.");
            mainApp.showLoginScreen();
        }
    }

    private TabPane createCitizenPortal() {
        StackPane wrapper = new StackPane();
        wrapper.setBackground(new Background(new BackgroundFill(Color.web(DARK_BG), CornerRadii.EMPTY, Insets.EMPTY)));

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().addAll(
            createTab("👤 My Profile", createCitizenProfilePane()),
            createTab("⚙️ My Requests", createCitizenRequestsPane()),
            createTab("📄 My Documents", createCitizenDocumentsPane())
        );

        styleTabPane(tabPane);

        wrapper.getChildren().add(tabPane);
        return tabPane;
    }

    private Node createCitizenProfilePane() {

    VBox content = new VBox(20);
    content.setPadding(new Insets(20));
    content.setBackground(new Background(new BackgroundFill(Color.web(DARK_BG), CornerRadii.EMPTY, Insets.EMPTY)));

    // STATUS LABEL
    Label statusLabel = new Label();
    statusLabel.setStyle("-fx-font-size: 12; -fx-padding: 5; -fx-background-radius: 4;");
    statusLabel.setVisible(false);

    PauseTransition hideStatus = new PauseTransition(Duration.seconds(3));
    hideStatus.setOnFinished(evt -> statusLabel.setVisible(false));

    Consumer<String> showError = msg -> {
        statusLabel.setText("⚠️ " + msg);
        statusLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-background-color: #2a0000; -fx-padding: 5; -fx-background-radius: 4;");
        statusLabel.setVisible(true);
        hideStatus.playFromStart();
    };

    Consumer<String> showSuccess = msg -> {
        statusLabel.setText("✅ " + msg);
        statusLabel.setStyle("-fx-text-fill: #4cffb0; -fx-background-color: #002a1a; -fx-padding: 5; -fx-background-radius: 4;");
        statusLabel.setVisible(true);
        hideStatus.playFromStart();
    };

    // ============================
    // INFO SECTION
    // ============================
    VBox infoSection = new VBox(10);
    infoSection.setPadding(new Insets(15));
    infoSection.setStyle(createCardStyle());

    Label infoTitle = createSectionTitle("📋 Personal Information");
    Label idLabel = createInfoLabel("Citizen ID: " + loggedInCitizen.getId());
    Label nameLabel = createInfoLabel("Name: " + loggedInCitizen.getName());
    Label emailLabel = createInfoLabel("Email: " + loggedInCitizen.getEmail());
    Label phoneLabel = createInfoLabel("Phone: " + loggedInCitizen.getNumber());

    infoSection.getChildren().addAll(infoTitle, idLabel, nameLabel, emailLabel, phoneLabel);

    // ============================
    // UPDATE CONTACT INFO
    // ============================
    VBox editSection = new VBox(12);
    editSection.setPadding(new Insets(15));
    editSection.setStyle(createCardStyle());

    Label editTitle = createSectionTitle("✏️ Update Contact Information");

    GridPane grid = new GridPane();
    grid.setHgap(15);
    grid.setVgap(12);

    TextField emailField = createTextField(loggedInCitizen.getEmail());
    TextField phoneField = createTextField(loggedInCitizen.getNumber());

    grid.add(createLabel("Email:"), 0, 0);
    grid.add(emailField, 1, 0);
    grid.add(createLabel("Phone:"), 0, 1);
    grid.add(phoneField, 1, 1);

    ColumnConstraints col1 = new ColumnConstraints(80);
    ColumnConstraints col2 = new ColumnConstraints();
    col2.setHgrow(Priority.ALWAYS);
    grid.getColumnConstraints().addAll(col1, col2);

    Button updateBtn = createButton("💾 Update", SUCCESS_GREEN);
    updateBtn.setOnAction(e -> {
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (!Validator.isValidEmail(email)) {
            showError.accept("Invalid email format.");
            return;
        }

        if (!Validator.isValidCitizenNumber(phone)) {
            showError.accept("Invalid phone number.");
            return;
        }

        for (Citizen c : citizenMap.values()) {
            if (!c.getId().equals(loggedInCitizen.getId())) {
                if (c.getEmail().equalsIgnoreCase(email)) {
                    showError.accept("Email is already in use.");
                    return;
                }
                if (c.getNumber().equals(phone)) {
                    showError.accept("Phone number is already in use.");
                    return;
                }
            }
        }

        loggedInCitizen.setEmail(email);
        loggedInCitizen.setNumber(phone);
        database.updateCitizen(loggedInCitizen.getId(), loggedInCitizen);

        emailLabel.setText("Email: " + email);
        phoneLabel.setText("Phone: " + phone);

        showSuccess.accept("Contact information updated.");
    });

    HBox btnBox = new HBox(10, updateBtn);
    btnBox.setAlignment(Pos.CENTER_RIGHT);
    editSection.getChildren().addAll(editTitle, grid, btnBox);

    // ============================
    // PASSWORD CHANGE
    // ============================
    VBox passwordSection = new VBox(12);
    passwordSection.setPadding(new Insets(15));
    passwordSection.setStyle(createCardStyle());

    Label pwdTitle = createSectionTitle("🔐 Change Password");

    GridPane pwdGrid = new GridPane();
    pwdGrid.setHgap(15);
    pwdGrid.setVgap(12);

    PasswordField currentPwdField = createPasswordField("Current password");
    PasswordField newPwdField = createPasswordField("New password");
    PasswordField confirmPwdField = createPasswordField("Confirm new password");

    pwdGrid.add(createLabel("Current:"), 0, 0);
    pwdGrid.add(currentPwdField, 1, 0);
    pwdGrid.add(createLabel("New:"), 0, 1);
    pwdGrid.add(newPwdField, 1, 1);
    pwdGrid.add(createLabel("Confirm:"), 0, 2);
    pwdGrid.add(confirmPwdField, 1, 2);

    pwdGrid.getColumnConstraints().addAll(col1, col2);

    Button changePwdBtn = createButton("🔄 Change Password", SUCCESS_GREEN);
    changePwdBtn.setOnAction(e -> {
        String c = currentPwdField.getText();
        String n = newPwdField.getText();
        String r = confirmPwdField.getText();

        if (!loggedInCitizen.getPassword().equals(c)) {
            showError.accept("Current password is incorrect.");
            return;
        }

        if (n.length() < 6) {
            showError.accept("Password must be at least 6 characters.");
            return;
        }

        if (!n.equals(r)) {
            showError.accept("New passwords do not match.");
            return;
        }

        loggedInCitizen.setPassword(n);
        database.updateCitizen(loggedInCitizen.getId(), loggedInCitizen);

        currentPwdField.clear();
        newPwdField.clear();
        confirmPwdField.clear();

        showSuccess.accept("Password updated successfully.");
    });

    HBox pwdBtnBox = new HBox(10, changePwdBtn);
    pwdBtnBox.setAlignment(Pos.CENTER_RIGHT);
    passwordSection.getChildren().addAll(pwdTitle, pwdGrid, pwdBtnBox);

    // ============================
    // SCROLL AREA
    // ============================
    VBox scrollContent = new VBox(15, statusLabel, infoSection, editSection, passwordSection);
    scrollContent.setPadding(new Insets(10));
    scrollContent.setBackground(new Background(new BackgroundFill(Color.web(DARK_BG), CornerRadii.EMPTY, Insets.EMPTY)));

    ScrollPane scroll = new ScrollPane(scrollContent);
    scroll.setFitToWidth(true);
    scroll.setStyle(
        "-fx-background: " + DARK_BG + ";" +
        "-fx-background-color: " + DARK_BG + ";" +
        "-fx-control-inner-background: " + DARK_BG + ";"
    );

    return scroll;
}



    private Node createCitizenRequestsPane() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: " + DARK_BG + ";");

        VBox createSection = new VBox(12);
        createSection.setPadding(new Insets(15));
        createSection.setStyle(createCardStyle());

        Label createTitle = createSectionTitle("➕ New Service Request");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(
            "License Renewal",
            "Document Request",
            "Certificate Issuance",
            "Permit Application",
            "Other"
        );
        typeCombo.setValue("License Renewal");
        applyComboBoxStyle(typeCombo);

        TextField descField = createTextField("Describe your request...");
        descField.setPrefHeight(60);

        grid.add(createLabel("Request Type:"), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(createLabel("Description:"), 0, 1);
        grid.add(descField, 1, 1);

        ColumnConstraints col1 = new ColumnConstraints(100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        Button submitBtn = createButton("✚ Submit Request", SUCCESS_GREEN);
        submitBtn.setOnAction(e -> {
            submitRequest(typeCombo.getValue(), descField.getText().trim());
            typeCombo.setValue("License Renewal");
            descField.clear();
            viewCitizenRequests();
        });

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        btnBox.getChildren().add(submitBtn);

        createSection.getChildren().addAll(createTitle, grid, btnBox);

        citizenRequestsStatusArea = createTextArea();
        VBox.setVgrow(citizenRequestsStatusArea, Priority.ALWAYS);

        Button viewBtn = createButton("👁️ View My Requests", PRIMARY_BLUE);
        viewBtn.setOnAction(e -> viewCitizenRequests());

        content.getChildren().addAll(
            createTitle("⚙️ Service Requests"),
            createSection,
            new Separator(),
            viewBtn,
            new Separator(),
            citizenRequestsStatusArea
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setStyle(
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-control-inner-background: " + DARK_BG + ";"
        );
        scroll.setFitToWidth(true);

        return scroll;
    }

    private Node createCitizenDocumentsPane() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: " + DARK_BG + ";");

        VBox uploadSection = new VBox(12);
        uploadSection.setPadding(new Insets(15));
        uploadSection.setStyle(createCardStyle());

        Label uploadTitle = createSectionTitle("⬆️ Upload Document");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);

        TextField requestIdField = createTextField("Enter Request ID");
        TextField filePathField = createTextField("Selected file will appear here...");
        filePathField.setEditable(false);

        Button browseBtn = createButton("📁 Browse File", PRIMARY_BLUE);
        browseBtn.setOnAction(e -> browseAndSelectFile(filePathField));

        grid.add(createLabel("Request ID:"), 0, 0);
        grid.add(requestIdField, 1, 0);
        grid.add(createLabel("File Path:"), 0, 1);

        HBox fileBox = new HBox(10);
        fileBox.setAlignment(Pos.CENTER_LEFT);
        fileBox.getChildren().addAll(filePathField, browseBtn);
        HBox.setHgrow(filePathField, Priority.ALWAYS);

        grid.add(fileBox, 1, 1);

        ColumnConstraints col1 = new ColumnConstraints(100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        Button uploadBtn = createButton("⬆️ Upload Document", SUCCESS_GREEN);
        uploadBtn.setOnAction(e -> uploadDocument(
            requestIdField.getText().trim(),
            filePathField.getText().trim()
        ));

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        btnBox.getChildren().add(uploadBtn);

        uploadSection.getChildren().addAll(uploadTitle, grid, btnBox);

        citizenDocumentsStatusArea = createTextArea();
        VBox.setVgrow(citizenDocumentsStatusArea, Priority.ALWAYS);

        Button viewBtn = createButton("👁️ View My Documents", PRIMARY_BLUE);
        viewBtn.setOnAction(e -> viewCitizenDocuments());

        content.getChildren().addAll(
            createTitle("📄 My Documents"),
            uploadSection,
            new Separator(),
            viewBtn,
            new Separator(),
            citizenDocumentsStatusArea
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setStyle(
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-control-inner-background: " + DARK_BG + ";"
        );
        scroll.setFitToWidth(true);

        return scroll;
    }

    private void browseAndSelectFile(TextField filePathField) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Document File");

        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("All Documents", "*.pdf", "*.doc", "*.docx", "*.jpg", "*.png", "*.txt"),
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
            new FileChooser.ExtensionFilter("Word Documents", "*.doc", "*.docx"),
            new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        String userHome = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new File(userHome + "/Documents"));

        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    private TabPane createAdminPortal() {
        StackPane wrapper = new StackPane();
        wrapper.setBackground(new Background(new BackgroundFill(Color.web(DARK_BG), CornerRadii.EMPTY, Insets.EMPTY)));

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().addAll(
            createTab("👥 Citizens", createAdminCitizensPane()),
            createTab("⚙️ Service Requests", createAdminRequestsPane()),
            createTab("📄 Documents", createAdminDocumentsPane()),
            createTab("📁 Archives", createAdminArchivePane())
        );

        styleTabPane(tabPane);

        wrapper.getChildren().add(tabPane);
        return tabPane;
    }

    private Node createAdminCitizensPane() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setBackground(new Background(new BackgroundFill(Color.web(DARK_BG), CornerRadii.EMPTY, Insets.EMPTY)));

        VBox searchSection = new VBox(12);
        searchSection.setPadding(new Insets(15));
        searchSection.setStyle(createCardStyle());
        Label searchTitle = createSectionTitle("🔍 Search Citizens");
        TextField searchField = createTextField("Search by ID or name...");
        Button searchBtn = createButton("🔎 Search", ACCENT_CYAN);
        searchBtn.setOnAction(e -> adminSearchCitizens(searchField.getText().trim()));
        Button showAllBtn = createButton("👁️ Show All Citizens", PRIMARY_BLUE);
        showAllBtn.setOnAction(e -> adminShowAllCitizens());
        HBox btnBox = new HBox(10, searchField, searchBtn, showAllBtn);
        btnBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchSection.getChildren().addAll(searchTitle, btnBox);

        VBox addSection = createAddCitizenPane();

        VBox deleteSection = new VBox(12);
        deleteSection.setPadding(new Insets(15));
        deleteSection.setStyle(createCardStyle());
        
        Label deleteTitle = createSectionTitle("🗑️ Delete Citizen");
        
        TextField deleteCitizenIdField = createTextField("Enter Citizen ID to delete...");
        
        Button deleteCitizenBtn = createButton("🗑️ Delete Citizen", ERROR_RED);
        deleteCitizenBtn.setOnAction(e -> showDeleteCitizenDialog(deleteCitizenIdField.getText().trim()));
        
        HBox deleteBox = new HBox(10, deleteCitizenIdField, deleteCitizenBtn);
        deleteBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(deleteCitizenIdField, Priority.ALWAYS);
        
        deleteSection.getChildren().addAll(deleteTitle, deleteBox);

        adminCitizensStatusArea = createTextArea();
        VBox.setVgrow(adminCitizensStatusArea, Priority.ALWAYS);

        content.getChildren().addAll(
            createTitle("👥 Citizen Management"),
            searchSection,
            new Separator(),
            addSection,
            new Separator(),
            deleteSection,
            new Separator(),
            adminCitizensStatusArea
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        
        scroll.setStyle(
            "-fx-background: " + DARK_BG + ";" +
            "-fx-background-color: " + DARK_BG + ";"
        );
        
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        
        scroll.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                javafx.application.Platform.runLater(() -> styleScrollBarNodes(scroll));
            }
        });
        
        ((VBox) scroll.getContent()).setBackground(
            new Background(new BackgroundFill(Color.web(DARK_BG), CornerRadii.EMPTY, Insets.EMPTY))
        );

        return scroll;
    }

    private void styleScrollBarNodes(ScrollPane scrollPane) {
        for (Node node : scrollPane.lookupAll(".scroll-bar")) {
            if (node instanceof javafx.scene.control.ScrollBar) {
                javafx.scene.control.ScrollBar scrollBar = (javafx.scene.control.ScrollBar) node;
                
                scrollBar.setStyle(
                    "-fx-background-color: " + CARD_BG + ";" +
                    "-fx-border-color: " + ACCENT_CYAN + ";" +
                    "-fx-border-width: 1px;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-pref-width: 16px;"
                );
                
                for (Node track : node.lookupAll(".track")) {
                    track.setStyle(
                        "-fx-background-color: #0a0a15;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-border-color: " + ACCENT_CYAN + ";" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 6px;"
                    );
                }
                
                for (Node thumb : node.lookupAll(".thumb")) {
                    thumb.setStyle(
                        "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 6px;" +
                        "-fx-border-color: " + PRIMARY_BLUE + ";" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 6px;"
                    );
                    
                    thumb.setOnMouseEntered(e -> thumb.setStyle(
                        "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 6px;" +
                        "-fx-border-color: " + PRIMARY_BLUE + ";" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 6px;"
                    ));
                    
                    thumb.setOnMouseExited(e -> thumb.setStyle(
                        "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 6px;" +
                        "-fx-border-color: " + PRIMARY_BLUE + ";" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 6px;"
                    ));
                    
                    thumb.setOnMousePressed(e -> thumb.setStyle(
                        "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 6px;" +
                        "-fx-border-color: #ffffff;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 6px;"
                    ));
                    
                    thumb.setOnMouseReleased(e -> thumb.setStyle(
                        "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 6px;" +
                        "-fx-border-color: " + PRIMARY_BLUE + ";" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 6px;"
                    ));
                }
                
                for (Node button : node.lookupAll(".increment-button, .decrement-button")) {
                    button.setVisible(false);
                    button.setManaged(false);
                }
            }
        }
    }

    private void showDeleteCitizenDialog(String citizenIdToDelete) {
        if (citizenIdToDelete. isEmpty()) {
            showError("❌ Please enter a Citizen ID.", adminCitizensStatusArea);
            return;
        }

        Citizen targetCitizen = citizenMap.get(citizenIdToDelete);
        if (targetCitizen == null) {
            showError("❌ Citizen not found with ID: " + citizenIdToDelete, adminCitizensStatusArea);
            return;
        }

        Alert dialog = new Alert(Alert.AlertType.WARNING);
        dialog.setTitle("⚠️ Delete Citizen - Security Confirmation");
        dialog.setHeaderText("WARNING: This action cannot be undone!");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        Label citizenInfo = new Label("Citizen to Delete:\n" +
            "ID: " + targetCitizen.getId() + "\n" +
            "Name: " + targetCitizen.getName() + "\n" +
            "Email: " + targetCitizen.getEmail());
        citizenInfo.setWrapText(true);
        citizenInfo.setStyle("-fx-text-fill: #f85149; -fx-font-weight: bold;");

        Label warningLabel = new Label("⚠️ This will permanently delete all data associated with this citizen including:");
        warningLabel.setWrapText(true);
        warningLabel.setStyle("-fx-text-fill: #f85149;");

        Label warningDetails = new Label("• All service requests\n• All documents\n• All citizen records");
        warningDetails.setWrapText(true);
        warningDetails.setStyle("-fx-text-fill: #f85149; -fx-font-size: 11;");

        Label reasonLabel = new Label("Reason for deletion (required):");
        TextField reasonField = createTextField("Enter reason (e.g., Account Closure, Request by Citizen, etc.)");

        Label adminPassLabel = new Label("🔐 Admin Password (required for security):");
        PasswordField adminPassField = createPasswordField("Enter admin password");

        content.getChildren().addAll(
            citizenInfo,
            new Separator(),
            warningLabel,
            warningDetails,
            new Separator(),
            reasonLabel,
            reasonField,
            adminPassLabel,
            adminPassField
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        dialog.getDialogPane().setContent(scrollPane);

        ButtonType confirmDeleteBtn = new ButtonType("🗑️ Confirm Delete", ButtonBar.ButtonData. FINISH);
        dialog.getDialogPane().getButtonTypes().addAll(confirmDeleteBtn, ButtonType.CANCEL);

        java.util.Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == confirmDeleteBtn) {
            String adminPassword = adminPassField.getText();
            String reason = reasonField.getText(). trim();

            if (reason.isEmpty()) {
                showError("❌ Please enter a reason for deletion.", adminCitizensStatusArea);
                return;
            }

            if (!"123".equals(adminPassword)) {
                showError("❌ Incorrect admin password.  Deletion cancelled.", adminCitizensStatusArea);
                return;
            }

            deleteCitizen(citizenIdToDelete, reason);
        }
    }

    private void deleteCitizen(String citizenId, String reason) {
        Citizen deletedCitizen = citizenMap.get(citizenId);
        if (deletedCitizen == null) {
            showError("❌ Citizen not found.", adminCitizensStatusArea);
            return;
        }

        try {
            LocalDatabase db = new LocalDatabase();
            db.archiveCitizen(citizenId, reason);

            citizenMap.remove(citizenId);

            showSuccess(
                "✅ Citizen archived and deleted successfully!\n\n" +
                "Archived Citizen:\n" +
                "ID: " + deletedCitizen. getId() + "\n" +
                "Name: " + deletedCitizen.getName() + "\n" +
                "Email: " + deletedCitizen. getEmail() + "\n\n" +
                "Reason: " + reason + "\n\n" +
                "You can view this citizen in the Archives tab.",
                adminCitizensStatusArea
            );

            adminShowAllCitizens();

            System.out.println("✓ Citizen archived: " + citizenId + " | Reason: " + reason);
        } catch (Exception e) {
            showError("❌ Error deleting citizen: " + e.getMessage(), adminCitizensStatusArea);
            System.err.println("Error deleting citizen: " + e.getMessage());
        }
    }

    private Node createAdminArchivePane() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setBackground(new Background(new BackgroundFill(Color.web(DARK_BG), CornerRadii. EMPTY, Insets.EMPTY)));

        VBox filterSection = new VBox(12);
        filterSection.setPadding(new Insets(15));
        filterSection.setStyle(createCardStyle());
        
        Label filterTitle = createSectionTitle("📁 View Archives");
        
        Button viewDeletedBtn = createButton("👤 Deleted Citizens", ACCENT_CYAN);
        viewDeletedBtn.setOnAction(e -> viewArchivedByType("DELETED_CITIZEN"));
        
        Button viewRejectedReqBtn = createButton("❌ Rejected Requests", ACCENT_CYAN);
        viewRejectedReqBtn.setOnAction(e -> viewArchivedByType("REJECTED_REQUEST"));
        
        Button viewRejectedDocBtn = createButton("❌ Rejected Documents", ACCENT_CYAN);
        viewRejectedDocBtn.setOnAction(e -> viewArchivedByType("REJECTED_DOCUMENT"));
        
        Button viewAllArchiveBtn = createButton("📋 All Archives", PRIMARY_BLUE);
        viewAllArchiveBtn.setOnAction(e -> viewAllArchives());

        HBox btnBox = new HBox(10, viewDeletedBtn, viewRejectedReqBtn, viewRejectedDocBtn, viewAllArchiveBtn);
        btnBox.setAlignment(Pos.CENTER_LEFT);
        btnBox.setSpacing(10);
        
        filterSection.getChildren().addAll(filterTitle, btnBox);

        archiveStatusArea = createTextArea();
        VBox. setVgrow(archiveStatusArea, Priority.ALWAYS);

        VBox restoreSection = new VBox(12);
        restoreSection.setPadding(new Insets(15));
        restoreSection.setStyle(createCardStyle());
        
        Label restoreTitle = createSectionTitle("♻️ Restore Deleted Citizen");
        
        TextField restoreCitizenIdField = createTextField("Enter Citizen ID to restore.. .");
        
        Button restoreBtn = createButton("♻️ Restore Citizen", SUCCESS_GREEN);
        restoreBtn.setOnAction(e -> restoreDeletedCitizen(restoreCitizenIdField.getText(). trim()));
        
        HBox restoreBox = new HBox(10, restoreCitizenIdField, restoreBtn);
        restoreBox.setAlignment(Pos. CENTER_LEFT);
        HBox.setHgrow(restoreCitizenIdField, Priority.ALWAYS);
        
        restoreSection.getChildren().addAll(restoreTitle, restoreBox);

        content.getChildren().addAll(
            createTitle("📁 Archive Management"),
            filterSection,
            new Separator(),
            restoreSection,
            new Separator(),
            archiveStatusArea
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle(
            "-fx-background: " + DARK_BG + ";" +
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-control-inner-background: " + DARK_BG + ";"
        );
        ((VBox) scroll.getContent()).setBackground(
            new Background(new BackgroundFill(Color.web(DARK_BG), CornerRadii.EMPTY, Insets.EMPTY))
        );

        viewAllArchives();

        return scroll;
    }

    private void viewAllArchives() {
        LocalDatabase db = new LocalDatabase();
        List<JSONObject> archives = db.getAllArchives();

        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════════════════════╗\n");
        sb. append("║                   ALL ARCHIVES                             ║\n");
        sb.append("╚════════════════════════════════════════════════════════════╝\n\n");

        if (archives.isEmpty()) {
            sb.append("📁 No archived records found.\n");
        } else {
            for (JSONObject arch : archives) {
                try {
                    sb.append(String.format(
                        "Archive ID: %s\n" +
                        "Type: %s\n" +
                        "Entity ID: %s\n" +
                        "Archived By: %s\n" +
                        "Date: %s\n" +
                        "Reason: %s\n" +
                        "────────────────────────────────────────────────────────────\n\n",
                        arch.getString("archiveId"),
                        arch. getString("type"),
                        arch.getString("entityId"),
                        arch. getString("archivedBy"),
                        arch.getString("archivedAt"),
                        arch.getString("reason")
                    ));
                } catch (Exception e) {
                    System.err.println("Error displaying archive: " + e.getMessage());
                }
            }
        }

        if (archiveStatusArea != null) {
            archiveStatusArea. setText(sb.toString());
        }
    }

    private void viewArchivedByType(String type) {
        LocalDatabase db = new LocalDatabase();
        List<JSONObject> archives = db. getArchivesByType(type);

        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════════════════════╗\n");
        sb.append("║  " + String.format("%-56s", type. replace("_", " ")) + "  ║\n");
        sb.append("╚════════════════════════════════════════════════════════════╝\n\n");

        if (archives.isEmpty()) {
            sb.append("📁 No archived records of this type.\n");
        } else {
            for (JSONObject arch : archives) {
                try {
                    sb.append(String.format(
                        "Archive ID: %s\n" +
                        "Entity ID: %s\n" +
                        "Archived By: %s\n" +
                        "Date: %s\n" +
                        "Reason: %s\n" +
                        "────────────────────────────────────────────────────────────\n\n",
                        arch.getString("archiveId"),
                        arch.getString("entityId"),
                        arch.getString("archivedBy"),
                        arch. getString("archivedAt"),
                        arch.getString("reason")
                    ));
                } catch (Exception e) {
                    System.err.println("Error displaying archive: " + e.getMessage());
                }
            }
        }

        if (archiveStatusArea != null) {
            archiveStatusArea.setText(sb.toString());
        }
    }

    private void restoreDeletedCitizen(String citizenIdToRestore) {
        if (citizenIdToRestore. isEmpty()) {
            showError("❌ Please enter a Citizen ID to restore.", archiveStatusArea);
            return;
        }

        LocalDatabase db = new LocalDatabase();
        List<JSONObject> archives = db. getArchivesByType("DELETED_CITIZEN");

        JSONObject deletedCitizenArchive = null;
        for (JSONObject arch : archives) {
            try {
                if (arch.getString("entityId").equals(citizenIdToRestore)) {
                    deletedCitizenArchive = arch;
                    break;
                }
            } catch (Exception e) {
                System.err.println("Error searching archive: " + e.getMessage());
            }
        }

        if (deletedCitizenArchive == null) {
            showError("❌ No deleted citizen found with ID: " + citizenIdToRestore, archiveStatusArea);
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("♻️ Restore Citizen Account");
        confirmDialog.setHeaderText("Confirm Restoration");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        try {
            String details = deletedCitizenArchive. getString("details");
            JSONObject citizenData = new JSONObject(details);
            
            Label info = new Label("Restore this citizen account:\n\n" +
                "ID: " + citizenData.getString("id") + "\n" +
                "Name: " + citizenData.getString("name") + "\n" +
                "Email: " + citizenData.getString("email") + "\n" +
                "Phone: " + citizenData.getString("number") + "\n\n" +
                "This will restore all account data.");
            info.setWrapText(true);
            
            content.getChildren().add(info);
        } catch (Exception e) {
            showError("❌ Error reading citizen data: " + e.getMessage(), archiveStatusArea);
            return;
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        confirmDialog.getDialogPane().setContent(scrollPane);

        java.util.Optional<ButtonType> result = confirmDialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String details = deletedCitizenArchive.getString("details");
                JSONObject citizenData = new JSONObject(details);

                Citizen restoredCitizen = new Citizen(
                    citizenData. getString("id"),
                    citizenData.getString("name"),
                    citizenData.getString("number"),
                    citizenData.getString("email"),
                    citizenData.getString("password")
                );

                db.addCitizen(restoredCitizen);
                
                citizenMap.put(restoredCitizen.getId(), restoredCitizen);

                showSuccess(
                    "✅ Citizen account restored successfully!\n\n" +
                    "ID: " + restoredCitizen.getId() + "\n" +
                    "Name: " + restoredCitizen.getName() + "\n" +
                    "Email: " + restoredCitizen. getEmail(),
                    archiveStatusArea
                );

                System.out.println("✓ Citizen restored: " + restoredCitizen.getId());
                
                viewAllArchives();
                
                adminShowAllCitizens();

            } catch (Exception e) {
                showError("❌ Error restoring citizen: " + e.getMessage(), archiveStatusArea);
                System.err.println("Error restoring citizen: " + e.getMessage());
            }
        }
    }

    private VBox createAddCitizenPane() {
        VBox addSection = new VBox(12);
        addSection.setPadding(new Insets(15));
        addSection.setStyle(createCardStyle());
        Label addTitle = createSectionTitle("➕ Add New Citizen");

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12;");

        Label idLabel = createLabel("Citizen ID (Auto-Generated):");
        TextField idField = createTextField("");
        idField.setEditable(false);
        idField.setStyle(
            "-fx-font-size: 12;" +
            "-fx-padding: 8;" +
            "-fx-background-color: #0a0e13;" +
            "-fx-text-fill: " + ACCENT_CYAN + ";" +
            "-fx-border-color: " + ACCENT_CYAN + ";" +
            "-fx-border-radius: 4;" +
            "-fx-border-width: 1;" +
            "-fx-font-family: 'Courier New';" +
            "-fx-opacity: 0.8;"
        );

        if (idField.getText().isEmpty()) {
            idField.setText(CitizenIdGenerator.generateCitizenId(citizenMap));
        }

        Button regenerateBtn = createButton("🔄 New ID", PRIMARY_BLUE);
        regenerateBtn.setPrefWidth(120);
        regenerateBtn.setOnAction(e -> {
            idField.setText(CitizenIdGenerator.generateCitizenId(citizenMap));
        });

        HBox idBox = new HBox(10, idField, regenerateBtn);
        HBox.setHgrow(idField, Priority.ALWAYS);

        TextField nameField = createTextField("Name...");
        TextField emailField = createTextField("Email...");
        TextField numberField = createTextField("Phone Number...");
        TextField passwordField = createTextField("Password...");

        Button addBtn = createButton("✅ Add Citizen", ACCENT_CYAN);
        addBtn.setOnAction(e -> {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String number = numberField.getText().trim();
            String password = passwordField.getText().trim();

            statusLabel.setText("");

            // empty check
            if (name.isEmpty() || email.isEmpty() || number.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Please fill in all fields.");
                statusLabel.setStyle("-fx-text-fill: #ff5555; -fx-font-size: 12;");
                return;
            }

            // validation
            if (!Validator.isValidCitizenName(name)) {
                statusLabel.setText("Invalid name format.");
                statusLabel.setStyle("-fx-text-fill: #ff5555; -fx-font-size: 12;");
                return;
            }
            if (!Validator.isValidEmail(email)) {
                statusLabel.setText("Invalid email format.");
                statusLabel.setStyle("-fx-text-fill: #ff5555; -fx-font-size: 12;");
                return;
            }
            if (!Validator.isValidCitizenNumber(number)) {
                statusLabel.setText("Invalid phone number format.");
                statusLabel.setStyle("-fx-text-fill: #ff5555; -fx-font-size: 12;");
                return;
            }

            if (citizenMap.containsKey(id)) {
                statusLabel.setText("Citizen ID already exists.");
                statusLabel.setStyle("-fx-text-fill: #ff5555; -fx-font-size: 12;");
                return;
            }

            for (Citizen c : citizenMap.values()) {
                if (c.getEmail().equalsIgnoreCase(email)) {
                    statusLabel.setText("Email already in use.");
                    statusLabel.setStyle("-fx-text-fill: #ff5555; -fx-font-size: 12;");
                    return;
                }
            }

            Citizen newCitizen = new Citizen(id, name, number, email, password);
            LocalDatabase db = new LocalDatabase();
            db.addCitizen(newCitizen);
            citizenMap.put(id, newCitizen);

            // success (GREEN)
            statusLabel.setText("Citizen added successfully: " + name);
            statusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 12;");

            // log success
            adminCitizensStatusArea.appendText(
                "✅ Added citizen: " + newCitizen.getName() + " (ID: " + id + ")\n"
            );

            idField.setText(CitizenIdGenerator.generateCitizenId(citizenMap));
            nameField.clear();
            emailField.clear();
            numberField.clear();
            passwordField.clear();
        });

        addSection.getChildren().addAll(
            addTitle,
            statusLabel,
            idLabel,
            idBox,
            createLabel("Name:"),
            nameField,
            createLabel("Email:"),
            emailField,
            createLabel("Phone Number:"),
            numberField,
            createLabel("Password:"),
            passwordField,
            addBtn
        );

        return addSection;
    }







    private Node createAdminRequestsPane() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setBackground(new Background(new BackgroundFill(Color.web(DARK_BG), CornerRadii.EMPTY, Insets.EMPTY)));

        VBox searchSection = new VBox(12);
        searchSection.setPadding(new Insets(15));
        searchSection.setStyle(createCardStyle());

        Label searchTitle = createSectionTitle("🔍 Search Requests");

        TextField citizenIdField = createTextField("Enter Citizen ID...");

        Button searchBtn = createButton("🔎 Search", ACCENT_CYAN);
        searchBtn.setOnAction(e -> adminSearchRequests(citizenIdField.getText().trim()));

        Button showAllBtn = createButton("👁️ Show All", PRIMARY_BLUE);
        showAllBtn.setOnAction(e -> adminShowAllRequests());

        HBox searchBox = new HBox(10, createLabel("Citizen ID:"), citizenIdField, searchBtn, showAllBtn);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(citizenIdField, Priority.ALWAYS);

        searchSection.getChildren().addAll(searchTitle, searchBox);

        VBox updateSection = new VBox(12);
        updateSection.setPadding(new Insets(15));
        updateSection.setStyle(createCardStyle());

        Label updateTitle = createSectionTitle("📝 Update Request Status");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);

        TextField reqIdField = createTextField("Enter Request ID");

        ComboBox<ServiceRequest.Status> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(
            ServiceRequest.Status.REQUESTED,
            ServiceRequest.Status.PROCESSING,
            ServiceRequest.Status.COMPLETED,
            ServiceRequest.Status.REJECTED
        );
        statusCombo.setValue(ServiceRequest.Status.PROCESSING);
        applyComboBoxStyle(statusCombo);

        TextField noteField = createTextField("Admin note...");

        grid.add(createLabel("Request ID:"), 0, 0);
        grid.add(reqIdField, 1, 0);
        grid.add(createLabel("New Status:"), 0, 1);
        grid.add(statusCombo, 1, 1);
        grid.add(createLabel("Admin Note:"), 0, 2);
        grid.add(noteField, 1, 2);

        ColumnConstraints col1 = new ColumnConstraints(100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        Button updateBtn = createButton("✅ Update Status", SUCCESS_GREEN);
        updateBtn.setOnAction(e -> adminUpdateRequestStatus(
            reqIdField.getText().trim(),
            statusCombo.getValue(),
            noteField.getText().trim()
        ));

        HBox btnBox = new HBox(10, updateBtn);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        updateSection.getChildren().addAll(updateTitle, grid, btnBox);

        adminRequestsStatusArea = createTextArea();
        VBox.setVgrow(adminRequestsStatusArea, Priority.ALWAYS);

        content.getChildren().addAll(
            createTitle("⚙️ Service Request Management"),
            searchSection,
            new Separator(),
            updateSection,
            new Separator(),
            adminRequestsStatusArea
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle(
            "-fx-background: " + DARK_BG + ";" +
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-control-inner-background: " + DARK_BG + ";"
        );

        ((VBox) scroll.getContent()).setBackground(
            new Background(new BackgroundFill(Color.web(DARK_BG), CornerRadii.EMPTY, Insets.EMPTY))
        );

        return scroll;
    }

    private Node createAdminDocumentsPane() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setBackground(new Background(new BackgroundFill(Color.web(DARK_BG), CornerRadii.EMPTY, Insets.EMPTY)));

        VBox searchSection = new VBox(12);
        searchSection.setPadding(new Insets(15));
        searchSection.setStyle(createCardStyle());

        Label searchTitle = createSectionTitle("🔍 Search Documents");

        TextField citizenIdField = createTextField("Search by Citizen ID...");
        TextField docIdField = createTextField("Or by Document ID...");

        Button searchCitizenBtn = createButton("🔎 By Citizen", ACCENT_CYAN);
        searchCitizenBtn.setOnAction(e -> adminSearchDocumentsByCitizen(citizenIdField.getText().trim()));

        Button searchDocBtn = createButton("🔎 By Doc ID", ACCENT_CYAN);
        searchDocBtn.setOnAction(e -> adminSearchDocumentsByDocId(docIdField.getText().trim()));

        Button showAllBtn = createButton("👁️ Show All", PRIMARY_BLUE);
        showAllBtn.setOnAction(e -> adminShowAllDocuments());

        HBox searchBox1 = new HBox(10, createLabel("Citizen ID:"), citizenIdField, searchCitizenBtn);
        searchBox1.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(citizenIdField, Priority.ALWAYS);

        HBox searchBox2 = new HBox(10, createLabel("Document ID:"), docIdField, searchDocBtn, showAllBtn);
        searchBox2.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(docIdField, Priority.ALWAYS);

        searchSection.getChildren().addAll(searchTitle, searchBox1, searchBox2);

        VBox updateSection = new VBox(12);
        updateSection.setPadding(new Insets(15));
        updateSection.setStyle(createCardStyle());

        Label updateTitle = createSectionTitle("✅ Review Document");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);

        TextField docIdUpdateField = createTextField("Enter Document ID");

        ComboBox<Document.Status> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(
            Document.Status.PENDING,
            Document.Status.APPROVED,
            Document.Status.REJECTED
        );
        statusCombo.setValue(Document.Status.APPROVED);
        applyComboBoxStyle(statusCombo);

        TextField reviewField = createTextField("Review remarks...");

        grid.add(createLabel("Document ID:"), 0, 0);
        grid.add(docIdUpdateField, 1, 0);
        grid.add(createLabel("Status:"), 0, 1);
        grid.add(statusCombo, 1, 1);
        grid.add(createLabel("Remarks:"), 0, 2);
        grid.add(reviewField, 1, 2);

        ColumnConstraints col1 = new ColumnConstraints(100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        Button reviewBtn = createButton("✅ Update Document", SUCCESS_GREEN);
        reviewBtn.setOnAction(e -> adminUpdateDocumentStatus(
            docIdUpdateField.getText().trim(),
            statusCombo.getValue(),
            reviewField.getText().trim()
        ));

        HBox btnBox = new HBox(10, reviewBtn);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        updateSection.getChildren().addAll(updateTitle, grid, btnBox);

        adminDocumentsStatusArea = createTextArea();
        VBox.setVgrow(adminDocumentsStatusArea, Priority.ALWAYS);

        content.getChildren().addAll(
            createTitle("📄 Document Management"),
            searchSection,
            new Separator(),
            updateSection,
            new Separator(),
            adminDocumentsStatusArea
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle(
            "-fx-background: " + DARK_BG + ";" +
            "-fx-background-color: " + DARK_BG + ";" +
            "-fx-control-inner-background: " + DARK_BG + ";"
        );

        ((VBox) scroll.getContent()).setBackground(
            new Background(new BackgroundFill(Color.web(DARK_BG), CornerRadii.EMPTY, Insets.EMPTY))
        );

        return scroll;
    }


    private void updateProfile(String email, String phone) {
        if (email.isEmpty() || phone.isEmpty()) {
            showError("❌ Email and phone cannot be empty.", null);
            return;
        }

        if (! Validator.isValidEmail(email)) {
            showError("❌ Invalid email format.", null);
            return;
        }

        if (!isValidPhilippinePhoneNumber(phone)) {
            showError("❌ Invalid phone number.", null);
            return;
        }

        for (Citizen c : citizenMap.values()) {
            if (! c.getId().equals(loggedInCitizen.getId()) && c.getEmail().equalsIgnoreCase(email)) {
                showError("❌ Email already in use.", null);
                return;
            }
        }

        CustomDialog dialog = new CustomDialog();
        dialog.showAndWait("Update Profile", "Update your profile with the new information?", "/com/govagency/govicon1.png");
        if (!dialog.isConfirmed()) {
            return;
        }


        loggedInCitizen.setEmail(email);
        loggedInCitizen. setNumber(phone);
        database.updateCitizen(loggedInCitizen.getId(), loggedInCitizen);

        showSuccess("✅ Profile updated successfully!", null);
    }


    private void changePassword(String currentPassword, String newPassword, String confirmPassword) {
        if (currentPassword.isEmpty() || newPassword. isEmpty() || confirmPassword.isEmpty()) {
            showError("❌ All password fields are required.", null);
            return;
        }

        if (!loggedInCitizen.getPassword().equals(currentPassword)) {
            showError("❌ Current password is incorrect.", null);
            return;
        }

        if (newPassword.length() < 6) {
            showError("❌ New password must be at least 6 characters long.", null);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("❌ New passwords do not match.", null);
            return;
        }

        if (newPassword.equals(currentPassword)) {
            showError("❌ New password must be different from current password.", null);
            return;
        }

        CustomDialog dialog = new CustomDialog();
        dialog.showAndWait(
            "Change Password",
            "Change your password?\n\nYou will need to log in again with your new password.",
            "/com/govagency/govicon1.png"
        );
        if (!dialog.isConfirmed()) {
            return;
        }


        loggedInCitizen.setPassword(newPassword);
        database.updateCitizen(loggedInCitizen. getId(), loggedInCitizen);

        showSuccess("✅ Password changed successfully!\nPlease log in again.", null);
    }

    private void submitRequest(String type, String description) {
        if (type == null || type.isEmpty()) {
            showError("❌ Please select a request type.", citizenRequestsStatusArea);
            return;
        }

        if (description. isEmpty()) {
            showError("❌ Description cannot be empty.", citizenRequestsStatusArea);
            return;
        }

        CustomDialog dialog = new CustomDialog();
        dialog.showAndWait(
            "Submit Request",
            "Submit a " + type + " request?\n\nDescription: " + description,
            "/com/govagency/govicon1.png"
        );
        if (!dialog.isConfirmed()) {
            return;
        }


        String reqId = generateRequestId(loggedInCitizen.getId());
        ServiceRequest sr = new ServiceRequest(reqId, loggedInCitizen.getId(), type, description);
        sr.setStatus(ServiceRequest.Status.REQUESTED);

        serviceRequests.add(sr);
        database.addRequest(sr);

        showSuccess(
            "✅ Request submitted successfully!\n" +
            "Request ID: " + reqId,
            citizenRequestsStatusArea
        );
    }

    private void viewCitizenRequests() {
        List<ServiceRequest> myRequests = new ArrayList<>();
        for (ServiceRequest sr : serviceRequests) {
            if (sr.getCitizenId().equals(loggedInCitizen.getId())) {
                myRequests.add(sr);
            }
        }

        if (myRequests.isEmpty()) {
            citizenRequestsStatusArea.setText("📄 You have not submitted any service requests yet.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════════════════════╗\n");
        sb.append("║               MY SERVICE REQUESTS                           ║\n");
        sb.append("╚════════════════════════════════════════════════════════════╝\n\n");

        for (ServiceRequest sr : myRequests) {
            sb.append(String.format(
                "ID: %s\n" +
                "Type: %s\n" +
                "Description: %s\n" +
                "Status: %s %s\n" +
                (sr.getAdminNote().isEmpty() ? "" : "Admin Note: %s\n") +
                "────────────────────────────────────────────────────────────\n\n",
                sr.getId(),
                sr.getServiceType(),
                sr.getDescription(),
                getStatusEmoji(sr.getStatus().name()),
                sr.getStatus().name(),
                sr.getAdminNote()
            ));
        }

        citizenRequestsStatusArea.setText(sb.toString());
    }

    private void uploadDocument(String requestId, String filePath) {
        if (requestId.isEmpty() || filePath.isEmpty()) {
            showError("❌ Request ID and file path cannot be empty.", citizenDocumentsStatusArea);
            return;
        }

        ServiceRequest targetRequest = null;
        for (ServiceRequest sr : serviceRequests) {
            if (sr.getId().equals(requestId) && sr.getCitizenId().equals(loggedInCitizen.getId())) {
                targetRequest = sr;
                break;
            }
        }

        if (targetRequest == null) {
            showError("❌ Request not found or does not belong to you.", citizenDocumentsStatusArea);
            return;
        }

        String docId = generateDocumentId(loggedInCitizen.getId());
        Document doc = new Document(docId, requestId, filePath, loggedInCitizen.getId());
        doc.setStatus(Document.Status.PENDING);

        documents.add(doc);
        
        try {
            database.addDocument(doc);
            System.out.println("Document added - ID: " + docId + ", Citizen: " + loggedInCitizen.getId() + ", Request: " + requestId);
        } catch (Exception e) {
            System.err.println("Error saving document: " + e.getMessage());
            showError("❌ Error saving document to database.", citizenDocumentsStatusArea);
            return;
        }

        showSuccess(
            "✅ Document uploaded successfully!\n\n" +
            "Document ID: " + docId + "\n" +
            "Request ID: " + requestId + "\n" +
            "File: " + filePath + "\n" +
            "Status: PENDING REVIEW",
            citizenDocumentsStatusArea
        );
        
        viewCitizenDocuments();
    }

    private void viewCitizenDocuments() {
        List<Document> myDocs = new ArrayList<>();
        for (Document doc : documents) {
            if (doc. getCitizenId(). equals(loggedInCitizen.getId())) {
                myDocs. add(doc);
            }
        }

        if (myDocs.isEmpty()) {
            citizenDocumentsStatusArea.setText("📄 You have not uploaded any documents yet.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════════════════════╗\n");
        sb. append("║               MY UPLOADED DOCUMENTS                         ║\n");
        sb.append("╚════════════════════════════════════════════════════════════╝\n\n");

        for (Document doc : myDocs) {
            ServiceRequest relatedRequest = null;
            for (ServiceRequest sr : serviceRequests) {
                if (sr.getId().equals(doc.getAttachedRequestId())) {
                    relatedRequest = sr;
                    break;
                }
            }

            sb.append(String.format(
                "Document ID: %s\n" +
                "Request ID: %s\n" +
                "File: %s\n" +
                "Status: %s %s\n" +
                "Uploaded: %s\n" +
                (doc.getReviewComment(). isEmpty() ? "" : "Officer's Note: %s\n") +
                (relatedRequest != null && ! relatedRequest.getAdminNote().isEmpty() ? 
                    "Request Note: %s\n" : "") +
                "────────────────────────────────────────────────────────────\n\n",
                doc.getId(),
                doc.getAttachedRequestId(),
                doc.getFilePath(),
                getStatusEmoji(doc.getStatus(). name()),
                doc.getStatus().name(),
                doc.getUploadTime(). format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                doc.getReviewComment(),
                (relatedRequest != null ?  relatedRequest.getAdminNote() : "")
            ));
        }

        citizenDocumentsStatusArea.setText(sb.toString());
    }

    private void adminSearchCitizens(String search) {
        if (search.isEmpty()) {
            showError("❌ Please enter a search term.", adminCitizensStatusArea);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════════════════════╗\n");
        sb.append("║                    SEARCH RESULTS                           ║\n");
        sb.append("╚════════════════════════════════════════════════════════════╝\n\n");

        boolean found = false;
        String searchLower = search.toLowerCase();

        for (Citizen c : citizenMap.values()) {
            if (c.getId().contains(search) || c.getName().toLowerCase().contains(searchLower)) {
                sb.append(String.format(
                    "ID: %s\n" +
                    "Name: %s\n" +
                    "Email: %s\n" +
                    "Phone: %s\n" +
                    "────────────────────────────────────────────────────────────\n\n",
                    c.getId(),
                    c.getName(),
                    c.getEmail(),
                    c.getNumber()
                ));
                found = true;
            }
        }

        if (!found) {
            adminCitizensStatusArea.setText("❌ No citizens found matching: " + search);
        } else {
            adminCitizensStatusArea.setText(sb.toString());
        }
    }

    private void adminShowAllCitizens() {
    if (citizenMap.isEmpty()) {
        adminCitizensStatusArea.setText("❌ No citizens in the system.");
        return;
    }

    StringBuilder sb = new StringBuilder();
    sb.append("╔════════════════════════════════════════════════════════════╗\n");
    sb. append("║                    ALL CITIZENS                             ║\n");
    sb.append("╚════════════════════════════════════════════════════════════╝\n\n");

    int count = 1;
    for (Citizen c : citizenMap.values()) {
        sb.append(String. format(
            "%d. ID: %s\n" +
            "   Name: %s\n" +
            "   Email: %s\n" +
            "   Phone: %s\n\n",
            count,
            c.getId(),
            c. getName(),
            c.getEmail(),
            c.getNumber()
        ));
        count++;
    }

    sb.append("────────────────────────────────────────────────────────────\n");
    sb.append("TO DELETE A CITIZEN:\n");
    sb.append("1. Copy the Citizen ID\n");
    sb.append("2. Use the 'Delete Citizen' button below\n");
    sb.append("3. Enter the ID and confirm with admin password\n");

    adminCitizensStatusArea.setText(sb.toString());

}

    private void adminSearchRequests(String citizenId) {
        if (citizenId.isEmpty()) {
            showError("❌ Please enter a citizen ID.", adminRequestsStatusArea);
            return;
        }

        List<ServiceRequest> filtered = new ArrayList<>();
        for (ServiceRequest sr : serviceRequests) {
            if (sr.getCitizenId().equals(citizenId)) {
                filtered.add(sr);
            }
        }

        if (filtered.isEmpty()) {
            adminRequestsStatusArea.setText("❌ No requests found for citizen ID: " + citizenId);
            return;
        }

        displayRequestsAdmin(filtered, "Requests for Citizen: " + citizenId);
    }

    private void adminShowAllRequests() {
        if (serviceRequests.isEmpty()) {
            adminRequestsStatusArea.setText("❌ No service requests in the system.");
            return;
        }

        displayRequestsAdmin(serviceRequests, "All Service Requests");
    }

    private void displayRequestsAdmin(List<ServiceRequest> reqs, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════════════════════╗\n");
        sb.append("║  " + String.format("%-56s", title) + "  ║\n");
        sb.append("╚════════════════════════════════════════════════════════════╝\n\n");

        for (ServiceRequest sr : reqs) {
            sb.append(String.format(
                "Request ID: %s\n" +
                "Citizen ID: %s\n" +
                "Type: %s\n" +
                "Description: %s\n" +
                "Status: %s %s\n" +
                (sr.getAdminNote().isEmpty() ? "" : "Note: %s\n") +
                "────────────────────────────────────────────────────────────\n\n",
                sr.getId(),
                sr.getCitizenId(),
                sr.getServiceType(),
                sr.getDescription(),
                getStatusEmoji(sr.getStatus().name()),
                sr.getStatus().name(),
                sr.getAdminNote()
            ));
        }

        adminRequestsStatusArea.setText(sb.toString());
    }

    private void adminUpdateRequestStatus(String requestId, ServiceRequest.Status newStatus, String note) {
        if (requestId.isEmpty()) {
            showError("❌ Please enter a request ID.", adminRequestsStatusArea);
            return;
        }

        ServiceRequest targetReq = null;
        for (ServiceRequest sr : serviceRequests) {
            if (sr.getId().equals(requestId)) {
                targetReq = sr;
                break;
            }
        }

        if (targetReq == null) {
            showError("❌ Request not found with ID: " + requestId, adminRequestsStatusArea);
            return;
        }

        targetReq.setStatus(newStatus);
        targetReq.setAdminNote(note);
        database.updateRequest(requestId, targetReq);

        showSuccess(
            "✅ Request status updated!\n\n" +
            "Request ID: " + requestId + "\n" +
            "New Status: " + newStatus + "\n" +
            (note.isEmpty() ? "" : "Note: " + note),
            adminRequestsStatusArea
        );
    }

    private void adminSearchDocumentsByCitizen(String citizenId) {
        if (citizenId.isEmpty()) {
            showError("❌ Please enter a citizen ID.", adminDocumentsStatusArea);
            return;
        }

        List<Document> filtered = new ArrayList<>();
        for (Document doc : documents) {
            if (doc.getCitizenId().equals(citizenId)) {
                filtered.add(doc);
            }
        }

        if (filtered.isEmpty()) {
            adminDocumentsStatusArea.setText("❌ No documents found for citizen ID: " + citizenId);
            return;
        }

        displayDocumentsAdmin(filtered, "Documents for Citizen: " + citizenId);
    }

    private void adminSearchDocumentsByDocId(String docId) {
        if (docId.isEmpty()) {
            showError("❌ Please enter a document ID.", adminDocumentsStatusArea);
            return;
        }

        Document found = null;
        for (Document doc : documents) {
            if (doc.getId().equals(docId)) {
                found = doc;
                break;
            }
        }

        if (found == null) {
            adminDocumentsStatusArea.setText("❌ Document not found with ID: " + docId);
            return;
        }

        List<Document> list = new ArrayList<>();
        list.add(found);
        displayDocumentsAdmin(list, "Document Details");
    }

    private void adminShowAllDocuments() {
        if (documents.isEmpty()) {
            adminDocumentsStatusArea.setText("❌ No documents in the system.");
            return;
        }

        displayDocumentsAdmin(documents, "All Documents");
    }

    private void displayDocumentsAdmin(List<Document> docs, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════════════════════╗\n");
        sb.append("║  " + String.format("%-56s", title) + "  ║\n");
        sb.append("╚════════════════════════════════════════════════════════════╝\n\n");

        for (Document doc : docs) {
            sb.append(String.format(
                "Document ID: %s\n" +
                "Request ID: %s\n" +
                "Citizen ID: %s\n" +
                "File: %s\n" +
                "Status: %s %s\n" +
                "Uploaded: %s\n" +
                (doc.getReviewComment().isEmpty() ? "" : "Review: %s\n") +
                "────────────────────────────────────────────────────────────\n\n",
                doc.getId(),
                doc.getAttachedRequestId(),
                doc.getCitizenId(),
                doc.getFilePath(),
                getStatusEmoji(doc.getStatus().name()),
                doc.getStatus().name(),
                doc.getUploadTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                doc.getReviewComment()
            ));
        }

        adminDocumentsStatusArea.setText(sb.toString());
    }

    private void adminUpdateDocumentStatus(String docId, Document. Status newStatus, String remarks) {
        if (docId.isEmpty()) {
            showError("❌ Please enter a document ID.", adminDocumentsStatusArea);
            return;
        }

        Document targetDoc = null;
        for (Document doc : documents) {
            if (doc. getId().equals(docId)) {
                targetDoc = doc;
                break;
            }
        }

        if (targetDoc == null) {
            showError("❌ Document not found.", adminDocumentsStatusArea);
            return;
        }

        CustomDialog dialog = new CustomDialog();
        dialog.showAndWait(
            "Update Document Status",
            "Set document status to " + newStatus + "?" +
            (remarks.isEmpty() ? "" : "\n\nRemarks: " + remarks),
            "/com/govagency/govicon1.png"
        );
        if (!dialog.isConfirmed()) {
            return;
        }


        targetDoc.setStatus(newStatus);
        targetDoc.setReviewComment(remarks);
        database.updateDocument(docId, targetDoc);

        showSuccess(
            "✅ Document status updated!\n" +
            "Status: " + newStatus,
            adminDocumentsStatusArea
        );
    }


    private String generateRequestId(String citizenId) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddyy-HHmmss");
        return "REQ-" + citizenId + "-" + now.format(formatter);
    }

    private String generateDocumentId(String citizenId) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddyy-HHmmss");
        return "DOC-" + citizenId + "-" + now.format(formatter);
    }

    private String getStatusEmoji(String status) {
        return switch (status) {
            case "APPROVED" -> "✅";
            case "REJECTED" -> "❌";
            case "PENDING" -> "⏳";
            case "COMPLETED" -> "✔️";
            case "PROCESSING" -> "⚙️";
            case "REQUESTED" -> "📩";
            default -> "❓";
        };
    }

    private boolean isValidPhilippinePhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) return false;
        String normalized = phone.trim();
        return normalized.matches("^(09|\\+639|\\+6309)\\d{9}$");
    }

    private Tab createTab(String title, Node content) {
        darkenNode(content);
        
        Tab tab = new Tab(title, content);
        tab.setClosable(false);
        
        return tab;
    }

    private void darkenNode(Node node) {
        if (node instanceof ScrollPane scroll) {
            scroll.setFitToWidth(true);
            scroll.setStyle(
                "-fx-background: " + DARK_BG + ";" +
                "-fx-background-color: " + DARK_BG + ";" +
                "-fx-control-inner-background: " + DARK_BG + ";"
            );
            Node viewport = scroll.getContent();
            if (viewport != null) darkenNode(viewport);
            scroll.setBackground(new Background(new BackgroundFill(Color.web(DARK_BG), CornerRadii.EMPTY, Insets.EMPTY)));
            return;
        }

        if (node instanceof VBox || node instanceof HBox || node instanceof Pane || node instanceof BorderPane || node instanceof AnchorPane) {
            Region region = (Region) node;
            region.setBackground(new Background(new BackgroundFill(Color.web(DARK_BG), CornerRadii.EMPTY, Insets.EMPTY)));

            if (node instanceof Pane pane) {
                for (Node child : pane.getChildren()) darkenNode(child);
            }
            return;
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) darkenNode(child);
        }
    }

    private void styleTabPane(TabPane tabPane) {
        tabPane.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-tab-min-width: 100;" +
            "-fx-tab-max-width: 200;" +
            "-fx-focus-color: transparent;" +
            "-fx-padding: 0;"
        );

        applyDarkTabPane(tabPane);
    }

    private void applyDarkTabPane(TabPane tabPane) {
        tabPane.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            try {
                Region header = (Region) tabPane.lookup(".tab-header-area");
                if (header != null) {
                    header.setStyle("-fx-background-color: " + CARD_BG + ";");
                }

                Region headerBg = (Region) tabPane.lookup(".tab-header-background");
                if (headerBg != null) {
                    headerBg. setStyle("-fx-background-color: " + CARD_BG + ";");
                }

                Region content = (Region) tabPane.lookup(".tab-content-area");
                if (content != null) {
                    content.setStyle("-fx-background-color: " + DARK_BG + ";");
                }

                for (Tab tab : tabPane.getTabs()) {
                    String originalText = tab.getText();
                    
                    Label whiteLabel = new Label(originalText);
                    whiteLabel.setStyle(
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12;" +
                        "-fx-padding: 0;"
                    );
                    whiteLabel.setTextFill(Color.WHITE);
                    
                    tab.setGraphic(whiteLabel);
                    tab.setText(null);
                }

                Runnable updateTabStyles = () -> {
                    int selectedIndex = tabPane.getSelectionModel().getSelectedIndex();
                    int tabIndex = 0;
                    
                    for (Node tabNode : tabPane.lookupAll(".tab")) {
                        if (tabNode instanceof Region) {
                            Region tabRegion = (Region) tabNode;
                            
                            if (tabIndex == selectedIndex) {
                                tabRegion.setStyle(
                                    "-fx-background-color: " + DARK_BG + ";" +
                                    "-fx-background-radius: 6 6 0 0;" +
                                    "-fx-padding: 10 20;" +
                                    "-fx-border-color: " + ACCENT_CYAN + ";" +
                                    "-fx-border-width: 0 0 3 0;"
                                );
                            } else {
                                tabRegion. setStyle(
                                    "-fx-background-color: " + CARD_BG + ";" +
                                    "-fx-background-radius: 6 6 0 0;" +
                                    "-fx-padding: 10 20;" +
                                    "-fx-border-color: " + BORDER_COLOR + ";" +
                                    "-fx-border-width: 0 0 2 0;"
                                );
                            }
                        }
                        tabIndex++;
                    }
                };

                tabPane.getSelectionModel(). selectedItemProperty().addListener((o, oldTab, newTab) -> {
                    updateTabStyles.run();
                });

                int tabIndex = 0;
                for (Node tabNode : tabPane.lookupAll(".tab")) {
                    if (tabNode instanceof Region) {
                        Region tabRegion = (Region) tabNode;
                        final int currentTabIndex = tabIndex;

                        tabRegion.setOnMouseEntered(e -> {
                            if (tabPane.getSelectionModel().getSelectedIndex() != currentTabIndex) {
                                tabRegion. setStyle(
                                    "-fx-background-color: derive(" + CARD_BG + ", 20%);" +
                                    "-fx-background-radius: 6 6 0 0;" +
                                    "-fx-padding: 10 20;" +
                                    "-fx-border-color: " + BORDER_COLOR + ";" +
                                    "-fx-border-width: 0 0 2 0;"
                                );
                            }
                        });

                        tabRegion.setOnMouseExited(e -> {
                            if (tabPane. getSelectionModel().getSelectedIndex() != currentTabIndex) {
                                tabRegion.setStyle(
                                    "-fx-background-color: " + CARD_BG + ";" +
                                    "-fx-background-radius: 6 6 0 0;" +
                                    "-fx-padding: 10 20;" +
                                    "-fx-border-color: " + BORDER_COLOR + ";" +
                                    "-fx-border-width: 0 0 2 0;"
                                );
                            }
                        });
                    }
                    tabIndex++;
                }

                updateTabStyles.run();

            } catch (Exception e) {
                System.err.println("Error styling tabs: " + e. getMessage());
                e.printStackTrace();
            }
        });
    }


    private Label createTitle(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        label.setTextFill(Color.web(ACCENT_CYAN));
        return label;
    }

    private Label createSectionTitle(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        label.setTextFill(Color.web(TEXT_WHITE));
        return label;
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", 12));
        label.setTextFill(Color.web(TEXT_WHITE));
        return label;
    }

    private Label createInfoLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", 13));
        label.setTextFill(Color.web(TEXT_WHITE));
        return label;
    }

    private TextField createTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefHeight(35);
        field.setStyle(
            "-fx-font-size: 12;" +
            "-fx-padding: 8;" +
            "-fx-background-color: " + INPUT_BG + ";" +
            "-fx-text-fill: " + TEXT_WHITE + ";" +
            "-fx-prompt-text-fill: " + TEXT_GRAY + ";" +
            "-fx-border-color: " + BORDER_COLOR + ";" +
            "-fx-border-radius: 4;" +
            "-fx-border-width: 1;" +
            "-fx-font-family: 'Segoe UI';"
        );

        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                    "-fx-font-size: 12;" +
                    "-fx-padding: 8;" +
                    "-fx-background-color: " + INPUT_BG + ";" +
                    "-fx-text-fill: " + TEXT_WHITE + ";" +
                    "-fx-prompt-text-fill: " + TEXT_GRAY + ";" +
                    "-fx-border-color: " + ACCENT_CYAN + ";" +
                    "-fx-border-radius: 4;" +
                    "-fx-border-width: 2;" +
                    "-fx-font-family: 'Segoe UI';"
                );
            } else {
                field.setStyle(
                    "-fx-font-size: 12;" +
                    "-fx-padding: 8;" +
                    "-fx-background-color: " + INPUT_BG + ";" +
                    "-fx-text-fill: " + TEXT_WHITE + ";" +
                    "-fx-prompt-text-fill: " + TEXT_GRAY + ";" +
                    "-fx-border-color: " + BORDER_COLOR + ";" +
                    "-fx-border-radius: 4;" +
                    "-fx-border-width: 1;" +
                    "-fx-font-family: 'Segoe UI';"
                );
            }
        });

        return field;
    }

    private PasswordField createPasswordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.setPrefHeight(35);
        field.setStyle(
            "-fx-font-size: 12;" +
            "-fx-padding: 8;" +
            "-fx-background-color: " + INPUT_BG + ";" +
            "-fx-text-fill: " + TEXT_WHITE + ";" +
            "-fx-prompt-text-fill: " + TEXT_GRAY + ";" +
            "-fx-border-color: " + BORDER_COLOR + ";" +
            "-fx-border-radius: 4;" +
            "-fx-border-width: 1;" +
            "-fx-font-family: 'Segoe UI';"
        );

        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                    "-fx-font-size: 12;" +
                    "-fx-padding: 8;" +
                    "-fx-background-color: " + INPUT_BG + ";" +
                    "-fx-text-fill: " + TEXT_WHITE + ";" +
                    "-fx-prompt-text-fill: " + TEXT_GRAY + ";" +
                    "-fx-border-color: " + ACCENT_CYAN + ";" +
                    "-fx-border-radius: 4;" +
                    "-fx-border-width: 2;" +
                    "-fx-font-family: 'Segoe UI';"
                );
            } else {
                field.setStyle(
                    "-fx-font-size: 12;" +
                    "-fx-padding: 8;" +
                    "-fx-background-color: " + INPUT_BG + ";" +
                    "-fx-text-fill: " + TEXT_WHITE + ";" +
                    "-fx-prompt-text-fill: " + TEXT_GRAY + ";" +
                    "-fx-border-color: " + BORDER_COLOR + ";" +
                    "-fx-border-radius: 4;" +
                    "-fx-border-width: 1;" +
                    "-fx-font-family: 'Segoe UI';"
                );
            }
        });

        return field;
    }

    private TextArea createTextArea() {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setWrapText(true);
        applyTextAreaStyle(area);
        return area;
    }

    private void applyTextAreaStyle(TextArea area) {
        area.setStyle(
            "-fx-font-size: 11;" +
            "-fx-font-family: 'Courier New';" +
            "-fx-padding: 10;" +
            "-fx-border-color: " + ACCENT_CYAN + ";" +
            "-fx-border-radius: 4;" +
            "-fx-border-width: 1;" +
            "-fx-background-color: " + INPUT_BG + ";" +
            "-fx-text-fill: " + TEXT_WHITE + ";" +
            "-fx-control-inner-background: " + INPUT_BG + ";"
        );
    }

    private Button createButton(String text, String bgColor) {
        Button button = new Button(text);
        button.setPrefHeight(35);
        button.setStyle(
            "-fx-font-size: 11;" +
            "-fx-font-weight: bold;" +
            "-fx-background-color: " + bgColor + ";" +
            "-fx-text-fill: #ffffff;" +
            "-fx-border-radius: 5;" +
            "-fx-padding: 8;" +
            "-fx-cursor: hand;" +
            "-fx-font-family: 'Segoe UI';"
        );

        button.setOnMouseEntered(e -> {
            button.setStyle(
                "-fx-font-size: 11;" +
                "-fx-font-weight: bold;" +
                "-fx-background-color: " + brightenColor(bgColor) + ";" +
                "-fx-text-fill: #ffffff;" +
                "-fx-border-radius: 5;" +
                "-fx-padding: 8;" +
                "-fx-cursor: hand;" +
                "-fx-font-family: 'Segoe UI';"
            );

            ScaleTransition scale = new ScaleTransition(Duration.millis(150), button);
            scale.setToX(1.1);
            scale.setToY(1.1);
            scale.play();
        });

        button.setOnMouseExited(e -> {
            button.setStyle(
                "-fx-font-size: 11;" +
                "-fx-font-weight: bold;" +
                "-fx-background-color: " + bgColor + ";" +
                "-fx-text-fill: #ffffff;" +
                "-fx-border-radius: 5;" +
                "-fx-padding: 8;" +
                "-fx-cursor: hand;" +
                "-fx-font-family: 'Segoe UI';"
            );

            ScaleTransition scale = new ScaleTransition(Duration.millis(150), button);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();
        });

        return button;
    }

    private void applyComboBoxStyle(ComboBox<?> comboBox) {
        comboBox.setPrefHeight(35);
        comboBox.setStyle(
            "-fx-font-size: 12;" +
            "-fx-padding: 8;" +
            "-fx-background-color: " + INPUT_BG + ";" +
            "-fx-text-fill: " + TEXT_WHITE + ";" +
            "-fx-border-color: " + BORDER_COLOR + ";" +
            "-fx-border-radius: 4;" +
            "-fx-border-width: 1;" +
            "-fx-font-family: 'Segoe UI';"
        );
    }

    private String brightenColor(String hexColor) {
        if (hexColor == null) return null;
        try {
            String hex = hexColor.replace("#", "");
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);

            r = Math.min(r + 40, 255);
            g = Math.min(g + 40, 255);
            b = Math.min(b + 40, 255);

            return String.format("#%02x%02x%02x", r, g, b);
        } catch (Exception e) {
            return hexColor;
        }
    }

    private String createCardStyle() {
        return "-fx-background-color: " + CARD_BG + ";" +
               "-fx-border-color: " + ACCENT_CYAN + ";" +
               "-fx-border-radius: 8;" +
               "-fx-border-width: 1;";
    }

    private void showSuccess(String message, TextArea statusArea) {
        if (statusArea != null) {
            statusArea.setStyle(
                "-fx-font-size: 11;" +
                "-fx-font-family: 'Courier New';" +
                "-fx-padding: 10;" +
                "-fx-border-color: " + SUCCESS_GREEN + ";" +
                "-fx-border-radius: 4;" +
                "-fx-border-width: 2;" +
                "-fx-background-color: " + INPUT_BG + ";" +
                "-fx-text-fill: " + SUCCESS_GREEN + ";" +
                "-fx-control-inner-background: " + INPUT_BG + ";"
            );
            statusArea.setText(message);
        }
    }

    private void showError(String message, TextArea statusArea) {
        if (statusArea != null) {
            statusArea.setStyle(
                "-fx-font-size: 11;" +
                "-fx-font-family: 'Courier New';" +
                "-fx-padding: 10;" +
                "-fx-border-color: " + ERROR_RED + ";" +
                "-fx-border-radius: 4;" +
                "-fx-border-width: 2;" +
                "-fx-background-color: " + INPUT_BG + ";" +
                "-fx-text-fill: " + ERROR_RED + ";" +
                "-fx-control-inner-background: " + INPUT_BG + ";"
            );
            statusArea.setText(message);
        }
    }
}