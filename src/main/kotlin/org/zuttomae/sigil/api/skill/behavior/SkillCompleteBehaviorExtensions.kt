package org.zuttomae.sigil.api.skill.behavior

import org.zuttomae.sigil.api.skill.SkillInstance

fun <S : Any> skillCompleteBehavior(block: SkillInstance<out S>.() -> Unit): SkillCompleteBehavior<S> =
    SkillCompleteBehavior(block)

fun <S : Any> noOpSkillCompleteBehavior(): SkillCompleteBehavior<S> = SkillCompleteBehavior.noOp()

operator fun <S : Any> SkillCompleteBehavior<S>.plus(other: SkillCompleteBehavior<S>): SkillCompleteBehavior<S> =
    andThen(other)