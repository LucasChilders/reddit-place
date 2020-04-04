package com.lucaschilders.util;

public enum Color {
    WHITE("FFFFFF"),
    LIGHT_GRAY("E4E4E4"),
    GRAY("888888"),
    BLACK("222222"),
    PINK("FFA7D1"),
    RED("E50000"),
    ORANGE("E59500"),
    BROWN("A06A42"),
    YELLOW("E5D900"),
    LIGHT_GREEN("94E044"),
    GREEN("02BE01"),
    AQUA_BLUE("00E5F0"),
    GREEN_BLUE("0083C7"),
    BLUE("0000EA"),
    VIOLET("E04AFF"),
    PURPLE("820080");

    public static final Color[] values = values();
    public final String hexColor;

    Color(final String hexColor) {
        this.hexColor = hexColor;
    }

    public String getHexColor() {
        return this.hexColor;
    }
}
