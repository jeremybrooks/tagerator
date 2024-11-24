package net.jeremybrooks.tagerator.tasks;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.bg.PixelBoundaryBackground;
import com.kennycason.kumo.bg.RectangleBackground;
import com.kennycason.kumo.font.KumoFont;
import com.kennycason.kumo.font.scale.LinearFontScalar;
import com.kennycason.kumo.font.scale.SqrtFontScalar;
import com.kennycason.kumo.nlp.FrequencyFileLoader;
import com.kennycason.kumo.palette.ColorPalette;
import javafx.concurrent.Task;
import net.jeremybrooks.tagerator.Main;
import net.jeremybrooks.tagerator.TConstants;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.util.List;

public class WordFrequencyTask extends Task<Void> {

    private TConstants.CloudShape cloudShape = TConstants.CloudShape.Rectangle;
    private int width = 1000;
    private int height = 1000;
    private int radius = 1000;
    private File imageFile;

    public void setCloudShape(TConstants.CloudShape cloudShape) {
       this.cloudShape = cloudShape;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setRadius(int radius) {
       this.radius = radius;
    }

    public void setImageFile(File imageFile) {
        this.imageFile = imageFile;
    }

    @Override
    protected Void call() throws Exception {
        updateMessage("Loading word frequency from file...");
        FrequencyFileLoader frequencyFileLoader = new FrequencyFileLoader();
        List<WordFrequency> wordFrequencies = frequencyFileLoader.load(Main.tagCacheFile.toFile());
        updateMessage("Creating word cloud, this could take a while....");
        // create a word cloud

        WordCloud wordCloud = null;
        String filename = null;
        switch (cloudShape) {
            case Circle -> {
                final Dimension dimension = new Dimension(radius * 2, radius * 2);
                wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
                wordCloud.setPadding(2);
                wordCloud.setBackground(new CircleBackground(radius));
                wordCloud.setFontScalar(new SqrtFontScalar(10, 40));
                filename = "wordcloud_circle-" + System.currentTimeMillis() + ".png";
            }
            case Rectangle -> {
                final Dimension dimension = new Dimension(1000, 1000);
                wordCloud = new WordCloud(dimension, CollisionMode.RECTANGLE);
                wordCloud.setPadding(0);
                wordCloud.setBackground(new RectangleBackground(dimension));
                wordCloud.setFontScalar(new LinearFontScalar(10, 40));
                filename = "wordcloud_rectangle-" + System.currentTimeMillis() + ".png";
            }
            case Image -> {
                final Dimension dimension = new Dimension(width, height);
                wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
                wordCloud.setPadding(2);
                wordCloud.setBackground(new PixelBoundaryBackground(imageFile));
                wordCloud.setFontScalar(new LinearFontScalar(10, 40));
                filename = "wordcloud_image" + System.currentTimeMillis() + ".png";
            }
        }
        // TODO make these parameters in the UI
        wordCloud.setKumoFont(new KumoFont(Font.decode("Helvetica")));
        wordCloud.setColorPalette(new ColorPalette(Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE));
        wordCloud.build(wordFrequencies);
        wordCloud.writeToFile("/Users/jeremyb/Desktop/" + filename);
        updateMessage("Complete");
        return null;
    }
}
