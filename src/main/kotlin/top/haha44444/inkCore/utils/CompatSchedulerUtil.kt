package top.haha44444.inkCore.utils

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

// compat paper and folia
private fun isFolia(): Boolean =
    try {
        Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler")
        true
    } catch (_: ClassNotFoundException) { false }

fun runLater(plugin: Plugin, player: Player?, delayTicks: Long, task: () -> Unit) {
    if (isFolia()) {
        if (player != null) {
            // Folia：挂到玩家实体调度器
            player.scheduler.execute(plugin, { task() }, null, delayTicks)
        } else {
            // Folia：全局区域调度器
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, { task() }, delayTicks)
        }
    } else {
        // Paper/Spigot：老调度器（主线程）
        Bukkit.getScheduler().runTaskLater(plugin, Runnable { task() }, delayTicks)
    }
}
