package org.zuttomae.sigil.api.skill.registry;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.zuttomae.sigil.api.skill.Skill;

public interface SkillRegistries {
    Registry<Skill<?>> SKILL = FabricRegistryBuilder.createSimple(SkillRegistryKeys.SKILL).buildAndRegister();

    @ApiStatus.Internal
    static void initialize() {
    }
}
