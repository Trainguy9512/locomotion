package com.trainguy9512.locomotion.resource;

public record FormatVersion(int version) {

    public static FormatVersion of(int version) {
        return new FormatVersion(version);
    }

    public static FormatVersion ofDefault() {
        return FormatVersion.of(1);
    }

    public boolean postScaleFormatUpdate() {
        return this.version >= 4;
    }
}
