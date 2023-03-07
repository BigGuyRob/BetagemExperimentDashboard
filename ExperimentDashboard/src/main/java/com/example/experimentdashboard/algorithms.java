package com.example.experimentdashboard;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

public class algorithms {
    static double mean = 0;
    static double temp_sum = 0;
    static double summation = 0;
    static int count = 0;
    static double stdDev = 0;
    public static ObservableList<Double> stdDev(XYChart.Series s){
        ObservableList<Double> temp = FXCollections.observableArrayList();

        ObservableList<XYChart.Data> data = s.getData();
        data.forEach((dataPoint) -> {
            temp_sum += (double)dataPoint.getYValue();
            count += 1;
        });
        mean = temp_sum/count; //now we have the mean
        data.forEach((dataPoint) -> {
            summation = ((mean - (double)dataPoint.getYValue())*(mean - (double)dataPoint.getYValue())) / (count - 1);
        });
        stdDev = Math.sqrt(summation);
        temp.add(stdDev);
        return temp;
    }

    public static ObservableList<Double> mean(XYChart.Series s){
        ObservableList<Double> temp = FXCollections.observableArrayList();

        ObservableList<XYChart.Data> data = s.getData();
        data.forEach((dataPoint) -> {
            temp_sum += (double)dataPoint.getYValue();
            count += 1;
        });
        mean = temp_sum/count; //now we have the mean
        temp.add(mean);
        return temp;
    }


}
