module com.example.experimentdashboard {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fazecast.jSerialComm;
    requires aws.iot.device.sdk.java;
    requires org.eclipse.paho.client.mqttv3;
    requires jackson.databind;
    requires jackson.core;
    requires org.apache.commons.codec;
    requires org.apache.commons.io;
    requires java.desktop;


    opens com.example.experimentdashboard to javafx.fxml;
    exports com.example.experimentdashboard;
}