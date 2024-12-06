package com.uet.libraryManagement.Controllers;

import com.jfoenix.controls.JFXButton;
import com.uet.libraryManagement.Managers.SceneManager;
import com.uet.libraryManagement.Managers.SessionManager;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class MenuController {
    private boolean isMenuVisible = false;
    private boolean isLight = false;

    @FXML private Label Title_page;
    @FXML private JFXButton DL_mode;
    @FXML private ImageView DL_image;
    @FXML private AnchorPane overlayPane;
    @FXML private VBox menuBox;
    @FXML private AnchorPane contentPane;

    @FXML
    private void initialize() throws IOException {
        SceneManager.getInstance().setContentPane(contentPane);
        menuBox.setTranslateX(-menuBox.getPrefWidth());
        String fullName = SessionManager.getInstance().getUser().getFullName();
        String username = SessionManager.getInstance().getUser().getUsername();
        Title_page.setText("Welcome " + ((fullName == null || fullName.equals("N/A")) ? username : fullName) + " !");
    }

    @FXML
    private void exit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit confirmation");
        alert.setHeaderText("Do you really want to exit?");
        alert.setContentText("Choose OK to exit the application.");

        Optional<ButtonType> result = alert.showAndWait();

        if (((Optional<?>) result).isPresent() && result.get() == ButtonType.OK) {
            Platform.exit();
        }
    }

    public static void close(Stage stage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit confirmation");
        alert.setHeaderText("Do you really want to exit?");
        alert.setContentText("Choose OK to exit the application.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            stage.close();
        }
    }

    public void DL_mode(ActionEvent event) {
        if (isLight) {
            SceneManager.getInstance().setDL_Mode(isLight);
            DL_image.setImage(new Image(Objects.requireNonNull(getClass().getResource("/com/uet/libraryManagement/ICONS/dark.png")).toExternalForm()));
            isLight = false;
        } else {
            SceneManager.getInstance().setDL_Mode(isLight);
            Image image = new Image(Objects.requireNonNull(getClass().getResource("/com/uet/libraryManagement/ICONS/light.png")).toExternalForm());
            DL_image.setImage(image);
            isLight = true;
        }
    }

    @FXML
    private void Home() {
        if (SessionManager.getInstance().getUser().getRole().equals("user")) {
            SceneManager.getInstance().setSubScene("FXML/UserDashboard.fxml");
        } else {
            SceneManager.getInstance().setSubScene("FXML/AdminDashboard.fxml");
        }
        Title_page.setText("Dashboard");
        hideMenuBox();
    }

    @FXML
    private void Profile() {
        SceneManager.getInstance().setSubScene("FXML/Profile.fxml");
        Title_page.setText("Profile");
        hideMenuBox();
    }

    @FXML
    private void Users() {
        SceneManager.getInstance().setSubScene("FXML/Users.fxml");
        Title_page.setText("Users");
        hideMenuBox();
    }

    @FXML
    private void Documents() throws IOException {
        if (SessionManager.getInstance().getUser().getRole().equals("user")) {
            SceneManager.getInstance().setSubScene("FXML/UserDocuments.fxml");
        } else {
            SceneManager.getInstance().setSubScene("FXML/AdminDocuments.fxml");
        }
        Title_page.setText("Documents");
        hideMenuBox();
    }

    @FXML
    private void SearchDocuments() {
        SceneManager.getInstance().setSubScene("FXML/SearchDocuments.fxml");
        Title_page.setText("Search Documents");
        hideMenuBox();
    }

    @FXML
    private void History() {
        SceneManager.getInstance().setSubScene("FXML/UserHistory.fxml");
        Title_page.setText("History");
        hideMenuBox();
    }

    @FXML
    private void LogOut() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log Out Confirmation");
        alert.setHeaderText("Do you really want to log out?");
        alert.setContentText("Choose OK to log out.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SessionManager.getInstance().logout();

            try {
                SceneManager.getInstance().setLoginScene("FXML/Login.fxml");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void menuButton() {
        if (isMenuVisible) {
            hideMenuBox();
        } else {
            showMenuBox();
        }
    }

    private void showMenuBox() {
        overlayPane.setVisible(true);
        overlayPane.toFront();
        menuBox.toFront();

        FadeTransition fadeInOverlay = new FadeTransition(Duration.millis(300), overlayPane);
        fadeInOverlay.setFromValue(0);
        fadeInOverlay.setToValue(0.5);
        fadeInOverlay.play();

        TranslateTransition transition = new TranslateTransition(Duration.millis(200), menuBox);
        transition.setFromX(-menuBox.getPrefWidth());
        transition.setToX(0);
        transition.setOnFinished(event -> isMenuVisible = true);
        transition.play();
        menuBox.setVisible(true);

        overlayPane.setOnMouseClicked(event -> hideMenuBox());
    }

    private void hideMenuBox() {
        FadeTransition fadeOutOverlay = new FadeTransition(Duration.millis(200), overlayPane);
        fadeOutOverlay.setFromValue(0.5);
        fadeOutOverlay.setToValue(0);
        fadeOutOverlay.setOnFinished(event -> overlayPane.setVisible(false));
        fadeOutOverlay.play();

        TranslateTransition transition = new TranslateTransition(Duration.millis(200), menuBox);
        transition.setFromX(0);
        transition.setToX(-menuBox.getPrefWidth());
        transition.setOnFinished(event -> {
            menuBox.setVisible(false);
            overlayPane.toBack();
            menuBox.toBack();
            isMenuVisible = false;
        });
        transition.play();
    }
}
