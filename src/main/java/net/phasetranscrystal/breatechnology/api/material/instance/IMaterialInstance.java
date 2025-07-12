package net.phasetranscrystal.breatechnology.api.material.instance;

import net.phasetranscrystal.breatechnology.api.material.tag.MaterialTagInfo;
import net.phasetranscrystal.breatechnology.api.material.type.MetaMaterial;

public interface IMaterialInstance {
    default IMaterialInstance self(){
        return this;
    }

    MetaMaterial<?> getDefinition();

    MaterialTagInfo getInstanceType();

    default long getMeasure() {
        return getInstanceType().materialAmount();
    }
}