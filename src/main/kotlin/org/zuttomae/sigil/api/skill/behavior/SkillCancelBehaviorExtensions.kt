package org.zuttomae.sigil.api.skill.behavior

import org.zuttomae.sigil.api.skill.SkillInstance

fun <S : Any> skillCancelBehavior(block: SkillInstance<out S>.() -> Unit): SkillCancelBehavior<S> =
    SkillCancelBehavior(block)

fun <S : Any> noOpSkillCancelBehavior(): SkillCancelBehavior<S> = SkillCancelBehavior.noOp()

operator fun <S : Any> SkillCancelBehavior<S>.plus(other: SkillCancelBehavior<S>): SkillCancelBehavior<S> =
    andThen(other)