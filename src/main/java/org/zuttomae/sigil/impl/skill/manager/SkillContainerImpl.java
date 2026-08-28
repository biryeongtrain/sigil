package org.zuttomae.sigil.impl.skill.manager;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.zuttomae.sigil.api.skill.Skill;
import org.zuttomae.sigil.api.skill.SkillContext;
import org.zuttomae.sigil.api.skill.event.SkillAddedCallback;
import org.zuttomae.sigil.api.skill.event.SkillLoadedCallback;
import org.zuttomae.sigil.api.skill.event.SkillRemovedCallback;
import org.zuttomae.sigil.api.skill.manager.SkillContainer;
import org.zuttomae.sigil.api.skill.registry.SkillRegistries;
import org.zuttomae.sigil.impl.skill.SkillContextImpl;

import java.util.*;

public final class SkillContainerImpl implements SkillContainer {
    @SuppressWarnings("unchecked")
    public static final Codec<SkillContainer> CODEC = Codec.unboundedMap(
                    Identifier.CODEC,
                    SkillRegistries.SKILL.holderByNameCodec()
                            .listOf()
                            .<Set<Holder<Skill<?>>>>xmap(LinkedHashSet::new, List::copyOf)
            )
            .fieldOf("skills")
            .codec()
            .xmap(
                    SkillContainerImpl::new,
                    container -> (Map<Identifier, Set<Holder<Skill<?>>>>) (Map<?, ?>) Multimaps.asMap(
                            ((SkillContainerImpl) container).permanentSkillsBySource
                    )
            );

    private final SetMultimap<Identifier, Holder<? extends Skill<?>>> skillsBySource = LinkedHashMultimap.create();
    private final SetMultimap<Identifier, Holder<? extends Skill<?>>> permanentSkillsBySource = LinkedHashMultimap.create();
    private @Nullable LivingEntity owner = null;

    private SkillContainerImpl(
            Map<Identifier, ? extends Collection<? extends Holder<? extends Skill<?>>>> skills
    ) {
        Objects.requireNonNull(skills);

        skills.forEach(skillsBySource::putAll);
        skills.forEach(permanentSkillsBySource::putAll);
    }

    public SkillContainerImpl() {
        this(Map.of());
    }

    @Override
    public Set<? extends Holder<? extends Skill<?>>> getSkills() {
        return Set.copyOf(skillsBySource.values());
    }

    @Override
    public Set<? extends Holder<? extends Skill<?>>> getSkills(Identifier source) {
        Objects.requireNonNull(source);

        return Set.copyOf(skillsBySource.get(source));
    }

    @Override
    public Set<? extends Holder<? extends Skill<?>>> getPermanentSkills() {
        return Set.copyOf(permanentSkillsBySource.values());
    }

    @Override
    public Set<Identifier> getSources() {
        return Set.copyOf(skillsBySource.keySet());
    }

    @Override
    public boolean hasSkill(Holder<? extends Skill<?>> skill) {
        Objects.requireNonNull(skill);

        return skillsBySource.containsValue(skill);
    }

