package com.lucaschilders.pojo;

import com.lucaschilders.util.Color;
import java.io.IOException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Defines a Tile based on the provided CSV row entry (ts, user, posx, posy, color)
 */
public class Tile implements Comparable<Tile> {
    private static Logger LOGGER = LogManager.getLogger(Tile.class);

    public static class Position {
        public final int x;
        public final int y;

        private Position (final int x, final int y) {
            if (x > 1000 || y > 1000) {
                LOGGER.error(String.format("%d, %d was out of bounds!", x, y));
            }
            this.x = Math.min(x, 999);
            this.y = Math.min(y, 999);
        }
    }

    private final Long timestamp;
    private final String user;
    private final Position position;
    private final Color color;

    public Tile(final String[] row) throws IOException {
        this.timestamp = Long.valueOf(row[0]);
        this.user = row[1];
        this.position = new Position(Integer.parseInt(row[2]), Integer.parseInt(row[3]));
        this.color = Color.values[Integer.parseInt(row[4])];
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getUser() {
        return user;
    }

    public Position getPosition() {
        return position;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        return String.format("%d, %s, %d, %d, %s", this.timestamp, this.user,
            this.position.x, this.position.y, this.color);
    }

    /**
     * When sorting, compare the timestamp. Ideally the dataset is sorted.
     * @param tile
     */
    @Override
    public int compareTo(final Tile tile) {
        return this.timestamp.compareTo(tile.timestamp);
    }
}
