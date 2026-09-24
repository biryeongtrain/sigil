package org.zuttomae.sigil;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.zuttomae.sigil.api.skill.Skill;
import org.zuttomae.sigil.api.skill.SkillResponse;
import org.zuttomae.sigil.api.skill.registry.SkillRegistries;
import org.zuttomae.sigil.api.skill.state.SkillStateFactory;

public final class SigilGameTest implements ModInitializer {
    private static final ResourceLocation SOURCE = ResourceLocation.fromNamespaceAndPath("sigil_test", "learned");
    private static final ResourceLocation TEMPORARY = ResourceLocation.fromNamespaceAndPath("sigil_test", "temporary");
    private static final ResourceLocation MODIFIER = ResourceLocation.fromNamespaceAndPath("sigil_test", "casting");
    private static Holder<Skill<CastState>> skill;

    @SuppressWarnings("unchecked")
    @Override
    public void onInitialize() {
        skill = (Holder<Skill<CastState>>) (Holder<?>) Registry.registerForHolder(
                SkillRegistries.SKILL,
                ResourceLocation.fromNamespaceAndPath("sigil_test", "cast"),
                Skill.<CastState>builder()
                        .setStateFactory(SkillStateFactory.alwaysOk(context -> new CastState()))
                        .setDurationProvider(context -> context.getSource().getTags().contains("slow") ? 6 : 3)
                        .setStartBehavior(instance -> instance.getAttributeModifierTracker().applyModifier(
                                Attributes.MOVEMENT_SPEED, MODIFIER, 0.1, AttributeModifier.Operation.ADD_VALUE
                        ))
                        .setTickBehavior(instance -> instance.getState().ticks++)
                        .setCompleteBehavior(instance -> instance.getState().completed++)
                        .setCancelBehavior(instance -> instance.getState().cancelled++)
                        .setInterruptBehavior(instance -> instance.getState().interrupted++)
                        .setEndBehavior(instance -> instance.getState().ended++)
                        .build()
        );
    }

    @GameTest
    public void castingAndCooldownsAdvanceOnServerTicks(GameTestHelper helper) {
        var fast = helper.spawnWithNoFreeWill(EntityType.COW, 1, 1, 1);
        var slow = helper.spawnWithNoFreeWill(EntityType.COW, 2, 1, 1);
        slow.addTag("slow");
        helper.assertTrue(fast.getSkillManager().castSkill(skill) instanceof SkillResponse.Failure,
                Component.literal("An unlearned skill must be rejected"));
        fast.getSkillContainer().addPermanentSkill(skill, SOURCE);
        slow.getSkillContainer().addPermanentSkill(skill, SOURCE);
        helper.assertTrue(fast.getSkillManager().castSkill(skill) instanceof SkillResponse.Success,
                Component.literal("The fast cast must start"));
        helper.assertTrue(slow.getSkillManager().castSkill(skill) instanceof SkillResponse.Success,
                Component.literal("The slow cast must start"));
        var fastInstance = fast.getSkillManager().getCastingInstance(skill);
        var slowInstance = slow.getSkillManager().getCastingInstance(skill);
        var fastState = fastInstance.getState();
        var slowState = slowInstance.getState();
        helper.assertTrue(fastState != slowState && fastInstance.getDurationTicks() == 3
                        && slowInstance.getDurationTicks() == 6,
                Component.literal("Cast state and duration must be per entity"));
        helper.assertTrue(fast.getSkillManager().castSkill(skill) instanceof SkillResponse.Failure,
                Component.literal("A second concurrent cast must be rejected"));

        helper.runAfterDelay(8, () -> {
            helper.assertTrue(fastState.ticks == 3 && slowState.ticks == 6
                            && fastState.completed == 1 && slowState.completed == 1
                            && fastState.ended == 1 && slowState.ended == 1,
                    Component.literal("Casts must tick and complete exactly once at their durations"));
            helper.assertTrue(!fast.getSkillManager().isCasting() && !slow.getSkillManager().isCasting()
                            && !fast.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(MODIFIER),
                    Component.literal("Completion must clear casting state and temporary modifiers"));
            fast.getSkillCooldownManager().setCooldown(skill, 4);
            helper.assertTrue(fast.getSkillManager().castSkill(skill) instanceof SkillResponse.Failure,
                    Component.literal("Cooldown must prevent casting"));
            helper.runAfterDelay(6, () -> {
                helper.assertTrue(fast.getSkillCooldownManager().getCooldown(skill) == 0
                                && fast.getSkillManager().castSkill(skill) instanceof SkillResponse.Success,
                        Component.literal("Casting must be available after cooldown expiry"));
                var cancelled = fast.getSkillManager().getCastingInstance(skill).getState();
                helper.assertTrue(fast.getSkillManager().cancelCasting() && cancelled.cancelled == 1
                                && cancelled.completed == 0 && cancelled.ended == 1,
                        Component.literal("Cancellation must end without completing"));
                helper.succeed();
            });
        });
    }

