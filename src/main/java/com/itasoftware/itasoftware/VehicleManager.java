package com.itasoftware.itasoftware;

import java.util.ArrayList;
import java.util.List;

public class VehicleManager {
    private final List<Vehicle> vehiclesList = new ArrayList<>();

    public void spawnVehicle(MovementTrajectory traj) {
        vehiclesList.add(new Vehicle(traj));
    }

    public void updateVehicles(double simSpeed) {
        for (Vehicle v : vehiclesList) {
            v.updateVehiclePosition();
            v.setSimSpeed(simSpeed);
        }
        vehiclesList.removeIf(Vehicle::isFinished);     // Pojazd zostaje usunięty, jeśli dotarł do końca trasy
    }

    public List<Vehicle> getVehicles() {
        return vehiclesList;
    }

    public void clearVehicles() {
        vehiclesList.clear();
    }

}
