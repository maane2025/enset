package com.example.ensetchat;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ChatController {
    @FXML
    private ListView<String> listView_chat;
    @FXML
    private ListView<String> listView_conversations;
    @FXML
    private TextField textArea_messageBox;
    @FXML
    private Button button_send;

    private HashMap<String, List<String>> conversationsHistory = new HashMap<>();
    private List<String> messages = new ArrayList<>();
    private List<String> conversations = new ArrayList<>();
    private String currentConversation = null;
    private static final String SAVE_FILE_PATH = "conversations.dat";

    @FXML
    public void initialize() {
        loadConversations();
        listView_conversations.getItems().addAll(conversations);

        if (!conversations.isEmpty()) {
            currentConversation = conversations.get(0);
            messages = conversationsHistory.getOrDefault(currentConversation, new ArrayList<>());
            listView_chat.getItems().addAll(messages);
        }

        // Context menu for chat messages
        ContextMenu chatContextMenu = new ContextMenu();
        MenuItem editChatItem = new MenuItem("Modifier le message");

        editChatItem.setOnAction(event -> {
            int selectedIndex = listView_chat.getSelectionModel().getSelectedIndex();
            if (selectedIndex != -1) {
                String oldMessage = messages.get(selectedIndex);
                TextInputDialog dialog = new TextInputDialog(oldMessage);
                dialog.setTitle("Modifier le message");
                dialog.setHeaderText("Entrez le nouveau message:");
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(newMessage -> {
                    messages.set(selectedIndex, newMessage);
                    listView_chat.getItems().set(selectedIndex, newMessage);
                    saveConversations();
                });
            }
        });
        chatContextMenu.getItems().addAll(editChatItem);
        listView_chat.setContextMenu(chatContextMenu);

        // Context menu for conversations
        ContextMenu conversationContextMenu = new ContextMenu();
        MenuItem deleteConversationItem = new MenuItem("Supprimer la conversation");

        deleteConversationItem.setOnAction(event -> {
            int selectedIndex = listView_conversations.getSelectionModel().getSelectedIndex();
            if (selectedIndex != -1) {
                String conversationToDelete = conversations.get(selectedIndex);
                conversations.remove(selectedIndex);
                conversationsHistory.remove(conversationToDelete);
                listView_conversations.getItems().remove(selectedIndex);
                saveConversations();

                // Clear messages when a conversation is deleted
                messages.clear();
                listView_chat.getItems().clear();
            }
        });

        conversationContextMenu.getItems().addAll(deleteConversationItem);
        listView_conversations.setContextMenu(conversationContextMenu);

        // Add listener for conversation selection
        listView_conversations.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        currentConversation = newValue;
                        messages = conversationsHistory.getOrDefault(currentConversation, new ArrayList<>());
                        listView_chat.getItems().clear();
                        listView_chat.getItems().addAll(messages);
                    }
                }
        );
    }

    private void loadConversations() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_FILE_PATH))) {
            conversationsHistory = (HashMap<String, List<String>>) ois.readObject();
            conversations = new ArrayList<>(conversationsHistory.keySet());
        } catch (FileNotFoundException e) {
            System.out.println("Aucun fichier de sauvegarde trouvé. Création d'un nouveau fichier.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveConversations() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE_PATH))) {
            oos.writeObject(conversationsHistory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void sendButtonAction() {
        sendMessage();
    }

    @FXML
    private void sendMethod(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            sendMessage();
        }
    }

    private void sendMessage() {
        if (currentConversation == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attention");
            alert.setHeaderText("Aucune conversation sélectionnée");
            alert.setContentText("Veuillez créer ou sélectionner une conversation avant d'envoyer un message.");
            alert.showAndWait();
            return;
        }

        String message = textArea_messageBox.getText();
        if (!message.isEmpty()) {
            messages.add(message);
            listView_chat.getItems().add(message);
            conversationsHistory.put(currentConversation, messages);
            saveConversations();
            textArea_messageBox.clear();
        }
    }

    @FXML
    private void attachFile() {
        if (currentConversation == null) return;

        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            String filePath = selectedFile.getAbsolutePath();
            messages.add("Pièce jointe : " + filePath);
            listView_chat.getItems().add("Pièce jointe : " + filePath);
            conversationsHistory.put(currentConversation, messages);
            saveConversations();
        }
    }

    @FXML
    private void clearHistory() {
        if (currentConversation != null) {
            messages.clear();
            listView_chat.getItems().clear();
            conversationsHistory.put(currentConversation, messages);
            saveConversations();
        }
    }

    @FXML
    private void newConversation() {
        TextInputDialog dialog = new TextInputDialog("Nom de la conversation");
        dialog.setTitle("Nouvelle conversation");
        dialog.setHeaderText("Entrez le nom de la nouvelle conversation:");

        dialog.showAndWait().ifPresent(conversationName -> {
            if (!conversations.contains(conversationName)) {
                conversations.add(conversationName);
                listView_conversations.getItems().add(conversationName);
                currentConversation = conversationName;
                messages = new ArrayList<>();
                conversationsHistory.put(conversationName, messages);
                listView_chat.getItems().clear();
                saveConversations();
            }
        });
    }
}