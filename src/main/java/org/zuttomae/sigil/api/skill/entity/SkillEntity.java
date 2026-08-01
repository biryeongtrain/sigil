package org.zuttomae.sigil.api.skill.entity;

import org.zuttomae.sigil.api.skill.manager.SkillContainer;
import org.zuttomae.sigil.api.skill.manager.SkillCooldownManager;
import org.zuttomae.sigil.api.skill.manager.SkillManager;

public interface SkillEntity {
    default SkillContainer getSkillContainer() {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default SkillCooldownManager getSkillCooldownManager() {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default SkillManager getSkillManager() {
        throw new UnsupportedOperationException("Implemented via mixin");
    }
}
