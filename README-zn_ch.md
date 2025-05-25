# YACA 毯上添花 - Carpet规则介绍文档

本模组为 Carpet Mod 的扩展：**Yet Another Carpet Addition（YACA）**，提供了多种调试、性能优化、游戏机制调整等功能规则。以下为所有规则的详细介绍。

![owo](https://count.getloli.com/@YACAC?name=YACAC&theme=3d-num&padding=7&offset=0&align=top&scale=1&pixelated=1&darkmode=auto)

---

## 🧭 主亮点功能&指令

### /tick freeze <phase>      <- 暂停部分阶段，如 `entities` `block event` 

### `enableTickStepCounter` - 刻步进计数器

* **说明**：在 `/tick freeze` 后启用计数器，用于计算步进了多少个游戏刻。
* **用途**：便于观察游戏在冻结状态下的执行进度。

### `unicodeArgumentSupport` - Unicode参数支持

* **说明**：指令将支持非英文字符作为参数

### `commandEnhance` - 指令体验提升

* **说明**：提升部分指令的使用体验
现支持:

•	鼠标悬停于/clear指令的反馈上时将显示移除了什么，和分别移除了多少

•	鼠标悬停于/kill指令的反馈上时将显示杀死了什么，和分别杀死了多少

•	鼠标悬停于/data指令的反馈上时将显示data的具体更改内容

•	/data get 指令的反馈更加易查看；点击nbt建名可复制对应的/data modify指令

•  /gamerules 指令的反馈更加易查看；行为完全模仿了/carpet指令。

•  /help 指令的反馈更加易查看； 它将只返回可点击的根指令名，如 `[gamerule]` , 每个根指令名被点击后将展示更多指令格式信息


---

## 🌍 世界更新暂停类规则

### `stopCheckEntityDespawn` - 停止实体消失检查

* **说明**：禁止 Minecraft 对实体进行 despawn 检查，使其不会自然消失。

### `stopTickingEntities` - 停止实体更新

* **说明**：阻止怪物、动物、玩家等实体进行刻更新。

### `stopTickingBlockEntities` - 停止方块实体更新

* **说明**：冻结如箱子、熔炉等方块实体的状态更新。

### `stopTickingSpawners` - 阻止怪物生成器更新

* **说明**：暂停怪物生成行为。

### `stopTickingWorldBorder` - 停止世界边界更新

* **说明**：阻止世界边界在每个游戏刻更新其状态。

### `stopTickingWeather` - 停止天气更新

* **说明**：禁用游戏中的天气系统，如雨、雷暴。

### `stopTickingTime` - 停止时间流逝

* **说明**：暂停游戏内的白天/黑夜循环。

### `stopTickingBlocks` - 停止方块更新

* **说明**：冻结所有计划刻和随机刻，如作物生长、红石更新等。

### `stopTickingFluids` - 停止流体更新

* **说明**：禁止水、岩浆等液体进行流动更新。

### `stopTickingRaid` - 停止袭击更新

* **说明**：停止村庄袭击事件的状态推进。

### `stopTickingChunkManager` - 停止区块管理器更新

* **说明**：防止区块加载/卸载的管理逻辑运行。

### `stopTickingBlockEvents` - 停止方块事件更新

* **说明**：冻结如活塞等的方块事件队列。

### `stopTickingDragonFight` - 停止末影龙战斗更新

* **说明**：使末地中末影龙战斗流程暂停。

---

## 🔧 指令增强与便利性功能

### `bypassModifyPlayerDataRestriction` - /data 指令绕过玩家数据限制

* **说明**：允许通过 `/data` 指令修改玩家 NBT。
* **注意**：为实验性功能，可能会导致奇怪的行为。

### `bypassCrashForcibly` - 强行阻止崩溃

* **说明**：暴力尝试拦截服务器tick中发生的任何异常，防止游戏崩溃。
* **注意**：这可能导致崩溃调试的不便。

### `enableMountPlayers` - 解除骑乘玩家限制

* **说明**：允许其他实体骑乘玩家（如乘坐玩家的坐骑等）。
* **注意**：为实验性功能，可能会导致奇怪的行为。⚠️风险高，可能导致崩溃。

### `enchantCommandLimitOverwrite` - /enchant 指令上限覆写

* **说明**：允许 `/enchant` 指令施加高达 255 级的附魔。

### `enchantCommandBypassItemType` - /enchant 指令物品类型限制解除

* **说明**：可将任意附魔加到任意物品上。

### `mergeSmartAndRegularCommandSuggestions` - 合并智能与普通指令提示

* **说明**：将carpet规则指令的智能提示建议和普通提示建议融合。

### `silenceTP` - 无声传送

* **说明**：传送真人玩家时将其设置为旁观者，避免对他人造成干扰。

### `commandEasyItemShadowing` - 简单物品分身

* **说明**：使用 `/itemshadowing` 快速将主手物品复制至副手。

### `commandRenameItem` - 快速重命名

* **说明**：使用 `/rename <名称>` 命名手中物品，空参数恢复原名。

---

## 🧪 危险功能（可能导致崩溃）

### `instantSchedule` - 瞬时计划刻

* **说明**：立即处理所有计划刻事件，⚠️风险高，可能导致崩溃。

### `instantFalling` - 瞬时落沙

* **说明**：沙子、沙砾等立即坠落。

---

## 🏗 结构与行为优化

### `optimizedStructureBlock` - 优化结构方块

* **说明**：提升结构方块在检测区域角落时的性能。

### `morphMovingPiston` - 变形移动活塞

* **说明**：移动活塞更准确地模拟其持有方块的行为。
* **注意**：为实验性功能，可能会导致奇怪的行为。

### `movingPistonSpeed` - 移动活塞速度

* **说明**：控制活塞推进动画的速度。

### `blocksNoSelfCheck` - 方块无自检

* **说明**：禁用方块的 `OnBlockAdded()` 逻辑

### `blocksNoSuffocate` - 方块无窒息

* **说明**：防止方块造成窒息效果 —— 禁用窒息机制

### `blocksPlaceAtAnywhere` - 方块任意放置

* **说明**：允许在无效或通常受限制的位置放置方块

### `blockNoBreakParticles` - 方块无破坏粒子

* **说明**：破坏方块时不会生成粒子效果

---

## 🛏 杂项功能拓展

### `bedsRecordSleeperFacing` - 床记录使用者朝向

* **说明**：记录玩家在入睡时的朝向。

### `copyablePlayerMessages` - 可复制的玩家消息

* **说明**：聊天栏消息可被复制（客户端侧增强体验）。

### `moreHardCollisions` - 更多硬碰撞

* **说明**：让更多实体之间有物理碰撞箱。
* **注意**：可能会导致奇怪的行为。

### `farlandReintroduced` - 边境之地

* **说明**：重新添加 Minecraft Beta 版中的“边境之地”特性。

### `fallingSnowLayers` - 雪片下坠

* **说明**：雪层像沙子一样受重力影响会下坠。

---

## 🌟 可视化调试功能

### `scheduledTickVisualize` - 可视化计划刻

* **说明**：在世界中渲染计划刻的信息（红：距离触发的时间；绿：优先级；蓝：创建顺序）。

### `hopperCooldownVisualize` - 漏斗冷却可视化

* **说明**：显示每个漏斗的冷却状态。

### `randomTickVisualize` - 随机刻可视化

* **说明**：展示随机刻击中的位置。

### `gameEventVisualize` - 游戏事件可视化

* **说明**：可视化游戏事件，如脚步声、方块互动等。

### `blockEventVisualize` - 方块事件可视化

* **说明**：可视化如活塞等的方块事件顺序。

### `blockUpdateVisualize` - NC更新可视化

* **说明**：展示NC更新发生的位置。

### `stateUpdateVisualize` - PP更新可视化

* **说明**：展示PP更新发生的位置

### `comparatorUpdateVisualize` - 比较器更新可视化

* **说明**：展示比较器更新发生的位置
---

## 🔦 光照控制功能

### `forceMaxLightLevel` - 强制最大光照级别

* **说明**：所有方块和天空光照恒定为 15（完全明亮）。
* **注意**：可能会导致奇怪的行为。

### `disableLightUpdate` - 禁用光照更新

* **说明**：阻止光照传播和重新计算，保持当前光照静态。
* **注意**：可能会导致奇怪的行为。

---
## 指令
###  `/scheduleTick` 指令格式

```
/scheduleTick <pos> <block> <time> <priority>
```

* `pos`: 目标方块的位置（例如：`0 64 0`）
* `block`: 方块状态（例如：`minecraft:stone`）
* `time`: 延迟时间（整数，≥0）
* `priority`: 优先级（范围：-3 到 3）

### `/blockEvent` 指令格式

```
/blockEvent <pos> <block> <type> <data>
```

* `pos`: 目标方块的位置
* `block`: 方块状态
* `type`: 事件类型（0\~2）
* `data`: 附加数据（0\~5）

###  `/randomTick` 指令格式

```
/randomTick <pos>
```

* `pos`: 方块位置

###  `/gameEvent` 指令格式（可组合参数）

```
/gameEvent <pos> <reason> [entity] [blockstate]
```

支持以下组合：

1. 只提供 `<pos> <reason>`
2. 提供 `<pos> <reason> <entity>`
3. 提供 `<pos> <reason> <blockstate>`
4. 提供 `<pos> <reason> <entity> <blockstate>`

* `pos`: 事件位置（三维坐标，如：`0.5 64.0 0.5`）
* `reason`: 事件名称（字符串）
* `entity`: 触发事件的实体（可选）
* `blockstate`: 方块状态（可选）

### `/worldEvent` 指令格式（支持两种形式）

1. 带玩家参数：

```
/worldEvent <player> <pos> <event> <data>
```

2. 不带玩家参数：

```
/worldEvent <pos> <event> <data>
```

* `player`: 玩家实体（可选）
* `pos`: 事件位置
* `event`: 事件名称（支持建议）
* `data`: 附加数据（整数）

### `/rename` 指令格式（用于重命名物品）

该指令有两种使用方式：

1. 指定名称：

```
/rename <name>
```

* `name`: 新名称（字符串，可以包含空格，需加引号）

2. 不带参数：

```
/rename
```

* 回归默认名

---

###  `/itemshadowing` 指令格式

```
/itemshadowing
```

* 将主手物品分身到副手

以下是你提供的 Minecraft 指令注册代码中 `waypoint` 指令相关的格式说明（中文，仅说明格式，不解释功能或用途）：

---

###  `/waypoint` 指令（主命令）

#### 子命令：`/waypoint save`（保存一个新的路径点）

1. **保存路径点到玩家当前位置**：

   ```
   /waypoint save <name>
   ```
   ```
   /waypoint save <name> <pos>
   ```

   * `name`: 路径点名称（字符串）
   * `pos`: 路径点位置（方块坐标）

#### 子命令：`/waypoint remove`（删除指定名称的路径点）

```
/waypoint remove <name>
```

* `name`: 要删除的路径点名称（字符串）

#### 子命令：`/waypoint tp`（传送到指定名称的路径点）

```
/waypoint tp <name>
```

* `name`: 路径点名称（字符串）

#### 子命令：`/waypoint list`（列出所有已保存的路径点）

```
/waypoint list
```

* 无参数，显示所有已保存的路径点名称，并且每个名称都是可点击的链接，点击后会传送到该路径点。
---

## 📱 GUI功能

### 地毯规则GUI（默认F9）开启

* **说明**：为carpet规则准备的GUI。
  ![img.png](carpetGUI.png)
* **注意**：服务器与客户端均需要安装。

### 漏斗计数器GUI（/counterGUI）开启

* **说明**：为漏斗计数器准备的GUI。
  ![img.png](counterGUI.png)
*
* **注意**：请设置HopperCounterDataRecorder规则 为非 off 的任意数字，
  这个数字将代表服务器上间隔多少tick记录一次漏斗计数器的数据。
  此外，确保 hopperCounters规则被打开。
  这是个BETA测试项目，有待完善。
  使用结束后请记得关闭HopperCounterDataRecorder（设为 off），
  （可选：清空漏斗计数器，这回一并清楚缓存文件）
  避免堆积多余数据。
