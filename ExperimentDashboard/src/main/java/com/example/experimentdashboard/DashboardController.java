package com.example.experimentdashboard;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.text.Text;

import javax.swing.*;

public class DashboardController {
	@FXML
	private ComboBox<String> comboSelComm;
	@FXML
	private ComboBox<String> comboCerts;
	File certLocations = new File("C:/Data/certList.txt");
	private ObservableList<String> certs = FXCollections.observableArrayList("Private Key","Root Certificate","Device List");
	private ObservableList<String> Requiredcerts = FXCollections.observableArrayList("Private Key","Root Certificate","Device List");
	boolean certFill = false;
	boolean flag = true; //for if we had to create a new file
	private String privateKeyPath = "Private Key";
	private String rootCertPath = "Root Certificate";
	private String device_listPath = "Device List";
	@FXML
	private ComboBox<String> comboSensor;
	private ObservableList<String> SensorsList = FXCollections.observableArrayList("BPW34 Silicon PIN Photodiode");
	@FXML
	private ComboBox<String> comboProbe;
	private ObservableList<String> ProbesList = FXCollections.observableArrayList("White LED", "Green LED", "Red LED", "Yellow LED");
	
	@FXML
	private Button btnconAWS;
	@FXML 
	private Button btnScan;
	@FXML 
	private Button btnBegin;
	@FXML 
	private Button btnSendParameters;
	@FXML
	private Button btnGetParameters;
	@FXML
	private Button btnChooseFile;
	@FXML
	private TextArea notes;
	@FXML
	private TextArea outputArea;
	private String outputAreaTest;
	
	@FXML
	private TextField txtServoStartPos;
	private int DEFAULT_START_POS = 0; //degree
	@FXML
	private Text tx_current_start;
	@FXML
	private TextField txtServoEndPos;
	private int DEFAULT_END_POS = 180; //degree
	@FXML
	private Text tx_current_end;
	@FXML
	private TextField txtDelta;
	private int DEFAULT_DELTA = 10; //degree
	@FXML
	private Text tx_current_delta;
	@FXML
	private TextField txtNumDataPoints;
	private int DEFAULT_NUMDATAPOINTS = 10; //dec
	@FXML
	private Text tx_current_dataPoints;
	@FXML
	private TextField txtMovementTime;
	private int DEFAULT_MOVEMENTTIME = 10; //ms
	@FXML
	private Text tx_current_movementTime;
	@FXML
	private TextField txtExposureTime;
	private int DEFAULT_EXPOSURETIME = 10; //ms
	@FXML
	private Text tx_current_exposureTime;
	private Text[] current_values = {tx_current_start, tx_current_end, tx_current_delta,tx_current_dataPoints, tx_current_movementTime, tx_current_exposureTime};
	
	private static final int NUM_PARAMETERS = 6;
	private double PARAMS[] = {0,180,10,10,10,10};
	private String Plabels[] = {"Servo Start Position",
							 "Servo End Position",
							 "Delta",
							 "Number of Data Points to Collect",
							 "Movement Time",
							 "Exposure Time"};
	
	
	//IMPORTANT INFO
	private static final short ARDUINO_VENDOR_ID = 0x2341;
	private static final short MKR_PRODUCT_ID = (short) 0x8054;
	
	private AWSIotMqttClient client = null;
	private String subscribedTopic = "";
	private File append = null;
	private SerialPort myCommPort = null;
	private ArrayList<Integer> availablePorts = new ArrayList<Integer>();
	private ObservableList<String> options = FXCollections.observableArrayList();
    private AWSIotQos TestTopicQos = AWSIotQos.QOS0;
    private String experimentTopic = "MKR_WIFI_experiment";
	private String clientEndpoint = "a21hi64alhm13r-ats.iot.us-east-1.amazonaws.com";   // use value returned by describe-endpoint --endpoint-type "iot:Data-ATS"

