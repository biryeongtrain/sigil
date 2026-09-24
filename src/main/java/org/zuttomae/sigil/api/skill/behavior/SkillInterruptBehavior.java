package org.zuttomae.sigil.api.skill.behavior;

import org.jetbrains.annotations.ApiStatus;
import org.zuttomae.sigil.api.skill.SkillInstance;

import java.util.Objects;

@FunctionalInterface
public interface SkillInterruptBehavior<S> {
    static <S> SkillInterruptBehavior<S> noOp() {
        return ignored -> {};
    }

    void execute(SkillInstance<? extends S> instance);

    @ApiStatus.NonExtendable
    default SkillInterruptBehavior<S> andThen(SkillInterruptBehavior<? super S> after) {
        Objects.requireNonNull(after);
        return instance -> {
            execute(instance);
            after.execute(instance);
        };
    }
}
