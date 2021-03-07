package main.java.helper;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.*;

/**
 * A class to get the Color value from a string color name
 */
@Getter @AllArgsConstructor
public class ColorFromString {

    /**
     * Get the color from a string name
     *
     * @param col name of the color
     * @return White if no color is given, otherwise the Color object
     */
    public Color getColor(String col) {
        Color color = Color.WHITE;
        switch (col.toLowerCase()) {
            case "black":
            case "schwarz":
                color = Color.BLACK;
                break;
            case "blue":
            case "blau":
                color = Color.BLUE;
                break;
            case "cyan":
                color = Color.CYAN;
                break;
            case "darkgray":
            case "dunkelgrau":
                color = Color.DARK_GRAY;
                break;
            case "gray":
            case "grau":
                color = Color.GRAY;
                break;
            case "green":
            case "grün":
                color = Color.GREEN;
                break;
            case "yellow":
            case "gelb":
                color = Color.YELLOW;
                break;
            case "lightgray":
            case "hellgrau":
                color = Color.LIGHT_GRAY;
                break;
            case "magenta":
                color = Color.MAGENTA;
                break;
            case "orange":
                color = Color.ORANGE;
                break;
            case "pink":
                color = Color.PINK;
                break;
            case "red":
            case "rot":
                color = Color.RED;
                break;
        }
        return color;
    }
}
