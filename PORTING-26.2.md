# Yet Another Carpet Addition 升级到 Minecraft 26.2 改动说明

> 目标：在 preprocess 多版本框架下，把本模组从最高支持 1.21.11 扩展到 **26.2**，
> 同时保留 1.20.4 ~ 1.21.11 全部旧节点可编译、可出包。

---

## 一、升级概览

### 新增节点

| 节点 | Minecraft | Fabric Loader | Fabric API | Carpet | Java | Loom |
| --- | --- | --- | --- | --- | --- | --- |
| `26.1.2`（覆盖 26.1 / 26.1.1 / 26.1.2） | 26.1.2 | 0.19.5 | 0.155.3+26.1.2 | 26.1+v260401 | 25 | `net.fabricmc.fabric-loom` 1.17.20 |
| `26.2` | 26.2 | 0.19.5 | 0.160.0+26.2 | 26.2+v260616 | 25 | `net.fabricmc.fabric-loom` 1.17.20 |

- `26.1` / `26.1.1` / `26.1.2` 只有补丁级差异，共用 `26.1.2` 节点，产物通过
  `minecraft_dependency=>=26.1 <26.2` 覆盖这三个版本。
- 旧节点（1.20.4 / 1.20.6 / 1.21.1 / 1.21.3 / 1.21.4 / 1.21.5 / 1.21.6 / 1.21.9 / 1.21.11）
  全部保留；主开发版本仍是 `1.21.11`（与 Carpet AMS Addition 等同类多版本项目一致），
  26.x 的差异全部用 `//#if MC >= 260100` / `//#if MC >= 260200` 分支表达。

### 最终验证结论

- `compileJava`：11 个节点全部 **0 错误**。
- `build`：**BUILD SUCCESSFUL**，11 个 `validateAccessWidener` 任务全部通过。
- 真实启动（**服务端**）：26.2 与 26.1.2 均启动到 `Done (...)!`，日志无 Mixin apply 失败。
- 真实启动（**客户端**）：26.2 客户端进入世界（`Loaded 1688 advancements`）、
  26.1.2 客户端起来到 `Sound engine started`，**两者均无 Mixin apply 失败**。
  客户端首次启动曾因 `client.optionalTicking.WorldRenderFreeze` 的
  `@Shadow removeProgress` 在 26.2 不存在而直接崩，已修复（见 4.3）。
- 主动触发：假人生成、YACA 自定义命令、carpet 规则查询/切换均已实测（见第五节）。

---

## 二、构建配置改动

| 文件 | 改动 |
| --- | --- |
| `build.gradle` | 新增 `net.fabricmc.fabric-loom-remap`（旧版用）与 `net.fabricmc.fabric-loom`（26.x 用）两个插件声明；preprocess 插件 `d452ef7612` → `c5abb4fb12`（旧版不认识未混淆版本）；新增 `26.1.2`、`26.2` 两个节点与两条 link |
| `common.gradle` | 按 `mcVersion >= 260000` 计算 `unobfuscated`，据此切换 loom 插件、去掉 `mappings`、把 `modImplementation/modRuntimeOnly` 换成 `implementation/runtimeOnly`；Java 目标 25；mixin compatibility level 由 `JAVA_21` 改为按版本计算；加 `-Xmaxerrs 2000`；依赖级排除传递的 fabric-loader |
| `gradle.properties` | 新增 `mixinextras_version_unobf=0.5.5`、`mixin_auditor_version=0.1.0`、`mixin_auditor_version_unobf=0.2.0-u` |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle `9.1.0` → `9.6.0`（loom 1.17 要求 plugin api ≥ 9.5） |
| `settings.json` | 追加 `26.1.2`、`26.2` |
| `versions/26.1.2/`、`versions/26.2/` | 新增 `gradle.properties`（MC/loader/fabric-api/carpet 版本）与 `YACA.accesswidener`（头部 `accessWidener v1 official`） |
| `versions/mapping-1.21.11-26.1.2.txt`、`versions/mapping-26.1.2-26.2.txt` | 新增（空文件）。**注意**：preprocess 的 extra mapping 在未混淆链路上不生效（插件在该分支直接跳过 mapping 设置），26.x 的改名必须在源码里用 `//#if` 表达 |

