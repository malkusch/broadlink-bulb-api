package de.malkusch.broadlinkLb2Api;

public record Brightness(int value) {

    public Brightness(int value) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException("Brightness must be between 0 and 255");
        }
        this.value = value;
    }
}
