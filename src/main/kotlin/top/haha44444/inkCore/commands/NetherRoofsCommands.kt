package top.haha44444.inkCore.commands

import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import top.haha44444.inkCore.InkCore
import java.util.Locale
import top.haha44444.inkCore.event.PlayerOnNetherRoofs
import top.haha44444.inkCore.utils.colorize
import top.haha44444.inkCore.utils.runLater

class NetherRoofsCommands(private val plugin: InkCore) : CommandExecutor, TabCompleter {
    // config
    var netherRoofsCommandPermission: String? = null
    var notInNetherMsg: String? = null
    var inRoofsMsg: String? = null
    var inFloorMsg: String? = null
    var noPermissionMsg: String? = null
    var consoleMsg: String? = null

    fun loadConfig() {
        netherRoofsCommandPermission = plugin.config.getString("height-limits.nether.permission")
        notInNetherMsg = plugin.config.getString("height-limits.nether.messages.not-in-nether")?.colorize()
        inRoofsMsg = plugin.config.getString("height-limits.nether.messages.in-roofs")?.colorize()
        inFloorMsg = plugin.config.getString("height-limits.nether.messages.in-floor")?.colorize()
        noPermissionMsg = plugin.config.getString("height-limits.nether.messages.no-permission")?.colorize()
        consoleMsg = plugin.config.getString("height-limits.nether.console")?.colorize()
    }

    init {
        loadConfig()
    }

    private fun CommandSender.msg(text: String) {
        this.sendMessage(Component.text(text))
    }
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) {
            consoleMsg?.let { sender.msg(it) }
            return true
        }
        netherRoofsCommandPermission?.let {
            if (!sender.hasPermission(it)) {
                noPermissionMsg?.let { text -> sender.msg(text) }
                return true
            }
        }

        val name = command.name.lowercase(Locale.ROOT)
        val loc = sender.location

        when(name){
            "totop" -> {
                // not in nether
                if (!isInNether(loc)) {
                    notInNetherMsg?.let { sender.msg(it) }
                    return true
                }
                if (PlayerOnNetherRoofs(plugin).isOnNetherRoof(loc)) {
                    inRoofsMsg?.let { sender.msg(it) }
                    return true
                }
                // teleport to top
                val w = sender.world
                val targetLoc = Location(w, loc.blockX + 0.5, 128.0, loc.blockZ + 0.5)

                runLater(plugin, sender, 1L) {
                    sender.teleportAsync(targetLoc)
                }
            }

            "todown" -> {
                // not in nether
                if (!isInNether(loc)) {
                    notInNetherMsg?.let { sender.msg(it) }
                    return true
                }
                if (!PlayerOnNetherRoofs(plugin).isOnNetherRoof(loc)) {
                    inFloorMsg?.let { sender.msg(it) }
                    return true
                }

                // teleport to down

                runLater(plugin, sender, 1L) {
                    PlayerOnNetherRoofs(plugin).correctEntityDownwards(sender)
                }
            }

        }
        return true
    }

    private fun isInNether(loc: Location): Boolean {
        if (loc.world == null) return false
        return loc.world.environment == World.Environment.NETHER
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        return mutableListOf()
    }


}