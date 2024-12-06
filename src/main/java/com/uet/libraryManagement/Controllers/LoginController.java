package com.uet.libraryManagement.Controllers;

import com.uet.libraryManagement.ConnectJDBC;
import com.uet.libraryManagement.Managers.SceneManager;
import com.uet.libraryManagement.Managers.SessionManager;
import com.uet.libraryManagement.Managers.TaskManager;
import com.uet.libraryManagement.User;
import com.uet.libraryManagement.Repositories.UserRepository;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.io.IOException;

public class LoginController {
    public TextField username_textfield;
    public TextField password_textfield;
    public Label messageLabel;

    @FXML
    public void initialize() {
        Task<Void> dbConnectTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ConnectJDBC.connect();
                return null;
            }
        };

        TaskManager.runTask(dbConnectTask, null, () -> messageLabel.setText("Failed to connect to the database"));

        username_textfield.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER -> handleLogin();
            }
        });

        password_textfield.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER -> handleLogin();
            }
        });
    }

    @FXML
    public void handleLogin() {
        String username = username_textfield.getText();
        String password = password_textfield.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password.");
            return;
        }

        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                return UserRepository.getInstance().validateUser(username, password);
            }
        };

        Runnable onSuccess = () -> {
            User user = loginTask.getValue();
            if (user != null && user.getRole().equals("user")) {
                SessionManager.getInstance().setUser(user);
                try {
                    SceneManager.getInstance().setScene("FXML/UserMenu.fxml");
                    SceneManager.getInstance().setSubScene("FXML/UserDashboard.fxml");
                } catch (IOException e) {
                    e.printStackTrace();
                    messageLabel.setText("Failed to load the application.");
                }
            } else if (user != null && user.getRole().equals("admin"))  {
                SessionManager.getInstance().setUser(user);
                try {
                    SceneManager.getInstance().setScene("FXML/AdminMenu.fxml");
                    SceneManager.getInstance().setSubScene("FXML/AdminDashboard.fxml");
                } catch (IOException e) {
                    e.printStackTrace();
                    messageLabel.setText("Failed to load the application.");
                }
            }
            else {
                messageLabel.setText("Invalid username or password.");
            }
        };

        Runnable onFailure = () -> {
            Throwable ex = loginTask.getException();
            if (ex != null) ex.printStackTrace();
            messageLabel.setText("Failed to authenticate user.");
        };

        TaskManager.runTask(loginTask, onSuccess, onFailure);
    }

    @FXML
    public void handleRegister() throws IOException {
        SceneManager.getInstance().setLoginScene("FXML/Register.fxml");
    }
}
