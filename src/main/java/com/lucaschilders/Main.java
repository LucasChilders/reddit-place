package com.lucaschilders;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Grab the CSV dataset from https://www.reddit.com/r/redditdata/comments/6640ru/place_datasets_april_fools_2017/
 */
public class Main {
    private static Logger LOGGER = LogManager.getLogger(Main.class);
    private static String DATASET_LOC_PROPERTY = "dataset_location";
    private static String IMAGE_LOC_PROPERTY = "image_location";
    private static String HEATMAP_LOC_PROPERTY = "heatmap_location";

    public static void main(final String[] args) {
        BasicConfigurator.configure();

        final String datasetPath = System.getProperty(DATASET_LOC_PROPERTY);
        final String newImagePath = System.getProperty(IMAGE_LOC_PROPERTY);
        final String newHeatMapPath = System.getProperty(HEATMAP_LOC_PROPERTY);
        Preconditions.checkNotNull(datasetPath);
        Preconditions.checkNotNull(newImagePath);
        Preconditions.checkNotNull(newHeatMapPath);

        LOGGER.info(String.format("Provided dataset location: [%s]", datasetPath));

        try {
            final Stopwatch start = Stopwatch.createStarted();
            final Place place = new Place(datasetPath);
            place.sortTilesByTimeCreated();

            // draw it once per 10 minutes
            final List<Integer> time = Lists.newArrayList();
            for (int i = 0; i < 432; i++) {
                time.add(i);
            }

            final String path = newImagePath.split("\\.bmp")[0];

            time.parallelStream().forEach(t -> {
                final BufferedImage image = place.drawBoard(place.generatePlaceBoard(), Duration.ofMinutes(t * 10));
                LOGGER.info(String.format("Writing bitmap.", newImagePath));
                try {
                    ImageIO.write(image, "bmp", new File(String.format("%s%d.bmp", path, t * 10)));
                }
                catch (final IOException e) {
                    LOGGER.error(e);
                }
            });

//            final BufferedImage heatmap = place.drawHeatmap(place.generateHeatmapBoard());
//            LOGGER.info(String.format("Writing heatmap bitmap to %s", newHeatMapPath));
//            ImageIO.write(heatmap, "bmp", new File(newHeatMapPath));

            LOGGER.info(String.format("Finished after %s seconds.", start.stop().elapsed(TimeUnit.SECONDS)));
        } catch (final IOException e) {
            LOGGER.error(e); }
    }
}
