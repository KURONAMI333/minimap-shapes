package com.kuronami.minimapshapes;

public enum Shape {
    STANDARD,
    ROUNDED_SQUARE,
    HEXAGON,
    OCTAGON,
    HORIZONTAL_RECTANGLE,
    VERTICAL_RECTANGLE;

    public boolean isCustom() {
        return this != STANDARD;
    }
}
