package org.zuttomae.sigil.api.skill.manager;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import org.zuttomae.sigil.api.skill.Skill;

import java.util.Collection;
import java.util.Set;

public interface SkillContainer {
    Set<? extends Holder<? extends Skill<?>>> getSkills();

    Set<? extends Holder<? extends Skill<?>>> getSkills(ResourceLocation source);

    Set<? extends Holder<? extends Skill<?>>> getPermanentSkills();

    Set<ResourceLocation> getSources();

    boolean hasSkill(Holder<? extends Skill<?>> skill);

    boolean hasSkill(Holder<? extends Skill<?>> skill, ResourceLocation source);

    boolean addTransientSkill(Holder<? extends Skill<?>> skill, ResourceLocation source);

    int addTransientSkills(Collection<? extends Holder<? extends Skill<?>>> skills, ResourceLocation source);

    boolean addPermanentSkill(Holder<? extends Skill<?>> skill, ResourceLocation source);

    int addPermanentSkills(Collection<? extends Holder<? extends Skill<?>>> skills, ResourceLocation source);

    boolean removeSkill(Holder<? extends Skill<?>> skill, ResourceLocation source);

    boolean removeSkill(Holder<? extends Skill<?>> skill);

    int removeSkills(Collection<? extends Holder<? extends Skill<?>>> skills, ResourceLocation source);

    int removeSkills(Collection<? extends Holder<? extends Skill<?>>> skills);

    int removeSkills(ResourceLocation source);
}
