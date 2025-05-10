package com.itasoftware.itasoftware;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VehicleManager {
    private final List<Vehicle> vehiclesList = new ArrayList<>();

    public void spawnVehicle(List<TextFieldVehicleNumber> tfVNInput, Map<MovementRelations, MovementTrajectory> movementMap) {
        // Dodanie pojazdów z zadaną trajektorią do listy pojazdów
        for (TextFieldVehicleNumber tfVN : tfVNInput) {
            for (int i = 0; i < tfVN.getCarsNumber().intValue(); i++) {     // Dodanie takiej liczby pojazdów, jaka została zadana w tfVN dla danej relacji
                for (MovementRelations mr : MovementRelations.movementRelations) {
                    if (tfVN.getLocalization() == mr.getObjectA().getLocalization() && tfVN.getDestination() == mr.getObjectB().getLocalization()) {
                        MovementTrajectory traj = movementMap.get(mr);  // Wybranie trajektorii przypisanej w Hash Mapie do danej relacji
                        if (traj != null) {
                           vehiclesList.add(new Vehicle(traj));
                        }
                    }
                }
            }
        }

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