`fabric.mod.json` 未做结构性改动：`depends` 里已经是 `fabric-api`（26.2 的 Fabric API 聚合 id 仍是
`fabric-api`，已读 jar 内 `fabric.mod.json` 确认），`carpet` 与 `minecraft` 由各节点 gradle.properties 展开。

---

## 三、API 迁移对照

### 3.1 通用（两版都存在，直接替换，无需分支）

| 旧 | 新 | 说明 |
| --- | --- | --- |
| `blockPos.getCenter()` | `Vec3.atCenterOf(blockPos)` | 34 处 |
| `player.displayClientMessage(msg, false)` | `player.sendSystemMessage(msg)` | |
| `world.random` | `world.getRandom()` | |
| `int i = x.getUpdatedRenderTick()` | `int i = (int) x.getUpdatedRenderTick()` | 26.x 返回 long |

### 3.2 需要 `//#if MC >= 260100`（26.1 起）

| 旧 | 新 |
| --- | --- |
| `net.minecraft.client.gui.GuiGraphics` | `net.minecraft.client.gui.GuiGraphicsExtractor` |
| `Screen#render(GuiGraphics,..)` / `renderBackground` | `extractRenderState(...)` / `extractBackground(...)` |
| `AbstractWidget#renderWidget(GuiGraphics,..)` | `extractWidgetRenderState(...)` |
| `GuiGraphics#drawString` / `drawCenteredString` | `GuiGraphicsExtractor#text` / `centeredText` |
| `Minecraft#setScreen` | `Minecraft#setScreenAndShow` |
| `EntityType.XXX` | `EntityTypes.XXX`（注册常量搬到 `EntityTypes`） |
| `Entity#getTags()` | `Entity#entityTags()` |
| `net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents` | `ServerLevelEvents` |
| `ServerTickEvents.END_WORLD_TICK` | `END_LEVEL_TICK` |
| `net.fabricmc.fabric.api.client.command.v2.ClientCommandManager` | `ClientCommands` |
| `...client.keybinding.v1.KeyBindingHelper` | `...client.keymapping.v1.KeyMappingHelper` |
| `PayloadTypeRegistry.playS2C()/playC2S()` | `clientboundPlay()/serverboundPlay()` |
| `ChunkPos#x` / `#z`（字段） | `x()` / `z()`（record） |
| `new ChunkPos(blockPos)` | `ChunkPos.containing(blockPos)` |
| `Level#random`（protected 字段） | `Level#getRandom()` |
| `PlayerTeam#setColor(ChatFormatting)` | `setColor(Optional.of(TeamColor))` |
| `HoverEvent.ShowItem(ItemStack)` | `HoverEvent.ShowItem(ItemStackTemplate.fromNonEmptyStack(stack))` |
| `GlStateManager._enableBlend()/_disableBlend()` | 26.x 由 GUI 渲染管线管理，直接省略（与 vanilla 滚动条一致） |

### 3.3 需要 `//#if MC >= 260200`（26.2 起）

| 旧 | 新 |
| --- | --- |
| `Blocks.BLACK_WOOL` 等单色常量 | `Blocks.WOOL.black()`（`ColorCollection`） |
| `Blocks.RED_STAINED_GLASS` / `..._PANE` | `Blocks.STAINED_GLASS.red()` / `STAINED_GLASS_PANE.red()` |
| `Blocks.YELLOW_TERRACOTTA` | `Blocks.DYED_TERRACOTTA.yellow()` |
| `Blocks.COPPER_BLOCK` | `Blocks.COPPER_BLOCK.weathering().pick(WeatheringCopper.WeatherState.UNAFFECTED)` |
| `net.minecraft.world.level.dimension.end.EndDragonFight` | `EnderDragonFight` |
| `TreeFeature#getMaxFreeTreeHeight(LevelSimulatedReader,..)` | 首参类型改 `WorldGenLevel` |
| `FoliagePlacer#createFoliage(LevelSimulatedReader,..)` | 首参类型改 `WorldGenLevel` |
| `Block#updateEntityMovementAfterFallOn` | 26.2 已移除（见 4.3） |
| `net.minecraft.world.level.block.entity.BedBlockEntity` | 26.2 已移除（见 4.3） |

