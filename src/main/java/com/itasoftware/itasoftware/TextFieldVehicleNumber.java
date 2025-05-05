package com.itasoftware.itasoftware;

public class TextFieldVehicleNumber extends IntersectionLane {

    private Localization destination;
    private Double CarsNumber;

    public TextFieldVehicleNumber(Localization localization, Type type, Localization destination) {
        super(localization, type, 0);
        this.destination = destination;
    }

    public Localization getDestination() {
        return destination;
    }

    public void setCarsNumber(double carsNumber) {
        this.CarsNumber = carsNumber;
    }

    public Double getCarsNumber() {
        return CarsNumber;
    }

}
