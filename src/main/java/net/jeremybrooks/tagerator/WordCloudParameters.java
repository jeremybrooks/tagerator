package net.jeremybrooks.tagerator;

import java.io.File;

public class WordCloudParameters {
    private TConstants.CloudShape cloudShape;
    private int width;
    private int height;
    private String imageFile;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    private String errorMessage;

    public TConstants.CloudShape getCloudShape() {
        return cloudShape;
    }

    public void setCloudShape(TConstants.CloudShape cloudShape) {
        this.cloudShape = cloudShape;
    }

    public int getRadius() {
        return width;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(String width) {
        try {
           this.width = Integer.parseInt(width);
        } catch (NumberFormatException e) {
           this.width = 0;
        }
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(String height) {
        try {
            this.height = Integer.parseInt(height);
        } catch (NumberFormatException e) {
            this.height = 0;
        }
    }

    public String getImageFile() {
        return imageFile;
    }

    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }

    public void validate() {
       switch (cloudShape) {
           case Rectangle -> {
               if (width < 100 || height < 100) {
                   errorMessage = "Width and height must be at least 100.";
               }
           }
           case Circle -> {
               if (width < 100) {
                   errorMessage = "Radius must be at least 100.";
               }
           }
           case Image -> {
               if (imageFile == null || !new File(imageFile).exists()) {
                   errorMessage = "You must specify an image file to use.";
               }
           }
       }
    }
}