	private String purpose = "def";
	@FXML
	private void connectToAWS(ActionEvent event) {
		flag = false;
		if(purpose == "AWS") {
			purpose = "def"; //reset the purpose
			if(client != null) {
				try {
					client.disconnect();
					btnconAWS.setText("Connect to AWS");
					btnScan.setDisable(false);
					comboSelComm.setDisable(true);
					options.clear();
					comboSelComm.setItems(options);
					disableInput(true);//re-disable input since back to square one
				} catch (AWSIotException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else {
			String errorText = "";
			try {
					List<String> paths = null;
					try {
						paths = Files.readAllLines(Paths.get(certLocations.getPath()));
					} catch (IOException e){ flag = true;}
					privateKeyPath = paths.get(0);
					rootCertPath = paths.get(1);
					device_listPath = paths.get(2);
					certs.clear();
					certs.addAll(paths);
					comboCerts.setItems(certs);
			} catch (IndexOutOfBoundsException e){
				//if we couldn't read data that doesnt mean we didnt just add the data

			}
			//flag indicates the file could not be read or was not created
			if(flag){
				//in that case we need to ping user to upload required certs
				comboCerts.setItems(Requiredcerts);
				errorText += "File could not be read/accessed please upload certificates\n";
			}
			String certificateFile = rootCertPath;
			String privateKeyFile = privateKeyPath;
			String device_list = device_listPath;
			certs.clear();
			certs.add(privateKeyPath);
			certs.add(rootCertPath);
			certs.add(device_listPath);
			comboCerts.setItems(certs);
			String clientId = "betagem_dashboard";
			File tmp_cert = new File(rootCertPath);
			File tmp_privatekey = new File(privateKeyPath);
			File tmp_device_list = new File(device_list);
			if(tmp_cert.exists() == false){
				errorText += "\nCould not find file root certificate:\n" + certificateFile;
			}
			if(tmp_privatekey.exists() == false){
				errorText += "\nCould not find file private key:\n" + privateKeyFile;
			}
			if(tmp_device_list.exists() == false){
				errorText += "\nCould not find device list:\n" + device_list;
			}
		    //so that the buttons know what to do 
			//now that everything is loaded
			if(errorText == "") {
				SampleUtils.KeyStorePasswordPair pair = SampleUtils.getKeyStorePasswordPair(tmp_cert.getAbsolutePath(), tmp_privatekey.getAbsolutePath());
				try {
					client = new AWSIotMqttClient(clientEndpoint, clientId, pair.keyStore, pair.keyPassword);
				}catch(NullPointerException e){
					errorText+= "Could not create AWS client due to file paths";
				}
				try {
					client.setPort(8883);
					client.connect();
					//System.out.println(client.getConnectionStatus());
					options.clear();
					try {
						List<String> allLines = Files.readAllLines(Paths.get(device_list));
						options.addAll(allLines);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						//e1.printStackTrace();
						options.add("no devices");
					}
					comboSelComm.setItems(options);
					comboSelComm.setDisable(false);//enable the box
					disableInput(false); //re-enable input for machine but they will now be dual purpose
					btnScan.setDisable(true); //disable comm port scanner
					//now I do something called set purpose
					purpose = "AWS";
					btnconAWS.setText("Disconnect AWS");
					String p = "MKR_WIFI_experiment";
					comboSensor.setItems(SensorsList);
					comboProbe.setItems(ProbesList);
					AWSIotTopic topic = new TestTopicListener(p, TestTopicQos, this);
					client.subscribe(topic, true);
					outputArea.setText("Connection Successful to:\n" + clientEndpoint);
					writeSavedData(); //once a successful connection is made write the saved data

					fillDefaultValueLabels();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					outputArea.setText(errorText);
				}
			}else{
				outputArea.setText(errorText + "\n" + "Upload certificates and device list to retry");
			}
		}
	}
	@FXML 
	private void ScanPorts(ActionEvent event) {
		if(purpose != "SER") {
			//Part of reset
			myCommPort = null;
			availablePorts.clear();
			options.clear();
			//End reset
			disableInput(false); //re enable input
			comboSensor.setItems(SensorsList);
			comboProbe.setItems(ProbesList);
			comboSelComm.setItems(options);
			SerialPort comPorts[] = SerialPort.getCommPorts();
			int c = 0;
			for(SerialPort port : comPorts) {
				options.add(port.getDescriptivePortName());
				availablePorts.add(c);
				c+=1;
				comboSelComm.setDisable(false);
			}
			comboSelComm.setItems(options);
			purpose = "SER";//set the purpose for the rest of the application
			btnScan.setText("Stop Serial");
			fillDefaultValueLabels();
			btnconAWS.setDisable(true);
		}else {
			myCommPort = null;
			availablePorts.clear();
			options.clear();
			purpose = "def";
			btnScan.setText("Scan Ports");
			btnconAWS.setDisable(false);
			comboSelComm.setDisable(true);
			disableInput(true);//true because back to square one
		}
	}
	private int result = -1; //This is the number of bytes read
	@FXML
	private void beginExperiment(ActionEvent event) {
		outputArea.setText("");
		if(purpose == "SER") {
			beginSerial();
		}else if(purpose == "AWS") {
			if(client != null) {
				try {
					outputAreaTest = "";
					if(comboSensor.getSelectionModel().getSelectedIndex() < 0) comboSensor.getSelectionModel().select(0); //default
					if(comboProbe.getSelectionModel().getSelectedIndex() < 0) comboProbe.getSelectionModel().select(0); //default
					outputAreaTest += ("----NOTES----\n");
					String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
					outputAreaTest+= timeStamp + "\n";
					outputAreaTest += "Sensor Selected: " + comboSensor.getSelectionModel().getSelectedItem() +"\n";
					outputAreaTest += "Probe Selected: " + comboProbe.getSelectionModel().getSelectedItem() + "\n";
					outputAreaTest += (notes.getText() + "\n");
					notes.setText("");
					outputAreaTest += ("----END NOTES----\n");
					outputAreaTest += ("----PARAMETERS----\n");
					for(int i = 0; i < NUM_PARAMETERS; i ++) {
						outputAreaTest += Plabels[i] + " : " + PARAMS[i] +"\n";
					}
					outputAreaTest += "----END PARAMETERS----\n";
					outputArea.setText(outputAreaTest);
					client.publish(subscribedTopic, "begin");
				} catch (AWSIotException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	private String val = "";
	private int paramCounter = 0;
	@FXML
	private void getCurrentParameters(ActionEvent event) {
		if(purpose == "SER") {
			getCurrentParamsSerial();
		}else if(purpose == "AWS") {
			if(client !=null) {
				try {
					client.publish(subscribedTopic, "get");
				} catch (AWSIotException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	@FXML
	private void sendNewParameters(ActionEvent event) {
		outputArea.setText("");
		if(purpose == "SER") {
			sendParamsSerial();
		}else if(purpose == "AWS") {
			String[] tempParams = {txtServoStartPos.getText(), txtServoEndPos.getText(), txtDelta.getText(),txtNumDataPoints.getText(), txtMovementTime.getText(), txtExposureTime.getText()};
			String errText = validateInput();
			if( errText == "") { //if we get no error text we can proceed
				String update = "updt/";
				for(int x = 0;x < NUM_PARAMETERS; x++) {
					update += tempParams[x] +"/";
				}
				if(client != null) {
					try {
						client.publish(subscribedTopic, update);
						client.publish(subscribedTopic, "get");//so that we also run get when we send
					} catch (AWSIotException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}else {
				outputArea.setText(errText);
			}
		}
	}
	public void updateOutputArea(String payload) {
		if(payload.contains("retp")) {
			Platform.runLater(() -> fillParams(payload));
		}else {
			Platform.runLater(() -> {outputArea.appendText(payload); if(payload.contains("!")){appendToFile();}});
		}
	}
	@FXML
	private void selectDevice(ActionEvent event) {
		if(purpose == "SER") {
			int thePortIndex = availablePorts.get(comboSelComm.getSelectionModel().getSelectedIndex());
			myCommPort = SerialPort.getCommPorts()[thePortIndex];
			btnSendParameters.setDisable(false);
			btnBegin.setDisable(false);
			btnGetParameters.setDisable(false);
		}else if(purpose == "AWS"){
			subscribedTopic = comboSelComm.getSelectionModel().getSelectedItem();
			btnSendParameters.setDisable(false);
			btnBegin.setDisable(false);
			btnGetParameters.setDisable(false);
		}
	}
	private String validateInput() {
		String errText = "";
		double temp_End = -1;
		double temp_Start = -1;
		double temp_Delta = -1;
		double temp_DataPoints = -1;
		double temp_movementTime = -1;
		double temp_exposureTime = -1;
		
		try {
			temp_Start = Double.parseDouble(txtServoStartPos.getText());
			if(temp_Start < 0) {
				errText += "Start Position must be greater than 0\n";
			}
		}catch(NumberFormatException e) {
			errText += "Start Position is Invalid\n";
		}
		
		try {
			temp_End = Double.parseDouble(txtServoEndPos.getText());
			if(temp_End > 180) {
				errText += "End Position must not be greater than 180\n";
			}else if(temp_End <= temp_Start) {
				errText += "End Position must be greater than Start Position\n";
			}
		}catch(NumberFormatException e) {
			errText += "End Position is Invalid\n";
		}
		
		try {
			temp_Delta = Double.parseDouble(txtDelta.getText());
			if(temp_Delta > (temp_End - temp_Start)) {
				errText += "Delta must be less than End - Start Positions\n";
			}else if(temp_Delta < 0 ) {
				errText += "Delta must be positive\n";
			}
		}catch(NumberFormatException e) {
			errText += "Delta is Invalid\n";
		}
		
		try {
			temp_DataPoints = Double.parseDouble(txtNumDataPoints.getText());
			if(temp_DataPoints <= 0) {
				errText += "Number of data points to collect must be greater than 0\n";
			}
		}catch(NumberFormatException e) {
			errText += "Number of Data Points to collect is Invalid\n";
		}
		
		try {
			temp_movementTime = Double.parseDouble(txtMovementTime.getText());
			if(temp_movementTime <= 0) {
				errText += "Movement Time must be greater than 0\n";
			}
		}catch(NumberFormatException e) {
			errText += "Movement Time is Invalid\n";
		}
		
		try {
			temp_exposureTime = Double.parseDouble(txtExposureTime.getText());
			if(temp_exposureTime <= 0) {
				errText += "Exposure Time must be greater than 0\n";
			}
		}catch(NumberFormatException e) {
			errText += "Exposure Time is Invalid\n";
		}
		
		return errText;
	}
	private void fillParams(String cmd) {
		String[] cmdSplit = cmd.split("/");
		int paramCounter = 0;
		for(String STR : cmdSplit) {
			if(STR != "retp") {
				try {
				PARAMS[paramCounter] = Double.parseDouble(STR);
				paramCounter++;
				}catch(NumberFormatException e) {
					//e.printStackTrace();
					//skip 
				}
			}
		}
		fillCurrentValueLabels();
	}
	private void fillCurrentValueLabels() {
		tx_current_start.setText(String.valueOf(PARAMS[0]));
		tx_current_end.setText(String.valueOf(PARAMS[1]));
		tx_current_delta.setText(String.valueOf(PARAMS[2]));
		tx_current_dataPoints.setText(String.valueOf(PARAMS[3]));
		tx_current_movementTime.setText(String.valueOf(PARAMS[4]));
		tx_current_exposureTime.setText(String.valueOf(PARAMS[5]));
	}
	private void fillDefaultValueLabels(){
		txtServoStartPos.setText(String.valueOf(DEFAULT_START_POS));
		txtServoEndPos.setText(String.valueOf(DEFAULT_END_POS));
		txtDelta.setText(String.valueOf(DEFAULT_DELTA));
		txtNumDataPoints.setText(String.valueOf(DEFAULT_NUMDATAPOINTS));
		txtMovementTime.setText(String.valueOf(DEFAULT_MOVEMENTTIME));
		txtExposureTime.setText(String.valueOf(DEFAULT_EXPOSURETIME));
	}
	private void appendToFile() {
		if(append != null) {
		FileWriter fr;
			try {
				fr = new FileWriter(append, true);
				fr.write(outputArea.getText());
				fr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	@FXML
	private void chooseFile(ActionEvent event) {
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            //This is where a real application would open the file.
            btnChooseFile.setText("output to " + file.getName());
            append = file;
        } else {
            //we dont do anything on any other option
        	append = null;
        	btnChooseFile.setText("Choose Output File");
        }
	}
	private void disableInput(boolean state) {
		btnBegin.setDisable(state);
		btnSendParameters.setDisable(state);
		btnGetParameters.setDisable(state);
		comboSensor.setDisable(state);
		comboProbe.setDisable(state);
	}
	public void finalDisconnect() {
		try {
			client.disconnect();
		} catch (AWSIotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void beginSerial() {
		if(myCommPort != null) {
			outputArea.setText("");
			disableInput(true);
			outputAreaTest = "";
			if(comboSensor.getSelectionModel().getSelectedIndex() < 0) comboSensor.getSelectionModel().select(0); //default
			if(comboProbe.getSelectionModel().getSelectedIndex() < 0) comboProbe.getSelectionModel().select(0); //default
			outputAreaTest += ("----NOTES----\n");
			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
			outputAreaTest+= timeStamp + "\n";
			outputAreaTest += "Sensor Selected: " + comboSensor.getSelectionModel().getSelectedItem() +"\n";
			outputAreaTest += "Probe Selected: " + comboProbe.getSelectionModel().getSelectedItem() + "\n";
			outputAreaTest += (notes.getText() + "\n");
			notes.setText("");
			outputAreaTest += ("----END NOTES----\n");
			outputAreaTest += ("----PARAMETERS----\n");
			for(int i = 0; i < NUM_PARAMETERS; i ++) {
				outputAreaTest += Plabels[i] + " : " + PARAMS[i] +"\n";
			}
			outputAreaTest += "----END PARAMETERS----\n";
			outputArea.setText(outputAreaTest);
			myCommPort.openPort();
			myCommPort.setBaudRate(9600);
			String start = "strt\n";
			myCommPort.writeBytes(start.getBytes(), start.length() + 1);
			myCommPort.flushIOBuffers();
			myCommPort.addDataListener(new SerialPortDataListener() {
				@Override
				public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_RECEIVED; }
				@Override
				public void serialEvent(SerialPortEvent event) {
					//byte[] newData = new byte[myCommPort.bytesAvailable()];
					byte[] newData = event.getReceivedData();
				    
					for(int i = 0; i < newData.length; i++) {
						if(myCommPort.isOpen()) {
							if((char)newData[i] == '!') {
								outputAreaTest += "---END EXPERIMENT---\n";
								outputAreaTest += "\n";
								Platform.runLater(() -> outputArea.setText(outputAreaTest));
								Platform.runLater(() -> appendToFile());
								disableInput(false);
								myCommPort.removeDataListener();
								myCommPort.closePort();
							}else {
								try {
									outputAreaTest += (char)newData[i];
								} catch(IndexOutOfBoundsException e) {
									e.printStackTrace();
								}
							}
						}
					} //END FOR
				}
			});
		}else {
			outputArea.setText("No Device Selected");
		}
	}
	private void getCurrentParamsSerial() {
		if(myCommPort !=null) {
			myCommPort.openPort();
			myCommPort.setBaudRate(9600);
			myCommPort.flushIOBuffers();
			String get = "getp\n";
			myCommPort.writeBytes(get.getBytes(), get.length() + 1);
			myCommPort.flushIOBuffers();
			paramCounter = 0;
			val = "";
			myCommPort.addDataListener(new SerialPortDataListener() {
				@Override
				public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_RECEIVED; }
				@Override
				public void serialEvent(SerialPortEvent event) {
					//byte[] newData = new byte[myCommPort.bytesAvailable()];
					byte[] newData = event.getReceivedData();
				    
					for(int i = 0; i < newData.length; i++) {
						//System.out.print((char)newData[i]);
						char c = (char)newData[i];
						if(c == '!') {
							Platform.runLater(() -> fillParams(val));
							myCommPort.removeDataListener();
							myCommPort.closePort();
							val = "";
						}else {
							val += c;
						}
					} //END FOR
				}
			});
		}else {
			outputArea.setText("No Device Selected");
		}
	}
	private void sendParamsSerial() {
		String[] tempParams = {txtServoStartPos.getText(), txtServoEndPos.getText(), txtDelta.getText(),txtNumDataPoints.getText(), txtMovementTime.getText(), txtExposureTime.getText()};
		String errText = validateInput();
		if( errText == "") { //if we get no error text we can proceed
		if(myCommPort !=null) {
			myCommPort.openPort();
			myCommPort.setBaudRate(9600);
			myCommPort.flushIOBuffers();
			String update = "updt/";
			paramCounter = 0;
			val = "";
			for(int x = 0;x < NUM_PARAMETERS; x++) {
				update += tempParams[x] +"/";
			}
			update+= "\n";
			System.out.println(update);
			myCommPort.writeBytes(update.getBytes(), update.length() + 1);
			myCommPort.flushIOBuffers();
			myCommPort.addDataListener(new SerialPortDataListener() {
				@Override
				public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_RECEIVED; }
				@Override
				public void serialEvent(SerialPortEvent event) {
					//byte[] newData = new byte[myCommPort.bytesAvailable()];
					byte[] newData = event.getReceivedData();
				    
					for(int i = 0; i < newData.length; i++) {
						//System.out.print((char)newData[i]);
						char c = (char)newData[i];
						if(c == '!') {
							Platform.runLater(() -> outputArea.setText(val));
							myCommPort.removeDataListener();
							myCommPort.closePort();
							val = "";
						}else {
							val += c;
						}
					} //END FOR
				}
			});
			}
		}else {//if we did not validate input
			outputArea.setText(errText);
		}
	}
	private void writeSavedData(){
		//first erase contents of file
		try {
			File certLocationFile = new File(certLocations.getAbsolutePath());
			FileOutputStream mystream = new FileOutputStream(certLocationFile, false); // true to append
			mystream.write(privateKeyPath.getBytes());
			mystream.write(("\n").getBytes());
			mystream.write(rootCertPath.getBytes());
			mystream.write(("\n").getBytes());
			mystream.write(device_listPath.getBytes());
			mystream.close();
		}catch (IOException e){
			//do nothing if for some reason we couldnt get to file
			outputArea.setText("Failed to save data to cert locations");
		}
	}
	@FXML
	private void chooseKeyFiles(ActionEvent event){
		int selectedItemIndex = comboCerts.getSelectionModel().getSelectedIndex();
		//the order is private key, root cert, device list
		if(selectedItemIndex == 0) {//this would be the private key
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				privateKeyPath = file.getAbsolutePath();
			} else {
				privateKeyPath = "Private Key";
			}
		}
		if(selectedItemIndex == 1){ //for the root cert
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				rootCertPath = file.getAbsolutePath();
			} else {
				rootCertPath = "Root Certificate";
			}
		}
		if(selectedItemIndex == 2){
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				device_listPath = file.getAbsolutePath();
			} else {
				device_listPath = "Device List";
			}
		}
		ObservableList<String> currentCerts = FXCollections.observableArrayList();
		currentCerts.add(privateKeyPath);
		currentCerts.add(rootCertPath);
		currentCerts.add(device_listPath);

		//now I want to put the certs observable list back together
		comboCerts.setItems(currentCerts);
	}
}
