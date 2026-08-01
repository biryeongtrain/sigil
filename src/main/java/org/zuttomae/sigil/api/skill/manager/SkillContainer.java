package org.zuttomae.sigil.api.skill.manager;

import net.minecraft.core.Holder;
import org.zuttomae.sigil.api.skill.Skill;

import java.util.Collection;
import java.util.Set;

public interface SkillContainer {
    Set<? extends Holder<? extends Skill<?>>> getSkills();

    boolean hasSkill(Holder<? extends Skill<?>> skill);

    boolean addSkill(Holder<? extends Skill<?>> skill);

    int addSkills(Collection<? extends Holder<? extends Skill<?>>> skills);

    boolean removeSkill(Holder<? extends Skill<?>> skill);

    int removeSkills(Collection<? extends Holder<? extends Skill<?>>> skills);
}
