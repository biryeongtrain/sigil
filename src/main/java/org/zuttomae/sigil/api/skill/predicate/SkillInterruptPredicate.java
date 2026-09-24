package org.zuttomae.sigil.api.skill.predicate;

import org.jetbrains.annotations.ApiStatus;
import org.zuttomae.sigil.api.skill.SkillInstance;

import java.util.Objects;

@FunctionalInterface
public interface SkillInterruptPredicate<S> {
    static <S> SkillInterruptPredicate<S> allowed() {
        return ignored -> true;
    }

    static <S> SkillInterruptPredicate<S> denied() {
        return ignored -> false;
    }

    boolean test(SkillInstance<? extends S> instance);

    @ApiStatus.NonExtendable
    default SkillInterruptPredicate<S> and(SkillInterruptPredicate<? super S> other) {
        Objects.requireNonNull(other);
        return instance -> test(instance) && other.test(instance);
    }

    @ApiStatus.NonExtendable
    default SkillInterruptPredicate<S> or(SkillInterruptPredicate<? super S> other) {
        Objects.requireNonNull(other);
        return instance -> test(instance) || other.test(instance);
    }
}
