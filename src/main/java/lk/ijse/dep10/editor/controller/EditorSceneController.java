package lk.ijse.dep10.editor.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.PageLayout;
import javafx.print.Printer;
import javafx.print.PrinterAttributes;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.naming.directory.SearchResult;
import javax.print.attribute.standard.PrinterName;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditorSceneController {

    public HTMLEditor txtEditor;
    public MenuItem mnNew;
    public MenuItem mnOpen;
    public MenuItem mnSave;
    public MenuItem mnPrint;
    public MenuItem mnClose;
    public MenuItem mnOnlySave;
    public TextField txtFind;
    public Button btnDown;
    public Button btnUp;
    public TextField txtReplace;
    public Button btnReplace;
    public Button btnReplaceAll;
    public CheckBox chkMatchCase;
    public Label lblResults;

    boolean isSaved = true;
    File fileLocation;
    Stage stage;

    private ArrayList<SearchResult> searchResultList = new ArrayList<>();
    private int pos = 0;

    public void initialize() {

    }

    @FXML
    void mnAboutOnAction(ActionEvent event) throws IOException {
        Stage window = new Stage();

        URL fxmlFile = this.getClass().getResource("/view/AboutScene.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlFile);
        AnchorPane root = fxmlLoader.load();

        Scene scene = new Scene(root);
        window.setScene(scene);

        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("About TD Text Editor");
        window.show();
        window.centerOnScreen();
    }

    @FXML
    void mnCloseOnAction(ActionEvent event) {
        if(!isSaved) mnSave.fire();
        else Platform.exit();
    }

    @FXML
    void mnNewOnAction(ActionEvent event) {
        stage = (Stage) txtEditor.getScene().getWindow();
        if (!isSaved) {
            Alert cnfmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to save unsaved " + stage.getTitle().substring(1) + " file before open new file?", ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> optButton = cnfmAlert.showAndWait();

            if (optButton.get() == ButtonType.NO) {
                txtEditor.setHtmlText("");
                stage.setTitle("Untitled Document");
                isSaved = true;
                return;
            } else if (optButton.get() == ButtonType.YES) {
                mnSave.fire();
                txtEditor.setHtmlText("");
                stage.setTitle("Untitled Document");
                isSaved = true;
            }
        } else {
            txtEditor.setHtmlText("");
            stage.setTitle("Untitled Document");
        }
    }

    @FXML
    void mnOpenOnAction(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open a text file");
        File file = fileChooser.showOpenDialog(txtEditor.getScene().getWindow());
        if(file == null) return;

        FileInputStream fis = new FileInputStream(file);
        byte[] bytes = fis.readAllBytes();
        fis.close();

        txtEditor.setHtmlText(new String(bytes));

        stage = (Stage) txtEditor.getScene().getWindow();
        stage.setTitle(file.getName());

        fileLocation = file;

        isSaved = true;
    }

    @FXML
    void mnPrintOnAction(ActionEvent event) {
        PrinterJob printerJob = PrinterJob.createPrinterJob();

        if (printerJob != null) {
            PageLayout pageLayout = printerJob.getPrinter().getDefaultPageLayout();
            printerJob.getJobSettings().setPageLayout(pageLayout);

            printerJob.printPage(txtEditor);

            printerJob.endJob();
        }
    }

    @FXML
    void mnSaveOnAction(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save a text file");
        File file = fileChooser.showSaveDialog(txtEditor.getScene().getWindow());
        if (file == null) return;

        FileOutputStream fos = new FileOutputStream(file, false);
        String text = txtEditor.getHtmlText();
        byte[] bytes = text.getBytes();
        fos.write(bytes);
        fos.close();
        isSaved = true;
        fileLocation = file;
    }

    public void rootOnDragOver(DragEvent dragEvent) {
        dragEvent.acceptTransferModes(TransferMode.ANY);
    }

    public void rootOnDragDropped(DragEvent dragEvent) throws IOException {
        dragEvent.setDropCompleted(true);
        File droppedFile = dragEvent.getDragboard().getFiles().get(0);

        FileInputStream fis = new FileInputStream(droppedFile);
        byte[] bytes = fis.readAllBytes();
        fis.close();

        txtEditor.setHtmlText(new String(bytes));
    }

    public void txtEditorOnKeyPressed(KeyEvent keyEvent) {
        isSaved = false;
    }

    public void txtEditorOnInputMethodTextChanged(InputMethodEvent inputMethodEvent) {
    }

    public void txtEditorOnKeyReleased(KeyEvent keyEvent) {
        isSaved = false;
        stage = (Stage) txtEditor.getScene().getWindow();
        if (txtEditor.getHtmlText().length() > 72 && stage.getTitle().charAt(0) != '*') {
            stage.setTitle("*" + stage.getTitle());
        }

        stage.setOnCloseRequest(windowEvent -> {
            if(!isSaved) {
                Alert cnfmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to save unsaved " + stage.getTitle().substring(1) + "file before open new file?", ButtonType.YES, ButtonType.NO);
                Optional<ButtonType> optButton = cnfmAlert.showAndWait();

                if (optButton.get() == ButtonType.NO) {
                    Platform.exit();
                } else if (optButton.get() == ButtonType.YES) {
                    mnSave.fire();
                    txtEditor.setHtmlText("");
                    stage.setTitle("Untitled Document");
                    isSaved = true;
                }
            }
        });
    }

    public void txtEditorOnKeyTyped(KeyEvent keyEvent) {
        isSaved = false;
    }

    public void mnOnlySaveOnAction(ActionEvent actionEvent) throws IOException {
        if (fileLocation == null) {
            mnSave.fire();
        } else {
            FileOutputStream fos = new FileOutputStream(fileLocation, false);
            String text = txtEditor.getHtmlText();
            byte[] bytes = text.getBytes();
            fos.write(bytes);
            fos.close();
            isSaved = true;
        }
    }

    public static void printAsPDF(String content) {
        // Create a PrinterJob
        PrinterJob printerJob = PrinterJob.createPrinterJob();

        if (printerJob != null) {
            // Set the printer to "Microsoft Print to PDF"
            Printer defaultPrinter = Printer.getDefaultPrinter();
            PrinterAttributes printerAttributes = defaultPrinter.getPrinterAttributes();
            PrinterName pdfPrinterName = new PrinterName("Microsoft Print to PDF", null);

            // Set the print options
            PageLayout pageLayout = printerJob.getJobSettings().getPageLayout();
            PrinterJob pdfPrinterJob = PrinterJob.createPrinterJob();
            pdfPrinterJob.getJobSettings().setPageLayout(pageLayout);

            // Set the content to be printed
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();
            webEngine.loadContent(content);
            pdfPrinterJob.printPage(webView);

            // End the printer job
            pdfPrinterJob.endJob();
        }
    }
}
