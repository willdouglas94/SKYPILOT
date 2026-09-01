package com.skypilot.backend.domain;

public class Airline {
    private final String id;
    private final String name;
    private final String iata;
    private final String icao;
    private final String country;
    private final String baseCity;
    private final String mainAirportCode;
    private final String dataSource;

    public Airline(String id, String name, String iata, String icao, String country, String mainAirportCode) {
        this(id, name, iata, icao, country, "Base City", mainAirportCode, "DEMO");
    }

    public Airline(String id, String name, String iata, String icao, String country, String baseCity,
                  String mainAirportCode, String dataSource) {
        this.id = id == null || id.isBlank() ? java.util.UUID.randomUUID().toString() : id;
        this.name = name == null || name.isBlank() ? "Airline" : name.trim();
        this.iata = iata == null || iata.isBlank() ? "XXX" : iata.trim().toUpperCase();
        this.icao = icao == null || icao.isBlank() ? "XXXX" : icao.trim().toUpperCase();
        this.country = country == null || country.isBlank() ? "Brazil" : country.trim();
        this.baseCity = baseCity == null || baseCity.isBlank() ? "Base City" : baseCity.trim();
        this.mainAirportCode = mainAirportCode == null || mainAirportCode.isBlank() ? "GRU" : mainAirportCode.trim().toUpperCase();
        this.dataSource = dataSource == null || dataSource.isBlank() ? "DEMO" : dataSource.trim().toUpperCase();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getIata() { return iata; }
    public String getIcao() { return icao; }
    public String getCountry() { return country; }
    public String getBaseCity() { return baseCity; }
    public String getMainAirportCode() { return mainAirportCode; }
    public String getDataSource() { return dataSource; }
}
