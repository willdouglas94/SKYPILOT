package com.skypilot.backend.service;

import com.skypilot.backend.domain.FlightOffer;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class RuleBasedEmployerAI {

    public FlightOffer chooseOffer(List<FlightOffer> offers) {
        if (offers == null || offers.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma oferta disponível");
        }

        return offers.stream()
                .sorted(Comparator.comparing(FlightOffer::getDate)
                        .thenComparing(FlightOffer::getDepartureTime)
                        .thenComparing(offer -> offer.getRoute().getDestination().getCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Oferta inválida"));
    }
}