---

## 四、Mixin 适配（只在运行期才暴露）

### 4.1 未混淆版本没有 refMap

26.x 不再混淆，mixin 的 `method = "..."` 直接用官方名。原代码里依赖 intermediary 名的一处
（`ServerWorldMixin` 的 `method_31420`）在 26.x 需要写成 lambda 名：

```java
//#if MC >= 260100
//$$ method = "lambda$tick$0",
//#else
method = "method_31420",
//#endif
```

### 4.2 目标签名变化（逐个用 `javap` 核对后修正）

| Mixin | 问题 | 处理 |
| --- | --- | --- |
| `ServerWorldMixin` | `method_31420` 在未混淆版本不存在 | 26.x 用 `lambda$tick$0` |
| `features.treefarm.FoliagePlacerMixin` | `createFoliage` 首参 `LevelSimulatedReader` → `WorldGenLevel` | 描述符与 handler 参数都加分支（26.1 起就是新签名，已用 26.1.2 的 jar 核对） |
| `features.visualizers.FoliagePlacerMixin` | 同上 | 同上 |
| `features.visualizers.TreeGrowthObstacleUpdaterMixin` | `getMaxFreeTreeHeight` 首参类型变化 | `@At target` 加分支（2 处） |
| `features.visualizers.DisableVisualizerEntitySave` | `@Shadow getTags()` | 26.x 分支改用 `entityTags()` |

### 4.3 26.2 上无法等价实现、已降级为占位空 Mixin（**行为差异**）

| Mixin | 原因 | 影响 |
| --- | --- | --- |
| `bedRecordHeadRotation.BedBlockEntityMixin` | 26.2 移除了 `BedBlockEntity`（床改用方块模型渲染） | 该功能的“记录睡姿”在 26.2 不再生效；`BedBlockMixin` / `PlayerEntityWakeUpMixin` 仍在，但 `instanceof BedBlockEntityExtension` 永远为 false，静默不生效 |
| `features.instantFalling.PointedDripstoneBlockMixin` | `PointedDripstoneBlock` 在 26.2 不再 override scheduled tick（逻辑移到 `randomTick`） | `instantFalling` 规则对滴水石不再生效，其余（沙、脚手架、可疑沙等）不受影响 |
| `features.morphMovingPiston.MovingPistionBlockMixin` 的落地处理 | `Block#updateEntityMovementAfterFallOn` 在 26.2 被移除（弹跳改为 `bounciness` 属性） | 该 mixin 只有这一个方法在 26.x 被去掉，`useWithoutItem` / `getDirectSignal` 等其余注入保留 |
| `client.optionalTicking.WorldRenderFreeze` | 26.2 把 `LevelRenderer` 重写成 render-state / submit-node 架构：`ticks`、`destroyingBlocks`、`removeProgress`、`tick(Camera)`、`extractEntity` 全部消失（26.1.2 仍是旧结构，已用两侧 jar 核对） | 客户端的“渲染冻结/破坏进度冻结”在 26.2 不再生效；26.1.2 及更早仍正常 |

四处都保留空 Mixin 占位，`yetanothercarpetaddition.mixins.json` 不需要随版本改动。

> 这类“26.2 才发生”的重构要特别注意边界：`WorldRenderFreeze` 一开始被误判成 26.1 起失效，
> 实际用 `javap` 对 26.1.2 与 26.2 的 `LevelRenderer` 各查一遍，才确定是 **26.2** 才重构的。

