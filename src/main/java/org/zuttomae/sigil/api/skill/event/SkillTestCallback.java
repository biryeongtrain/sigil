package org.zuttomae.sigil.api.skill.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.zuttomae.sigil.api.skill.SkillContext;
import org.zuttomae.sigil.api.skill.SkillResponse;

@FunctionalInterface
public interface SkillTestCallback {
    Event<SkillTestCallback> EVENT = EventFactory.createArrayBacked(SkillTestCallback.class, callbacks -> context -> {
        for (SkillTestCallback callback : callbacks) {
            if (callback.onTest(context) instanceof SkillResponse.Failure failure) {
                return failure;
            }
        }

        return SkillResponse.success();
    });

    SkillResponse onTest(SkillContext<?> context);
}
