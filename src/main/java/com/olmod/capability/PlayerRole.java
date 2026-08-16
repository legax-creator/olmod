package com.olmod.capability;

public enum PlayerRole {
    OYUNCU,
    KOPEK,
    KEDI,
    AT;

    /** Bu rollerin hiçbiri blok kıramaz, blokla etkileşime giremez (kapı vb.), envanteri yoktur. */
    public boolean isPet() {
        return this == KOPEK || this == KEDI || this == AT;
    }

    /** Bu roller kimseye saldıramaz / hasar veremez. */
    public boolean isHarmless() {
        return this == KEDI || this == AT;
    }

    public static PlayerRole fromArg(String arg) {
        switch (arg.toLowerCase()) {
            case "oyuncu":
                return OYUNCU;
            case "köpek":
            case "kopek":
                return KOPEK;
            case "kedi":
                return KEDI;
            case "at":
                return AT;
            default:
                return null;
        }
    }
}