### 4.4 静态校验脚本

新增 `tools/mixin_check.py`：按 preprocess 条件把源码还原成目标版本视图，再用 `javap`
校验 `@Mixin` 目标类、`method =`、`@At(target = "...")` 是否存在。

```bash
python3 tools/mixin_check.py 260200 \
  "$HOME/.gradle/caches/fabric-loom/26.2/minecraft-merged.jar:<carpet.jar>:<brigadier.jar>"
```

> 该脚本对 `throws` 方法、缺少 import 的简单类名、内部类名（`A.B` → `A$B`）等情况会**误报**，
> 只适合作为定位线索，最终以实际启动日志为准。

---

## 五、验证记录

| 项目 | 方式 | 结果 |
| --- | --- | --- |
| 编译（26.1.2 / 26.2） | IDE 终端 `gradlew :26.1.2:compileJava :26.2:compileJava` | 0 错误 |
| 编译（1.20.4 ~ 1.21.11） | `gradlew compileJava --continue` | 0 错误（含修掉 1.20.4 两处历史遗留错误，见第七节） |
| 完整构建 + AW 校验 | IDE 终端 `gradlew build --continue` | BUILD SUCCESSFUL，11 个 `validateAccessWidener` 通过 |
| 产物抽查（26.2 jar） | `unzip -p` | `accessWidener v1 official`；mixin `compatibilityLevel=JAVA_25`；JiJ 内含 `conditional-mixin-fabric-0.6.4` / `mixinextras-fabric-0.5.5`；`depends` 为 `minecraft >=26.2`、`carpet >=26.2`、`fabric-api *` |
| **26.2 服务端启动** | `gradlew :26.2:runServer` | `Done (1.650s)!`，无 `Mixin apply ... failed` |
| **26.1.2 服务端启动** | `gradlew :26.1.2:runServer` | `Done (2.822s)!`，无 mixin 失败 |
| 假人路径（26.2） | RCON `player Bot spawn` | `Bot[local] logged in ... Bot joined the game` |
| 假人路径（26.1.2） | RCON `player Bot2 spawn` | `Bot2[local] logged in ... joined the game` |
| YACA 命令 | RCON | `scheduleTick 0 64 0 minecraft:stone 20 0`、`randomTick 0 64 0`、`itemshadowing`、`subscribeRule instantFalling`、`waypoint list` 均正常响应 |
| 规则开关 | RCON `carpet <rule>` | `instantFalling`、`stopTickingEntities`、`commandEasyItemShadowing` 查询与切换均生效 |
| **26.2 客户端启动** | `gradlew :26.2:runClient` | `Sound engine started`、纹理 atlas 全部创建、进入世界（`Loaded 1688 advancements`、`Time elapsed: 1131 ms`），**无 mixin 失败、无 YACA 异常**（客户端首次启动曾因 `WorldRenderFreeze` 的 `@Shadow removeProgress` 崩溃，修复后通过） |
| **26.1.2 客户端启动** | `gradlew :26.1.2:runClient` | 渲染线程正常启动（`Setting user`），**无 mixin 失败**；本次因手工关窗未走完全部资源加载 |
| 客户端兼容性检查 | `javap` 对比 26.1.2 / 26.2 | 26.2 重写 `LevelRenderer`（旧结构仅 26.1.2 及更早存在），据此把 `WorldRenderFreeze` 的禁用边界定在 260200 |

RCON 使用 `tools/rcon.py`（`run/server.properties` 临时开启 rcon，验证后已还原）。

---

## 六、未验证项 / 需要注意的点

1. **客户端已启动验证，但未做交互式操作验证**：26.2 客户端已真实启动并进入世界
   （`Sound engine started`、纹理 atlas 创建、`Loaded 1688 advancements`），
   26.1.2 客户端也已启动到渲染线程与音效引擎就绪，两者日志均无 mixin 失败。
   但以下只在客户端起作用的路径**没有做交互式触发**（打开 GUI、按键、复制方块状态等）：
   `screen/*` 的渲染代码（`GuiGraphicsExtractor` 系列）、`ClientWorldFreezeMixin`、
   `features.copyBlockState.MinecraftClientMixin`（后者带
   `@Restriction(require = ... versionPredicates = "<1.21.4")`，在 26.x 本就不注入）。
