package com.itasoftware.itasoftware;

import java.util.List;
import java.util.Random;

public class VehicleSpawnSchedule {

    private final List<MovementTrajectory> possibleTrajectories;  // Lista możliwych trajektorii
    private final long spawnInterval;      // Interwał pomiędzy następującymi po sobie spawn-ami
    private long nextSpawnTime;       // Odstęp czasu, po jakim następuje kolejny spawn
    private int remainingVehicles;
    private final Random random = new Random();

    public VehicleSpawnSchedule(List<MovementTrajectory> trajectories, int numVehicles, long simTimeLength) {
        this.possibleTrajectories = trajectories;
        this.remainingVehicles = numVehicles;
        this.spawnInterval = numVehicles > 0 ? simTimeLength / numVehicles : Long.MAX_VALUE;
        this.nextSpawnTime = 0;  // Start od 0
    }

    // Warunek określający, czy już powinien się zespawnować kolejny pojazd
    public boolean shouldSpawn(long currentTime) {
        return remainingVehicles > 0 && currentTime >= nextSpawnTime;
    }

    public void markSpawned() {
        remainingVehicles--;
        nextSpawnTime += spawnInterval;
    }

    public MovementTrajectory getRandomTrajectory() {
        if (possibleTrajectories.isEmpty()) return null;
        return possibleTrajectories.get(random.nextInt(possibleTrajectories.size()));
    }

}
