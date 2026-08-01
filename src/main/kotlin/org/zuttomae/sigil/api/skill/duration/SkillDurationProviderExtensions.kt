package org.zuttomae.sigil.api.skill.duration

import org.zuttomae.sigil.api.skill.SkillContext

fun skillDurationProvider(block: SkillContext<*>.() -> Int): SkillDurationProvider =
    SkillDurationProvider(block)

fun constantSkillDurationProvider(durationTicks: Int): SkillDurationProvider =
    SkillDurationProvider.constant(durationTicks)

fun infiniteSkillDurationProvider(): SkillDurationProvider = SkillDurationProvider.infinite()

fun instantSkillDurationProvider(): SkillDurationProvider = SkillDurationProvider.instant()