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

    private SkillCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("skill")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(
                                Commands.argument("target", EntityArgument.entity())
                                        .then(argumentAdd())
                                        .then(argumentRemove())
                                        .then(argumentTest())
                                        .then(argumentCast())
                                        .then(argumentCancel())
                                        .then(argumentInterrupt())
                                        .then(argumentTerminate())
                                        .then(argumentCooldown())
                        )
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> argumentAdd() {
        return Commands.literal("add")
                .then(
                        Commands.argument("skill", IdentifierArgument.id())
                                .suggests((_, builder) ->
                                        SharedSuggestionProvider.suggestResource(SkillRegistries.SKILL.keySet(), builder)
                                )
                                .then(
                                        Commands.argument("source", IdentifierArgument.id())
                                                .executes(context ->
                                                        executeAdd(
                                                                context.getSource(),
                                                                EntityArgument.getEntity(context, "target"),
                                                                List.of(getSkill(IdentifierArgument.getId(context, "skill"))),
                                                                IdentifierArgument.getId(context, "source")
                                                        )
                                                )
                                )
                )
                .then(
                        Commands.literal("*")
                                .then(
                                        Commands.argument("source", IdentifierArgument.id())
                                                .executes(context ->
                                                        executeAdd(
                                                                context.getSource(),
                                                                EntityArgument.getEntity(context, "target"),
                                                                SkillRegistries.SKILL.listElements().toList(),
                                                                IdentifierArgument.getId(context, "source")
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
                                .then(
                                        Commands.argument("source", IdentifierArgument.id())
                                                .suggests((context, builder) ->
                                                        SharedSuggestionProvider.suggestResource(
                                                                getSources(EntityArgument.getEntity(context, "target")),
                                                                builder
                                                        )
                                                )
                                                .executes(context ->
                                                        executeRemove(
                                                                context.getSource(),
                                                                EntityArgument.getEntity(context, "target"),
                                                                List.of(getSkill(IdentifierArgument.getId(context, "skill"))),
                                                                IdentifierArgument.getId(context, "source")
                                                        )
                                                )
                                )
                )
                .then(
                        Commands.literal("*")
                                .then(
                                        Commands.argument("source", IdentifierArgument.id())
                                                .suggests((context, builder) ->
                                                        SharedSuggestionProvider.suggestResource(
                                                                getSources(EntityArgument.getEntity(context, "target")),
                                                                builder
                                                        )
                                                )
                                                .executes(context ->
                                                        executeRemove(
                                                                context.getSource(),
                                                                EntityArgument.getEntity(context, "target"),
                                                                SkillRegistries.SKILL.listElements().toList(),
                                                                IdentifierArgument.getId(context, "source")
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
                                                EntityArgument.getEntity(context, "target"),
                                                getSkill(IdentifierArgument.getId(context, "skill"))
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
                                                EntityArgument.getEntity(context, "target"),
                                                getSkill(IdentifierArgument.getId(context, "skill"))
                                        )
                                )
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> argumentCancel() {
        return Commands.literal("cancel")
                .executes(context ->
                        executeCancel(
                                context.getSource(),
                                EntityArgument.getEntity(context, "target")
                        )
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> argumentInterrupt() {
        return Commands.literal("interrupt")
                .executes(context ->
                        executeInterrupt(
                                context.getSource(),
                                EntityArgument.getEntity(context, "target")
                        )
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> argumentTerminate() {
        return Commands.literal("terminate")
                .executes(context ->
                        executeTerminate(
                                context.getSource(),
                                EntityArgument.getEntity(context, "target")
                        )
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> argumentCooldown() {
        return Commands.literal("cooldown")
                .then(
                        Commands.literal("get")
                                .then(
                                        Commands.argument("skill", IdentifierArgument.id())
                                                .suggests((_, builder) ->
                                                        SharedSuggestionProvider.suggestResource(SkillRegistries.SKILL.keySet(), builder)
                                                )
                                                .executes(context ->
                                                        executeCooldownGet(
                                                                context.getSource(),
                                                                EntityArgument.getEntity(context, "target"),
                                                                getSkill(IdentifierArgument.getId(context, "skill"))
                                                        )
                                                )
                                )
                )
                .then(
                        Commands.literal("set")
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
                                                                                EntityArgument.getEntity(context, "target"),
                                                                                getSkill(IdentifierArgument.getId(context, "skill")),
                                                                                IntegerArgumentType.getInteger(context, "ticks")
                                                                        )
                                                                )
                                                )
                                )
                )
                .then(
                        Commands.literal("clear")
                                .executes(context ->
                                        executeCooldownClear(
                                                context.getSource(),
                                                EntityArgument.getEntity(context, "target")
                                        )
                                )
                );
    }

    private static int executeAdd(
            CommandSourceStack stack,
            Entity target,
            Collection<? extends Holder<? extends Skill<?>>> skills,
            Identifier source
    ) throws CommandSyntaxException {
        int added = getLivingEntity(target).getSkillContainer().addPermanentSkills(skills, source);
        if (added == 0) {
            throw ADD_FAILED_EXCEPTION.create();
        }

        stack.sendSuccess(() -> Component.translatable("commands.skill.add.success", added, target.getName()), true);
        return added;
    }

    private static int executeRemove(
            CommandSourceStack stack,
            Entity target,
            Collection<? extends Holder<? extends Skill<?>>> skills,
            Identifier source
    ) throws CommandSyntaxException {
        int removed = getLivingEntity(target).getSkillContainer().removeSkills(skills, source);
        if (removed == 0) {
            throw REMOVE_FAILED_EXCEPTION.create();
        }

        stack.sendSuccess(() -> Component.translatable("commands.skill.remove.success", removed, target.getName()), true);
        return removed;
    }

    private static int executeTest(
            CommandSourceStack stack,
            Entity target,
            Holder<? extends Skill<?>> skill
    ) throws CommandSyntaxException {
        switch (
                getLivingEntity(target).getSkillManager().testSkill(skill)
        ) {
            case SkillResponse.Success ignored ->
                    stack.sendSuccess(() -> Component.translatable("commands.skill.test.success", skill.value().getName(), target.getName()), true);
            case SkillResponse.Failure(Component reason) ->
                    stack.sendSuccess(() -> Component.translatable("commands.skill.test.failure", skill.value().getName(), target.getName(), reason), true);
        }

        return 1;
    }

    private static int executeCast(
            CommandSourceStack stack,
            Entity target,
            Holder<? extends Skill<?>> skill
    ) throws CommandSyntaxException {
        switch (
                getLivingEntity(target).getSkillManager().castSkill(skill)
        ) {
            case SkillResponse.Success ignored ->
                    stack.sendSuccess(() -> Component.translatable("commands.skill.cast.success", skill.value().getName(), target.getName()), true);
            case SkillResponse.Failure(Component reason) ->
                    stack.sendSuccess(() -> Component.translatable("commands.skill.cast.failure", skill.value().getName(), target.getName(), reason), true);
        }

        return 1;
    }

    private static int executeCancel(
            CommandSourceStack stack,
            Entity target
    ) throws CommandSyntaxException {
        if (!getLivingEntity(target).getSkillManager().cancelCasting()) {
            throw CANCEL_FAILED_EXCEPTION.create();
        }

        stack.sendSuccess(() -> Component.translatable("commands.skill.cancel.success", target.getName()), true);
        return 1;
    }

    private static int executeInterrupt(
            CommandSourceStack stack,
            Entity target
    ) throws CommandSyntaxException {
        if (!getLivingEntity(target).getSkillManager().interruptCasting()) {
            throw INTERRUPT_FAILED_EXCEPTION.create();
        }

        stack.sendSuccess(() -> Component.translatable("commands.skill.interrupt.success", target.getName()), true);
        return 1;
    }

    private static int executeTerminate(
            CommandSourceStack stack,
            Entity target
    ) throws CommandSyntaxException {
        if (!getLivingEntity(target).getSkillManager().terminateCasting()) {
            throw TERMINATE_FAILED_EXCEPTION.create();
        }

        stack.sendSuccess(() -> Component.translatable("commands.skill.terminate.success", target.getName()), true);
        return 1;
    }

    private static int executeCooldownGet(
            CommandSourceStack stack,
            Entity target,
            Holder<? extends Skill<?>> skill
    ) throws CommandSyntaxException {
        int cooldown = getLivingEntity(target).getSkillCooldownManager().getCooldown(skill);

        if (cooldown > 0) {
            stack.sendSuccess(() -> Component.translatable("commands.skill.cooldown.get.success", skill.value().getName(), target.getName(), cooldown), true);
        } else {
            stack.sendSuccess(() -> Component.translatable("commands.skill.cooldown.get.none", skill.value().getName(), target.getName()), true);
        }

        return cooldown;
    }

    private static int executeCooldownSet(
            CommandSourceStack stack,
            Entity target,
            Holder<? extends Skill<?>> skill,
            int ticks
    ) throws CommandSyntaxException {
        getLivingEntity(target).getSkillCooldownManager().setCooldown(skill, ticks);

        stack.sendSuccess(() -> Component.translatable("commands.skill.cooldown.set.success", skill.value().getName(), ticks, target.getName()), true);
        return 1;
    }

    private static int executeCooldownClear(
            CommandSourceStack stack,
            Entity target
    ) throws CommandSyntaxException {
        getLivingEntity(target).getSkillCooldownManager().clearCooldowns();

        stack.sendSuccess(() -> Component.translatable("commands.skill.cooldown.clear.success", target.getName()), true);
        return 1;
    }

    private static Collection<Identifier> getSources(Entity target) throws CommandSyntaxException {
        return getLivingEntity(target).getSkillContainer().getSources();
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
