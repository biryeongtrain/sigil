package org.zuttomae.sigil.api.skill.state;

import org.jetbrains.annotations.ApiStatus;
import org.zuttomae.sigil.api.skill.SkillContext;
import org.zuttomae.sigil.api.skill.SkillResponse;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

@FunctionalInterface
public interface SkillStateFactory<S> {
    static <S> SkillStateFactory<S> alwaysOk(
            Function<? super SkillContext<?>, ? extends S> provider
    ) {
        Objects.requireNonNull(provider);
        return context -> SkillStateCreationResult.ok(provider.apply(context));
    }

    static <S> SkillStateFactory<S> alwaysError(
            Function<? super SkillContext<?>, ? extends SkillResponse.Failure> provider
    ) {
        Objects.requireNonNull(provider);
        return context -> SkillStateCreationResult.error(provider.apply(context));
    }

    static <S> SkillStateFactory<S> constant(S state) {
        Objects.requireNonNull(state);
        return alwaysOk(ignored -> state);
    }

    SkillStateCreationResult<S> create(SkillContext<?> context);

    @ApiStatus.NonExtendable
    default <R> SkillStateFactory<R> map(
            BiFunction<? super SkillContext<?>, ? super S, ? extends R> mapper
    ) {
        Objects.requireNonNull(mapper);
        return context -> create(context).map(state -> mapper.apply(context, state));
    }

    @ApiStatus.NonExtendable
    default <R> SkillStateFactory<R> flatMap(
            BiFunction<? super SkillContext<?>, ? super S, ? extends SkillStateCreationResult<R>> mapper
    ) {
        Objects.requireNonNull(mapper);
        return context -> create(context).flatMap(state -> mapper.apply(context, state));
    }
}
