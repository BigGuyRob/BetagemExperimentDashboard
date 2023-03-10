package com.example.experimentdashboard;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import static com.example.experimentdashboard.Globals.savedDataPath;
import javax.imageio.ImageIO;

public class experiment_pdf {
    private static String FILE = savedDataPath;
    private static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18,
            Font.BOLD);
    private static Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12,
            Font.NORMAL, BaseColor.RED);
    private static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16,
            Font.BOLD);
    private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12,
            Font.BOLD);

    private String dateCreated;
    private WritableImage img;

    private String notes ="";
    private String parameters ="";
    private ObservableList<Double> positions = FXCollections.observableArrayList();
    private ObservableList<Double> AvIntensityON = FXCollections.observableArrayList();
    private ObservableList<Double> AvIntensityOFF = FXCollections.observableArrayList();
    private ObservableList<Double> AvIntensityDIFF = FXCollections.observableArrayList();

    private String textboxData;
    //Image of the table
    //output of the experiment parameters
    public experiment_pdf(WritableImage img, String textboxData){
        this.img = img;
        this.textboxData = textboxData;
        scan(); //first thing is fill out the data fields from the input textboxData;
        FILE = savedDataPath + "Experiment_" + System.currentTimeMillis() + ".pdf";
    }

    public String export(){
        File f = new File(FILE);
        try{
            f.createNewFile();
        }catch(Exception e){
            return "File could not be created";
        }
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(f.getAbsolutePath()));
            document.open();
            addMetaData(document);
            addTitlePage(document);
            addNotes(document);
            addParams(document);
            addData(document);
            addImage(document);
            document.close();
        } catch (Exception e) {
            return "File could not be written to";
        }
        return "File successfully created";
    }

    private static void addMetaData(Document document) {
        document.addTitle("Experiment Details");
        document.addKeywords("Java, Arduino, MKR, AWS, IoT");
        document.addAuthor("Experiment Dashboard");
        document.addCreator("Rob Reid");
    }

    private void addTitlePage(Document document)
            throws DocumentException {
        Paragraph preface = new Paragraph();
        addEmptyLine(preface, 1);
        preface.add(new Paragraph(("Betagem Experiment " + dateCreated), catFont));
        addEmptyLine(preface, 1);
        document.add(preface);
    }

    private void addData(Document document){
         try{
             Paragraph preface = new Paragraph();
             addEmptyLine(preface, 2);
             // first row
             PdfPTable Datatable = new PdfPTable(3); Datatable.setTotalWidth(new float[]{ 80, 80,80 });
             PdfPCell PositionsLabel = new PdfPCell(new Phrase("Position", smallBold)); PositionsLabel.setFixedHeight(25); PositionsLabel.setColspan(1);
             PdfPCell AvOnLabel = new PdfPCell(new Phrase("Average ON", smallBold)); AvOnLabel.setFixedHeight(25); AvOnLabel.setColspan(1);
             PdfPCell AvOffLabel = new PdfPCell(new Phrase("Average OFF", smallBold)); AvOffLabel.setFixedHeight(25); AvOffLabel.setColspan(1);
             Datatable.addCell(PositionsLabel);
             Datatable.addCell(AvOnLabel);
             Datatable.addCell(AvOffLabel);


             for(int x = 0; x < AvIntensityON.size(); x++){ //plot average intensity on
                 PdfPCell pos = new PdfPCell(new Phrase(positions.get(x).toString(), smallBold)); pos.setFixedHeight(25); pos.setColspan(1);
                 PdfPCell avon = new PdfPCell(new Phrase(AvIntensityON.get(x).toString(), smallBold)); avon.setFixedHeight(25); avon.setColspan(1);
                 PdfPCell avoff = new PdfPCell(new Phrase(AvIntensityOFF.get(x).toString(), smallBold)); avoff.setFixedHeight(25); avoff.setColspan(1);
                 Datatable.addCell(pos);
                 Datatable.addCell(avon);
                 Datatable.addCell(avoff);
             }
             document.add(Datatable);
         }catch(DocumentException e){
             //nothing
         }
    }

    private void addImage(Document document) throws IOException, DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedImage bImage = SwingFXUtils.fromFXImage(img, null);
        ImageIO.write(bImage, "png", baos);
        byte[] imageData = baos.toByteArray();
        Image image = Image.getInstance(imageData);
        document.add(image);
    }

    private void addNotes(Document document) throws DocumentException {
        Paragraph preface = new Paragraph();
        addEmptyLine(preface, 1);
        preface.add(new Paragraph((notes), smallBold));
        addEmptyLine(preface, 1);
        document.add(preface);
    }

    private void addParams(Document document) throws DocumentException{
        Paragraph preface = new Paragraph();
        addEmptyLine(preface, 1);
        preface.add(new Paragraph((parameters), smallBold));
        addEmptyLine(preface, 1);
        document.add(preface);
    }
    private void scan(){
        String experimentOutput = textboxData;
        Scanner scanner = new Scanner(experimentOutput);
        boolean flag_gotten_to_data = false;
        boolean flag_gotten_all_notes = false;
        boolean flag_gotten_all_params = false;
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            if(line.contains("NOTES") && flag_gotten_all_notes == false){
                dateCreated = scanner.nextLine();
                line = scanner.nextLine();
                while(flag_gotten_all_notes == false){
                    notes += line;
                    notes += "\n";
                    line = scanner.nextLine();
                    if(line.contains("NOTES")){
                        flag_gotten_all_notes = true;
                    }
                }
            }
            if(line.contains("PARAMETERS") && flag_gotten_all_params == false){
                line = scanner.nextLine();
                while(flag_gotten_all_params == false){
                    parameters += line;
                    parameters += "\n";
                    line = scanner.nextLine();
                    if(line.contains("PARAMETERS")){
                        flag_gotten_all_params = true;
                    }
                }
            }
            if(line.contains("Intensity")) {
                flag_gotten_to_data = true;
                line = scanner.nextLine();
            }
            if(flag_gotten_to_data){
                if(line.contains("!")){
                    break;
                }
                String[] data = line.split(" ");
                ArrayList<String> cleanedData = new ArrayList<String>();
                for(String d : data) {
                    if(d.trim() != ""){
                        cleanedData.add(d);
                    }
                }
                //index 1 should be av on intensity
                Double AvIntensityON_val = Double.parseDouble(cleanedData.get(1));
                AvIntensityON.add(AvIntensityON_val);
                //index 2 should be av off intensity
                Double AvIntensityOFF_val = Double.parseDouble(cleanedData.get(2));
                AvIntensityOFF.add(AvIntensityOFF_val);

                AvIntensityDIFF.add(AvIntensityON_val - AvIntensityOFF_val);
                //index 0 should be the position
                positions.add(Double.parseDouble(cleanedData.get(0)));
                cleanedData.clear();
            }
        }
    }
    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }
}
