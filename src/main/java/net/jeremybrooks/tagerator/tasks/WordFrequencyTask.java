package net.jeremybrooks.tagerator.tasks;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.RectangleBackground;
import com.kennycason.kumo.font.KumoFont;
import com.kennycason.kumo.font.scale.LinearFontScalar;
import com.kennycason.kumo.nlp.FrequencyFileLoader;
import com.kennycason.kumo.palette.ColorPalette;
import javafx.concurrent.Task;
import net.jeremybrooks.tagerator.Main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

public class WordFrequencyTask extends Task<Void> {


    @Override
    protected Void call() throws Exception {
        updateMessage("Loading word frequency from file...");
        FrequencyFileLoader frequencyFileLoader = new FrequencyFileLoader();
        List<WordFrequency> wordFrequencies = frequencyFileLoader.load(Main.tagCacheFile.toFile());
        updateMessage("Creating word cloud, this could take a while....");
        // create a word cloud
        String filename = "wordcloud_rectangle-" + System.currentTimeMillis() + ".png";
        final Dimension dimension = new Dimension(1000, 1000);
        final WordCloud wordCloud = new WordCloud(dimension, CollisionMode.RECTANGLE);
        // TODO make these parameters in the UI
        wordCloud.setKumoFont(new KumoFont(Font.decode("Helvetica")));
        wordCloud.setPadding(0);
        wordCloud.setBackground(new RectangleBackground(dimension));
        wordCloud.setColorPalette(new ColorPalette(Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE));
        wordCloud.setFontScalar(new LinearFontScalar(10, 40));
        wordCloud.build(wordFrequencies);
        wordCloud.writeToFile("/Users/jeremyb/Desktop/" + filename);
        updateMessage("Complete");
        return null;
    }
}
