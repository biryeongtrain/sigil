package org.zuttomae.sigil.api.skill;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

public sealed interface SkillResponse {
    static Success success() {
        return new Success();
    }

    static Failure failure(Component reason) {
        return new Failure(reason);
    }

    static Failure inProgress() {
        return failure(Component.translatable("skill.failure.in_progress"));
    }

    static Failure notLearned() {
        return failure(Component.translatable("skill.failure.not_learned"));
    }

    static Failure cooldown() {
        return failure(Component.translatable("skill.failure.cooldown"));
    }

    static Failure unavailable() {
        return failure(Component.translatable("skill.failure.unavailable"));
    }

    record Success() implements SkillResponse {
        @ApiStatus.Internal
        public Success {
        }
    }

    record Failure(Component reason) implements SkillResponse {
        @ApiStatus.Internal
        public Failure {
            Objects.requireNonNull(reason);
        }
    }
}
