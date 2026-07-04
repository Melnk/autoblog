package com.autoblog.publicreport.api.dto;

import com.autoblog.publicreport.application.PublicVehicleView;

public record PublicVehicleResponse(
        String vin,
        String make,
        String model,
        String generation,
        Integer year,
        String engine,
        String transmission,
        String trim,
        String market
) {
    public static PublicVehicleResponse from(PublicVehicleView view) {
        return new PublicVehicleResponse(
                view.vin(),
                view.make(),
                view.model(),
                view.generation(),
                view.year(),
                view.engine(),
                view.transmission(),
                view.trim(),
                view.market()
        );
    }
}
