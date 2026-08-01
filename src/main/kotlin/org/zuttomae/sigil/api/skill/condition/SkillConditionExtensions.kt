package org.zuttomae.sigil.api.skill.condition

import org.zuttomae.sigil.api.skill.SkillContext
import org.zuttomae.sigil.api.skill.SkillResponse

fun skillCondition(block: SkillContext<*>.() -> SkillResponse): SkillCondition =
    SkillCondition(block)

fun requireLearnedSkillCondition(): SkillCondition = SkillCondition.requireLearned()

fun requireNoCooldownSkillCondition(): SkillCondition = SkillCondition.requireNoCooldown()

fun requireInGameSkillCondition(): SkillCondition = SkillCondition.requireInGame()

fun defaultSkillCondition(): SkillCondition = SkillCondition.defaultConditions()

operator fun SkillCondition.plus(other: SkillCondition): SkillCondition = and(other)