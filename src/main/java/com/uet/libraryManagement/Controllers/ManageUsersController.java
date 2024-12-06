package com.uet.libraryManagement.Controllers;

import com.uet.libraryManagement.Managers.SceneManager;
import com.uet.libraryManagement.Managers.SessionManager;
import com.uet.libraryManagement.Managers.TaskManager;
import com.uet.libraryManagement.Repositories.UserRepository;
import com.uet.libraryManagement.User;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ManageUsersController {
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> idCol;
    @FXML private TableColumn<User, String> userNameCol;
    @FXML private TableColumn<User, String> passwordCol;
    @FXML private TableColumn<User, String> fullNameCol;
    @FXML private TableColumn<User, String> emailCol;
    @FXML private TableColumn<User, String> phoneCol;
    @FXML private TableColumn<User, String> roleCol;
    @FXML private TextField searchBar;

    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        userNameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        passwordCol.setCellValueFactory(new PropertyValueFactory<>("password"));
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        loadUsers();

        usersTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                if (usersTable.getSelectionModel().getSelectedItem() != null) {
                    showUserDetails();
                }
            }
        });

        HandleOutsideClickListener();
    }

    protected void showUserDetails() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/uet/libraryManagement/FXML/UserDetail.fxml"));
                Parent detailRoot = loader.load();

                UserDetailController controller = loader.getController();
                controller.setUserDetails(selectedUser);

                Scene detailScene = new Scene(detailRoot);

                detailScene.getStylesheets().add(SceneManager.getInstance().get_css());
                Stage detailStage = new Stage();
                detailStage.setResizable(false);
                String icon_url = Objects.requireNonNull(this.getClass().getResource("/com/uet/libraryManagement/ICONS/logo.png")).toExternalForm();
                Image icon = new Image(icon_url);
                detailStage.getIcons().add(icon);
                detailStage.setTitle("User information");
                detailStage.setScene(detailScene);
                detailStage.initModality(Modality.APPLICATION_MODAL);
                detailStage.showAndWait();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadUsers() {
        Task<List<User>> loadUsersTask = new Task<>() {
            @Override
            protected List<User> call() {
                return UserRepository.getInstance().getAllUsers();
            }
        };

        TaskManager.runTask(loadUsersTask,
                () -> usersTable.getItems().setAll(loadUsersTask.getValue()),
                () -> showAlert("Failed to load users. Please try again.")
        );
    }

    @FXML
    private void searchUser() {
        String searchTerm = searchBar.getText();
        if (searchTerm != null && !searchTerm.isEmpty()) {
            Task<List<User>> searchTask = new Task<>() {
                @Override
                protected List<User> call() {
                    return UserRepository.getInstance().searchUsers(searchTerm);
                }
            };

            TaskManager.runTask(searchTask,
                    () -> usersTable.setItems(FXCollections.observableArrayList(searchTask.getValue())),
                    () -> showAlert("Failed to search users. Please try again.")
            );
        } else {
            showAlert("Please enter a search term");
        }
    }

    @FXML
    private void clearSearchTerm() {
        searchBar.clear();
        loadUsers();
    }

    @FXML
    private void issueHistory() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/uet/libraryManagement/FXML/AdminHistory.fxml"));
                Parent historyRoot = loader.load();

                HistoryController historyController = loader.getController();
                historyController.loadHistory(selectedUser.getId(), "Books");
                historyController.setSelectedUserId(selectedUser.getId());

                Scene detailScene = new Scene(historyRoot);

                detailScene.getStylesheets().add(SceneManager.getInstance().get_css());
                Stage historyStage = new Stage();
                historyStage.setResizable(false);
                String icon_url = Objects.requireNonNull(this.getClass().getResource("/com/uet/libraryManagement/ICONS/logo.png")).toExternalForm();
                Image icon = new Image(icon_url);
                historyStage.getIcons().add(icon);
                historyStage.setTitle("Borrowing History for " + selectedUser.getUsername());
                historyStage.setScene(detailScene);
                historyStage.initModality(Modality.APPLICATION_MODAL);
                historyStage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert("Please select a user to view their borrowing history.");
        }
    }

    @FXML
    private void addUser() throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/uet/libraryManagement/FXML/AddUser.fxml"));
            Parent detailRoot = loader.load();

            Scene detailScene = new Scene(detailRoot);

            detailScene.getStylesheets().add(SceneManager.getInstance().get_css());
            Stage detailStage = new Stage();
            detailStage.setResizable(false);
            String icon_url = Objects.requireNonNull(this.getClass().getResource("/com/uet/libraryManagement/ICONS/logo.png")).toExternalForm();
            Image icon = new Image(icon_url);
            detailStage.getIcons().add(icon);
            detailStage.setTitle("Add user");
            detailStage.setScene(detailScene);
            detailStage.initModality(Modality.APPLICATION_MODAL);
            detailStage.showAndWait();

            loadUsers();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void editUser() {
        User user = usersTable.getSelectionModel().getSelectedItem();
        if (user != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/uet/libraryManagement/FXML/UserForm.fxml"));
                Parent editRoot = loader.load();

                UserFormController userFormController = loader.getController();
                userFormController.setUserInfo(user);

                Scene detailScene = new Scene(editRoot);

                detailScene.getStylesheets().add(SceneManager.getInstance().get_css());
                Stage editStage = new Stage();
                editStage.setResizable(false);
                String icon_url = Objects.requireNonNull(this.getClass().getResource("/com/uet/libraryManagement/ICONS/logo.png")).toExternalForm();
                Image icon = new Image(icon_url);
                editStage.getIcons().add(icon);
                editStage.setTitle("Edit User");
                editStage.setScene(detailScene);
                editStage.initModality(Modality.APPLICATION_MODAL);
                editStage.showAndWait();

                loadUsers();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert("Please select a user to edit.");
        }
    }

    @FXML
    private void deleteUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            if (selectedUser.getId() == SessionManager.getInstance().getUser().getId()) {
                showAlert("You can't delete your own user");
                return;
            }

            if (UserRepository.getInstance().isUserBorrowingDocuments(selectedUser.getId())) {
                showAlert("You can't delete user borrowing documents");
                return;
            }

            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Delete User");
            confirmationAlert.setContentText("Are you sure you want to delete this user?");

            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Task<Void> deleteTask = new Task<>() {
                    @Override
                    protected Void call() {
                        UserRepository.getInstance().deleteUser(selectedUser.getId());
                        return null;
                    }
                };

                TaskManager.runTask(deleteTask,
                        () -> {
                            showAlert("User deleted successfully");
                            loadUsers();
                        },
                        () -> showAlert("Failed to delete user. Please try again.")
                );
            }
        } else {
            showAlert("Please select a user to delete.");
        }
    }

    private void HandleOutsideClickListener() {
        usersTable.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                    if (event.getTarget() instanceof Node) {
                        Node target = (Node) event.getTarget();
                        while (target != null) {
                            if (target == usersTable) {
                                return;
                            }
                            target = target.getParent();
                        }
                        usersTable.getSelectionModel().clearSelection();
                    }
                });
            }
        });
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
