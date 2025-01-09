package com.example.ensetchat;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.scene.control.cell.TextFieldListCell;

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
    @FXML
    private Button clearConversationButton;


    private HashMap<String, List<String>> conversationsHistory = new HashMap<>();
    private List<String> messages = new ArrayList<>();
    private List<String> conversations = new ArrayList<>();
    private String currentConversation = null;
    private static final String SAVE_FILE_PATH = "conversations.dat";
    private static final int MAX_MESSAGE_LENGTH = 70;

    @FXML
    public void initialize() {
        loadConversations();
        listView_conversations.getItems().addAll(conversations);

        if (!conversations.isEmpty()) {
            currentConversation = conversations.get(0);
            messages = conversationsHistory.getOrDefault(currentConversation, new ArrayList<>());
            listView_chat.getItems().addAll(messages);
        }

        listView_chat.setCellFactory(TextFieldListCell.forListView());
        listView_chat.setEditable(true);

        listView_chat.setOnEditCommit(event -> {
            int editedIndex = event.getIndex();
            String newMessage = event.getNewValue();

            if (!messages.get(editedIndex).startsWith("IA :")) {
                messages.set(editedIndex, newMessage);

                int aiResponseIndex = editedIndex + 1;
                if (aiResponseIndex < messages.size() && messages.get(aiResponseIndex).startsWith("IA :")) {
                    messages.remove(aiResponseIndex);
                    listView_chat.getItems().remove(aiResponseIndex);
                    simulateAIResponse(true);
                }
                conversationsHistory.put(currentConversation, messages);
                saveConversations();

            } else {
                listView_chat.getItems().set(editedIndex, messages.get(editedIndex));

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information");
                alert.setHeaderText(null);
                alert.setContentText("Les messages de l'IA ne peuvent pas être modifiés.");
                alert.showAndWait();
            }
        });

        listView_conversations.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        currentConversation = newValue;
                        messages = conversationsHistory.getOrDefault(currentConversation, new ArrayList<>());
                        listView_chat.getItems().clear();
                        for (String message : messages) {
                            listView_chat.getItems().add(formatMessage(message));
                        }
                    }
                }
        );

        listView_chat.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setWrapText(true);
                    setPrefWidth(item.length() > MAX_MESSAGE_LENGTH ? 500 : 300);
                    if (item.startsWith("Utilisateur :")) {
                        setStyle("-fx-background-color: #e6f2ff; -fx-alignment: center-right;");
                    } else if (item.startsWith("IA :")) {
                        setStyle("-fx-background-color: #f0f0f0; -fx-alignment: center-left;");
                    }
                }
            }
        });

        clearConversationButton.setOnAction(event -> clearHistory());
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
            String formattedMessage = "Utilisateur : " + message;
            messages.add(formattedMessage);
            listView_chat.getItems().add(formatMessage(formattedMessage));
            conversationsHistory.put(currentConversation, messages);
            saveConversations();

            simulateAIResponse(false);
            textArea_messageBox.clear();
        }
    }

    private void simulateAIResponse(boolean modifyResponse) {
        String aiResponse = modifyResponse ? "IA : La réponse a été modifiée suite à la modification du message utilisateur." : "IA : Voici la réponse à votre message.";
        messages.removeIf(msg -> msg.startsWith("IA :"));
        messages.add(aiResponse);
        listView_chat.getItems().add(formatMessage(aiResponse));
        conversationsHistory.put(currentConversation, messages);
        saveConversations();
    }

    private String formatMessage(String message) {
        StringBuilder formattedMessage = new StringBuilder();
        int length = message.length();
        for (int i = 0; i < length; i += MAX_MESSAGE_LENGTH) {
            if (i + MAX_MESSAGE_LENGTH < length) {
                formattedMessage.append(message, i, i + MAX_MESSAGE_LENGTH).append("\n");
            } else {
                formattedMessage.append(message.substring(i));
            }
        }
        return formattedMessage.toString();
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

            // Ajouter une question ou description après l'importation du fichier
            String question = "Utilisateur : Veuillez poser une question ou donner une description concernant ce fichier.";
            messages.add(question);
            listView_chat.getItems().add(formatMessage(question));
            conversationsHistory.put(currentConversation, messages);
            saveConversations();
        }
    }

    @FXML
    private void clearHistory() {
        if (currentConversation != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer la conversation ?");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer toute la conversation ? Cette action est irréversible.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                messages.clear();
                listView_chat.getItems().clear();
                conversationsHistory.remove(currentConversation);
                conversations.remove(currentConversation);
                listView_conversations.getItems().remove(currentConversation);

                if (!conversations.isEmpty()) {
                    currentConversation = conversations.get(0);
                    messages = conversationsHistory.getOrDefault(currentConversation, new ArrayList<>());
                    listView_chat.getItems().addAll(messages);
                } else {
                    currentConversation = null;
                    messages = new ArrayList<>();
                }

                saveConversations();
            }

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
                listView_chat.getItems().addAll(messages);
                saveConversations();
            }
        });
    }
}
