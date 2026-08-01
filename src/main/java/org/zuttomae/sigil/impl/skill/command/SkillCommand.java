package org.zuttomae.sigil.impl.skill.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.zuttomae.sigil.api.skill.Skill;
import org.zuttomae.sigil.api.skill.SkillResponse;
import org.zuttomae.sigil.api.skill.registry.SkillRegistries;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public final class SkillCommand {
    private static final DynamicCommandExceptionType ENTITY_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            name -> Component.translatableEscape("commands.skill.failed.entity", name)
    );
    private static final DynamicCommandExceptionType NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(
            id -> Component.translatableEscape("skill.notFound", id)
    );
    private static final SimpleCommandExceptionType ADD_FAILED_EXCEPTION = new SimpleCommandExceptionType(
            Component.translatable("commands.skill.add.failed")
    );
    private static final SimpleCommandExceptionType REMOVE_FAILED_EXCEPTION = new SimpleCommandExceptionType(
            Component.translatable("commands.skill.remove.failed")
    );
    private static final SimpleCommandExceptionType CANCEL_FAILED_EXCEPTION = new SimpleCommandExceptionType(
            Component.translatable("commands.skill.cancel.failed")
    );
    private static final SimpleCommandExceptionType INTERRUPT_FAILED_EXCEPTION = new SimpleCommandExceptionType(
            Component.translatable("commands.skill.interrupt.failed")
    );
    private static final SimpleCommandExceptionType TERMINATE_FAILED_EXCEPTION = new SimpleCommandExceptionType(
            Component.translatable("commands.skill.terminate.failed")
    );
    private static final SimpleCommandExceptionType COOLDOWN_SET_FAILED_EXCEPTION = new SimpleCommandExceptionType(
            Component.translatable("commands.skill.cooldown.set.failed")
    );
    private static final SimpleCommandExceptionType COOLDOWN_CLEAR_FAILED_EXCEPTION = new SimpleCommandExceptionType(
            Component.translatable("commands.skill.cooldown.clear.failed")
    );

    private SkillCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(argumentSkill());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> argumentSkill() {
        return Commands.literal("skill")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(argumentAdd())
                .then(argumentRemove())
                .then(argumentTest())
                .then(argumentCast())
                .then(argumentCancel())
                .then(argumentInterrupt())
                .then(argumentTerminate())
                .then(argumentCooldown());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> argumentAdd() {
        return Commands.literal("add")
                .then(
                        Commands.argument("skill", IdentifierArgument.id())
                                .suggests((_, builder) ->
                                        SharedSuggestionProvider.suggestResource(SkillRegistries.SKILL.keySet(), builder)
                                )
                                .executes(context ->
                                        executeAdd(
                                                context.getSource(),
                                                List.of(context.getSource().getEntityOrException()),
                                                List.of(getSkill(IdentifierArgument.getId(context, "skill")))
                                        )
                                )
                                .then(
                                        Commands.argument("targets", EntityArgument.entities())
                                                .executes(context ->
                                                        executeAdd(
                                                                context.getSource(),
                                                                EntityArgument.getEntities(context, "targets"),
                                                                List.of(getSkill(IdentifierArgument.getId(context, "skill")))
                                                        )
                                                )
                                )
                )
                .then(
                        Commands.literal("*")
                                .executes(context ->
                                        executeAdd(
                                                context.getSource(),
                                                List.of(context.getSource().getEntityOrException()),
                                                SkillRegistries.SKILL.listElements().toList()
                                        )
                                )
                                .then(
                                        Commands.argument("targets", EntityArgument.entities())
                                                .executes(context ->
                                                        executeAdd(
                                                                context.getSource(),
                                                                EntityArgument.getEntities(context, "targets"),
                                                                SkillRegistries.SKILL.listElements().toList()
                                                        )
                                                )
                                )
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> argumentRemove() {
        return Commands.literal("remove")
                .then(
                        Commands.argument("skill", IdentifierArgument.id())
                                .suggests((_, builder) ->
                                        SharedSuggestionProvider.suggestResource(SkillRegistries.SKILL.keySet(), builder)
                                )
                                .executes(context ->
                                        executeRemove(
                                                context.getSource(),
                                                List.of(context.getSource().getEntityOrException()),
                                                List.of(getSkill(IdentifierArgument.getId(context, "skill")))
                                        )
                                )
                                .then(
                                        Commands.argument("targets", EntityArgument.entities())
                                                .executes(context ->
                                                        executeRemove(
                                                                context.getSource(),
                                                                EntityArgument.getEntities(context, "targets"),
                                                                List.of(getSkill(IdentifierArgument.getId(context, "skill")))
                                                        )
                                                )
                                )
                )
                .then(
                        Commands.literal("*")
                                .executes(context ->
                                        executeRemove(
                                                context.getSource(),
                                                List.of(context.getSource().getEntityOrException()),
                                                SkillRegistries.SKILL.listElements().toList()
                                        )
                                )
                                .then(
                                        Commands.argument("targets", EntityArgument.entities())
                                                .executes(context ->
                                                        executeRemove(
                                                                context.getSource(),
                                                                EntityArgument.getEntities(context, "targets"),
                                                                SkillRegistries.SKILL.listElements().toList()
                                                        )
                                                )
                                )
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> argumentTest() {
        return Commands.literal("test")
                .then(
                        Commands.argument("skill", IdentifierArgument.id())
                                .suggests((_, builder) ->
                                        SharedSuggestionProvider.suggestResource(SkillRegistries.SKILL.keySet(), builder)
                                )
                                .executes(context ->
                                        executeTest(
                                                context.getSource(),
                                                context.getSource().getEntityOrException(),
                                                getSkill(IdentifierArgument.getId(context, "skill"))
                                        )
                                )
                                .then(
                                        Commands.argument("target", EntityArgument.entity())
                                                .executes(context ->
                                                        executeTest(
                                                                context.getSource(),
                                                                EntityArgument.getEntity(context, "target"),
                                                                getSkill(IdentifierArgument.getId(context, "skill"))
                                                        )
                                                )
                                )
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> argumentCast() {
        return Commands.literal("cast")
                .then(
                        Commands.argument("skill", IdentifierArgument.id())
                                .suggests((_, builder) ->
                                        SharedSuggestionProvider.suggestResource(SkillRegistries.SKILL.keySet(), builder)
                                )
                                .executes(context ->
                                        executeCast(
                                                context.getSource(),
                                                context.getSource().getEntityOrException(),
                                                getSkill(IdentifierArgument.getId(context, "skill"))
                                        )
                                )
                                .then(
                                        Commands.argument("target", EntityArgument.entity())
                                                .executes(context ->
                                                        executeCast(
                                                                context.getSource(),
                                                                EntityArgument.getEntity(context, "target"),
                                                                getSkill(IdentifierArgument.getId(context, "skill"))
                                                        )
                                                )
                                )
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> argumentCancel() {
        return Commands.literal("cancel")
                .executes(context ->
                        executeCancel(
                                context.getSource(),
                                List.of(context.getSource().getEntityOrException())
                        )
                )
                .then(
                        Commands.argument("targets", EntityArgument.entities())
                                .executes(context ->
                                        executeCancel(
                                                context.getSource(),
                                                EntityArgument.getEntities(context, "targets")
                                        )
                                )
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> argumentInterrupt() {
        return Commands.literal("interrupt")
                .executes(context ->
                        executeInterrupt(
                                context.getSource(),
                                List.of(context.getSource().getEntityOrException())
                        )
                )
                .then(
                        Commands.argument("targets", EntityArgument.entities())
                                .executes(context ->
                                        executeInterrupt(
                                                context.getSource(),
                                                EntityArgument.getEntities(context, "targets")
                                        )
                                )
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> argumentTerminate() {
        return Commands.literal("terminate")
                .executes(context ->
                        executeTerminate(
                                context.getSource(),
                                List.of(context.getSource().getEntityOrException())
                        )
                )
                .then(
                        Commands.argument("targets", EntityArgument.entities())
                                .executes(context ->
                                        executeTerminate(
                                                context.getSource(),
                                                EntityArgument.getEntities(context, "targets")
                                        )
                                )
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> argumentCooldown() {
        return Commands.literal("cooldown")
                .then(argumentCooldownSet())
                .then(argumentCooldownGet())
                .then(argumentCooldownClear());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> argumentCooldownGet() {
        return Commands.literal("get")
                .then(
                        Commands.argument("skill", IdentifierArgument.id())
                                .suggests((_, builder) ->
                                        SharedSuggestionProvider.suggestResource(SkillRegistries.SKILL.keySet(), builder)
                                )
                                .executes(context ->
                                        executeCooldownGet(
                                                context.getSource(),
                                                context.getSource().getEntityOrException(),
                                                getSkill(IdentifierArgument.getId(context, "skill"))
                                        )
                                )
                                .then(
                                        Commands.argument("target", EntityArgument.entity())
                                                .executes(context ->
                                                        executeCooldownGet(
                                                                context.getSource(),
                                                                EntityArgument.getEntity(context, "target"),
                                                                getSkill(IdentifierArgument.getId(context, "skill"))
                                                        )
                                                )
                                )
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> argumentCooldownSet() {
        return Commands.literal("set")
                .then(
                        Commands.argument("skill", IdentifierArgument.id())
                                .suggests((_, builder) ->
                                        SharedSuggestionProvider.suggestResource(SkillRegistries.SKILL.keySet(), builder)
                                )
                                .then(
                                        Commands.argument("ticks", IntegerArgumentType.integer(0))
                                                .executes(context ->
                                                        executeCooldownSet(
                                                                context.getSource(),
                                                                List.of(context.getSource().getEntityOrException()),
                                                                getSkill(IdentifierArgument.getId(context, "skill")),
                                                                IntegerArgumentType.getInteger(context, "ticks")
                                                        )
                                                )
                                                .then(
                                                        Commands.argument("targets", EntityArgument.entities())
                                                                .executes(context ->
                                                                        executeCooldownSet(
                                                                                context.getSource(),
                                                                                EntityArgument.getEntities(context, "targets"),
                                                                                getSkill(IdentifierArgument.getId(context, "skill")),
                                                                                IntegerArgumentType.getInteger(context, "ticks")
                                                                        )
                                                                )
                                                )
                                )
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> argumentCooldownClear() {
        return Commands.literal("clear")
                .executes(context ->
                        executeCooldownClear(
                                context.getSource(),
                                List.of(context.getSource().getEntityOrException())
                        )
                )
                .then(
                        Commands.argument("targets", EntityArgument.entities())
                                .executes(context ->
                                        executeCooldownClear(
                                                context.getSource(),
                                                EntityArgument.getEntities(context, "targets")
                                        )
                                )
                );
    }

    private static int executeAdd(
            CommandSourceStack source,
            Collection<? extends Entity> targets,
            Collection<? extends Holder<? extends Skill<?>>> skills
    ) throws CommandSyntaxException {
        List<? extends LivingEntity> entities =
                filterLivingEntities(targets, entity -> entity.getSkillContainer().addSkills(skills) > 0);

        if (entities.isEmpty()) {
            throw ADD_FAILED_EXCEPTION.create();
        }

        if (entities.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.skill.add.success.single", skills.size(), entities.getFirst().getName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.skill.add.success.multiple", skills.size(), entities.size()), true);
        }

        return entities.size();
    }

    private static int executeRemove(
            CommandSourceStack source,
            Collection<? extends Entity> targets,
            Collection<? extends Holder<? extends Skill<?>>> skills
    ) throws CommandSyntaxException {
        List<? extends LivingEntity> entities =
                filterLivingEntities(targets, entity -> entity.getSkillContainer().removeSkills(skills) > 0);

        if (entities.isEmpty()) {
            throw REMOVE_FAILED_EXCEPTION.create();
        }

        if (entities.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.skill.remove.success.single", skills.size(), entities.getFirst().getName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.skill.remove.success.multiple", skills.size(), entities.size()), true);
        }

        return entities.size();
    }

    private static int executeTest(
            CommandSourceStack source,
            Entity target,
            Holder<? extends Skill<?>> skill
    ) throws CommandSyntaxException {
        switch (
                getLivingEntity(target).getSkillManager().testSkill(skill)
        ) {
            case SkillResponse.Success ignored ->
                    source.sendSuccess(() -> Component.translatable("commands.skill.test.success", skill.value().getName(), target.getName()), true);
            case SkillResponse.Failure(Component reason) ->
                    source.sendSuccess(() -> Component.translatable("commands.skill.test.failure", skill.value().getName(), target.getName(), reason), true);
        }

        return 1;
    }

    private static int executeCast(
            CommandSourceStack source,
            Entity target,
            Holder<? extends Skill<?>> skill
    ) throws CommandSyntaxException {
        switch (
                getLivingEntity(target).getSkillManager().castSkill(skill)
        ) {
            case SkillResponse.Success ignored ->
                    source.sendSuccess(() -> Component.translatable("commands.skill.cast.success", skill.value().getName(), target.getName()), true);
            case SkillResponse.Failure(Component reason) ->
                    source.sendSuccess(() -> Component.translatable("commands.skill.cast.failure", skill.value().getName(), target.getName(), reason), true);
        }

        return 1;
    }

    private static int executeCancel(
            CommandSourceStack source,
            Collection<? extends Entity> targets
    ) throws CommandSyntaxException {
        List<? extends LivingEntity> entities =
                filterLivingEntities(targets, entity -> entity.getSkillManager().cancelCasting());

        if (entities.isEmpty()) {
            throw CANCEL_FAILED_EXCEPTION.create();
        }

        if (entities.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.skill.cancel.success.single", entities.getFirst().getName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.skill.cancel.success.multiple", entities.size()), true);
        }

        return entities.size();
    }

    private static int executeInterrupt(
            CommandSourceStack source,
            Collection<? extends Entity> targets
    ) throws CommandSyntaxException {
        List<? extends LivingEntity> entities =
                filterLivingEntities(targets, entity -> entity.getSkillManager().interruptCasting());

        if (entities.isEmpty()) {
            throw INTERRUPT_FAILED_EXCEPTION.create();
        }

        if (entities.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.skill.interrupt.success.single", entities.getFirst().getName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.skill.interrupt.success.multiple", entities.size()), true);
        }

        return entities.size();
    }

    private static int executeTerminate(
            CommandSourceStack source,
            Collection<? extends Entity> targets
    ) throws CommandSyntaxException {
        List<? extends LivingEntity> entities =
                filterLivingEntities(targets, entity -> entity.getSkillManager().terminateCasting());

        if (entities.isEmpty()) {
            throw TERMINATE_FAILED_EXCEPTION.create();
        }

        if (entities.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.skill.terminate.success.single", entities.getFirst().getName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.skill.terminate.success.multiple", entities.size()), true);
        }

        return entities.size();
    }

    private static int executeCooldownGet(
            CommandSourceStack source,
            Entity target,
            Holder<? extends Skill<?>> skill
    ) throws CommandSyntaxException {
        LivingEntity livingEntity = getLivingEntity(target);
        int cooldown = livingEntity.getSkillCooldownManager().getCooldown(skill);

        if (cooldown > 0) {
            source.sendSuccess(() -> Component.translatable("commands.skill.cooldown.get.success", skill.value().getName(), target.getName(), cooldown), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.skill.cooldown.get.none", skill.value().getName(), target.getName()), true);
        }

        return cooldown;
    }

    private static int executeCooldownSet(
            CommandSourceStack source,
            Collection<? extends Entity> targets,
            Holder<? extends Skill<?>> skill,
            int ticks
    ) throws CommandSyntaxException {
        List<? extends LivingEntity> entities =
                filterLivingEntities(targets, entity -> {
                    entity.getSkillCooldownManager().setCooldown(skill, ticks);
                    return true;
                });

        if (entities.isEmpty()) {
            throw COOLDOWN_SET_FAILED_EXCEPTION.create();
        }

        if (entities.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.skill.cooldown.set.success.single", skill.value().getName(), ticks, entities.getFirst().getName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.skill.cooldown.set.success.multiple", skill.value().getName(), ticks, entities.size()), true);
        }

        return entities.size();
    }

    private static int executeCooldownClear(
            CommandSourceStack source,
            Collection<? extends Entity> targets
    ) throws CommandSyntaxException {
        List<? extends LivingEntity> entities =
                filterLivingEntities(targets, entity -> {
                    entity.getSkillCooldownManager().clearCooldowns();
                    return true;
                });

        if (entities.isEmpty()) {
            throw COOLDOWN_CLEAR_FAILED_EXCEPTION.create();
        }

        if (entities.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.skill.cooldown.clear.success.single", entities.getFirst().getName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.skill.cooldown.clear.success.multiple", entities.size()), true);
        }

        return entities.size();
    }

    private static List<? extends LivingEntity> filterLivingEntities(
            Collection<? extends Entity> entities,
            Predicate<? super LivingEntity> predicate
    ) {
        return entities.stream()
                .filter(LivingEntity.class::isInstance)
                .map(LivingEntity.class::cast)
                .filter(predicate)
                .toList();
    }

    private static LivingEntity getLivingEntity(Entity entity) throws CommandSyntaxException {
        if (!(entity instanceof LivingEntity livingEntity)) {
            throw ENTITY_FAILED_EXCEPTION.create(entity.getName());
        }

        return livingEntity;
    }

    private static Holder<? extends Skill<?>> getSkill(Identifier id) throws CommandSyntaxException {
        return SkillRegistries.SKILL.get(id)
                .orElseThrow(() -> NOT_FOUND_EXCEPTION.create(id));
    }
}
