package dev.kearls.cardinaldirection;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class CardinalDirectionCalculator {
    private static final String API_KEY = System.getenv("GOOGLE_MAPS_API_KEY");
    public static final int COMPASS_DEGREES = 360;

    public static final String NORTH = "North";
    public static final String NORTH_EAST = "NorthEast";
    public static final String EAST = "East";
    public static final String SOUTH_EAST = "SouthEast";
    public static final String SOUTH = "South";
    public static final String SOUTH_WEST = "SouthWest";
    public static final String WEST = "West";
    public static final String NORTH_WEST = "NorthWest";

    public static final int NORTHWEST_MAX = 337;
    public static final int NORTHEAST_MIN = 23;
    public static final int NORTHEAST_MAX = 67;
    public static final int SOUTHEAST_MIN = 112;
    public static final int SOUTHEAST_MAX = 157;
    public static final int SOUTHWEST_MIN = 202;
    public static final int SOUTHWEST_MAX = 247;
    public static final int NORTHWEST_MIN = 292;

    private final Logger logger = LoggerFactory.getLogger(CardinalDirectionCalculator.class);
    private final GeoApiContext globalContext;

    public CardinalDirectionCalculator() {
        globalContext = new GeoApiContext.Builder()  // NOTE: we can set things like rate limit here?
                .apiKey(API_KEY)
                .build();
    }

    /**
     *
     * @param placeName
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws ApiException
     */
    @Cacheable("geocodingresults")
    public Optional<GeocodingResult> getLocation(final String placeName)
            throws IOException, InterruptedException, ApiException
    {
        logger.debug("Calling google maps api for [{}]", placeName);
        GeocodingResult[] results = GeocodingApi.geocode(globalContext, placeName).await();
        if (results.length != 0) {  // TODO verify that we should always return the first entry here
            return Optional.of(results[0]);
        } else {
            return Optional.empty();
        }
    }
    /**
     *
     * @param startPoint
     * @param endPoint
     * @return
     *
     * This is based on: https://www.movable-type.co.uk/scripts/latlong.html and specifically on the Kotlin
     * implementation here: https://github.com/bahadirarslan/Jeodezi
     *
     * Use this site to verify: https://www.sunearthtools.com/tools/distance.php
     *
     */
    private Double getCardinalDirectionInDegrees(final LatLng startPoint, final LatLng endPoint) {
        if (startPoint == null || endPoint == null || startPoint.equals(endPoint)) {
            return 0.0; // FIXME what to do here?
        }

        // Convert all coordinates to radians
        var startPointLongitude = Math.toRadians(startPoint.lng);
        var startPointLatitude = Math.toRadians(startPoint.lat);
        var endPointLongitude = Math.toRadians(endPoint.lng);
        var endPointLatitude = Math.toRadians(endPoint.lat);

        var deltaLongitude = endPointLongitude - startPointLongitude;
        var y = Math.sin(deltaLongitude) * Math.cos(endPointLatitude);
        var x = (Math.cos(startPointLatitude) * Math.sin(endPointLatitude))
                - (Math.sin(startPointLatitude) * Math.cos(endPointLatitude)) * Math.cos(deltaLongitude);
        var phi = Math.atan2(y, x);

        var result = Math.toDegrees(phi);
        if (result >= 0 && result <= COMPASS_DEGREES) {  // Checkstyle yelled at me if I just used 360
            return result;
        } else {
            return (result % COMPASS_DEGREES + COMPASS_DEGREES) % COMPASS_DEGREES;
        }
    }

    /**
     * Convert a compass direction in degrees to the text equivalent
     *
     * See http://tamivox.org/dave/compass/ for a definition of compass points
     *
     * FIXME - see https://github.com/jasondma/DesGuess/issues/113
     */
    public String getCardinalDirection(final LatLng startPoint, final LatLng endPoint) {
        var directionInDegrees = getCardinalDirectionInDegrees(startPoint, endPoint).intValue();
        System.out.println(startPoint + ", " + " Direction in degrees: " + directionInDegrees);

        // TODO can the current Java switch statement handle ranges?  If so this would be neater
        // TODO add enums for names and boundaries
        if (directionInDegrees >= NORTHWEST_MAX || directionInDegrees < NORTHEAST_MIN) {
            return NORTH;
        } else  if (directionInDegrees >= NORTHEAST_MIN && directionInDegrees < NORTHEAST_MAX) {
            return NORTH_EAST;
        } else  if (directionInDegrees >= NORTHEAST_MAX && directionInDegrees < SOUTHEAST_MIN) {
            return EAST;
        } else  if (directionInDegrees >= SOUTHEAST_MIN && directionInDegrees < SOUTHEAST_MAX) {
            return SOUTH_EAST;
        } else  if (directionInDegrees >= SOUTHEAST_MAX && directionInDegrees < SOUTHWEST_MIN) {
            return SOUTH;
        } else  if (directionInDegrees >= SOUTHWEST_MIN && directionInDegrees < SOUTHWEST_MAX) {
            return SOUTH_WEST;
        } else  if (directionInDegrees >= SOUTHWEST_MAX && directionInDegrees < NORTHWEST_MIN) {
            return WEST;
        } else  if (directionInDegrees >= NORTHWEST_MIN && directionInDegrees < NORTHWEST_MAX) {
            return NORTH_WEST;
        } else {
            logger.warn("Couldn't convert directionInDegrees of {} to a string", directionInDegrees);
            return "?";
        }
    }
}
