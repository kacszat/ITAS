package com.itasoftware.itasoftware;

import javafx.scene.control.TextField;

public class TextFieldCarNumber extends IntersectionLane {

    private Localization destination;

    public TextFieldCarNumber(Localization localization, Type type, Localization destination) {
        super(localization, type, 0);
        this.destination = destination;
    }

    public Localization getDestination() {
        return destination;
    }

}
