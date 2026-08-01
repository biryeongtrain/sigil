package org.zuttomae.sigil.api.skill;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import org.zuttomae.sigil.api.skill.manager.SkillContainer;
import org.zuttomae.sigil.api.skill.manager.SkillCooldownManager;
import org.zuttomae.sigil.api.skill.manager.SkillManager;

public interface SkillContext<S> {
    Holder<? extends Skill<S>> getSkill();

    LivingEntity getSource();

    default SkillContainer getSkillContainer() {
        return getSource().getSkillContainer();
    }

    default SkillCooldownManager getSkillCooldownManager() {
        return getSource().getSkillCooldownManager();
    }

    default SkillManager getSkillManager() {
        return getSource().getSkillManager();
    }
}
