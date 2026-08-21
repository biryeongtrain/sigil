package org.zuttomae.sigil.api.skill

import net.minecraft.network.chat.Component

fun success(): SkillResponse.Success =
    SkillResponse.success()

fun failure(reason: Component): SkillResponse.Failure =
    SkillResponse.failure(reason)

fun inProgress(): SkillResponse.Failure =
    SkillResponse.inProgress()

fun notLearned(): SkillResponse.Failure =
    SkillResponse.notLearned()

fun cooldown(): SkillResponse.Failure =
    SkillResponse.cooldown()

fun noTarget(): SkillResponse.Failure =
    SkillResponse.noTarget()

fun unavailable(): SkillResponse.Failure =
    SkillResponse.unavailable()