package org.zuttomae.sigil.api.skill.predicate

import org.zuttomae.sigil.api.skill.SkillInstance

fun <S : Any> skillInterruptPredicate(block: SkillInstance<out S>.() -> Boolean): SkillInterruptPredicate<S> =
    SkillInterruptPredicate(block)

fun <S : Any> allowedSkillInterruptPredicate(): SkillInterruptPredicate<S> = SkillInterruptPredicate.allowed()

fun <S : Any> deniedSkillInterruptPredicate(): SkillInterruptPredicate<S> = SkillInterruptPredicate.denied()

operator fun <S : Any> SkillInterruptPredicate<S>.plus(other: SkillInterruptPredicate<S>): SkillInterruptPredicate<S> =
    and(other)