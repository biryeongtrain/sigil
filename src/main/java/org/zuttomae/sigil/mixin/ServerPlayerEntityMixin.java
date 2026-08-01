package org.zuttomae.sigil.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zuttomae.sigil.api.skill.manager.SkillManager;

@Mixin(value = ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Player {
    private ServerPlayerEntityMixin(Level world, GameProfile profile) {
        super(world, profile);
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
}
