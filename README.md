# inkCore

A Paper plugin for 2b2t.ink anarchy server.  
提供基础功能和实用工具，帮助玩家在无政府服务器环境中更好地游戏。
---

## Features
- **Home 系统**：设置、删除和传送至家（支持设置家数量）
- **下界上层 权限控制**：限制/传送至下界顶层与底层
- **统计信息**：显示玩家在线时长、加入时间、击杀与死亡等
- **Frame Dupe 支持**：物品展示框和荧光展示框复制（含 VIP 权限倍率）
- **配置热重载**：通过命令快速重载插件配置
- **PistonChat兼容**：可以在启用PistonChat时同时使用聊天/id颜色（需要使用我的[PistonChat fork](https://github.com/haha44444/PistonChat)，构建文件可以在[此处](https://github.com/haha44444/inkCore/tree/main/lib)找到）

## Commands
- `/sethome <1|2|3|x>` - 设置家
- `/delhome <1|2|3|x>` - 删除家
- `/home <1|2|3|x>` - 回家
- `/homes` - 查看已设置的家
- `/totop` - 传送到下界顶层
- `/todown` - 传送到下界底层
- `/stat` - 查看玩家统计与权限
- `/inkcore reload` (`/ic reload`) - 重载`config.yml`和`data.yml`

## Permissions
- `inkcore.command.home` - 使用`/home` `/homes` `/addhome` `/delhome`指令
- `inkcore.nether.roofs.bypass` - 进入下界上层+使用`/totop` `/todown`指令
- `inkcore.admin` - `/inkcore reload`指令
- `inkcore.frame-dupe` / `inkcore.frame-dupe.vip` - 荧光/普通 展示框复制，荧光/普通 展示框复制vip节点
- `inkcore.chat.name.green` / `inkcore.chat.message.green` - 绿色名字，聊天

## Thanks
This project is inspired by and thanks to:  
- [FrameDupe by MrRafter](https://github.com/MrRafter/FrameDupe)
- [PistonChat by AlexProgrammerDE](https://github.com/AlexProgrammerDE/PistonChat)


---

