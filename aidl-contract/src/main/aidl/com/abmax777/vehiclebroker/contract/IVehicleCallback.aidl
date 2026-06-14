package com.abmax777.vehiclebroker.contract;

import com.abmax777.vehiclebroker.contract.VehicleData;

interface IVehicleCallback {
    // Broker invokes this on each consumer when new telemetry arrives.
    // 'oneway' = fire-and-forget: the broker doesn't block waiting for
    // the consumer to finish processing. Critical here — one slow
    // consumer must not stall the broker's fan-out to everyone else.
    oneway void onVehicleData(in VehicleData data);
}