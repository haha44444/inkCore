package top.haha44444.inkCore.commands

import net.kyori.adventure.text.Component
import net.luckperms.api.LuckPerms
import net.luckperms.api.node.types.PermissionNode
import org.bukkit.Statistic
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import top.haha44444.inkCore.InkCore
import top.haha44444.inkCore.utils.colorize
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.Date
import java.util.Locale
import kotlin.text.replace

class StatCommands(private val plugin: InkCore) : CommandExecutor, TabCompleter {
    // config
    var pattern: String ?= null
    var yesIcon: String ?= null
    var noIcon: String ?= null
    var tempYesIcon: String ?= null
    var consoleMsg: String ?= null
    var statMsg: String ?= null

    init {
        loadConfig()
    }

    fun loadConfig() {
        pattern = plugin.config.getString("stat.pattern")?.colorize()
        yesIcon = plugin.config.getString("stat.messages.yes-icon")?.colorize()
        noIcon = plugin.config.getString("stat.messages.no-icon")?.colorize()
        tempYesIcon = plugin.config.getString("stat.messages.temp-yes-icon")?.colorize()
        consoleMsg = plugin.config.getString("stat.messages.console")?.colorize()
        statMsg = plugin.config.getString("stat.messages.stat")?.colorize()
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
        // console
        if (sender !is Player) {
            consoleMsg?.let { sender.msg(it) }
            return true
        }

        val name = command.name.lowercase(Locale.ROOT)

        when(name){
            "stat" -> {
                val simpleDateFormat = SimpleDateFormat(pattern)
                val onlineTime = getOnlineTime(sender)
                val firstJoinTime = simpleDateFormat.format(Date(sender.firstPlayed))
                val lastQuitTime = simpleDateFormat.format(Date(sender.lastPlayed))
                val deathCount = getDeathCount(sender)
                val killedPlayers = getKilledPlayers(sender)
                // check permission
                val hasNetherRoofsPermission = checkPermission(sender, plugin.netherRoofsCmd.netherRoofsCommandPermission.toString())
                val hasHomePermission = checkPermission(sender, plugin.homeCmd.homePermission.toString())
                val hasGreenChat = checkPermission(sender, plugin.playerChatEvent.greenMessagePermission.toString())
                val hasGreenName = checkPermission(sender, plugin.playerChatEvent.greenNamePermission.toString())
                val hasFrameDupe = checkPermission(sender, plugin.frameBreakEventAll.level1Permission.toString())
                val hasFrameDupeVip = checkPermission(sender, plugin.frameBreakEventAll.level2Permission.toString())
                val hasGlowFrameDupe = checkPermission(sender, plugin.frameBreakEventSpecific.level1Permission.toString())
                val hasGlowFrameDupeVip = checkPermission(sender, plugin.frameBreakEventSpecific.level2Permission.toString())


                statMsg?.let {
                    sender.msg(
                        it
                            // online time
                            .replace("%onlineTime_days%", "${onlineTime.days}")
                            .replace("%onlineTime_hours%", "${onlineTime.hours}")
                            .replace("%onlineTime_minutes%", "${onlineTime.minutes}")
                            .replace("%onlineTime_seconds%", "${onlineTime.seconds}")
                            // first join
                            .replace("%firstJoinTime%", "$firstJoinTime")
                            // last quit
                            .replace("%lastQuitTime%", "$lastQuitTime")
                            // death count
                            .replace("%deathCount%", "$deathCount")
                            // killed players
                            .replace("%killedPlayers%", "$killedPlayers")
                            // have permission
                            .replace("%hasNetherRootfsPermission%", hasNetherRoofsPermission)
                            .replace("%hasHomePermission%", hasHomePermission)
                            .replace("%hasGreenChat%", hasGreenChat)
                            .replace("%hasGreenName%", hasGreenName)
                            .replace("%hasFrameDupe%", hasFrameDupe)
                            .replace("%hasFrameDupeVip%", hasFrameDupeVip)
                            .replace("%hasGlowFrameDupe%", hasGlowFrameDupe)
                            .replace("%hasGlowFrameDupeVip%", hasGlowFrameDupeVip)
                    )
                }


            }
        }


        return true
    }

    data class DHMS(
        val days: Long,
        val hours: Int,   // 0..23
        val minutes: Int, // 0..59
        val seconds: Int  // 0..59
    )

    private fun getOnlineTime(player: Player): DHMS {
        val ticks: Int = player.getStatistic(Statistic.PLAY_ONE_MINUTE)
        val totalSeconds = ticks / 20L

        val days = totalSeconds / 86_400L
        val remainderAfterDays = totalSeconds % 86_400L

        val hours = (remainderAfterDays / 3_600L).toInt()
        val remainderAfterHours = remainderAfterDays % 3_600L

        val minutes = (remainderAfterHours / 60L).toInt()
        val seconds = (remainderAfterHours % 60L).toInt()

        return DHMS(days, hours, minutes, seconds)

    }

    private fun getDeathCount(player: Player): Int {
        return player.getStatistic(Statistic.DEATHS)
    }

    private fun getKilledPlayers(player: Player): Int {
        return player.getStatistic(Statistic.PLAYER_KILLS)
    }

    private fun hasPermissionExpired(player: Player, permission: String, includeExpired: Boolean = false): Boolean? {
        val luckPerms = plugin.server.servicesManager.load(LuckPerms::class.java)
        val user = luckPerms?.getPlayerAdapter(Player::class.java)?.getUser(player)

        return user?.resolveInheritedNodes(user.queryOptions)
            ?.asSequence()
            ?.filterIsInstance<PermissionNode>()
            ?.filter {it.permission.equals(permission, true) }
            ?.any { it.hasExpiry() && (includeExpired || !it.hasExpired()) }
    }

    private fun getPermissionExpired(player: Player, permission: String): Duration? {
        val luckPerms = plugin.server.servicesManager.load(LuckPerms::class.java)
        val user = luckPerms?.getPlayerAdapter(Player::class.java)?.getUser(player)

        return user?.resolveInheritedNodes(user.queryOptions)
            ?.asSequence()
            ?.filterIsInstance<PermissionNode>()
            ?.filter {it.permission.equals(permission, true) }
            ?.filter { it.hasExpiry() && !it.hasExpired() }
            ?.mapNotNull {it.expiryDuration}
            ?.maxOrNull()
    }

    private fun formatDuration(dur: Duration?): String {
        val seconds = dur?.seconds ?: return "-"
        val d = seconds / 86_400
        val h = (seconds % 86_400) / 3_600
        val m = (seconds % 3_600) / 60
        val s = seconds % 60
        return buildString {
            if (d > 0) append("${d}天 ")
            if (h > 0 || d > 0) append("${h}小时 ")
            if (m > 0 || h > 0 || d > 0) append("${m}分钟 ")
            append("${s}秒")
        }.trim()
    }

    private fun checkPermission(player: Player, permission: String): String {
        val yes = yesIcon.orEmpty()
        val no = noIcon.orEmpty()
        val temp = tempYesIcon.orEmpty()

        val timeText = formatDuration(getPermissionExpired(player, permission))

        return if (player.hasPermission(permission)) {
            if (hasPermissionExpired(player, permission) == true) {
                temp.replace("%time%", timeText)
            } else {
                yes
            }
        } else {
            no
        }
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