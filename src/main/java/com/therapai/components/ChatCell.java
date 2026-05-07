package com.therapai.components;

import com.therapai.models.ConversationModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.function.Consumer;

//Necesitamos ListCells para poder personalizar la lista de chats
public class ChatCell extends ListCell<ConversationModel> {
    private final HBox root = new HBox();
    private final Label title = new Label();
    private final Button deleteBtn = new Button("\uD83D\uDDD1");

    public ChatCell(Consumer<ConversationModel> onDelete, Consumer<ConversationModel> onOpen) {

        root.setSpacing(10);
        root.setPadding(new Insets(12));
        root.setAlignment(Pos.CENTER_LEFT);
        root.setStyle("-fx-background-color: #1E293B; -fx-background-radius: 8;");

        title.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-font-size: 18px;");
        deleteBtn.setOnAction(e -> {
            ConversationModel chat = getItem();
            if (chat != null) onDelete.accept(chat);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        root.setOnMouseClicked(e -> {
            ConversationModel chat = getItem();
            if (chat != null) onOpen.accept(chat);
        });

        root.getChildren().addAll(title, spacer, deleteBtn);
    }

    @Override
    protected void updateItem(ConversationModel chat, boolean empty) {
        super.updateItem(chat, empty);

        if (empty || chat == null) {
            setGraphic(null);
        } else {
            title.setText(chat.getTitulo());
            setGraphic(root);
        }
    }

}
