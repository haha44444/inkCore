package top.haha44444.inkCore.commands

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import top.haha44444.inkCore.InkCore
import top.haha44444.inkCore.storage.HomeStorage
import top.haha44444.inkCore.utils.colorize
import java.util.*
import java.util.concurrent.ConcurrentHashMap


class HomeCommands(private val plugin: InkCore, private val storage: HomeStorage) : CommandExecutor, TabCompleter, Listener {
    // config
    var maxHomes: Int = 3
    var warmupSeconds: Int = 5

    var homePermission: String? = null
    var consoleMsg: String? = null
    var noPermissionMsg: String? = null
    var setHomeMsg: String? = null
    var delHomeMsg: String? = null
    var homeNotFoundMsg: String? = null
    var teleportFieldMsg: String? = null
    var teleportWaitMsg: String? = null
    var teleportWaitTimerErrorMsg: String? = null
    var hoverMsg: String? = null
    var haveNotHomeMsg: String? = null
    var urHomeMsg: String? = null
    var teleportCancelMsg: String? = null
    var commandUsageMsg: String? = null
    var linePartMsg: String? = null

    fun loadConfig() {
        maxHomes = plugin.config.getInt("home.max-homes")
        warmupSeconds = plugin.config.getInt("home.teleport-warmup-seconds")

        homePermission = plugin.config.getString("home.permission")
        consoleMsg = plugin.config.getString("home.messages.console")?.colorize()
        noPermissionMsg = plugin.config.getString("home.messages.no-permission")?.colorize()
        setHomeMsg = plugin.config.getString("home.messages.set-home")?.colorize()
        delHomeMsg = plugin.config.getString("home.messages.del-home")?.colorize()
        homeNotFoundMsg = plugin.config.getString("home.messages.home-not-found")?.colorize()
        teleportFieldMsg = plugin.config.getString("home.messages.teleport-failed")?.colorize()
        teleportWaitMsg = plugin.config.getString("home.messages.teleport-wait")?.colorize()
        teleportWaitTimerErrorMsg = plugin.config.getString("home.messages.teleport-wait-timer-error")?.colorize()
        hoverMsg = plugin.config.getString("home.messages.hover")?.colorize()
        haveNotHomeMsg = plugin.config.getString("home.messages.have-not-home")?.colorize()
        urHomeMsg = plugin.config.getString("home.messages.ur-home")?.colorize()
        teleportCancelMsg = plugin.config.getString("home.messages.teleport-cancel")?.colorize()
        commandUsageMsg = plugin.config.getString("home.messages.command-usage")?.colorize()
        linePartMsg = plugin.config.getString("home.messages.line-part")?.colorize()
    }

    init {
        loadConfig()
    }

    private val pending: MutableMap<UUID, ScheduledTask> = ConcurrentHashMap()
    private val startBlockLoc: MutableMap<UUID, Location> = ConcurrentHashMap()