    @Override
    public boolean hasSkill(Holder<? extends Skill<?>> skill, Identifier source) {
        Objects.requireNonNull(skill);
        Objects.requireNonNull(source);

        return skillsBySource.containsEntry(source, skill);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean addTransientSkill(Holder<? extends Skill<?>> skill, Identifier source) {
        Objects.requireNonNull(skill);
        Objects.requireNonNull(source);

        boolean firstGrant = !skillsBySource.containsValue(skill);
        if (!skillsBySource.put(source, skill)) {
            return false;
        }

        if (firstGrant) {
            handleSkillAdded((Holder<? extends Skill<Object>>) skill);
        }

        return true;
    }

    @Override
    public int addTransientSkills(Collection<? extends Holder<? extends Skill<?>>> skills, Identifier source) {
        Objects.requireNonNull(skills);

        int count = 0;
        for (Holder<? extends Skill<?>> skill : skills) {
            if (addTransientSkill(skill, source)) {
                count++;
            }
        }

        return count;
    }

    @Override
    public boolean addPermanentSkill(Holder<? extends Skill<?>> skill, Identifier source) {
        if (!addTransientSkill(skill, source)) {
            return false;
        }

        permanentSkillsBySource.put(source, skill);
        return true;
    }

    @Override
    public int addPermanentSkills(Collection<? extends Holder<? extends Skill<?>>> skills, Identifier source) {
        Objects.requireNonNull(skills);

        int count = 0;
        for (Holder<? extends Skill<?>> skill : skills) {
            if (addPermanentSkill(skill, source)) {
                count++;
            }
        }

        return count;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean removeSkill(Holder<? extends Skill<?>> skill, Identifier source) {
        Objects.requireNonNull(skill);
        Objects.requireNonNull(source);

        if (!skillsBySource.remove(source, skill)) {
            return false;
        }

        permanentSkillsBySource.remove(source, skill);
        if (!skillsBySource.containsValue(skill)) {
            handleSkillRemoved((Holder<? extends Skill<Object>>) skill);
        }

        return true;
    }

    @Override
    public boolean removeSkill(Holder<? extends Skill<?>> skill) {
        Objects.requireNonNull(skill);

        boolean removed = false;
        for (Identifier source : List.copyOf(skillsBySource.keySet())) {
            removed |= removeSkill(skill, source);
        }

        return removed;
    }

    @Override
    public int removeSkills(Collection<? extends Holder<? extends Skill<?>>> skills, Identifier source) {
        Objects.requireNonNull(skills);
        Objects.requireNonNull(source);

        int count = 0;
        for (Holder<? extends Skill<?>> skill : skills) {
            if (removeSkill(skill, source)) {
                count++;
            }
        }

        return count;
    }

    @Override
    public int removeSkills(Collection<? extends Holder<? extends Skill<?>>> skills) {
        Objects.requireNonNull(skills);

        int count = 0;
        for (Holder<? extends Skill<?>> skill : skills) {
            if (removeSkill(skill)) {
                count++;
            }
        }

        return count;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int removeSkills(Identifier source) {
        Objects.requireNonNull(source);

        Set<Holder<? extends Skill<?>>> removed = skillsBySource.removeAll(source);
        permanentSkillsBySource.removeAll(source);
        for (Holder<? extends Skill<?>> skill : removed) {
            if (!skillsBySource.containsValue(skill)) {
                handleSkillRemoved((Holder<? extends Skill<Object>>) skill);
            }
        }

        return removed.size();
    }

    public @Nullable LivingEntity getOwner() {
        return owner;
    }

    public void setOwner(LivingEntity owner) {
        this.owner = Objects.requireNonNull(owner);
    }

    @SuppressWarnings("unchecked")
    public void refresh() {
        for (Holder<? extends Skill<?>> skill : getSkills()) {
            handleSkillLoaded((Holder<? extends Skill<Object>>) skill);
        }
    }

    private <S> void handleSkillAdded(Holder<? extends Skill<S>> skill) {
        SkillContext<S> context = createContext(skill);

        skill.value().getAttributeModifiers().applyTemporaryModifiers(getOwnerOrThrow().getAttributes());
        SkillAddedCallback.EVENT.invoker().onAdded(context);
    }

    private <S> void handleSkillRemoved(Holder<? extends Skill<S>> skill) {
        SkillContext<S> context = createContext(skill);

        skill.value().getAttributeModifiers().removeModifiers(getOwnerOrThrow().getAttributes());
        SkillRemovedCallback.EVENT.invoker().onRemoved(context);
    }

    private <S> void handleSkillLoaded(Holder<? extends Skill<S>> skill) {
        SkillContext<S> context = createContext(skill);

        skill.value().getAttributeModifiers().applyTemporaryModifiers(getOwnerOrThrow().getAttributes());
        SkillLoadedCallback.EVENT.invoker().onLoaded(context);
    }

    private <S> SkillContext<S> createContext(Holder<? extends Skill<S>> skill) {
        return new SkillContextImpl<>(skill, getOwnerOrThrow());
    }

    private LivingEntity getOwnerOrThrow() {
        return Optional.ofNullable(owner)
                .orElseThrow(() -> new IllegalStateException("Owner entity is not set"));
    }
}
