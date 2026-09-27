package edu.aldermere.university.generator;

import java.util.Random;

/** Seeded wording. The selected sentence never decides a relationship. */
public final class Prose {
    private static final String[] OPENERS = {
            "Pièce tenue à jour concernant %s.",
            "Formulation retenue dans le dossier de %s.",
            "Suivi administratif pour %s."
    };

    public String variant(Random random, String anchor) {
        return OPENERS[random.nextInt(OPENERS.length)].formatted(anchor);
    }
}
