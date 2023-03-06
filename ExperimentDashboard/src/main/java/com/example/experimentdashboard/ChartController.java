package com.example.experimentdashboard;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;

import javax.swing.text.Document;
import java.io.FileOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

public class ChartController {
    private DashboardController ds;
    private String data;
    private String experiment_title;

    private ObservableList<XYChart.Series> allSeries;
    @FXML
    private LineChart chartMain;
    @FXML
    private MenuButton menuTrendLines;
    @FXML
    private TextArea txtInputData;
    @FXML
    private Button btnGeneratePDF;

    public void setDashboardController(DashboardController ds){
        this.ds = ds;
    }

    public void setChart(ObservableList<XYChart.Series> series){
        addAllSeries(series);
        chartMain.setLegendSide(Side.RIGHT);
        chartMain.getXAxis().setLabel("Positions");
        chartMain.getYAxis().setLabel("Intensity");
        chartMain.setTitle("Experiment");
        chartMain.setCreateSymbols(false);
    }

    private int index = -1;
    private int cnt = 0;
    private int chartIndex = -1;
    private int cntChart = 0;
    private EventHandler<ActionEvent> event1 = new EventHandler<ActionEvent>() {
        public void handle(ActionEvent e) {
            index = -1;
            cnt = 0;
            chartIndex = -1;
            cntChart = 0;
            //get the source event name
            String trend = ((MenuItem) e.getSource()).getText();

            ObservableList<XYChart.Series> series = allSeries;
            series.forEach((node) -> {
                if (trend == node.getName()) {
                    index = cnt;
                }
                cnt+=1;
            });
            if (index != -1) {
                //we have found the node we can repeat the process to see if it is on the chart
                ObservableList<XYChart.Series> chartseries = chartMain.getData();
                chartseries.forEach((node) -> {
                    if (trend == node.getName()) {
                        chartIndex = cntChart;
                    }
                    cntChart += 1;
                });
                if (chartIndex == -1) {
                    //here we have found the trend in the list but it is not on the chart, therefore I can just add it.
                    chartMain.getData().add(allSeries.get(index));
                } else {
                    //here we have found the trend in the list but it is on the chart, therefore it can be removed from the chart.
                    chartMain.getData().remove(allSeries.get(index));
                }
            } else {
                //here the node was not found in the list, this should not happen
                //since the allSeries is never modified after the original stage is set
            }
        }
    };
    private void addAllSeries(ObservableList<XYChart.Series> series){

        allSeries = series;
        series.forEach((node) -> {
            chartMain.getData().add(node);
            MenuItem m = new MenuItem(node.getName());
            menuTrendLines.getItems().add(m);
            m.setOnAction(event1);
        });

    }

    private void removeAllSeries(){
        chartMain.getData().removeAll();
    }

    public void setInputData(String data){
        txtInputData.setText(data);
        this.data = data;
    }

    @FXML
    private void setUpPDF(ActionEvent e){
        Robot robot = new Robot();
        Stage thisStage = (Stage) btnGeneratePDF.getScene().getWindow();
        Scene currentScene = thisStage.sceneProperty().get();
        int width = (int) currentScene.widthProperty().get();
        int height = (int) currentScene.heightProperty().get();
        WritableImage img = currentScene.snapshot(new WritableImage(width, height));
        experiment_pdf pdf = new experiment_pdf(img);
        String errorText = pdf.export();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("PDF");
        alert.setContentText(errorText);
        alert.showAndWait();

    }




}
