package org.zuttomae.sigil.api.skill.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;
import org.zuttomae.sigil.api.skill.Skill;

public interface SkillRegistryKeys {
    ResourceKey<Registry<Skill<?>>> SKILL = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("sigil", "skill"));

    @ApiStatus.Internal
    static void initialize() {
    }
}
