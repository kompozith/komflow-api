package com.kompozith.komflow.features.core.controller;

import com.kompozith.komflow.features.core.dto.GeoCityDto;
import com.kompozith.komflow.features.core.dto.GeoCountryDto;
import com.kompozith.komflow.features.core.service.GeoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/geo")
@RequiredArgsConstructor
@Tag(name = "Geo", description = "Geographic reference data")
public class GeoController {

    private final GeoService geoService;

    @GetMapping("/countries")
    @Operation(summary = "List countries")
    public List<GeoCountryDto> listCountries() {
        return geoService.getCountries();
    }

    @GetMapping("/countries/{countryCode}/cities")
    @Operation(summary = "List cities by country")
    public List<GeoCityDto> listCitiesByCountry(@PathVariable String countryCode) {
        return geoService.getCitiesByCountry(countryCode);
    }

    @GetMapping("/country-by-timezone")
    @Operation(summary = "Get country by timezone")
    public GeoCountryDto getCountryByTimezone(@org.springframework.web.bind.annotation.RequestParam String timezone) {
        return geoService.getCountryByTimezone(timezone);
    }
}