2. **旧节点只验证了编译**：1.20.4 ~ 1.21.11 未逐个启动（服务端与客户端都没跑）；
   本次改动对它们的运行时影响面主要是 `common.gradle` 的依赖排除方式与 loom 版本变化。
3. **需要玩家上下文的命令未实测**：`waypoint save`（需要 `getPlayer()`）、
   `bindToFake <真实玩家> <假人>`（本机无真实玩家）。
4. **可视化功能未逐一触发**：`visualizingFeatures` 下的大量 mixin 在启动时不会全部加载，
   只验证到 `scheduleTick` / `randomTick` 两条命令能正常执行。
5. **行为差异**（见 4.3）：床睡姿记录、滴水石 instantFalling、活塞落地处理、
   客户端渲染冻结（`WorldRenderFreeze`）四个功能在 26.2 被禁用。
6. `mixin-auditor` 已升级到 `0.2.0-u`（26.x 用），但本轮未用它产出完整审计报告。
7. `tools/mixin_check.py` 存在已知误报，不作为门禁标准。

---

## 七、改动文件清单

**构建/元数据（7）**：`build.gradle`、`common.gradle`、`gradle.properties`、
`gradle/wrapper/gradle-wrapper.properties`、`settings.json`、
`versions/26.1.2/**`、`versions/26.2/**`（含两个空 mapping 文件）

**源码（50 个文件，+820/-63）**，按类别：

- 入口与网络：`YetAnotherCarpetAdditionServer/Client`、`commands/HopperCounterRequestCommand`、
  `features/log2Chat/LogAppender`
- 可视化：`features/visualizingFeatures/*`（10 个文件）、`features/moreCommandOperations/*`、
  `features/GridWorldGen/FlatGridChunkGenerator`、`utils/render/PieChartRenderer`
- Mixin：`mixin/features/ServerWorldMixin`、`mixin/features/visualizers/*`（含 FoliagePlacer、
  TreeGrowthObstacleUpdater、DisableVisualizerEntitySave、HopperEntityCooldown）、
  `mixin/features/treefarm/FoliagePlacerMixin`、`mixin/features/instantFalling/*`、
  `mixin/features/gridWorldPreset/FlatChunkGeneratorMixin`、
  `mixin/features/moreCollidableEntities/EntityCollisionMixin`、
  `mixin/features/betterCommmand/*`、`mixin/features/bypassRideRestriction/RideCommandMixin`、
  `mixin/features/optionalTicking/EnderDragonFightMixin`、
  `mixin/features/bedRecordHeadRotation/*`、`mixin/features/morphMovingPiston/MovingPistionBlockMixin`
- UI：`screen/rulesEditScreen/*`、`screen/countersViewerScreen/CounterViewerScreen`
- 工具/适配：`utils/adapter/HoverEvent`、`utils/POIManage`、
  `mixin/features/treefarm/SaplingBlockMixin`、`mixin/client/optionalTicking/WorldRenderFreeze`

**新增工具**：`tools/mixin_check.py`（mixin 静态校验）、`tools/rcon.py`（RCON 触发脚本）

### 顺带修掉的历史遗留问题（1.20.4 节点原本就编译不过）

- `ExtraVaniallaCommandFeatureManager`：1.20.4 分支用了不存在的 `Identifier`，改为 `ResourceLocation`。
- `BrushableBlockMixin`：`loadWithComponents` 只存在于 1.20.5+，把条件从 `MC > 12101` 拆成
  `MC >= 12005`（loadWithComponents）/ `< 12005`（`load(CompoundTag)`），1.20.4 与 1.20.6/1.21.1 都能编译。
