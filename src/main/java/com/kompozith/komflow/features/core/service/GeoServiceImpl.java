package com.kompozith.komflow.features.core.service;

import com.ibm.icu.util.TimeZone;
import com.kompozith.komflow.features.core.dto.GeoCityDto;
import com.kompozith.komflow.features.core.dto.GeoCountryDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class GeoServiceImpl implements GeoService {

    @Override
    public List<GeoCountryDto> getCountries() {
        Set<String> isoCountries = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2);
        List<GeoCountryDto> countries = new ArrayList<>();

        for (String code : isoCountries) {
            Locale locale = new Locale("", code);
            String name = locale.getDisplayCountry(Locale.ENGLISH);
            if (name != null && !name.isBlank()) {
                countries.add(new GeoCountryDto(code, name));
            }
        }

        countries.sort(Comparator.comparing(GeoCountryDto::getName, String.CASE_INSENSITIVE_ORDER));
        return countries;
    }

    @Override
    public List<GeoCityDto> getCitiesByCountry(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return List.of();
        }

        String normalizedCountryCode = countryCode.trim().toUpperCase(Locale.ROOT);
        String[] zoneIds = TimeZone.getAvailableIDs(normalizedCountryCode);
        return citiesFromTimezones(zoneIds);
    }

    private List<GeoCityDto> citiesFromTimezones(String[] zoneIds) {
        Map<String, GeoCityDto> byTimezone = new LinkedHashMap<>();
        for (String timezone : zoneIds) {
            if (timezone == null || timezone.isBlank() || !timezone.contains("/")) {
                continue;
            }

            String cityPart = timezone.substring(timezone.lastIndexOf('/') + 1);
            String city = cityPart.replace('_', ' ').replace('-', ' ').trim();
            if (!city.isBlank()) {
                byTimezone.putIfAbsent(timezone, new GeoCityDto(city, timezone));
            }
        }

        return byTimezone.values().stream()
                .sorted(Comparator.comparing(GeoCityDto::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}
