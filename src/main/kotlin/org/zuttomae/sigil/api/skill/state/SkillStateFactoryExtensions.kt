package org.zuttomae.sigil.api.skill.state

import org.zuttomae.sigil.api.skill.SkillContext
import org.zuttomae.sigil.api.skill.SkillResponse

fun <S : Any> skillStateFactory(block: SkillContext<*>.() -> SkillStateCreationResult<S>): SkillStateFactory<S> =
    SkillStateFactory(block)

fun <S : Any> alwaysOkSkillStateFactory(block: SkillContext<*>.() -> S): SkillStateFactory<S> =
    SkillStateFactory.alwaysOk(block)

fun <S : Any> alwaysErrorSkillStateFactory(block: SkillContext<*>.() -> SkillResponse.Failure): SkillStateFactory<S> =
    SkillStateFactory.alwaysError(block)

fun <S : Any> constantSkillStateFactory(state: S): SkillStateFactory<S> =
    SkillStateFactory.constant(state)