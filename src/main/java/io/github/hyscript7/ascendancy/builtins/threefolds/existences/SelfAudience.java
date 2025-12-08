package io.github.hyscript7.ascendancy.builtins.threefolds.existences;

import io.github.hyscript7.ascendancy.features.threefold.ThreefoldAudience;
import io.github.hyscript7.ascendancy.features.threefold.ThreefoldAudiencePowerLevel;

public class SelfAudience extends ThreefoldAudience {
    public static final String id = "audience self";
    public SelfAudience() {
        super(id, "Self", new String[]{
                "I",
                "In my name"
        }, ThreefoldAudiencePowerLevel.REGULAR);
    }
}
