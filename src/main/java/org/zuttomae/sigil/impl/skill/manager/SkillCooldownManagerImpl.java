package org.zuttomae.sigil.impl.skill.manager;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.zuttomae.sigil.api.skill.Skill;
import org.zuttomae.sigil.api.skill.SkillContext;
import org.zuttomae.sigil.api.skill.event.SkillCooldownEndedCallback;
import org.zuttomae.sigil.api.skill.event.SkillCooldownStartedCallback;
import org.zuttomae.sigil.api.skill.manager.SkillCooldownManager;
import org.zuttomae.sigil.api.skill.registry.SkillRegistries;
import org.zuttomae.sigil.impl.skill.SkillContextImpl;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class SkillCooldownManagerImpl implements SkillCooldownManager {
    @SuppressWarnings("unchecked")
    public static final Codec<SkillCooldownManager> CODEC = RecordCodecBuilder.<SkillCooldownManagerImpl>create(instance ->
            instance.group(
                            Codec.unboundedMap(SkillRegistries.SKILL.holderByNameCodec(), Codec.INT)
                                    .fieldOf("cooldowns")
                                    .forGetter(cooldownManager ->
                                            Map.copyOf(
                                                    (Map<Holder<Skill<?>>, Integer>) (Map<?, ?>) cooldownManager.cooldowns
                                            )
                                    )
                    )
                    .apply(instance, SkillCooldownManagerImpl::new)
    ).xmap(
            cooldownManager -> cooldownManager,
            cooldownManager -> (SkillCooldownManagerImpl) cooldownManager
    );

    private final Map<Holder<? extends Skill<?>>, Integer> cooldowns;
    private @Nullable LivingEntity source = null;

    private SkillCooldownManagerImpl(
            Map<? extends Holder<? extends Skill<?>>, ? extends Integer> cooldowns
    ) {
        this.cooldowns = new ConcurrentHashMap<>(Objects.requireNonNull(cooldowns));
    }

    public SkillCooldownManagerImpl() {
        this(Map.of());
    }

    @Override
    public boolean isCoolingDown(Holder<? extends Skill<?>> skill) {
        Objects.requireNonNull(skill);
        return getCooldown(skill) > 0;
    }

    @Override
    public int getCooldown(Holder<? extends Skill<?>> skill) {
        Objects.requireNonNull(skill);
        return cooldowns.getOrDefault(skill, 0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setCooldown(Holder<? extends Skill<?>> skill, int cooldown) {
        Objects.requireNonNull(skill);
        if (cooldown > 0) {
            if (cooldowns.put(skill, cooldown) == null) {
                handleCooldownStarted((Holder<? extends Skill<Object>>) skill);
            }
        } else {
            if (cooldowns.remove(skill) != null) {
                handleCooldownEnded((Holder<? extends Skill<Object>>) skill);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void clearCooldowns() {
        Iterator<Map.Entry<Holder<? extends Skill<?>>, Integer>> iterator = cooldowns.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Holder<? extends Skill<?>>, Integer> entry = iterator.next();
            iterator.remove();
            handleCooldownEnded((Holder<? extends Skill<Object>>) entry.getKey());
        }
    }

    public @Nullable LivingEntity getSource() {
        return source;
    }

    public void setSource(LivingEntity source) {
        this.source = Objects.requireNonNull(source);
    }

    @SuppressWarnings("unchecked")
    public void update() {
        Iterator<Map.Entry<Holder<? extends Skill<?>>, Integer>> iterator = cooldowns.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Holder<? extends Skill<?>>, Integer> entry = iterator.next();
            if (entry.getValue() > 1) {
                entry.setValue(entry.getValue() - 1);
            } else {
                iterator.remove();
                handleCooldownEnded((Holder<? extends Skill<Object>>) entry.getKey());
            }
        }
    }

    private <S> void handleCooldownStarted(Holder<? extends Skill<S>> skill) {
        SkillContext<?> context = createContext(skill);

        SkillCooldownStartedCallback.EVENT.invoker().onCooldownStarted(context);
    }

    private <S> void handleCooldownEnded(Holder<? extends Skill<S>> skill) {
        SkillContext<?> context = createContext(skill);

        SkillCooldownEndedCallback.EVENT.invoker().onCooldownEnded(context);
    }

    private <S> SkillContext<S> createContext(Holder<? extends Skill<S>> skill) {
        return new SkillContextImpl<>(skill, getSourceOrThrow());
    }

    private LivingEntity getSourceOrThrow() {
        return Optional.ofNullable(source)
                .orElseThrow(() -> new IllegalStateException("Source entity is not set"));
    }
}
