package org.zuttomae.sigil.api.skill.behavior

import org.zuttomae.sigil.api.skill.SkillInstance

fun <S : Any> skillStartBehavior(block: SkillInstance<out S>.() -> Unit): SkillStartBehavior<S> =
    SkillStartBehavior(block)

fun <S : Any> noOpSkillStartBehavior(): SkillStartBehavior<S> = SkillStartBehavior.noOp()

operator fun <S : Any> SkillStartBehavior<S>.plus(other: SkillStartBehavior<S>): SkillStartBehavior<S> =
    andThen(other)