package top.haha44444.inkCore.event

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.pistonmaster.pistonchat.api.PistonChatReceiveEvent
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import top.haha44444.inkCore.InkCore

class PistonPlayerChatEvent(private val plugin: InkCore): ChatEventHandler  {
    // config
    override var greenNamePermission: String ?= "inkcore.chat.name.green"
    override var greenMessagePermission: String ?= "inkcore.chat.message.green"

    fun loadConfig() {
        greenMessagePermission = plugin.config.getString("color.green-message-permission")
        greenNamePermission = plugin.config.getString("color.green-name-permission")
    }

    init {
        loadConfig()
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerChat(e: PistonChatReceiveEvent) {
        val player = e.sender
        val raw = e.message

        // green chat
        greenMessagePermission?.let {
            if (player.hasPermission(it)) {
                e.message = "${ChatColor.GREEN}$raw"
            }
        }
        // green name
        greenNamePermission?.let {
            if (player.hasPermission(it)) {
                val format = Component.text()
                    .append(Component.text("<", NamedTextColor.WHITE))
                    .append(Component.text(e.sender.name, NamedTextColor.GREEN))
                    .append(Component.text(">", NamedTextColor.WHITE))
                    .build()
                e.format = format
            }
        }
    }
}