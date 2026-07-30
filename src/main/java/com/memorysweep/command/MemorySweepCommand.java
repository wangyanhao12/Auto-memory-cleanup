package com.memorysweep.command;

import com.memorysweep.MemoryMonitor;
import com.memorysweep.MemorySweepMod;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

/**
 * 注册 {@code /memorysweep} 指令。
 *
 * <ul>
 *   <li>{@code /memorysweep} —— 立即手动执行一次内存清理。</li>
 *   <li>{@code /memorysweep status} —— 查看当前内存使用情况与自动清理配置状态(附加功能)。</li>
 * </ul>
 *
 * <p>两者都要求命令源至少拥有 {@link Permissions#COMMANDS_MODERATOR} 权限
 * (即原版权限等级 2/管理员及以上,命令方块同样满足此条件),避免普通玩家滥用。</p>
 */
public final class MemorySweepCommand {

    private MemorySweepCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("memorysweep")
                    .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                    .executes(MemorySweepCommand::executeSweep)
                    .then(Commands.literal("status").executes(MemorySweepCommand::executeStatus)));
        });
    }

    private static int executeSweep(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MemoryMonitor monitor = MemorySweepMod.getMemoryMonitor();

        if (monitor == null) {
            source.sendFailure(Component.literal("MemorySweep 尚未初始化完成,请稍后再试。"));
            return 0;
        }

        MemoryMonitor.CleanupResult result = monitor.performCleanup(source.getServer(), MemoryMonitor.CleanupReason.MANUAL);
        source.sendSuccess(() -> Component.literal(result.toChatText()).withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int executeStatus(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MemoryMonitor monitor = MemorySweepMod.getMemoryMonitor();

        if (monitor == null) {
            source.sendFailure(Component.literal("MemorySweep 尚未初始化完成,请稍后再试。"));
            return 0;
        }

        source.sendSuccess(() -> Component.literal(monitor.statusText()).withStyle(ChatFormatting.AQUA), false);
        return 1;
    }
}
