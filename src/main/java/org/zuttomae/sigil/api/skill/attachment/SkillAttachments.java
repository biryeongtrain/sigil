package org.zuttomae.sigil.api.skill.attachment;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.zuttomae.sigil.api.skill.manager.SkillContainer;
import org.zuttomae.sigil.api.skill.manager.SkillCooldownManager;
import org.zuttomae.sigil.api.skill.manager.SkillManager;
import org.zuttomae.sigil.impl.skill.manager.SkillContainerImpl;
import org.zuttomae.sigil.impl.skill.manager.SkillCooldownManagerImpl;

public interface SkillAttachments {
    AttachmentType<SkillContainer> SKILL_CONTAINER = AttachmentRegistry
            .create(
                    ResourceLocation.fromNamespaceAndPath("sigil", "skill_container"),
                    builder -> builder
                            .persistent(SkillContainerImpl.CODEC)
                            .copyOnDeath()
            );

    AttachmentType<SkillCooldownManager> SKILL_COOLDOWN_MANAGER = AttachmentRegistry
            .create(
                    ResourceLocation.fromNamespaceAndPath("sigil", "skill_cooldown_manager"),
                    builder -> builder
                            .persistent(SkillCooldownManagerImpl.CODEC)
                            .copyOnDeath()
            );

    AttachmentType<SkillManager> SKILL_MANAGER = AttachmentRegistry
            .create(
                    ResourceLocation.fromNamespaceAndPath("sigil", "skill_manager")
            );

    @ApiStatus.Internal
    static void initialize() {
    }
}