    @GameTest
    public void skillsAndCooldownsSurviveEntitySerialization(GameTestHelper helper) {
        var original = helper.spawnWithNoFreeWill(EntityType.COW, 1, 1, 1);
        original.getSkillContainer().addPermanentSkill(skill, SOURCE);
        original.getSkillContainer().addTransientSkill(skill, TEMPORARY);
        original.getSkillCooldownManager().setCooldown(skill, 17);
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        original.saveWithoutId(output);
        var restored = new Cow(EntityType.COW, helper.getLevel());
        restored.load(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), output.buildResult()));
        helper.assertTrue(restored.getSkillContainer().hasSkill(skill, SOURCE)
                        && !restored.getSkillContainer().hasSkill(skill, TEMPORARY)
                        && restored.getSkillCooldownManager().getCooldown(skill) == 17,
                Component.literal("Attachments must preserve permanent skills and cooldowns, excluding transient grants"));
        helper.assertTrue(restored.getSkillManager().testSkill(skill) instanceof SkillResponse.Failure,
                Component.literal("A restored cooldown must still block the skill"));
        restored.getSkillCooldownManager().clearCooldowns();
        helper.assertTrue(restored.getSkillManager().castSkill(skill) instanceof SkillResponse.Success,
                Component.literal("Restored attachments must bind to the restored entity"));
        restored.getSkillManager().terminateCasting();
        helper.succeed();
    }

    @GameTest
    public void removalAndPlayerDeathInterruptCasting(GameTestHelper helper) {
        var cow = helper.spawnWithNoFreeWill(EntityType.COW, 1, 1, 1);
        cow.getSkillContainer().addPermanentSkill(skill, SOURCE);
        cow.getSkillManager().castSkill(skill);
        var removedState = cow.getSkillManager().getCastingInstance(skill).getState();
        cow.discard();
        helper.assertTrue(!cow.getSkillManager().isCasting() && removedState.interrupted == 1
                        && removedState.ended == 1 && removedState.completed == 0
                        && !cow.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(MODIFIER),
                Component.literal("Entity removal must interrupt and clean up its cast"));

        var player = helper.makeMockServerPlayerInLevel();
        player.getSkillContainer().addPermanentSkill(skill, SOURCE);
        helper.assertTrue(player.getSkillManager().castSkill(skill) instanceof SkillResponse.Success,
                Component.literal("Server players must expose the injected skill API"));
        var deadState = player.getSkillManager().getCastingInstance(skill).getState();
        player.die(helper.getLevel().damageSources().generic());
        helper.assertTrue(!player.getSkillManager().isCasting() && deadState.interrupted == 1
                        && deadState.ended == 1 && deadState.completed == 0,
                Component.literal("Player death must interrupt and end casting exactly once"));
        helper.succeed();
    }

    private static final class CastState {
        int ticks;
        int completed;
        int cancelled;
        int interrupted;
        int ended;
    }
}
