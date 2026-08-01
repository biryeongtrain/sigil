package org.zuttomae.sigil.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zuttomae.sigil.api.skill.attachment.SkillAttachments;
import org.zuttomae.sigil.api.skill.entity.SkillEntity;
import org.zuttomae.sigil.api.skill.manager.SkillContainer;
import org.zuttomae.sigil.api.skill.manager.SkillCooldownManager;
import org.zuttomae.sigil.api.skill.manager.SkillManager;
import org.zuttomae.sigil.impl.skill.manager.SkillContainerImpl;
import org.zuttomae.sigil.impl.skill.manager.SkillCooldownManagerImpl;
import org.zuttomae.sigil.impl.skill.manager.SkillManagerImpl;

@Mixin(value = LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements SkillEntity {
    private LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public SkillContainer getSkillContainer() {
        SkillContainerImpl skillContainer =
                (SkillContainerImpl) getAttachedOrCreate(SkillAttachments.SKILL_CONTAINER, SkillContainerImpl::new);

        if (skillContainer.getSource() != (Object) this) {
            skillContainer.setSource(sigil$livingEntity());
        }

        return skillContainer;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public SkillCooldownManager getSkillCooldownManager() {
        SkillCooldownManagerImpl skillCooldownManager =
                (SkillCooldownManagerImpl) getAttachedOrCreate(SkillAttachments.SKILL_COOLDOWN_MANAGER, SkillCooldownManagerImpl::new);

        if (skillCooldownManager.getSource() != (Object) this) {
            skillCooldownManager.setSource(sigil$livingEntity());
        }

        return skillCooldownManager;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public SkillManager getSkillManager() {
        SkillManagerImpl skillManager =
                (SkillManagerImpl) getAttachedOrCreate(SkillAttachments.SKILL_MANAGER, SkillManagerImpl::new);

        if (skillManager.getSource() != (Object) this) {
            skillManager.setSource(sigil$livingEntity());
        }

        return skillManager;
    }

    @Inject(method = "die", at = @At(value = "TAIL"))
    private void sigil$onDeath(DamageSource source, CallbackInfo ci) {
        if (level().isClientSide()) {
            return;
        }

        SkillManager skillManager = getSkillManager();
        if (!skillManager.interruptCasting()) {
            skillManager.terminateCasting();
        }
    }

    @Inject(method = "onRemoval", at = @At(value = "TAIL"))
    private void sigil$onRemoval(RemovalReason reason, CallbackInfo ci) {
        if (level().isClientSide()) {
            return;
        }

        SkillManager skillManager = getSkillManager();
        if (!skillManager.interruptCasting()) {
            skillManager.terminateCasting();
        }
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void sigil$onFirstTick(CallbackInfo ci) {
        if (level().isClientSide()) {
            return;
        }

        if (firstTick) {
            ((SkillContainerImpl) getSkillContainer()).refresh();
        }
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void sigil$onTick(CallbackInfo ci) {
        if (level().isClientSide()) {
            return;
        }

        ((SkillCooldownManagerImpl) getSkillCooldownManager()).update();
        ((SkillManagerImpl) getSkillManager()).update();
    }

    @Unique
    private LivingEntity sigil$livingEntity() {
        return (LivingEntity) (Object) this;
    }
}
