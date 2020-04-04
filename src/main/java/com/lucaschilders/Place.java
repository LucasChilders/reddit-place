package com.lucaschilders;

import com.google.common.collect.Lists;
import com.lucaschilders.pojo.Tile;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Place {
    private static Logger LOGGER = LogManager.getLogger(Place.class);

    private final List<Tile> tileSet;
    private Tile[][] tileMap;
    private Integer[][] heatMap;

    public Place(final String datasetPath) throws IOException {
        this.tileSet = loadTileSet(datasetPath);
    }

    public List<Tile> getTileSet() {
        return this.tileSet;
    }

    /**
     * {@code sortTiles()} should be ran before this in order to end up with the correct final
     * Place image.
     * @return 1000,1000 2D array of Tiles, built in order of the {@code this.tileSet}.
     */
    public Tile[][] generateStandardTileMap() {
        LOGGER.info("Generating tile map (1000, 1000).");
        final Long startTime = System.currentTimeMillis();

        final Tile[][] tileMap = new Tile[1000][1000];
        for (final Tile tile : this.tileSet) {
            try {
                tileMap[tile.getPosition().x][tile.getPosition().y] = tile;
            } catch (final ArrayIndexOutOfBoundsException e) {
                LOGGER.error(String.format("Fell out of bounds at %d, %d.", tile.getPosition().x, tile.getPosition().y), e);
            }
        }

        int emptyTiles = 0;
        for (int x = 0; x <= 999; x++) {
            for (int y = 0; y <= 999; y++) {
                if (tileMap[x][y] == null) {
                    emptyTiles++;
                    LOGGER.warn(String.format("Empty tile at %d, %d. Defaulting to white.", x, y));
                }
            }
        }
        final Long endTime = System.currentTimeMillis();
        LOGGER.info(String.format("Tile map generation and null check complete after %d seconds. Found %d empty tiles.",
            TimeUnit.MILLISECONDS.toSeconds(endTime - startTime), emptyTiles));

        this.tileMap = tileMap;
        return tileMap;
    }

    /**
     * @return 1000,1000 2D array of integers, where more common tiles are higher values.
     */
    public Integer[][] generateHeatmapTileMap() {
        final Integer[][] heatMap = new Integer[1000][1000];

        for (final Tile tile : this.tileSet) {
            if (heatMap[tile.getPosition().x][tile.getPosition().y] == null) {
                heatMap[tile.getPosition().x][tile.getPosition().y] = 0;
            }
            heatMap[tile.getPosition().x][tile.getPosition().y]++;
        }

        // Fill in blanks
        for (int x = 0; x <= 999; x++) {
            for (int y = 0; y <= 999; y++) {
                if (heatMap[x][y] == null) {
                    heatMap[x][y] = 0;
                }
            }
        }

        this.heatMap = heatMap;
        return heatMap;
    }

    /**
     * Creates a BufferedImage of {@code this.heatMap} values. If heatMap is null, it is created.
     * @return BufferedImage of 1000 x 1000 (RGB)
     */
    public BufferedImage generateHeatmap() {
        if (this.tileMap == null) {
            generateHeatmapTileMap();
        }

        LOGGER.info("Generating heatmap bitmap...");
        final Long startTime = System.currentTimeMillis();
        final BufferedImage image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x <= 999; x++) {
            for (int y = 0; y <= 999; y++) {
                try {
                    String color;
                    if (this.heatMap[x][y] == 0) {
                        color = String.format("%02x%02x%02x", 0, 0, 0);
                    } else {
                        color = String.format("%02x%02x%02x", (int) Math.min(this.heatMap[x][y] * 1.5, 255), (this.heatMap[x][y] * 2) % 255, 0);
                    }
                    image.setRGB(x, y, new BigInteger(color, 16).intValue());
                } catch (final ArrayIndexOutOfBoundsException e) {
                    LOGGER.error(String.format("%d, %d", x, y), e);
                }
            }
        }
        final Long endTime = System.currentTimeMillis();
        LOGGER.info(String.format("Heatmap bitmap complete after %d seconds.", TimeUnit.MILLISECONDS.toSeconds(endTime - startTime)));

        return image;
    }

    /**
     * Creates a BufferedImage of {@code this.tileMap}. If tileMap is null, it is created.
     * @return BufferedImage of 1000 x 1000 (RGB)
     */
    public BufferedImage generateImage() {
        if (this.tileMap == null) {
            generateStandardTileMap();
        }

        LOGGER.info("Generating bitmap...");
        final Long startTime = System.currentTimeMillis();
        final BufferedImage image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x <= 999; x++) {
            for (int y = 0; y <= 999; y++) {
                try {
                    final String color = this.tileMap[x][y] == null ? "FFFFFF" : this.tileMap[x][y].getColor().getHexColor();
                    image.setRGB(x, y, new BigInteger(color, 16).intValue());
                } catch (final ArrayIndexOutOfBoundsException e) {
                    LOGGER.error(String.format("%d, %d", x, y), e);
                }
            }
        }
        final Long endTime = System.currentTimeMillis();
        LOGGER.info(String.format("Bitmap complete after %d seconds.", TimeUnit.MILLISECONDS.toSeconds(endTime - startTime)));

        return image;
    }

    /**
     * Sorts the {@code this.tileSet} based on their placement date
     */
    public void sortTiles() {
        LOGGER.info(String.format("Attempting to sort %d rows, that's ~120,000,000 operations. GLHF!",
            getTileSet().size()));
        final long startTime = System.currentTimeMillis();
        Collections.sort(this.tileSet);
        final long endTime = System.currentTimeMillis();
        LOGGER.info(String.format("Sort finished after %d seconds.", TimeUnit.MILLISECONDS.toSeconds(endTime - startTime)));
    }

    /**
     * @param datasetPath
     * @return A Set of {@code Tile} objects, generated from the provided dataset.
     * @throws IOException
     */
    private static List<Tile> loadTileSet(final String datasetPath) throws IOException {
        LOGGER.info("Attempting to process dataset...");
        final int estimatedCapacity = 16559897;
        final List<Tile> tileSet = Lists.newArrayListWithCapacity(estimatedCapacity);
        final BufferedReader reader = new BufferedReader(new FileReader(datasetPath));

        // Skip the heading
        reader.readLine();

        String row;
        int count = 0;
        final long startTime = System.currentTimeMillis();
        while ((row = reader.readLine()) != null) {
            tileSet.add(new Tile(row.split(",")));
            count++;
            LOGGER.info(String.format("[ %d / %d ]", count, estimatedCapacity));
        }
        final long endTime = System.currentTimeMillis();
        reader.close();

        LOGGER.info(String.format("Finished after %d seconds.", TimeUnit.MILLISECONDS.toSeconds(endTime - startTime)));

        return tileSet;
    }
}
