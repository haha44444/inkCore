package top.haha44444.inkCore.event

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import top.haha44444.inkCore.InkCore

class PlayerChatEvent(private val plugin: InkCore): Listener {
    // config
    var greenNamePermission: String ?= "inkcore.chat.name.green"
    var greenMessagePermission: String ?= "inkcore.chat.message.green"

    fun loadConfig() {
        greenMessagePermission = plugin.config.getString("color.green-message-permission")
        greenNamePermission = plugin.config.getString("color.green-name-permission")
    }

    init {
        loadConfig()
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerChat(e: AsyncChatEvent) {
        val player = e.player
        val msg = PlainTextComponentSerializer.plainText().serialize(e.message())
        // reset color
        player.displayName(Component.text(player.name).style(Style.empty()))
        e.message(Component.text(msg).style(Style.empty()))
        // green chat
        greenMessagePermission?.let {
            if (player.hasPermission(it)) {
                e.message(Component.text(msg, NamedTextColor.GREEN))
            }
        }
        // green name
        greenNamePermission?.let {
            if (player.hasPermission(it)) {
                player.displayName(Component.text(player.name, NamedTextColor.GREEN))
            }
        }
    }
}