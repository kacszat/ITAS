package com.itasoftware.itasoftware;

public class TextFieldVehicleNumber extends IntersectionLane {

    private Localization destination;
    private Double VehiclesNumber;

    public TextFieldVehicleNumber(Localization localization, Type type, Localization destination) {
        super(localization, type, 0);
        this.destination = destination;
    }

    public Localization getDestination() {
        return destination;
    }

    public void setVehiclesNumber(double vehiclesNumber) {
        this.VehiclesNumber = vehiclesNumber;
    }

    public Double getVehiclesNumber() {
        return VehiclesNumber;
    }

}
