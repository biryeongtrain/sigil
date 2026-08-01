package org.zuttomae.sigil.impl.skill;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import org.zuttomae.sigil.api.entity.attribute.AttributeModifierTracker;
import org.zuttomae.sigil.api.skill.Skill;
import org.zuttomae.sigil.api.skill.SkillInstance;

import java.util.Objects;

public final class SkillInstanceImpl<S> implements SkillInstance<S> {
    private final Holder<? extends Skill<S>> skill;
    private final LivingEntity source;
    private S state;

    private final AttributeModifierTracker attributeModifierTracker;
    private int durationTicks;

    private boolean markedForCompletion;
    private int elapsedTicks;

    public SkillInstanceImpl(
            Holder<? extends Skill<S>> skill,
            LivingEntity source, S state,
            AttributeModifierTracker attributeModifierTracker,
            int durationTicks
    ) {
        this.skill = Objects.requireNonNull(skill);
        this.source = Objects.requireNonNull(source);
        this.state = Objects.requireNonNull(state);
        this.attributeModifierTracker = Objects.requireNonNull(attributeModifierTracker);
        this.durationTicks = durationTicks;
    }

    @Override
    public Holder<? extends Skill<S>> getSkill() {
        return skill;
    }

    @Override
    public LivingEntity getSource() {
        return source;
    }

    @Override
    public S getState() {
        return state;
    }

    @Override
    public void setState(S state) {
        this.state = Objects.requireNonNull(state);
    }

    @Override
    public AttributeModifierTracker getAttributeModifierTracker() {
        return attributeModifierTracker;
    }

    @Override
    public int getDurationTicks() {
        return durationTicks;
    }

    @Override
    public void setDurationTicks(int durationTicks) {
        this.durationTicks = durationTicks;
    }

    @Override
    public boolean isMarkedForCompletion() {
        return markedForCompletion;
    }

    @Override
    public void setMarkedForCompletion(boolean markedForCompletion) {
        this.markedForCompletion = markedForCompletion;
    }

    @Override
    public int getElapsedTicks() {
        return elapsedTicks;
    }

    @Override
    public void setElapsedTicks(int elapsedTicks) {
        this.elapsedTicks = elapsedTicks;
    }

    public void update() {
        elapsedTicks++;
    }

    @Override
    public String toString() {
        return "SkillInstanceImpl{" +
                "skill=" + skill +
                ", source=" + source +
                ", state=" + state +
                ", attributeModifierTracker=" + attributeModifierTracker +
                ", durationTicks=" + durationTicks +
                ", markedForCompletion=" + markedForCompletion +
                ", elapsedTicks=" + elapsedTicks +
                '}';
    }
}
