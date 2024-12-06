package com.uet.libraryManagement.Controllers;

import com.uet.libraryManagement.Managers.SceneManager;
import com.uet.libraryManagement.Managers.SessionManager;
import com.uet.libraryManagement.Managers.TaskManager;
import com.uet.libraryManagement.Repositories.BookRepository;
import com.uet.libraryManagement.Document;
import com.uet.libraryManagement.Repositories.ThesisRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
        import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public abstract class DocumentsController implements Initializable {
    @FXML
    protected TableView<Document> docsTable;
    @FXML
    protected TableColumn<Document, Integer> idCol, quantityCol;
    @FXML
    protected TableColumn<Document, String> titleCol, authorCol, categoryCol, publisherCol, dateCol, isbn10Col, isbn13Col, statusCol;
    @FXML
    protected ComboBox<String> docTypeBox;
    @FXML
    protected TextField titleFilter, authorFilter, categoryFilter, isbn10Filter, isbn13Filter, searchBar;
    @FXML
    protected DatePicker startDateFilter, endDateFilter;
    @FXML
    protected VBox filtersPanel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        publisherCol.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("year"));
        isbn10Col.setCellValueFactory(new PropertyValueFactory<>("isbn10"));
        isbn13Col.setCellValueFactory(new PropertyValueFactory<>("isbn13"));

        if (SessionManager.getInstance().getUser().getRole().equals("admin")) {
            quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        } else {
            statusCol.setCellValueFactory(cellData -> {
                Document doc = cellData.getValue();
                return doc.getQuantity() > 0 ? new SimpleStringProperty("Available") : new SimpleStringProperty("Out of stock");
            });
        }

        docTypeBox.getItems().addAll("Books", "Theses");
        docTypeBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            loadDocuments(newValue);
        });

        docTypeBox.getSelectionModel().select("Books");

        docsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                if (docsTable.getSelectionModel().getSelectedItem() != null) {
                    showDocumentDetails();
                }
            }
        });

        filtersPanel.setVisible(false);
        HandleOutsideClickListener();
    }

    @FXML
    private void toggleFilters() {
        filtersPanel.setVisible(!filtersPanel.isVisible());
        if (filtersPanel.isVisible()) {
            filtersPanel.toFront();
        }
    }

    @FXML
    private void handleSearchAction() {
        String searchTerm = searchBar.getText();
        loadSearchedDocuments(searchTerm);
    }

    @FXML
    private void clearSearchTerm() {
        searchBar.clear();
        loadDocuments(docTypeBox.getValue());
    }

    @FXML
    private void applyFilters() {
        String title = titleFilter.getText();
        String author = authorFilter.getText();
        String category = categoryFilter.getText();
        String isbn10 = isbn10Filter.getText();
        String isbn13 = isbn13Filter.getText();
        String startYear = (startDateFilter.getValue() != null) ? String.valueOf(startDateFilter.getValue().getYear()) : null;
        String endYear = (endDateFilter.getValue() != null) ? String.valueOf(endDateFilter.getValue().getYear()) : null;
        filtersPanel.setVisible(false);
        loadDocuments(title, author, category, startYear, endYear, isbn10, isbn13);
    }

    @FXML
    private void clearFilters() {
        titleFilter.clear();
        authorFilter.clear();
        categoryFilter.clear();
        isbn10Filter.clear();
        isbn13Filter.clear();
        startDateFilter.setValue(null);
        endDateFilter.setValue(null);
        loadDocuments(docTypeBox.getValue());
    }

    protected void loadDocuments(String docType) {
        Task<ObservableList<Document>> loadDocumentsTask = new Task<ObservableList<Document>>() {
            @Override
            protected ObservableList<Document> call() throws Exception {
                ObservableList<Document> documents;
                if ("Books".equals(docType)) {
                    documents = BookRepository.getInstance().getAllBooks();
                } else {
                    documents = ThesisRepository.getInstance().getAllTheses();
                }
                return documents;
            }
        };

        Runnable onSuccess = () -> {
            ObservableList<Document> documents = loadDocumentsTask.getValue();
            docsTable.setItems(documents);
        };

        Runnable onFailure = () -> {
            Throwable ex = loadDocumentsTask.getException();
            if (ex != null) ex.printStackTrace();
        };

        TaskManager.runTask(loadDocumentsTask, onSuccess, onFailure);
    }

    private void HandleOutsideClickListener() {
        docsTable.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                    if (event.getTarget() instanceof Node) {
                        Node target = (Node) event.getTarget();
                        while (target != null) {
                            if (target == docsTable) {
                                return;
                            }
                            target = target.getParent();
                        }
                        docsTable.getSelectionModel().clearSelection();
                    }
                });
            }
        });
    }

    protected void showDocumentDetails() {
        Document selectedDocument = docsTable.getSelectionModel().getSelectedItem();
        String docType = docTypeBox.getValue();
        System.out.println(docType);
        if (selectedDocument != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/uet/libraryManagement/FXML/DocumentDetail.fxml"));
                Parent detailRoot = loader.load();

                DocumentDetailController controller = loader.getController();
                controller.setDocumentDetails(selectedDocument);
                controller.setDocument(selectedDocument);
                controller.setDocType(docType);
                controller.loadComments(selectedDocument.getId(), docType);
                Scene detailScene = new Scene(detailRoot);

                detailScene.getStylesheets().add(SceneManager.getInstance().get_css());
                Stage detailStage = new Stage();
                detailStage.setResizable(false);
                String icon_url = Objects.requireNonNull(this.getClass().getResource("/com/uet/libraryManagement/ICONS/logo.png")).toExternalForm();
                Image icon = new Image(icon_url);
                detailStage.getIcons().add(icon);
                detailStage.setTitle("Document Details");
                detailStage.setScene(detailScene);
                detailStage.initModality(Modality.APPLICATION_MODAL);
                detailStage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadDocuments(String title, String author, String category, String startYear, String endYear, String isbn10, String isbn13) {
        Task<ObservableList<Document>> loadDocumentsTask = new Task<>() {
            @Override
            protected ObservableList<Document> call() throws Exception {
                ObservableList<Document> documents;
                if (docTypeBox.getValue().equals("Books")) {
                    documents = BookRepository.getInstance().getFilteredDocuments(title, author, category, startYear, endYear, isbn10, isbn13);
                } else {
                    documents = ThesisRepository.getInstance().getFilteredDocuments(title, author, category, startYear, endYear, isbn10, isbn13);
                }
                return documents;
            }
        };

        Runnable onSuccess = () -> {
            ObservableList<Document> documents = loadDocumentsTask.getValue();
            docsTable.setItems(documents);
        };

        Runnable onFailure = () -> {
            Throwable ex = loadDocumentsTask.getException();
            if (ex != null) ex.printStackTrace();
        };

        TaskManager.runTask(loadDocumentsTask, onSuccess, onFailure);
    }

    private void loadSearchedDocuments(String searchTerm) {
        Task<ObservableList<Document>> loadSearchedDocumentsTask = new Task<>() {
            @Override
            protected ObservableList<Document> call() throws Exception {
                ObservableList<Document> documents;
                if (docTypeBox.getValue().equals("Books")) {
                    documents = BookRepository.getInstance().searchDocument(searchTerm);
                } else {
                    documents = ThesisRepository.getInstance().searchDocument(searchTerm);
                }
                return documents;
            }
        };

        Runnable onSuccess = () -> {
            ObservableList<Document> documents = loadSearchedDocumentsTask.getValue();
            docsTable.setItems(documents);
        };

        Runnable onFailure = () -> {
            Throwable ex = loadSearchedDocumentsTask.getException();
            if (ex != null) ex.printStackTrace();
        };

        TaskManager.runTask(loadSearchedDocumentsTask, onSuccess, onFailure);
    }

    public void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
