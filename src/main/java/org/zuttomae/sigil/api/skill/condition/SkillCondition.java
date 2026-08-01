package org.zuttomae.sigil.api.skill.condition;

import org.jetbrains.annotations.ApiStatus;
import org.zuttomae.sigil.api.skill.SkillContext;
import org.zuttomae.sigil.api.skill.SkillResponse;

import java.util.Objects;

@FunctionalInterface
public interface SkillCondition {
    static SkillCondition requireLearned() {
        return context -> context.getSource().getSkillContainer().hasSkill(context.getSkill()) ?
                SkillResponse.success() :
                SkillResponse.notLearned();
    }

    static SkillCondition requireNoCooldown() {
        return context -> context.getSource().getSkillCooldownManager().isCoolingDown(context.getSkill()) ?
                SkillResponse.cooldown() :
                SkillResponse.success();
    }

    static SkillCondition requireInGame() {
        return context -> context.getSource().canBeSeenByAnyone() ?
                SkillResponse.success() :
                SkillResponse.unavailable();
    }

    static SkillCondition defaultConditions() {
        return requireLearned()
                .and(requireNoCooldown())
                .and(requireInGame());
    }

    SkillResponse test(SkillContext<?> context);

    @ApiStatus.NonExtendable
    default SkillCondition and(SkillCondition other) {
        Objects.requireNonNull(other);
        return context -> test(context) instanceof SkillResponse.Failure failure ?
                failure :
                other.test(context);
    }

    @ApiStatus.NonExtendable
    default SkillCondition or(SkillCondition other) {
        Objects.requireNonNull(other);
        return context -> test(context) instanceof SkillResponse.Success success ?
                success :
                other.test(context);
    }
}
