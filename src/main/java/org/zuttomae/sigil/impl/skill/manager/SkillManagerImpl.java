package org.zuttomae.sigil.impl.skill.manager;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.zuttomae.sigil.api.entity.attribute.AttributeModifierTracker;
import org.zuttomae.sigil.api.skill.Skill;
import org.zuttomae.sigil.api.skill.SkillContext;
import org.zuttomae.sigil.api.skill.SkillInstance;
import org.zuttomae.sigil.api.skill.SkillResponse;
import org.zuttomae.sigil.api.skill.event.SkillTestCallback;
import org.zuttomae.sigil.api.skill.manager.SkillManager;
import org.zuttomae.sigil.api.skill.state.SkillStateCreationResult;
import org.zuttomae.sigil.impl.skill.SkillContextImpl;
import org.zuttomae.sigil.impl.skill.SkillInstanceImpl;

import java.util.Objects;
import java.util.Optional;

public final class SkillManagerImpl implements SkillManager {
    private @Nullable SkillInstance<?> instance = null;
    private @Nullable LivingEntity source = null;

    @Override
    public boolean isCasting() {
        return instance != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <S> SkillInstance<S> getCastingInstance(Holder<? extends Skill<S>> skill) {
        Objects.requireNonNull(skill);
        return instance == null || !instance.getSkill().equals(skill) ?
                null :
                (SkillInstance<S>) instance;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SkillResponse testSkill(Holder<? extends Skill<?>> skill) {
        Objects.requireNonNull(skill);
        return executeTest(
                (Holder<? extends Skill<Object>>) skill
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public SkillResponse castSkill(Holder<? extends Skill<?>> skill) {
        Objects.requireNonNull(skill);
        return executeCast(
                (Holder<? extends Skill<Object>>) skill
        );
    }

    @Override
    public boolean cancelCasting() {
        return isCasting() && executeCancel(instance);
    }

    @Override
    public boolean interruptCasting() {
        return isCasting() && executeInterrupt(instance);
    }

    @Override
    public boolean terminateCasting() {
        if (!isCasting()) {
            return false;
        }

        executeEnd(instance);
        return true;
    }

    public @Nullable LivingEntity getSource() {
        return source;
    }

    public void setSource(LivingEntity source) {
        this.source = Objects.requireNonNull(source);
    }

    public void update() {
        if (isCasting()) {
            executeTick(instance);
        }
    }

    private <S> SkillResponse executeTest(Holder<? extends Skill<S>> skill) {
        if (isCasting()) {
            return SkillResponse.inProgress();
        }

        SkillContext<S> context = createContext(skill);
        return SkillTestCallback.EVENT.invoker().onTest(context) instanceof SkillResponse.Failure failure ?
                failure :
                skill.value().getCondition().test(context);
    }

    private <S> SkillResponse executeCast(Holder<? extends Skill<S>> skill) {
        if (testSkill(skill) instanceof SkillResponse.Failure failure) {
            return failure;
        }

        SkillContext<S> context = createContext(skill);
        return switch (skill.value().getStateFactory().create(context)) {
            case SkillStateCreationResult.Error<S> error -> error.failure();
            case SkillStateCreationResult.Ok<S> ok -> {
                executeStart(createInstance(context, ok.state()));
                yield SkillResponse.success();
            }
        };
    }

    private <S> void executeStart(SkillInstance<S> instance) {
        this.instance = instance;
        instance.getSkill().value().getStartBehavior().execute(instance);

        if (isCasting() && isReadyToComplete(instance)) {
            executeComplete(instance);
        }
    }

    private <S> void executeComplete(SkillInstance<S> instance) {
        instance.getSkill().value().getCompleteBehavior().execute(instance);
        executeEnd(instance);
    }

    private <S> boolean executeCancel(SkillInstance<S> instance) {
        if (!instance.getSkill().value().getCancelPredicate().test(instance)) {
            return false;
        }

        instance.getSkill().value().getCancelBehavior().execute(instance);
        executeEnd(instance);
        return true;
    }

    private <S> boolean executeInterrupt(SkillInstance<S> instance) {
        if (!instance.getSkill().value().getInterruptPredicate().test(instance)) {
            return false;
        }

        instance.getSkill().value().getInterruptBehavior().execute(instance);
        executeEnd(instance);
        return true;
    }

    private <S> void executeEnd(SkillInstance<S> instance) {
        instance.getSkill().value().getEndBehavior().execute(instance);
        instance.getAttributeModifierTracker().clearModifiers();
        this.instance = null;
    }

    private <S> void executeTick(SkillInstance<S> instance) {
        ((SkillInstanceImpl<?>) instance).update();
        instance.getSkill().value().getTickBehavior().execute(instance);

        if (isCasting() && isReadyToComplete(instance)) {
            executeComplete(instance);
        }
    }

    private boolean isReadyToComplete(SkillInstance<?> instance) {
        return instance.isMarkedForCompletion() ||
                (instance.getDurationTicks() >= 0 &&
                        instance.getDurationTicks() <= instance.getElapsedTicks());
    }

    private <S> SkillContext<S> createContext(Holder<? extends Skill<S>> skill) {
        return new SkillContextImpl<>(
                skill,
                getSourceOrThrow()
        );
    }

    private <S> SkillInstance<S> createInstance(SkillContext<S> context, S state) {
        return new SkillInstanceImpl<>(
                context.getSkill(),
                context.getSource(),
                state,
                new AttributeModifierTracker(context.getSource().getAttributes()),
                context.getSkill().value().getDurationProvider().get(context)
        );
    }

    private LivingEntity getSourceOrThrow() {
        return Optional.ofNullable(source)
                .orElseThrow(() -> new IllegalStateException("Source entity is not set"));
    }
}
