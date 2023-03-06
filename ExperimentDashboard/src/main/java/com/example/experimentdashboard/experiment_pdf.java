package com.example.experimentdashboard;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Date;

import static com.example.experimentdashboard.Globals.savedDataPath;

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

    private Date dateCreated;
    private WritableImage img;
    //Image of the table
    //output of the experiment parameters
    public experiment_pdf(WritableImage img){
        dateCreated = new Date();
        this.img = img;
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

    private void addImage(Document document){

    }
    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }
}