    private fun CommandSender.msg(text: String, color: NamedTextColor = NamedTextColor.YELLOW) {
        this.sendMessage(Component.text(text).color(color))
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
        homePermission?.let {
            if (!sender.hasPermission(it)) {
                noPermissionMsg?.let { text -> sender.msg(text) }
                return true
            }
        }

        val name = command.name.lowercase(Locale.ROOT)
        val index = args.getOrNull(0)?.toIntOrNull()
        val commandUsageMsgReplaced = commandUsageMsg?.replace("%command%", name)


        when (name) {
            "sethome" -> {
                if (index == null || index !in 1..maxHomes) {
                    commandUsageMsgReplaced?.let { sender.msg(it) }
                    return true
                }
                val loc = sender.location
                storage.setHome(sender.uniqueId, index, loc)
                storage.saveAsync()
                setHomeMsg?.let { sender.msg(it.replace("%home%", "$index")) }
            }
            "delhome" -> {
                if (index == null || index !in 1..maxHomes) {
                    commandUsageMsgReplaced?.let { sender.msg(it) }
                    return true
                }
                val ok = storage.deleteHome(sender.uniqueId, index)
                if (ok) {
                    storage.saveAsync()
                    delHomeMsg?.let { sender.msg(it.replace("%home%", "$index")) }
                } else {
                    homeNotFoundMsg?.let { sender.msg(it.replace("%home%", "$index")) }
                }
            }
            "home" -> {
                if (index == null || index !in 1..maxHomes) {
                    commandUsageMsgReplaced?.let { sender.msg(it) }
                    return true
                }
                val loc = storage.getHome(sender.uniqueId, index)
                if (loc == null || loc.world == null) {
                    homeNotFoundMsg?.let { sender.msg(it.replace("%home%", "$index")) }
                    return true
                }

                val s = warmupSeconds.coerceAtLeast(0)
                if (s == 0) {
                    sender.teleportAsync(loc).thenAccept { success ->
                        if (!success) teleportFieldMsg?.let { sender.sendMessage(it) }
                    }
                    return true
                }
                // cancel wait
                cancelPending(sender.uniqueId, silent = true)

                startBlockLoc[sender.uniqueId] = sender.location.toBlockLocation()

                teleportWaitMsg
                    ?.replace("%home%", "$index")?.let {
                        sender.sendMessage(it
                            .replace("%second%", "$s")
                        )
                    }

                val ticks = (s * 20L).coerceAtLeast(1L)

                val task = sender.scheduler.runDelayed(plugin, { _: ScheduledTask ->
                    if (!pending.containsKey(sender.uniqueId)) return@runDelayed
                    pending.remove(sender.uniqueId)
                    startBlockLoc.remove(sender.uniqueId)

                    sender.teleportAsync(loc).thenAccept { success ->
                        if (!success) teleportFieldMsg?.let { sender.sendMessage(it) }
                    }
                }, null, ticks)

                if (task != null) {
                    pending[sender.uniqueId] = task
                } else {
                    teleportWaitTimerErrorMsg?.let { sender.msg(it) }
                    startBlockLoc.remove(sender.uniqueId)
                }
            }

            "homes" -> {
                val homes = storage.listHomeIndices(sender.uniqueId)
                if (homes.isEmpty()) {
                    haveNotHomeMsg?.let { sender.msg(it) }
                    return true
                }
                var line = urHomeMsg?.let { Component.text(it) } ?: Component.empty()
                homes.forEachIndexed { idx, h ->
                    val part = Component.text(linePartMsg + h)
                        .hoverEvent(hoverMsg?.let { HoverEvent.showText(Component.text(it.replace("%home%", "$h"))) })
                        .clickEvent(ClickEvent.runCommand("/home $h"))
                    line = line.append(part)
                    if (idx != homes.lastIndex) line = line.append(Component.text(" "))
                }
                sender.sendMessage(line)
            }

        }
        return true
    }

    @EventHandler
    private fun onMove(e: PlayerMoveEvent) {
        val uuid = e.player.uniqueId
        if (!pending.containsKey(uuid)) return
        val fromBlock = startBlockLoc[uuid] ?: return
        val to = e.to

        if (to.blockX != fromBlock.blockX || to.blockY != fromBlock.blockY || to.blockZ != fromBlock.blockZ) {
            cancelPending(uuid)
        }
    }

    @EventHandler
    private fun onDamage(e: EntityDamageByEntityEvent) {
        val uuid = e.entity.uniqueId
        cancelPending(uuid)
    }

    @EventHandler
    private fun onQuit(e: PlayerQuitEvent) {
        if (pending.containsKey(e.player.uniqueId)) {
            cancelPending(e.player.uniqueId, silent = true)
        }
    }

    private fun cancelPending(uuid: UUID, silent: Boolean = false) {
        pending.remove(uuid)?.cancel()
        startBlockLoc.remove(uuid)
        if (!silent) {
            val p = Bukkit.getPlayer(uuid) ?: return
            p.scheduler.run(plugin, { _: ScheduledTask ->
                teleportCancelMsg?.let { p.sendMessage(it) }
            }, null)
        }
    }



    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        if (sender is Player && args.size == 1) {
            val name = command.name.lowercase(Locale.ROOT)
            return when (name) {
                "home" -> {
                    val list = storage.listHomeIndices(sender.uniqueId)
                    list.map(Int::toString)
                        .filter { it.startsWith(args[0]) }
                        .toMutableList()
                }
                else -> {
                    (1..maxHomes).map(Int::toString)
                        .filter { it.startsWith(args[0]) }
                        .toMutableList()
                }
            }
        }
        return mutableListOf()
    }
}