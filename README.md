# MemorySweep

一个适用于 **Minecraft Java 版 26.1.2（Fabric）** 的自动内存清理模组。

## 功能

- **`/memorysweep`** —— 立即手动执行一次内存清理。
- **`/memorysweep status`**（附加功能）—— 查看当前堆内存使用率、下一次定时清理的倒计时等状态信息。
- **定时自动清理** —— 默认每 **15 分钟**清理一次,可在配置文件中调整。
- **使用率自动清理** —— 当堆内存使用率达到设定阈值(默认 **80%**)时自动清理,但同一冷却周期内(默认 **2 分钟**)只会执行一次,避免因内存长期处于高位而反复触发。

两种自动触发方式相互独立又共享同一个“最近一次清理时间”:定时清理按自己的节奏雷打不动地执行;使用率触发的清理则会检查“距离上一次清理(无论是手动、定时还是使用率触发的)是否已经超过冷却时间”,超过才会再次触发,这样可以避免短时间内被反复强制触发。

## ⚠️ 关于“清理内存”的实际效果,请务必了解

这个模组的清理动作,本质上是调用 Java 的 `System.gc()`,也就是向 JVM **建议**它执行一次垃圾回收(GC)。这里有几点必须说明清楚:

1. **`System.gc()` 只是建议,不是强制命令。** 现代 JVM(尤其是默认的 G1 收集器)通常会响应这个建议并执行一次 Full GC,但如果服务器启动参数里加了 `-XX:+DisableExplicitGC`,这个调用会被直接忽略,清理前后内存不会有变化。
2. **“已用内存变少”不等于“性能变好”。** 现代垃圾回收器(G1、ZGC 等)本身已经会在合适的时机自动回收内存;强制触发一次 Full GC 有时反而会造成短暂的卡顿(STW 停顿),尤其是在堆比较大的服务器上。很多资深服主和开发者认为,这类“手动清内存”模组在日常运行中收益有限,更多是“看着已用内存数字下降会安心”的心理作用。
3. **合理的使用场景**:如果你只是想在某个时间点(比如维护、切图前)手动整理一下内存,或者想要一个大致的内存状态监控,这个模组能满足需求。但如果服务器出现持续的内存压力甚至 OOM,**根本的解决办法是调整 `-Xmx`、优化模组/数据包、减少加载的区块和实体数量**,而不是依赖频繁强制 GC。
4. 正因为如此,本模组默认把定时间隔设为 15 分钟、使用率触发的冷却设为 2 分钟,避免过于频繁地强制 GC 造成卡顿;如果你把间隔调得很短,请留意是否会引入额外的卡顿。

## 环境要求

| 项目 | 版本 |
|---|---|
| Minecraft | Java 版 26.1.2 |
| Fabric Loader | ≥ 0.19.3 |
| Fabric API | 0.155.2+26.1.2 或更新的 26.1.x 版本 |
| Java(运行环境) | 25 或更高 |

> 26.1 起 Fabric 生态已经从 Yarn 映射切换为 Mojang 官方映射,并要求 Java 25。如果你是从更老的版本移植代码过来,记得同步升级本地开发环境的 JDK。

## 构建方法

本项目已经包含 Gradle Wrapper,不需要本机预装 Gradle,但需要 **JDK 25**。

```bash
# Linux / macOS
./gradlew build

# Windows
gradlew.bat build
```

构建完成后,产物在 `build/libs/` 目录下:
- `memorysweep-1.0.0.jar` —— 正式的模组文件
- `memorysweep-1.0.0-sources.jar` —— 源码 jar(可选,IDE 用来查看源码跳转)

> 由于本项目开发环境的网络限制,这份代码是基于 Fabric 官方 `fabric-example-mod` 在 `26.1.2` 分支的真实工程结构、以及当前 Fabric 官方文档中已验证的 API(命令注册、权限检查、生命周期事件等)手写并交叉核对而成,但**没有条件在联网的真实 Minecraft/Fabric 环境中实际编译运行一遍**。建议你在本地执行一次 `./gradlew build` 作为最终确认;如果报错,把报错信息发给我,我可以帮你快速定位修正。

