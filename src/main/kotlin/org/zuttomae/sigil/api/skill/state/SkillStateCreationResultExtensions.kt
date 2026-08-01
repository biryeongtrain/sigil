package org.zuttomae.sigil.api.skill.state

import org.zuttomae.sigil.api.skill.SkillResponse

fun <S> ok(state: S): SkillStateCreationResult.Ok<S> =
    SkillStateCreationResult.Ok<S>(state)

fun <S> error(failure: SkillResponse.Failure): SkillStateCreationResult.Error<S> =
    SkillStateCreationResult.Error<S>(failure)