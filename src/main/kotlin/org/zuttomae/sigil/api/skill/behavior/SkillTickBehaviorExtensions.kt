package org.zuttomae.sigil.api.skill.behavior

import org.zuttomae.sigil.api.skill.SkillInstance

fun <S : Any> skillTickBehavior(block: SkillInstance<out S>.() -> Unit): SkillTickBehavior<S> =
    SkillTickBehavior(block)

fun <S : Any> noOpSkillTickBehavior(): SkillTickBehavior<S> = SkillTickBehavior.noOp()

operator fun <S : Any> SkillTickBehavior<S>.plus(other: SkillTickBehavior<S>): SkillTickBehavior<S> =
    andThen(other)