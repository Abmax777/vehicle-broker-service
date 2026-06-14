package com.abmax777.vehiclebroker.contract;

import com.abmax777.vehiclebroker.contract.VehicleData;
import com.abmax777.vehiclebroker.contract.IVehicleCallback;

interface IVehicleBroker {

    // A consumer registers its callback to start receiving telemetry.
    // Returns false if the caller lacks permission (Day 4 wiring).
    boolean subscribe(IVehicleCallback callback);

    // A consumer unregisters — broker stops pushing to it.
    void unsubscribe(IVehicleCallback callback);

    // The producer pushes a new telemetry sample in.
    // 'oneway' so the producer doesn't block on the broker's fan-out.
    oneway void publish(in VehicleData data);
}