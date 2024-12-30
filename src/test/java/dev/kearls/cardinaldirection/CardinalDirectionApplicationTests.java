package dev.kearls.cardinaldirection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class CardinalDirectionApplicationTests {
    @Autowired
    private CardinalDirectionCalculator directionCalculator;

    @Test
    public void testGetDirection() throws Exception {
        // FIXME make these global and put them in setup
        var bordeauxLocation = directionCalculator.getLocation("Bordeaux");
        var bostonLocation = directionCalculator.getLocation("Boston");
        var bourgesLocation = directionCalculator.getLocation("Bourges");
        var canadaLocation = directionCalculator.getLocation("Canada");
        var londonLocation = directionCalculator.getLocation("London");
        var lyonLocation = directionCalculator.getLocation("Lyon");
        var namibiaLocation = directionCalculator.getLocation("Namibia");
        var nantesLocation = directionCalculator.getLocation("Nantes");
        var parisLocation = directionCalculator.getLocation("Paris");

        assertTrue(bordeauxLocation.isPresent());
        assertTrue(bostonLocation.isPresent());
        assertTrue(bourgesLocation.isPresent());
        assertTrue(canadaLocation.isPresent());
        assertTrue(londonLocation.isPresent());
        assertTrue(lyonLocation.isPresent());
        assertTrue(namibiaLocation.isPresent());
        assertTrue(nantesLocation.isPresent());
        assertTrue(parisLocation.isPresent());

        var bordeauxLatLng = bordeauxLocation.get().geometry.location;
        var bostonLatLng = bostonLocation.get().geometry.location;
        var bourgesLatLng = bourgesLocation.get().geometry.location;
        var canadaLatLng = canadaLocation.get().geometry.location;
        var londonLatLng = londonLocation.get().geometry.location;
        var lyonLatLng = lyonLocation.get().geometry.location;
        var namibiaLatLng = namibiaLocation.get().geometry.location;
        var nantesLatLng = nantesLocation.get().geometry.location;
        var parisLatLng = parisLocation.get().geometry.location;

        assertEquals("North", directionCalculator.getCardinalDirection(nantesLatLng, londonLatLng));
        assertEquals("NorthEast", directionCalculator.getCardinalDirection(bordeauxLatLng, parisLatLng));
        assertEquals("East", directionCalculator.getCardinalDirection(nantesLatLng, bourgesLatLng));
        assertEquals("SouthEast", directionCalculator.getCardinalDirection(parisLatLng, lyonLatLng));
        assertEquals("South", directionCalculator.getCardinalDirection(parisLatLng, bourgesLatLng));
        assertEquals("SouthWest", directionCalculator.getCardinalDirection(parisLatLng, bordeauxLatLng));
        assertEquals("West", directionCalculator.getCardinalDirection(parisLatLng, bostonLatLng));
        assertEquals("NorthWest", directionCalculator.getCardinalDirection(lyonLatLng, parisLatLng));

        System.out.println(directionCalculator.getCardinalDirection(canadaLatLng, namibiaLatLng));
        System.out.println("Canada: " + canadaLatLng);
        System.out.println("Namibia: " + namibiaLatLng);
    }

}
