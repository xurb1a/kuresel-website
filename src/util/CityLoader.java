package util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.City;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CityLoader {
    private static final Logger LOGGER = Logger.getLogger(CityLoader.class.getName());
    private static List<City> cities;

    public static List<City> loadCities() {
        if (cities != null) {
            return cities;
        }

        cities = new ArrayList<>();
        try {
            InputStream inputStream = CityLoader.class.getResourceAsStream("/data/morocco_cities.json");
            if (inputStream == null) {
                LOGGER.severe("Could not find morocco_cities.json file");
                return loadFallbackCities();
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                StringBuilder jsonContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }

                JsonObject jsonObject = JsonParser.parseString(jsonContent.toString()).getAsJsonObject();
                JsonArray citiesArray = jsonObject.getAsJsonArray("cities");

                for (int i = 0; i < citiesArray.size(); i++) {
                    JsonObject cityObject = citiesArray.get(i).getAsJsonObject();
                    City city = new City(
                            cityObject.get("name").getAsString(),
                            cityObject.get("deliveryFee").getAsDouble(),
                            cityObject.get("deliveryDays").getAsInt(),
                            cityObject.get("region").getAsString()
                    );
                    cities.add(city);
                }
            }

            return cities;
        } catch (IOException | RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Failed to load cities data", e);
            return loadFallbackCities();
        }
    }

    private static List<City> loadFallbackCities() {
        LOGGER.info("Loading fallback cities data");
        List<City> fallbackCities = new ArrayList<>();
        fallbackCities.add(new City("Khouribga", 10.0, 1, "Béni Mellal-Khénifra"));
        fallbackCities.add(new City("Casablanca", 20.0, 2, "Casablanca-Settat"));
        fallbackCities.add(new City("Rabat", 25.0, 2, "Rabat-Salé-Kénitra"));
        fallbackCities.add(new City("Marrakech", 30.0, 3, "Marrakech-Safi"));
        return fallbackCities;
    }

    public static City getCityByName(String name) {
        if (name == null || name.isEmpty()) {
            return new City("Khouribga", 10.0, 1, "Béni Mellal-Khénifra");
        }

        return loadCities().stream()
                .filter(city -> city.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(new City("Khouribga", 10.0, 1, "Béni Mellal-Khénifra"));
    }
}
