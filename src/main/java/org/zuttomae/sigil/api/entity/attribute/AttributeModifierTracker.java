package org.zuttomae.sigil.api.entity.attribute;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class AttributeModifierTracker {
    private final AttributeMap container;
    private final Set<Record> records = new HashSet<>();

    @ApiStatus.Internal
    public AttributeModifierTracker(AttributeMap container) {
        this.container = Objects.requireNonNull(container);
    }

    public void applyModifier(
            Holder<Attribute> attribute,
            double value,
            AttributeModifier.Operation operation
    ) {
        Objects.requireNonNull(attribute);
        Objects.requireNonNull(operation);

        applyModifier(attribute, ResourceLocation.fromNamespaceAndPath("sigil", UUID.randomUUID().toString()), value, operation);
    }

    public void applyModifier(
            Holder<Attribute> attribute,
            ResourceLocation id,
            double value,
            AttributeModifier.Operation operation
    ) {
        Objects.requireNonNull(attribute);
        Objects.requireNonNull(id);
        Objects.requireNonNull(operation);

        applyModifier(attribute, new AttributeModifier(id, value, operation));
    }

    public void applyModifier(
            Holder<Attribute> attribute,
            AttributeModifier modifier
    ) {
        Objects.requireNonNull(attribute);
        Objects.requireNonNull(modifier);

        AttributeInstance instance = container.getInstance(attribute);
        if (instance != null) {
            instance.removeModifier(modifier);
            instance.addTransientModifier(modifier);

            records.add(new Record(attribute, modifier.id()));
        }
    }

    public void removeModifier(
            Holder<Attribute> attribute,
            AttributeModifier modifier
    ) {
        Objects.requireNonNull(attribute);
        Objects.requireNonNull(modifier);

        removeModifier(attribute, modifier.id());
    }

    public void removeModifier(
            Holder<Attribute> attribute,
            ResourceLocation id
    ) {
        Objects.requireNonNull(attribute);
        Objects.requireNonNull(id);

        AttributeInstance instance = container.getInstance(attribute);
        if (instance != null &&
                instance.removeModifier(id)
        ) {
            records.remove(new Record(attribute, id));
        }
    }

    public void clearModifiers() {
        for (Record record : records) {
            var instance = container.getInstance(record.attribute());
            if (instance != null) {
                instance.removeModifier(record.id());
            }
        }

        records.clear();
    }

    private record Record(
            Holder<Attribute> attribute,
            ResourceLocation id
    ) {
        private Record {
            Objects.requireNonNull(attribute);
            Objects.requireNonNull(id);
        }
    }
}
