package org.zuttomae.sigil.api.skill;

import org.zuttomae.sigil.api.entity.attribute.AttributeModifierTracker;

public interface SkillInstance<S> extends SkillContext<S> {
    S getState();

    void setState(S state);

    AttributeModifierTracker getAttributeModifierTracker();

    int getDurationTicks();

    void setDurationTicks(int durationTicks);

    boolean isMarkedForCompletion();

    void setMarkedForCompletion(boolean markedForCompletion);

    int getElapsedTicks();

    void setElapsedTicks(int elapsedTicks);

    default int getRemainingTicks() {
        int durationTicks = getDurationTicks();
        return durationTicks < 0 ?
                -1 :
                Math.max(0, durationTicks - getElapsedTicks());
    }

    default float getProgress() {
        int durationTicks = getDurationTicks();
        return durationTicks <= 0 ?
                0.0f :
                Math.clamp((float) getElapsedTicks() / durationTicks, 0.0f, 1.0f);
    }
}
