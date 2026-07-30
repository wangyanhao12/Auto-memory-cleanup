package com.memorysweep;

import com.memorysweep.command.MemorySweepCommand;
import com.memorysweep.config.MemorySweepConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MemorySweep —— 自动清理服务器内存的 Fabric 模组。
 *
 * <ul>
 *   <li>提供 {@code /memorysweep} 指令用于手动清理内存。</li>
 *   <li>默认每 15 分钟自动清理一次(可在 config/memorysweep.json 中调整)。</li>
 *   <li>同时根据堆内存使用率自动清理,但同一冷却周期(默认 2 分钟)内只执行一次。</li>
 * </ul>
 */
public final class MemorySweepMod implements ModInitializer {

    public static final String MOD_ID = "memorysweep";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static MemorySweepConfig config;
    private static MemoryMonitor memoryMonitor;

    @Override
    public void onInitialize() {
        config = MemorySweepConfig.load(LOGGER);
        memoryMonitor = new MemoryMonitor(config, LOGGER);

        MemorySweepCommand.register();

        ServerLifecycleEvents.SERVER_STARTED.register(memoryMonitor::onServerStarted);
        ServerTickEvents.END_SERVER_TICK.register(memoryMonitor::onServerTick);

        LOGGER.info("[MemorySweep] 模组已加载。使用 /memorysweep 手动清理内存,或编辑 config/memorysweep.json 调整自动清理行为。");
    }

    public static MemorySweepConfig getConfig() {
        return config;
    }

    public static MemoryMonitor getMemoryMonitor() {
        return memoryMonitor;
    }
}
