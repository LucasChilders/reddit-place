package com.lucaschilders;

import com.google.common.base.Preconditions;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
            final Place place = new Place(datasetPath);
            place.sortTiles();

            place.generateStandardTileMap();
            final BufferedImage image = place.generateImage();
            LOGGER.info(String.format("Writing bitmap to %s", newImagePath));
            ImageIO.write(image, "bmp", new File(newImagePath));

            place.generateHeatmapTileMap();
            final BufferedImage heatmap = place.generateHeatmap();
            LOGGER.info(String.format("Writing heatmap bitmap to %s", newHeatMapPath));
            ImageIO.write(heatmap, "bmp", new File(newHeatMapPath));
        } catch (final IOException e) {
            LOGGER.error(e);
        }
    }
}
