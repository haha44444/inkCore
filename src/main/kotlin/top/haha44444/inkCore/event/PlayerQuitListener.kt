package top.haha44444.inkCore.event

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import top.haha44444.inkCore.InkCore

class PlayerQuitListener(val plugin: InkCore): Listener {
    // config
    var enable: Boolean = true

    fun loadConfig() {
        enable = plugin.config.getBoolean("deop-on-quit")
    }

    init {
        loadConfig()
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        if (enable) {
            if (player.isOp) {
                player.isOp = false
            }
        }
    }
}