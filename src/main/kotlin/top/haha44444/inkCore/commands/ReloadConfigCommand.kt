package top.haha44444.inkCore.commands

import net.kyori.adventure.text.Component
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import top.haha44444.inkCore.InkCore
import top.haha44444.inkCore.utils.colorize
import java.util.Locale

class ReloadConfigCommand(private val plugin: InkCore): CommandExecutor, TabCompleter {
    // config
    private var adminPermission: String? = null
    private var noPermissionMsg: String? = null
    private var reloadedMsg: String? = null

    fun loadConfig() {
        adminPermission = plugin.config.getString("reload.permission")
        noPermissionMsg = plugin.config.getString("reload.messages.no-permission")?.colorize()
        reloadedMsg = plugin.config.getString("reload.messages.reloaded")?.colorize()
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
        adminPermission?.let {
            if (!sender.hasPermission(it)) {
                noPermissionMsg?.let { text -> sender.msg(text) }
                return true
            }
        }
        if (args.isEmpty()) return false

        val name = command.name.lowercase(Locale.ROOT)
        val arg0 = args[0].lowercase(Locale.ROOT)
        val plugin = plugin

        when (name) {
            "inkcore" ->
                if (args.size == 1) {
                    when (arg0) {
                        "reload" -> {
                            plugin.reloadConfig()
                            plugin.storage.reloadFromDisk()
                            // cmd
                            this.loadConfig()
                            plugin.statCmd.loadConfig()
                            plugin.homeCmd.loadConfig()
                            plugin.netherRoofsCmd.loadConfig()
                            // event
                            plugin.playerOnNetherRoofs.loadConfig()
                            plugin.frameBreakEventAll.loadConfig()
                            plugin.frameBreakEventSpecific.loadConfig()
                            plugin.playerQuitListener.loadConfig()

                            reloadedMsg?.let { sender.msg(it) }
                        }
                    }
                } else return false

        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        // do not tab complete when no permission
        adminPermission?.let { if (!sender.hasPermission(it)) return mutableListOf() }

        val name = command.name.lowercase(Locale.ROOT)
        when (name) {
            // main
            "inkcore" -> {
                if (args.size == 1) {
                    return mutableListOf("reload")
                }

            }
        }
        return mutableListOf()
    }
}