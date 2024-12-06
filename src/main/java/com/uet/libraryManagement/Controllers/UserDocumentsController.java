package com.uet.libraryManagement.Controllers;

import com.uet.libraryManagement.Book;
import com.uet.libraryManagement.Managers.TaskManager;
import com.uet.libraryManagement.Repositories.BorrowRepository;
import com.uet.libraryManagement.Document;
import com.uet.libraryManagement.Managers.SessionManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;

public class UserDocumentsController extends DocumentsController {
    @FXML
    private void takeDoc() {
        Document selectedDocument = docsTable.getSelectionModel().getSelectedItem();
        if (selectedDocument == null) {
            showAlert("No document selected. Please select a document to borrow.");
            return;
        }

        if (selectedDocument.getQuantity() == 0) {
            showAlert("This document is currently out of stock.");
            return;
        }

        int userId = SessionManager.getInstance().getUser().getId();
        int docId = selectedDocument.getId();
        String docType = (selectedDocument instanceof Book) ? "book" : "thesis";

        Task<Boolean> borrowTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                if (BorrowRepository.getInstance().hasUserAlreadyBorrowedDocument(userId, docId, docType)) {
                    updateMessage("You have already borrowed this document.");
                    return false;
                }

                BorrowRepository.getInstance().borrowDocument(userId, docId, docType);
                int newQuantity = selectedDocument.getQuantity() - 1;
                BorrowRepository.getInstance().updateDocumentQuantity(docId, docType, newQuantity);

                selectedDocument.setQuantity(newQuantity);
                return true;
            }
        };

        Runnable onSuccess = () -> {
            if (borrowTask.getValue()) {
                loadDocuments(docType.equals("book") ? "Books" : "Theses");
                showAlert("Document borrowed successfully.");
            } else {
                showAlert(borrowTask.getMessage());
            }
        };

        Runnable onFailure = () -> {
            showAlert("An error occurred while borrowing the document. Please try again.");
        };

        TaskManager.runTask(borrowTask, onSuccess, onFailure);
    }
}
