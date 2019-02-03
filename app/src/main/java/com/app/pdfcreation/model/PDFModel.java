package com.app.pdfcreation.model;

import com.app.pdfcreation.utils.PDFCreationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PDFModel {

    private boolean isPending;
    private boolean isReceived;
    private String price;
    private String name;
    private float rating;

    public boolean isPending() {
        return isPending;
    }

    public void setPending(boolean pending) {
        isPending = pending;
    }

    public boolean isReceived() {
        return isReceived;
    }

    public void setReceived(boolean received) {
        isReceived = received;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    /**
     * Create dummy PDF model
     *
     * @return PDF Models
     */

    public static List<PDFModel> createDummyPdfModel() {
        PDFCreationUtils.filePath.clear();
        PDFCreationUtils.progressCount = 1;

        boolean isFirstReceivedItem = false;
        List<PDFModel> pdfModels = new ArrayList<>();
        for (int i = 0; i < 110; i++) {
            Random rand = new Random();
            int price = rand.nextInt((1000 - 200) + 1) + 200;

            PDFModel model = new PDFModel();
            if (!isFirstReceivedItem) {
                model.setReceived(true);
                isFirstReceivedItem = true;
            } else {
                model.setReceived(false);
            }

            model.setPrice(String.valueOf(price) + String.valueOf(".0 Rs."));

            if (i % 4 == 0) {
                model.setName("Umesh Kumar " + i);
            } else {
                model.setName("Ram Kumar " + i);
            }
            model.setRating(i % 3);
            pdfModels.add(model);
        }

        return pdfModels;
    }
}