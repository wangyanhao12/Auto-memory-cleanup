package com.memorysweep.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * MemorySweep 的配置文件,对应磁盘上的 config/memorysweep.json。
 * <p>
 * 所有字段都是 public 的,便于 Gson 直接读写;字段名即为 JSON 中的键名。
 * 缺失的字段在读取时会使用下方声明的默认值填充,并在读取后立即重写一次配置文件,
 * 这样旧版本配置文件在模组更新后也能自动补全新增字段。
 */
public final class MemorySweepConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "memorysweep.json";

    /** 是否启用“定时自动清理”。 */
    public boolean autoCleanupEnabled = true;

    /** 定时自动清理的间隔时间,单位:分钟。默认 15 分钟。 */
    public int intervalMinutes = 15;

    /** 是否启用“根据内存使用率自动清理”。 */
    public boolean usageBasedCleanupEnabled = true;

    /** 触发使用率清理的堆内存占用阈值,单位:百分比(1-99)。默认 80。 */
    public int memoryUsageThresholdPercent = 80;

    /** 使用率触发的清理,两次执行之间的最短间隔,单位:秒。默认 120 秒(2 分钟)。 */
    public int usageCheckCooldownSeconds = 120;

    /** 后台检查内存使用率的频率,单位:秒。默认 5 秒检查一次(不代表会清理,只是检查)。 */
    public int usageCheckIntervalSeconds = 5;

    /** 每次清理后,是否在聊天栏向管理员(OP)播报清理结果。 */
    public boolean broadcastToOps = true;

    /** 每次清理后,是否在服务器控制台/日志中输出清理结果。 */
    public boolean logToConsole = true;

    /**
     * 从磁盘加载配置;文件不存在或解析失败时使用默认值,并把最终生效的配置写回磁盘。
     */
    public static MemorySweepConfig load(Logger logger) {
        Path path = configPath();
        MemorySweepConfig config = null;

        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                config = GSON.fromJson(reader, MemorySweepConfig.class);
            } catch (IOException | JsonParseException e) {
                logger.warn("[MemorySweep] 配置文件读取失败,将使用默认配置覆盖: {}", e.getMessage());
            }
        }

        if (config == null) {
            config = new MemorySweepConfig();
        }

        config.sanitize();
        config.save(logger);
        return config;
    }

    /** 将当前配置写回磁盘(格式化为带缩进的 JSON)。 */
    public void save(Logger logger) {
        Path path = configPath();
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            logger.warn("[MemorySweep] 配置文件保存失败: {}", e.getMessage());
        }
    }

    /** 对配置数值做基本的合法性纠正,避免用户填入荒谬的值(例如 0 分钟、负数、超过 100% 等)。 */
    private void sanitize() {
        if (intervalMinutes < 1) {
            intervalMinutes = 1;
        }
        if (memoryUsageThresholdPercent < 1) {
            memoryUsageThresholdPercent = 1;
        } else if (memoryUsageThresholdPercent > 99) {
            memoryUsageThresholdPercent = 99;
        }
        if (usageCheckCooldownSeconds < 1) {
            usageCheckCooldownSeconds = 1;
        }
        if (usageCheckIntervalSeconds < 1) {
            usageCheckIntervalSeconds = 1;
        }
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }
}
