package org.zuttomae.sigil.api.entity.attribute

fun attributeModifierSet(builderAction: AttributeModifierSet.Builder.() -> Unit): AttributeModifierSet =
    AttributeModifierSet.builder().apply(builderAction).build()