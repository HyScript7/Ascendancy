package io.github.hyscript7.ascendancy.features.reflection.citizens;

import lombok.Getter;
import lombok.Setter;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

import java.util.UUID;

@TraitName("reflectionowner")
public class ReflectionTrait extends Trait {
    @Persist
    @Getter
    @Setter
    private UUID ownerUuid;
    public ReflectionTrait() {
        super("reflectionowner");
    }
}
