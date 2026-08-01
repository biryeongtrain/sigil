package org.zuttomae.sigil.api.skill.predicate;

import org.jetbrains.annotations.ApiStatus;
import org.zuttomae.sigil.api.skill.SkillInstance;

import java.util.Objects;

@FunctionalInterface
public interface SkillCancelPredicate<S> {
    static <S> SkillCancelPredicate<S> allowed() {
        return _ -> true;
    }

    static <S> SkillCancelPredicate<S> denied() {
        return _ -> false;
    }

    boolean test(SkillInstance<? extends S> instance);

    @ApiStatus.NonExtendable
    default SkillCancelPredicate<S> and(SkillCancelPredicate<? super S> other) {
        Objects.requireNonNull(other);
        return instance -> test(instance) && other.test(instance);
    }

    @ApiStatus.NonExtendable
    default SkillCancelPredicate<S> or(SkillCancelPredicate<? super S> other) {
        Objects.requireNonNull(other);
        return instance -> test(instance) || other.test(instance);
    }
}
