# Yet Another Carpet Addition 升级到 Minecraft 26.3 改动说明

> 目标：把 `26.3-snapshot-9` 节点升级为 Minecraft **26.3 正式版**（Wilderness Bound，
> 2026-09-15 发布），并保留 1.20.4 ~ 26.2 全部旧节点可编译、可出包。
>
> Carpet 依赖改用 **GitHub release** 的正式版 jar（masa maven 尚未同步正式版），
> 做法参照同组织的 `sducarpetaddition`。

---

## 一、升级概览

### 版本状态（实证来源）

| 项 | 值 | 来源 |
| --- | --- | --- |
| Minecraft 26.3 | 2026-09-15 发布，Java SE 25，协议 777，数据版本 5023 | [minecraft.wiki/w/Java_Edition_26.3](https://minecraft.wiki/w/Java_Edition_26.3) |
| Fabric Loader | `0.19.5`（stable 最高） | `meta.fabricmc.net/v2/versions/loader/26.3` |
| Fabric API | `0.160.5+26.3` | Modrinth API `game_versions=["26.3"]` |
| Carpet | `26.3+v260915` | [GitHub release v26.3](https://github.com/gnembon/fabric-carpet/releases/tag/v26.3) |
| Loom / Gradle | 1.17（实测 1.17.21）/ Gradle 9.6.0 | [fabricmc.net 26.3 公告](https://fabricmc.net/2026/09/15/263.html) |

### 节点变化

- `26.3-snapshot-9` 节点**退役**，由 `26.3` 正式版节点取代（preprocess mcVersion 同为 `26_03_00`，
  因此源码里既有的 `//#if MC >= 260300` 分支全部继续有效，无需批量改写）。
- 与 `sducarpetaddition` 一致：不保留 snapshot 节点（该项目的 `26.3-snapshot-9` 目录也不在
  `settings.json` 里）。已删除 `versions/26.3-snapshot-9/` 与 `versions/mapping-26.2-26.3-snapshot-9.txt`。
- 最终 12 个节点：`1.20.4 / 1.20.6 / 1.21.1 / 1.21.3 / 1.21.4 / 1.21.5 / 1.21.6 / 1.21.9 / 1.21.11 / 26.1.2 / 26.2 / 26.3`。
  主开发版本仍为 `1.21.11`。

### 验证结论（本次已完成）

- `compileJava`：**12 个节点全部 0 错误**。
- `build --rerun-tasks`：**BUILD SUCCESSFUL**，148 个任务，0 FAILED；
  12 个 `validateAccessWidener` 全部通过。
- Mixin 静态检查：`26.3` 相对 `26.2` 基线**无新增注入点缺口**（唯一一处真实缺口已修复，见第四节）。
- **运行期验证尚未执行**，见第六节清单（由用户执行）。

---

## 二、构建配置改动

| 文件 | 改动 |
| --- | --- |
| `build.gradle` | `createNode('26.3-snapshot-9', 26_03_00, '')` → `createNode('26.3', 26_03_00, '')`；link 改用 `versions/mapping-26.2-26.3.txt` |
| `common.gradle` | Carpet 依赖改为「本地 release jar 优先，masa maven 兜底」 |
| `settings.json` | `26.3-snapshot-9` → `26.3` |
| `gradle.properties` | 未改动（loader / mixinextras / conditional-mixin 版本均已满足 26.3 要求） |
| `gradle/wrapper/gradle-wrapper.properties` | 未改动（已是 Gradle 9.6.0，满足 Loom 1.17 要求 ≥ 9.5） |
| `versions/26.3/gradle.properties` | 新增：MC `26.3`、loader `0.19.5`、fabric-api `0.160.5+26.3`、carpet `26.3+v260915` |
| `versions/26.3/YACA.accesswidener` | 新增：头部 `accessWidener v1 official`（与 26.2 内容一致，仅随节点各存一份） |
| `versions/mapping-26.2-26.3.txt` | 新增（空文件，link 需要文件存在） |
| `versions/26.2/gradle.properties` | **依赖上界修正**：`>=26.2` → `>=26.2 <26.3`（见下方说明） |
| `libs/fabric-carpet-26.3+v260915.jar` | 新增：Carpet 26.3 正式版，sha256 `7c40b93e807ed914064176898037117097446c6cc76e6a80a856c8bf7f7659f9` |

### Carpet 依赖：为什么用本地 jar

masa maven 上 26.3 只有 beta：

```
$ curl -s https://masa.dy.fi/maven/carpet/fabric-carpet/maven-metadata.xml | grep 26.3
<version>26.3-beta-1+v260702</version>
<version>26.3-beta-2+v260709</version>
<version>26.3-beta-3+v260820</version>
<latest>26.3-beta-3+v260820</latest>     # 正式版未同步
```

Carpet 正式版只在 GitHub release 分发（Modrinth 上 26.3 版本数为 0）。因此 `common.gradle` 改为：

```groovy
def carpetLocalJar = rootProject.file("libs/fabric-carpet-${project.carpet_version}.jar")
if (carpetLocalJar.exists()) {
    autoImplementation(files(carpetLocalJar))
} else {
    autoImplementation "carpet:fabric-carpet:${project.carpet_version}"
}
```

- 用 `implementation(files(...))` 引入**不会**打进本模组产物（只有 `include` 才 JiJ）。
  已核实产物 `META-INF/jars/` 内只有 `conditional-mixin-fabric` 与 `mixinextras-fabric`。
- 文件名按 `carpet_version` 推导，所以旧的 `26.2` 等节点仍走 masa maven，行为不变。
- **`libs/` 必须入库**：本项目 `.gitignore` 未忽略 `*.jar`（`git check-ignore` 未命中），
  CI（`.github/workflows/build.yml`）是直接 checkout 后 `./gradlew build`，
  本地 jar 不入库会导致 CI 解析 Carpet 失败。
  （注意 `sducarpetaddition` 的 `.gitignore` 含 `*.jar`，它的 libs jar 实际未入库——
  本项目不要照抄这一点。）

### 26.2 依赖上界修正（重要）

`26.2` 节点原为开放上界 `minecraft_dependency=>=26.2`。新增 26.3 节点后，该产物会声称自己
兼容 26.3，而两个节点的 API 已经不兼容（见第四节 `buildTerrain`），玩家在 26.3 上会被错误地
加载到 26.2 的产物。按项目既有的 `26.1.2` 先例（`>=26.1 <26.2`）收紧：

```
# versions/26.2/gradle.properties
minecraft_dependency=>=26.2 <26.3
game_versions=>=26.2 <26.3
```

已复核产物声明：

| 产物 | `minecraft` | `carpet` |
| --- | --- | --- |
| 26.1.2 | `>=26.1 <26.2` | `>=26.1` |
| 26.2 | `>=26.2 <26.3` | `>=26.2` |
| 26.3 | `>=26.3` | `>=26.3` |

### 未改动的构建项（已核实无需改）

- `fabric.mod.json`：`depends` 里已是 `fabric-api`，`carpet` / `minecraft` 由各节点展开，无结构改动。
- Fabric API 聚合 id 在 26.3 仍是 `fabric-api`（已读 jar 内 `fabric.mod.json`）。
- Mixin `compatibilityLevel` 由脚本算出 `JAVA_25`，与 Java 25 目标一致（已抽查产物）。

---

## 三、API 迁移对照

### 3.1 26.3 相对 26.2 的模组侧变更（本次实际处理）

| 旧 | 新 | 边界 |
| --- | --- | --- |
| `ChunkGenerator#fillFromNoise(Blender, RandomState, StructureManager, ChunkAccess)` | `ChunkGenerator#buildTerrain(ChunkAccess, Blender, RandomState, StructureManager, BiomeManager, WorldGenRegion, Set<Holder<Biome>>)` | `MC >= 260300` |

用两侧 jar `javap` 核实：

```
# 26.2 / 26.3-snapshot-9
public abstract CompletableFuture<ChunkAccess> fillFromNoise(Blender, RandomState, StructureManager, ChunkAccess);
# 26.3（fillFromNoise 已完全移除）
public abstract CompletableFuture<ChunkAccess> buildTerrain(ChunkAccess, Blender, RandomState, StructureManager, BiomeManager, WorldGenRegion, Set<Holder<Biome>>);
```

### 3.2 已在 snapshot 阶段处理、本次复核仍然成立

`26.3` 正式版与 `26.3-snapshot-9` 之间，**除 `fillFromNoise → buildTerrain` 外没有其他
模组可见差异**（mixin 静态检查逐项 diff 得出，见第四节）。以下既有 `MC >= 260300` 分支
经 `javap` 复核在正式版仍正确：

- `FoliagePlacer#createFoliage` 的 `TreeConfiguration` → `TreeFeature`（26.3 feature 重构）
- `Level#setBlock` 增加 `recursion` 形参（`setBlock(BlockPos, BlockState, int, int)`）
- `Blocks.STAINED_GLASS`（`ColorCollection`）等颜色集合
- `InputConstants.KEY_F8` 取代 `GLFW.GLFW_KEY_F8`（26.3 用 SDL 取代 GLFW）

### 3.3 26.3 移除的 API —— 本项目影响面为零

对 26.3 移除的 Fabric / vanilla API 逐项 grep，**全部 0 处引用**：

`FuelRegistry`、`CompostingChanceRegistry`、`FabricPotionBrewingBuilder`、
`StrippableBlockRegistry`、`TillableBlockRegistry`、`FlattenableBlockRegistry`、
`FluidVariantAttributes#enableColoredVanillaFluidNames`、`FeatureConfiguration`、`block_type`。

GUI 侧只用原版 `EditBox`（内部走 `InputConstants`），没有自定义文本输入控件，
因此 26.3 的 SDL `TextInputManager` 焦点变更**不受影响**。

---

## 四、Mixin 与源码调整

### 4.1 `FlatChunkGeneratorMixin`（真实注入点缺口，已修）

`@WrapMethod(method = "fillFromNoise")` 在 26.3 已不存在，会直接导致 mixin apply 失败。
改为按版本切换方法名与签名，并把 `original.call(...)` 也一并分支（26.3 的
`ChunkAccess` 移到了第一个参数）：

```java
//#if MC >= 260300
@WrapMethod(method = "buildTerrain")
public CompletableFuture<ChunkAccess> populateNoise(
        ChunkAccess chunk, Blender blender, RandomState noiseConfig, StructureManager structureAccessor,
        BiomeManager biomeManager, WorldGenRegion region, Set<Holder<Biome>> biomes,
        Operation<CompletableFuture<ChunkAccess>> original) {
//#else
@WrapMethod(method = "fillFromNoise") ...
```

`original.call` 的三分支（26.3 / ≤1.20.6 / 其余）均写成**整行** `//#if` 分支，
避免把预处理指令塞进方法调用参数中间（会生成被注释掉的参数，preprocess 报 `Unexpected else`）。

### 4.2 `FlatGridChunkGenerator`（服务端编译错误，已修）

同类问题：`fillFromNoise` 的 `@Override` 在 26.3 不再成立。把公共填充逻辑抽成私有方法
`fillGrid(ChunkAccess)`，上方用三分支声明入口（`buildTerrain` / 带 `Executor` 的旧签名 / 新签名）。
行为与原来完全一致，仅方法名与参数顺序随版本变化。

### 4.3 Mixin 静态检查结论

用 `tools/mixin_check.py` 对 `26.3` 与 `26.2` 基线各跑一遍并逐项 diff：

| | 26.2 基线 | 修复前 26.3 | 修复后 26.3 |
| --- | --- | --- | --- |
| 报告项数 | 24 | 26 | 25 |

- 修复前多出的 2 项中，`fillFromNoise not found in FlatLevelSource` 是**真实缺口**，已修（第四节 4.1）。
- 另一项 `LevelSnapshotMixin: MIXIN TARGET CLASS MISSING: net.minecraft.world.level.Level`
  经核实是**脚本误报**：`javap` 确认 `net/minecraft/world/level/Level.class` 在 26.2 与 26.3
  都存在且签名一致，脚本因未把简单类名 `Level` 解析为全限定名而误判。
  该文件里 `setBlock(BlockPos, BlockState, int, int)` 亦已用 `javap` 确认存在。
- 其余 24 项在 26.2 上同样存在（属脚本能力范围外的既有误报，如
  `com.mojang.brigadier.*` 类在独立依赖 jar 中、由 conditional-mixin 禁用的目标等），
  **不是本次升级引入的回归**。

> 结论：`26.3` 相对 `26.2` 的注入点差异已全部覆盖。

---

## 五、版本矩阵

| 节点 | Minecraft | Loader | Fabric API | Carpet | Java | Loom |
| --- | --- | --- | --- | --- | --- | --- |
| 1.20.4 | 1.20.4 | 0.17.3 | 见节点 properties | 1.20.4 线 | 17 | loom-remap |
| 1.20.6 | 1.20.6 | 0.17.3 | 同上 | 1.20.6 线 | 21 | loom-remap |
| 1.21.1 | 1.21.1 | 0.17.3 | 同上 | 1.21.1 线 | 21 | loom-remap |
| 1.21.3 / 1.21.4 / 1.21.5 / 1.21.6 / 1.21.9 / 1.21.11 | 同名 | 0.17.3 | 同上 | 对应线 | 21 | loom-remap |
| 26.1.2（覆盖 26.1 / 26.1.1 / 26.1.2） | 26.1.2 | 0.19.5 | 0.155.3+26.1.2 | 26.1+v260401 | 25 | loom |
| 26.2 | 26.2 | 0.19.5 | 0.160.0+26.2 | 26.2+v260616 | 25 | loom |
| **26.3（本次新增/替换）** | **26.3** | **0.19.5** | **0.160.5+26.3** | **26.3+v260915（本地 jar）** | **25** | **loom** |

- `26.3` 节点覆盖的 MC 版本：`>=26.3`。
- `26.2` 节点覆盖范围本改为 `>=26.2 <26.3`（原先误为开放上界，见第二节）。

---

## 六、用户验证清单（运行期，由用户执行）

> 前提：Agent 不代为启动游戏。以下命令请在项目根目录执行。
> 多节点共用 `run/` 目录，**跑第二个版本前请先改 `run/server.properties` 的 `level-name`**，
> 否则世界目录会冲突。

### 6.1 服务端启动（26.3）

```bash
bash ./gradlew :26.3:runServer --console=plain
```

判据：

- 日志出现 `Done (`. 开头的完成行。
- **无** `Mixin apply ... failed`、`was not located`、`No refMap loaded`。
- 无 `Incompatible mod set`（确认 Carpet 26.3 与 Fabric API 0.160.5 都从 `run/mods/` 之外正确加载）。

日志 grep：

```bash
grep -nE "Mixin apply|was not located|No refMap|Incompatible mod|Done \(" run/logs/latest.log
```

### 6.2 客户端启动（26.3，**不可省略**）

```bash
bash ./gradlew :26.3:runClient --console=plain
```

必须进入一个世界再退出。判据：`Sound engine started`、`Loaded \d+ advancements`，
且无 mixin apply 失败。

**为什么不能省**：`client` 段 mixin（渲染 / GUI / 按键）只在客户端加载。
本项目 26.2 升级时就是「只跑服务端全绿、一开客户端立刻因 `@Shadow` 失效崩溃」。

### 6.3 功能触发（本次改动直接相关）

本次修改的两个类都在「棋盘超平坦世界生成」功能上，**必须实测**：

1. 开服 / 进服后设置规则：
   ```
   /carpet chessboardSuperFlatSettings <任意值，例如 minecraft:white_stained_glass;minecraft:black_stained_glass;1>
   ```
2. 新建超平坦世界（或用 `/carpet` 打开规则界面确认该项可编辑），确认棋盘格正常生成
   —— 验证 `FlatChunkGeneratorMixin#buildTerrain` 生效。
3. 确认区块正常生成、无卡死/报错 —— 验证 `FlatGridChunkGenerator#buildTerrain` 生效。
4. 打开 carpet 规则编辑界面，在搜索框输入文字，确认能正常输入
   —— 覆盖 26.3 SDL 文本输入变更（本项目用原版 `EditBox`，预期正常）。
5. `/treeStats` 相关命令、假人生成等既有功能抽查一遍。

对应日志 grep：

```bash
grep -nE "buildTerrain|fillFromNoise|chessboard|FlatGridChunkGenerator" run/logs/latest.log
```

### 6.4 Mixin 审计（可选，更严格）

```bash
bash ./gradlew :26.3:runServerMixinAudit --console=plain
bash ./gradlew :26.3:runClientMixinAudit --console=plain
```

### 6.5 全节点产物自查

```bash
bash ./gradlew buildAndGather --console=plain
ls -la build/libs/*.jar
```

---

## 七、未验证项

以下内容**本次未执行**，需要用户确认：

1. **运行期一切结论**：26.3 的服务端与客户端均未启动过。
   编译 0 错误 + build 通过 + mixin 静态检查通过，只说明前置条件到位，不等于能跑起来。
2. **`buildTerrain` 的实际注入效果**：`@WrapMethod` 只是静态检查通过；
   真正的 `original.call(...)` 参数顺序与 26.3 运行时行为需在游戏里验证（见 6.3）。
3. **Carpet 本地 jar 在 CI 上是否可用**：`libs/` 会被 git 跟踪（已用 `git check-ignore` 确认），
   但 CI 实际跑通需要一次真实 push 验证。若 CI 失败，优先检查该 jar 是否随提交入库。
4. **Carpet 的 masa maven 同步**：一旦 masa maven 发布 `26.3+v260915`，
   可删除 `libs/` 下的 jar 直接走 maven（`common.gradle` 已写成分支兜底，无需改代码）。
   注意 masa maven 上 `26.3-beta-3` 仍在，不会被误选（本地 jar 优先）。
5. **旧节点（1.20.4 ~ 26.2）只验证了编译与出包**，未逐个启动。
6. **发布标注**：`game_versions=>=26.3` 已写入节点 properties，
   26.2 收窄为 `>=26.2 <26.3`，但未实际走发布流程验证。

---

## 八、本次改动文件清单

新增：

- `versions/26.3/gradle.properties`
- `versions/26.3/YACA.accesswidener`
- `versions/mapping-26.2-26.3.txt`
- `libs/fabric-carpet-26.3+v260915.jar`

修改：

- `build.gradle`（节点定义、link）
- `common.gradle`（Carpet 依赖分支）
- `settings.json`（版本列表）
- `versions/26.2/gradle.properties`（依赖上界收紧）
- `src/main/java/mypals/ml/mixin/features/gridWorldPreset/FlatChunkGeneratorMixin.java`（`buildTerrain` 分支）
- `src/main/java/mypals/ml/features/GridWorldGen/FlatGridChunkGenerator.java`（`buildTerrain` 分支 + 抽出 `fillGrid`）

删除：

- `versions/26.3-snapshot-9/`（退役，由 `26.3` 取代）
- `versions/mapping-26.2-26.3-snapshot-9.txt`
