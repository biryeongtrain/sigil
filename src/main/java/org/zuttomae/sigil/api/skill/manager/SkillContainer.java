package org.zuttomae.sigil.api.skill.manager;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import org.zuttomae.sigil.api.skill.Skill;

import java.util.Collection;
import java.util.Set;

public interface SkillContainer {
    Set<? extends Holder<? extends Skill<?>>> getSkills();

    Set<? extends Holder<? extends Skill<?>>> getSkills(Identifier source);

    Set<? extends Holder<? extends Skill<?>>> getPermanentSkills();

    Set<Identifier> getSources();

    boolean hasSkill(Holder<? extends Skill<?>> skill);

    boolean hasSkill(Holder<? extends Skill<?>> skill, Identifier source);

    boolean addTransientSkill(Holder<? extends Skill<?>> skill, Identifier source);

    int addTransientSkills(Collection<? extends Holder<? extends Skill<?>>> skills, Identifier source);

    boolean addPermanentSkill(Holder<? extends Skill<?>> skill, Identifier source);

    int addPermanentSkills(Collection<? extends Holder<? extends Skill<?>>> skills, Identifier source);

    boolean removeSkill(Holder<? extends Skill<?>> skill, Identifier source);

    boolean removeSkill(Holder<? extends Skill<?>> skill);

    int removeSkills(Collection<? extends Holder<? extends Skill<?>>> skills, Identifier source);

    int removeSkills(Collection<? extends Holder<? extends Skill<?>>> skills);

    int removeSkills(Identifier source);
}
