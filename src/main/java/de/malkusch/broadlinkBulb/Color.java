package de.malkusch.broadlinkBulb;

import static java.util.Objects.requireNonNull;

public record Color(Red red, Green green, Blue blue) {

    public Color(int red, int green, int blue) {
        this(new Red(red), new Green(green), new Blue(blue));
    }

    public Color {
        requireNonNull(red);
        requireNonNull(green);
        requireNonNull(blue);
    }

    public record Red(int value) {

        public Red {
            assertValidColorValue(value);
        }
    }

    public record Green(int value) {

        public Green {
            assertValidColorValue(value);
        }
    }

    public record Blue(int value) {

        public Blue {
            assertValidColorValue(value);
        }
    }

    private static void assertValidColorValue(int value) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException("Color must be between 0 and 255");
        }
    }
}
