package com.example.experimentdashboard;

import javafx.beans.binding.ObjectExpression;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
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
    private MenuButton menuAddTrend;
    @FXML
    private MenuButton menuOnSeries;
    @FXML
    private TextArea txtInputData;
    @FXML
    private Button btnGeneratePDF;
    @FXML
    private Button btnAddTrend;

    public void setDashboardController(DashboardController ds){
        this.ds = ds;
    }

    public void setChart(ObservableList<XYChart.Series> series){
        addAllSeries(series);
        chartMain.setLegendSide(Side.RIGHT);
        chartMain.getXAxis().setLabel("Positions");
        chartMain.getYAxis().setLabel("Intensity");
        chartMain.setTitle("Experiment");
        chartMain.setCreateSymbols(true);
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
                //System.out.println(trend + " == " + node.getName());
                if (trend.equals(node.getName())) {
                    index = cnt;
                }
                cnt+=1;
            });
            if (index != -1) {
                //we have found the node we can repeat the process to see if it is on the chart
                //System.out.println("Chart Found");
                ObservableList<XYChart.Series> chartseries = chartMain.getData();
                chartseries.forEach((node) -> {
                    if (trend.equals(node.getName())) {
                        chartIndex = cntChart;
                    }
                    cntChart += 1;
                });
                if (chartIndex == -1) {
                    //System.out.println("Chart added");
                    //here we have found the trend in the list but it is not on the chart, therefore I can just add it.
                    chartMain.getData().add(allSeries.get(index));
                } else {
                    //System.out.println("Chart removed");
                    //here we have found the trend in the list but it is on the chart, therefore it can be removed from the chart.
                    chartMain.getData().remove(allSeries.get(index));
                }
            } else {
                //System.out.println("Error chart not found in allSeries");
                //here the node was not found in the list, this should not happen
                //since the allSeries is never modified after the original stage is set
            }
        }
    };

    private EventHandler<ActionEvent> remove = new EventHandler<ActionEvent>() {
        public void handle(ActionEvent e) {
            removeAllSeries();
        }
    };

    private String addTrend = "";
    private XYChart.Series selectedSeriesForNewTrend = null;
    private EventHandler<ActionEvent> setAlgorithmForNewTrend = new EventHandler<ActionEvent>() {
        public void handle(ActionEvent e) {
            String trend = ((MenuItem) e.getSource()).getText();
            addTrend = trend;
            menuAddTrend.setText(addTrend);
        }
    };

    private EventHandler<ActionEvent> setSeriesforNewTrend = new EventHandler<ActionEvent>() {
        public void handle(ActionEvent e) {
            index = -1;
            cnt = 0;
            //get the source event name
            String trend = ((MenuItem) e.getSource()).getText();

            ObservableList<XYChart.Series> series = allSeries;
            series.forEach((node) -> {
                if (trend == node.getName()) {
                    index = cnt;
                }
                cnt+=1;
            });
            //here index = the selected series
            selectedSeriesForNewTrend = (XYChart.Series) allSeries.get(index);
            menuOnSeries.setText(trend);
        }
    };

    private void addAllSeries(ObservableList<XYChart.Series> series){
        allSeries = series;
        series.forEach((node) -> {
            chartMain.getData().add(node);
            MenuItem m = new MenuItem(node.getName());
            MenuItem a = new MenuItem(node.getName());
            menuTrendLines.getItems().add(m);
            menuOnSeries.getItems().add(a);
            m.setOnAction(event1);
            a.setOnAction(setSeriesforNewTrend);
        });
        MenuItem removeAllSeries = new MenuItem("Remove All");
        menuTrendLines.getItems().add(removeAllSeries);
        removeAllSeries.setOnAction(remove);
        addAllAlgorithms();
    }
    @FXML
    private void addTrend(ActionEvent e){
        if(addTrend != "" && selectedSeriesForNewTrend != null){
            MenuItem newTrend = new MenuItem(addTrend + " for " + selectedSeriesForNewTrend);
            menuTrendLines.getItems().add(newTrend);
            newTrend.setOnAction(event1);
            //here is where I need to get the series and add it to the chart and to all series
            XYChart.Series retSeries = createSeriesFromTrend(addTrend);
            chartMain.getData().add(retSeries);
            allSeries.add(retSeries);
            addTrend = "";
            selectedSeriesForNewTrend = null;
            menuOnSeries.setText("ON");
            menuAddTrend.setText("ADD");
        }
    }

    private void addAllAlgorithms(){
        MenuItem stdDev = new MenuItem("Standard Deviation");
        menuAddTrend.getItems().add(stdDev);
        stdDev.setOnAction(setAlgorithmForNewTrend);
        MenuItem mean = new MenuItem("Mean");
        menuAddTrend.getItems().add(mean);
        mean.setOnAction(setAlgorithmForNewTrend);
    }
    private XYChart.Series createSeriesFromTrend(String addTrend){
        ObservableList<Double> returnSet = FXCollections.observableArrayList();
        XYChart.Series retSeries = new XYChart.Series();
        retSeries.setName(addTrend + " for " + selectedSeriesForNewTrend);
        int cnt = 0;
        switch (addTrend) {
            case ("Standard Deviation"):
                returnSet = algorithms.stdDev(selectedSeriesForNewTrend);
                for (Double val : returnSet) { //plot average intensity on
                    XYChart.Data data = new XYChart.Data(195, val);
                    data.setNode(createDataNode(data.YValueProperty()));
                    retSeries.getData().add(data);
                    cnt += 1;
                }
                break;
            case ("Mean"):
                returnSet = algorithms.mean(selectedSeriesForNewTrend);
                for(Double val : returnSet){
                    for(int i = 0 ; i < 180 ; i += 10){
                        XYChart.Data data = new XYChart.Data(i, val);
                        data.setNode(createDataNode(data.YValueProperty()));
                        retSeries.getData().add(data);
                    }
                }
                break;
            default:
                return null;
        }
        return retSeries;
    }
    private void removeAllSeries(){
        chartMain.getData().removeAll(chartMain.getData());
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


    private static Node createDataNode(ObjectExpression<Number> value) {
        var label = new Label();
        label.textProperty().bind(value.asString("%,.2f"));
        var pane = new Pane(label);
        pane.setShape(new Rectangle(6,6));
        pane.setScaleShape(false);
        label.translateYProperty().bind(label.heightProperty().divide(-1.5));

        return pane;
    }


}
