package com.itasoftware.itasoftware;

import java.util.ArrayList;
import java.util.List;

public class VehicleManager {
    private final List<Vehicle> vehiclesList = new ArrayList<>();

    public void spawnVehicle() {
        // Na start: pojazd jadący z góry na dół
        vehiclesList.add(new Vehicle(500, -30, 500, 0, 2.0, 0, 1));
        System.out.println("spawn vehicle");
    }

    public void updateVehicles() {
        for (Vehicle v : vehiclesList) {
            v.updateVehiclePosition();
            System.out.println("update vehicle position");
        }
        vehiclesList.removeIf(Vehicle::isVehicleOutOfBounds);
    }

    public List<Vehicle> getVehicles() {
        System.out.println("returning vehicle list");
        return vehiclesList;
    }
}
