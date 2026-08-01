package org.zuttomae.sigil.api.skill.behavior

import org.zuttomae.sigil.api.skill.SkillInstance

fun <S : Any> skillEndBehavior(block: SkillInstance<out S>.() -> Unit): SkillEndBehavior<S> =
    SkillEndBehavior(block)

fun <S : Any> noOpSkillEndBehavior(): SkillEndBehavior<S> = SkillEndBehavior.noOp()

operator fun <S : Any> SkillEndBehavior<S>.plus(other: SkillEndBehavior<S>): SkillEndBehavior<S> =
    andThen(other)