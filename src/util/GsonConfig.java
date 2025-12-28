package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Configuration centralisée de Gson avec support pour LocalDateTime, LocalTime
 * et LocalDate
 */
public class GsonConfig {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // ✅ AJOUTÉ

    /**
     * Crée une instance Gson configurée avec les adaptateurs nécessaires
     */
    public static Gson createGson() {
        return new GsonBuilder()
                // Adaptateur pour LocalDateTime
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> context
                                .serialize(src.format(DATE_TIME_FORMATTER)))
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> LocalDateTime
                                .parse(json.getAsString(), DATE_TIME_FORMATTER))

                // Adaptateur pour LocalTime
                .registerTypeAdapter(LocalTime.class,
                        (JsonSerializer<LocalTime>) (src, typeOfSrc, context) -> context
                                .serialize(src.format(TIME_FORMATTER)))
                .registerTypeAdapter(LocalTime.class,
                        (JsonDeserializer<LocalTime>) (json, typeOfT, context) -> LocalTime.parse(json.getAsString(),
                                TIME_FORMATTER))

                // ✅ Adaptateur pour LocalDate (AJOUTÉ)
                .registerTypeAdapter(LocalDate.class,
                        (JsonSerializer<LocalDate>) (src, typeOfSrc, context) -> context
                                .serialize(src.format(DATE_FORMATTER)))
                .registerTypeAdapter(LocalDate.class,
                        (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> LocalDate.parse(json.getAsString(),
                                DATE_FORMATTER))

                .serializeNulls() // Inclure les champs null dans le JSON
                .setPrettyPrinting() // Formater le JSON pour le debug
                .create();
    }
}