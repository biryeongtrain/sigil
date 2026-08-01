package org.zuttomae.sigil.api.skill.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.zuttomae.sigil.api.skill.SkillContext;

@FunctionalInterface
public interface SkillAddedCallback {
    Event<SkillAddedCallback> EVENT = EventFactory.createArrayBacked(SkillAddedCallback.class, callbacks -> context -> {
        for (SkillAddedCallback callback : callbacks) {
            callback.onAdded(context);
        }
    });

    void onAdded(SkillContext<?> context);
}
