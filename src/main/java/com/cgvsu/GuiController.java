package com.cgvsu;

import com.cgvsu.objWriter.objWriter;
import com.cgvsu.render_engine.RenderEngine;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Vector3f;

import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.Camera;

import com.cgvsu.objWriter.objWriter;

import javafx.scene.control.Alert;

public class GuiController {

    final private float TRANSLATION = 5.0F;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    @FXML
    private Label statusLabel;

    private List<Model> models = new ArrayList<>();
    private Model selectedModel = null;

    private ComboBox<String> modelSelector;

    private Camera camera = new Camera(
            new Vector3f(0, 00, 100),
            new Vector3f(0, 0, 0),
            1.0F, 1, 0.01F, 100);

    private Timeline timeline;


    @FXML
    private void initialize() {
        // Настраиваем изменение размеров Canvas
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));


        modelSelector = new ComboBox<>();
        modelSelector.setLayoutX(10);
        modelSelector.setLayoutY(35);
        modelSelector.setPromptText("Выберите модель");
        modelSelector.setFocusTraversable(false); //чтобы стрелки не работали
        modelSelector.setOnAction(e -> onModelSelected());
        anchorPane.getChildren().add(modelSelector);

        // Создаём Timeline для анимации
        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            camera.setAspectRatio((float) (width / height));

            // Рисуем модель
            if (selectedModel != null) {
                RenderEngine.render(canvas.getGraphicsContext2D(), camera, selectedModel, (int) width, (int) height);
            }
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    @FXML
    // READER
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = Files.readString(fileName);
            Model loadedModel = ObjReader.read(fileContent);

            models.add(loadedModel);
            modelSelector.getItems().add(file.getName()); //имя
            modelSelector.getSelectionModel().selectLast(); //последняя=текущая
            selectedModel = loadedModel;

            updateStatusBar();

            // todo: обработка ошибок
        } catch (IOException exception) {
            //окошко ошибки
            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setTitle("Ошибка чтения файла");
            alert.setHeaderText("Не удалось прочитать файл");
            alert.setContentText("Файл может быть поврежден или занят другим процессом.");

            alert.showAndWait();
        } catch (Exception exception) {
            // ObjReader выбросил исключение
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка формата файла");
            alert.setHeaderText("Файл имеет неверный формат OBJ");
            alert.setContentText("Подробности: " + exception.getMessage());
            alert.showAndWait();
        }

    }

    @FXML
    // WRITER
    public void onSaveModelMenuItemClick() {

        if (selectedModel == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Нет модели");
            alert.setContentText("Сначала загрузите модель!");
            alert.showAndWait();
            return;
        }

        FileChooser saveChooser = new FileChooser();
        saveChooser.setTitle("Save Model");

        File file = saveChooser.showSaveDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        String fileName = file.getAbsolutePath();
        try {
            new objWriter().write(selectedModel, fileName);
            //успешно
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Победа!");
            alert.setContentText("Модель успешно сохранена!");
            alert.showAndWait();


            // todo: обработка ошибок
        } catch (IOException | IllegalArgumentException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка!");
            alert.setContentText("Ошибка сохранения: " + e.getMessage());
            alert.showAndWait();

        }

    }

    //для выбора моделей
    @FXML
    public void onModelSelected() {
        int index = modelSelector.getSelectionModel().getSelectedIndex(); //что выбрал пользователь
        if (index >= 0) {
            selectedModel = models.get(index);
        }
        updateStatusBar();

    }

    //статус бар
    private void updateStatusBar() {
        if (selectedModel != null) {
            int amountVertices = selectedModel.getVertices().size();
            int amountPolygons = selectedModel.getPolygons().size();
            statusLabel.setText(String.format("Вершин: %d | Полигонов: %d", amountVertices, amountPolygons));
        } else {
            statusLabel.setText("Модель не загружена");
        }
    }

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, -TRANSLATION, 0));
    }
}