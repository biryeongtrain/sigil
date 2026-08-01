package org.zuttomae.sigil.api.skill.manager;

import net.minecraft.core.Holder;
import org.zuttomae.sigil.api.skill.Skill;

public interface SkillCooldownManager {
    boolean isCoolingDown(Holder<? extends Skill<?>> skill);

    int getCooldown(Holder<? extends Skill<?>> skill);

    void setCooldown(Holder<? extends Skill<?>> skill, int cooldown);

    void clearCooldowns();
}
