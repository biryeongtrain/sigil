package org.zuttomae.sigil.api.skill.state;

import org.jetbrains.annotations.ApiStatus;
import org.zuttomae.sigil.api.skill.SkillResponse;

import java.util.Objects;
import java.util.function.Function;

public sealed interface SkillStateCreationResult<S> {
    static <S> Ok<S> ok(S state) {
        return new Ok<>(state);
    }

    static <S> Error<S> error(SkillResponse.Failure failure) {
        return new Error<>(failure);
    }

    @SuppressWarnings("unchecked")
    @ApiStatus.NonExtendable
    default <R> SkillStateCreationResult<R> map(
            Function<? super S, ? extends R> mapper
    ) {
        Objects.requireNonNull(mapper);
        return switch (this) {
            case Ok<S> ok -> ok(mapper.apply(ok.state()));
            case Error<S> error -> (Error<R>) error;
        };
    }

    @SuppressWarnings("unchecked")
    @ApiStatus.NonExtendable
    default <R> SkillStateCreationResult<R> flatMap(
            Function<? super S, ? extends SkillStateCreationResult<R>> mapper
    ) {
        Objects.requireNonNull(mapper);
        return switch (this) {
            case Ok<S> ok -> mapper.apply(ok.state());
            case Error<S> error -> (Error<R>) error;
        };
    }

    record Ok<S>(S state) implements SkillStateCreationResult<S> {
        @ApiStatus.Internal
        public Ok {
            Objects.requireNonNull(state);
        }
    }

    record Error<S>(SkillResponse.Failure failure) implements SkillStateCreationResult<S> {
        @ApiStatus.Internal
        public Error {
            Objects.requireNonNull(failure);
        }
    }
}
