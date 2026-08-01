package org.zuttomae.sigil.api.skill.duration;

import org.jetbrains.annotations.ApiStatus;
import org.zuttomae.sigil.api.skill.SkillContext;

import java.util.Objects;
import java.util.function.BiFunction;

@FunctionalInterface
public interface SkillDurationProvider {
    static SkillDurationProvider constant(int durationTicks) {
        return _ -> durationTicks;
    }

    static SkillDurationProvider infinite() {
        return _ -> -1;
    }

    static SkillDurationProvider instant() {
        return _ -> 0;
    }

    int get(SkillContext<?> context);

    @ApiStatus.NonExtendable
    default SkillDurationProvider map(
            BiFunction<? super SkillContext<?>, ? super Integer, ? extends Integer> mapper
    ) {
        Objects.requireNonNull(mapper);
        return context -> mapper.apply(context, get(context));
    }
}
