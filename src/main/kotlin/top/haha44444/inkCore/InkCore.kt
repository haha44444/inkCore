package top.haha44444.inkCore

import org.bukkit.plugin.java.JavaPlugin
import top.haha44444.inkCore.commands.HomeCommands
import top.haha44444.inkCore.commands.NetherRoofsCommands
import top.haha44444.inkCore.commands.ReloadConfigCommand
import top.haha44444.inkCore.commands.StatCommands
import top.haha44444.inkCore.commands.event.LoggerFunction
import top.haha44444.inkCore.commands.event.LoggerFunctionCompat
import top.haha44444.inkCore.event.ChatEventHandler
import top.haha44444.inkCore.event.FrameBreakEvent
import top.haha44444.inkCore.event.PistonPlayerChatEvent
import top.haha44444.inkCore.event.PlayerChatEvent
import top.haha44444.inkCore.event.PlayerOnNetherRoofs
import top.haha44444.inkCore.storage.HomeStorage
import top.haha44444.inkCore.utils.GetMinecraftVersionUtil

class InkCore : JavaPlugin() {
    lateinit var storage: HomeStorage
        private set

    lateinit var statCmd: StatCommands
        private set

    lateinit var netherRoofsCmd: NetherRoofsCommands
        private set

    lateinit var homeCmd: HomeCommands
        private set

    lateinit var getMinecraftVersionUtil: GetMinecraftVersionUtil
        private set

    lateinit var frameBreakEventAll: FrameBreakEvent.FrameAll
        private set

    lateinit var frameBreakEventSpecific: FrameBreakEvent.FrameSpecific
        private set

    lateinit var playerOnNetherRoofs: PlayerOnNetherRoofs
        private set

    lateinit var playerChatEvent: ChatEventHandler
        private set


    override fun onEnable() {
        // Plugin startup logic
        logger.info("Plugin has been enabled!")
        logger.info("author: haha44444")

        getMinecraftVersionUtil = GetMinecraftVersionUtil()
        val mcVersion = getMinecraftVersionUtil.getMinecraftVersion()
        logger.info("Minecraft version: 1.$mcVersion")
        this.saveDefaultConfig()

        // init storage
        storage = HomeStorage(this)
        storage.init()

        // cmd
        val reloadConfigCmd = ReloadConfigCommand(this)
        homeCmd = HomeCommands(this, storage)
        netherRoofsCmd = NetherRoofsCommands(this)
        statCmd = StatCommands(this)

        // event
        playerOnNetherRoofs = PlayerOnNetherRoofs(this)
        frameBreakEventAll = FrameBreakEvent(this).FrameAll()
        frameBreakEventSpecific = FrameBreakEvent(this).FrameSpecific()
        // PistonChat compat
        if (server.pluginManager.getPlugin("PistonChat") != null && server.pluginManager.isPluginEnabled("PistonChat")) {
            playerChatEvent = PistonPlayerChatEvent(this)
            server.pluginManager.registerEvents(LoggerFunctionCompat(this), this)
            logger.info("找到PistonChat 已启用支持")
            }
        else {
            playerChatEvent = PlayerChatEvent(this)
            server.pluginManager.registerEvents(LoggerFunction(this), this)
            logger.info("未找到PistonChat 已禁用支持")
        }


        // register events
        this.server.pluginManager.registerEvents(playerOnNetherRoofs, this)
        this.server.pluginManager.registerEvents(homeCmd, this)
        this.server.pluginManager.registerEvents(playerChatEvent, this)
        this.server.pluginManager.registerEvents(frameBreakEventAll, this)
        if (mcVersion >=17) this.server.pluginManager.registerEvents(frameBreakEventSpecific, this)


        // register commands
        getCommand("sethome")!!.setExecutor(homeCmd)
        getCommand("delhome")!!.setExecutor(homeCmd)
        getCommand("home")!!.setExecutor(homeCmd)
        getCommand("homes")!!.setExecutor(homeCmd)

        getCommand("totop")!!.setExecutor(netherRoofsCmd)
        getCommand("todown")!!.setExecutor(netherRoofsCmd)

        getCommand("stat")!!.setExecutor(statCmd)
        getCommand("inkcore")!!.setExecutor(reloadConfigCmd)

        // register tab completer
        getCommand("sethome")!!.tabCompleter = homeCmd
        getCommand("delhome")!!.tabCompleter = homeCmd
        getCommand("home")!!.tabCompleter = homeCmd
        getCommand("homes")!!.tabCompleter = homeCmd

        getCommand("totop")!!.tabCompleter = netherRoofsCmd
        getCommand("todown")!!.tabCompleter = netherRoofsCmd

        getCommand("stat")!!.tabCompleter = statCmd
        getCommand("inkcore")!!.tabCompleter = reloadConfigCmd
    }

    override fun onDisable() {
        // Plugin shutdown logic
        logger.info("Plugin has been disabled!")
    }
}
