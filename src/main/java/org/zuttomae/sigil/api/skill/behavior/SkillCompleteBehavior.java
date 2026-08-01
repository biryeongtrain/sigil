package org.zuttomae.sigil.api.skill.behavior;

import org.jetbrains.annotations.ApiStatus;
import org.zuttomae.sigil.api.skill.SkillInstance;

import java.util.Objects;

@FunctionalInterface
public interface SkillCompleteBehavior<S> {
    static <S> SkillCompleteBehavior<S> noOp() {
        return _ -> {};
    }

    void execute(SkillInstance<? extends S> instance);

    @ApiStatus.NonExtendable
    default SkillCompleteBehavior<S> andThen(SkillCompleteBehavior<? super S> after) {
        Objects.requireNonNull(after);
        return instance -> {
            execute(instance);
            after.execute(instance);
        };
    }
}