## 不想在本地装 JDK 25?用 GitHub Actions 云端构建

项目里已经带了 `.github/workflows/build.yml`,可以让 GitHub 帮你在云端完成真正的编译(云端可以正常访问 Mojang / Fabric 的服务器,构建结果是完整可用的)。步骤:

1. 在 GitHub 上新建一个仓库(公开或私有都可以)。
2. 把这个项目推上去:
   ```bash
   cd memorysweep
   git init
   git add .
   git commit -m "init"
   git branch -M main
   git remote add origin https://github.com/你的用户名/你的仓库名.git
   git push -u origin main
   ```
3. 推送后打开仓库页面的 **Actions** 标签页,会自动开始一次构建(通常 2-5 分钟)。
4. 构建完成后,点进这次运行,底部 **Artifacts** 里下载 `memorysweep-jar`,解压后就是可以直接放进 `mods` 文件夹的 `.jar` 文件。

之后每次改代码、推送到 GitHub,都会自动重新构建一次,不需要本地装任何东西。

## 安装方法

1. 安装对应版本的 [Fabric Loader](https://fabricmc.net/use/) 与 [Fabric API](https://modrinth.com/mod/fabric-api)。
2. 把构建出来的 `memorysweep-1.0.0.jar` 放进服务器(或客户端)的 `mods` 文件夹。
3. 启动服务器,首次启动会在 `config/memorysweep.json` 生成默认配置文件。

## 指令说明

| 指令 | 说明 | 权限要求 |
|---|---|---|
| `/memorysweep` | 立即执行一次内存清理 | 相当于原版管理员(OP)权限等级 2 及以上;命令方块同样可以执行 |
| `/memorysweep status` | 查看当前内存使用率与自动清理状态 | 同上 |

如果需要放宽或收紧权限要求,可以修改 `MemorySweepCommand.java` 中 `.requires(...)` 那一行使用的权限判断。

## 配置文件说明(`config/memorysweep.json`)

修改配置文件后需要**重启服务器**才能生效(本模组不做热重载)。

| 字段 | 默认值 | 说明 |
|---|---|---|
| `autoCleanupEnabled` | `true` | 是否启用“定时自动清理” |
| `intervalMinutes` | `15` | 定时自动清理的间隔(分钟) |
| `usageBasedCleanupEnabled` | `true` | 是否启用“根据内存使用率自动清理” |
| `memoryUsageThresholdPercent` | `80` | 触发使用率清理的堆内存占用阈值(百分比,1-99) |
| `usageCheckCooldownSeconds` | `120` | 使用率触发的清理,两次执行之间的最短间隔(秒);默认 2 分钟 |
| `usageCheckIntervalSeconds` | `5` | 后台检查内存使用率的频率(秒);仅是“检查”频率,不代表每次都会清理 |
| `broadcastToOps` | `true` | 清理后是否在聊天栏向管理员播报结果 |
| `logToConsole` | `true` | 清理后是否在服务器控制台/日志中输出结果 |

默认配置文件内容大致如下:

```json
{
  "autoCleanupEnabled": true,
  "intervalMinutes": 15,
  "usageBasedCleanupEnabled": true,
  "memoryUsageThresholdPercent": 80,
  "usageCheckCooldownSeconds": 120,
  "usageCheckIntervalSeconds": 5,
  "broadcastToOps": true,
  "logToConsole": true
}
```

## 项目结构

```
memorysweep/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── gradlew / gradlew.bat / gradle/wrapper/...
├── LICENSE
├── README.md
└── src/main/
    ├── java/com/memorysweep/
    │   ├── MemorySweepMod.java        # 模组入口,注册事件与指令
    │   ├── MemoryMonitor.java         # 核心逻辑:定时清理 + 使用率触发清理(含冷却)
    │   ├── command/MemorySweepCommand.java  # /memorysweep 指令
    │   └── config/MemorySweepConfig.java    # 配置文件读写
    └── resources/
        └── fabric.mod.json
```

## 个性化

发布前建议编辑 `src/main/resources/fabric.mod.json` 里的 `authors` 字段,填上你自己的名字。
