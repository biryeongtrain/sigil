package org.zuttomae.sigil.api.skill.behavior

import org.zuttomae.sigil.api.skill.SkillInstance

fun <S : Any> skillInterruptBehavior(block: SkillInstance<out S>.() -> Unit): SkillInterruptBehavior<S> =
    SkillInterruptBehavior(block)

fun <S : Any> noOpSkillInterruptBehavior(): SkillInterruptBehavior<S> = SkillInterruptBehavior.noOp()

operator fun <S : Any> SkillInterruptBehavior<S>.plus(other: SkillInterruptBehavior<S>): SkillInterruptBehavior<S> =
    andThen(other)