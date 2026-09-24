package org.zuttomae.sigil.api.entity.attribute;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public final class AttributeModifierSet {
    private static final AttributeModifierSet EMPTY = new AttributeModifierSet();

    private final Map<? extends Holder<Attribute>, ? extends List<? extends AttributeModifier>> attributeToModifiers;

    private AttributeModifierSet(Map<? extends Holder<Attribute>, ? extends List<? extends AttributeModifier>> attributeToModifiers) {
        this.attributeToModifiers = Objects.requireNonNull(attributeToModifiers).entrySet()
                .stream()
                .collect(
                        Collectors.toUnmodifiableMap(
                                Map.Entry::getKey,
                                entry -> List.copyOf(entry.getValue())
                        )
                );
    }

    private AttributeModifierSet() {
        this.attributeToModifiers = Map.of();
    }

    public static AttributeModifierSet empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void applyTemporaryModifiers(AttributeMap container) {
        Objects.requireNonNull(container);
        processModifiers(
                container,
                (instance, modifier) -> {
                    instance.removeModifier(modifier);
                    instance.addTransientModifier(modifier);
                }
        );
    }

    public void applyPersistentModifiers(AttributeMap container) {
        Objects.requireNonNull(container);
        processModifiers(
                container,
                (instance, modifier) -> {
                    instance.removeModifier(modifier);
                    instance.addPermanentModifier(modifier);
                }
        );
    }

    public void removeModifiers(AttributeMap container) {
        Objects.requireNonNull(container);
        processModifiers(
                container,
                AttributeInstance::removeModifier
        );
    }

    private void processModifiers(
            AttributeMap container,
            BiConsumer<? super AttributeInstance, ? super AttributeModifier> consumer
    ) {
        for (var entry : attributeToModifiers.entrySet()) {
            var instance = container.getInstance(entry.getKey());
            if (instance != null) {
                for (AttributeModifier modifier : entry.getValue()) {
                    consumer.accept(instance, modifier);
                }
            }
        }
    }

    public static final class Builder {
        private final Map<Holder<Attribute>, List<AttributeModifier>> attributeToModifiers = new HashMap<>();

        private Builder() {
        }

        public Builder addModifier(
                Holder<Attribute> attribute,
                double value,
                AttributeModifier.Operation operation
        ) {
            Objects.requireNonNull(attribute);
            Objects.requireNonNull(operation);
            return addModifier(attribute, ResourceLocation.fromNamespaceAndPath("sigil", UUID.randomUUID().toString()), value, operation);
        }

        public Builder addModifier(
                Holder<Attribute> attribute,
                ResourceLocation id,
                double value,
                AttributeModifier.Operation operation
        ) {
            Objects.requireNonNull(attribute);
            Objects.requireNonNull(id);
            Objects.requireNonNull(operation);
            return addModifier(attribute, new AttributeModifier(id, value, operation));
        }

        public Builder addModifier(
                Holder<Attribute> attribute,
                AttributeModifier modifier
        ) {
            Objects.requireNonNull(attribute);
            Objects.requireNonNull(modifier);
            attributeToModifiers.computeIfAbsent(attribute, key -> new ArrayList<>()).add(modifier);
            return this;
        }

        public AttributeModifierSet build() {
            return new AttributeModifierSet(attributeToModifiers);
        }
    }
}
