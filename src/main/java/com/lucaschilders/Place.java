package com.lucaschilders;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.lucaschilders.pojo.Tile;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Place {
    private static Logger LOGGER = LogManager.getLogger(Place.class);
    private static Long PLACE_START_TIME = 1490979600000L;

    public final List<Tile> tiles;

    /**
     * Creates a new instance of the Place dataset, this takes a while.
     * @param datasetPath
     * @throws IOException
     */
    public Place(final String datasetPath) throws IOException {
        this.tiles = loadTileSet(datasetPath);
    }

    /**
     * For testability.
     * @param tiles
     */
    protected Place(final List<Tile> tiles) {
        this.tiles = tiles;
    }

    /**
     * {@code sortTiles()} should be ran before this in order to end up with the correct final
     * Place image.
     * @return 1000,1000 2D array of Tiles, built in order of the {@code this.tiles}.
     */
    public ArrayList[][] generatePlaceBoard() {
        LOGGER.info("Generating tile map (1000, 1000).");
        final Stopwatch timer = Stopwatch.createStarted();

        final ArrayList[][] board = new ArrayList[1000][1000];
        for (final Tile tile : this.tiles) {
            try {
                if (board[tile.getPosition().x][tile.getPosition().y] == null) {
                    board[tile.getPosition().x][tile.getPosition().y] = new ArrayList<Tile>();
                }

                board[tile.getPosition().x][tile.getPosition().y].add(tile);
            } catch (final ArrayIndexOutOfBoundsException e) {
                LOGGER.error(String.format("Fell out of bounds at %d, %d.", tile.getPosition().x, tile.getPosition().y), e);
            }
        }

        int emptyTiles = 0;
        for (int x = 0; x <= 999; x++) {
            for (int y = 0; y <= 999; y++) {
                if (board[x][y] == null) {
                    board[x][y] = new ArrayList<Tile>();
                    emptyTiles++;
                }
            }
        }

        LOGGER.info(String.format("Tile map generation and null check complete after %d seconds. Found %d empty tiles.",
            timer.stop().elapsed(TimeUnit.SECONDS), emptyTiles));
        return board;
    }

    /**
     * Creates a BufferedImage of {@code this.tileMap}. If tileMap is null, it is created.
     * @param board to draw (see {@code generateStandardTileMap()}
     * @return BufferedImage of 1000 x 1000 (RGB)
     */
    public BufferedImage drawBoard(final ArrayList[][] board, final Duration durationPastStart) {
        Preconditions.checkNotNull(board);
        LOGGER.info("Generating bitmap...");
        final Stopwatch timer = Stopwatch.createStarted();

        final Long stopDrawingTime = PLACE_START_TIME + durationPastStart.toMillis();
        final BufferedImage image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x <= 999; x++) {
            for (int y = 0; y <= 999; y++) {
                try {
                    for (final Tile tile : (ArrayList<Tile>) board[x][y]) {
                        if (tile.getTimestamp() > stopDrawingTime) {
                            break;
                        }

                        final String color = board[x][y].isEmpty() ? "FFFFFF" : tile.getColor().getHexColor();
                        image.setRGB(x, y, new BigInteger(color, 16).intValue());
                    }
                } catch (final ArrayIndexOutOfBoundsException e) {
                    LOGGER.error(String.format("%d, %d", x, y), e);
                }
            }
        }

        LOGGER.info(String.format("Bitmap complete after %d seconds.",  timer.stop().elapsed(TimeUnit.SECONDS)));
        return image;
    }

    public BufferedImage drawBoard(final ArrayList[][] board) {
        return drawBoard(board, Duration.ofHours(72));
    }

    /**
     * @return 1000,1000 2D array of integers, where more common tiles are higher values.
     */
    public Integer[][] generateHeatmapBoard() {
        final Integer[][] heatMap = new Integer[1000][1000];

        for (final Tile tile : this.tiles) {
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

        return heatMap;
    }

    /**
     * Creates a BufferedImage of {@code this.heatMap} values. If heatMap is null, it is created.
     * @return BufferedImage of 1000 x 1000 (RGB)
     */
    public BufferedImage drawHeatmap(final Integer[][] board, final Duration durationPastStart) {
        LOGGER.info("Generating heatmap bitmap...");
        final Stopwatch timer = Stopwatch.createStarted();

        final Long stopDrawingTime = PLACE_START_TIME + durationPastStart.toMillis();
        final BufferedImage image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x <= 999; x++) {
            for (int y = 0; y <= 999; y++) {
                try {
                    String color;
                    if (board[x][y] == 0) {
                        color = String.format("%02x%02x%02x", 0, 0, 0);
                    } else {
                        color = String.format("%02x%02x%02x", (int) Math.min(board[x][y] * 1.5, 255), (board[x][y] * 2) % 255, 0);
                    }
                    image.setRGB(x, y, new BigInteger(color, 16).intValue());
                } catch (final ArrayIndexOutOfBoundsException e) {
                    LOGGER.error(String.format("%d, %d", x, y), e);
                }
            }
        }

        LOGGER.info(String.format("Heatmap bitmap complete after %d seconds.",  timer.stop().elapsed(TimeUnit.SECONDS)));
        return image;
    }

    public BufferedImage drawHeatmap(final Integer[][] board) {
        return drawHeatmap(board, Duration.ofHours(72));
    }

    /**
     * Sorts the provided based on their placement date
     * @return the sorted tiles
     */
    public void sortTilesByTimeCreated() {
        LOGGER.info(String.format("Attempting to sort %d rows.", tiles.size()));
        final Stopwatch timer = Stopwatch.createStarted();
        Collections.sort(tiles);
        LOGGER.info(String.format("Sort finished after %d seconds.", timer.stop().elapsed(TimeUnit.SECONDS)));
    }

    /**
     * @param datasetPath
     * @return A Set of {@code Tile} objects, generated from the provided dataset.
     * @throws IOException on file read exceptions
     */
    private static List<Tile> loadTileSet(final String datasetPath) throws IOException {
        LOGGER.info("Attempting to process dataset... this may take some time.");
        final int estimatedCapacity = 16559897;
        final List<Tile> tileSet = Lists.newArrayListWithCapacity(estimatedCapacity);
        final BufferedReader reader = new BufferedReader(new FileReader(datasetPath));

        // Skip the heading
        reader.readLine();

        String row;
        final Stopwatch timer = Stopwatch.createStarted();
        while ((row = reader.readLine()) != null) {
            tileSet.add(new Tile(row.split(",")));
        }
        reader.close();

        LOGGER.info(String.format("Finished after %d seconds.", timer.stop().elapsed(TimeUnit.SECONDS)));
        return tileSet;
    }
}
