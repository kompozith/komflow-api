package com.kompozith.komflow.features.core.service;

import com.kompozith.komflow.features.core.dto.GeoCityDto;
import com.kompozith.komflow.features.core.dto.GeoCountryDto;

import java.util.List;

public interface GeoService {
    List<GeoCountryDto> getCountries();
    List<GeoCityDto> getCitiesByCountry(String countryCode);
    GeoCountryDto getCountryByTimezone(String timezone);
}
