/*
    Frame dupe from https://www.spigotmc.org/resources/framedupe.102974/
    Transfer to kotlin by IntelliJ IDEA
    features add by haha44444
*/

package top.haha44444.inkCore.event

import org.bukkit.block.ShulkerBox
import org.bukkit.entity.EntityType
import org.bukkit.entity.GlowItemFrame
import org.bukkit.entity.ItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import top.haha44444.inkCore.InkCore

class FrameBreakEvent(private val plugin: InkCore) : Listener {
    inner class FrameAll : Listener {
        var enable: Boolean? = null
        var fixShulkers: Boolean? = null
        var needPermission: Boolean? = null
        var blacklistEnable: Boolean? = null
        var whitelistEnable: Boolean? = null

        var blacklistItems: List<String>? = null
        var whitelistItems: List<String>? = null

        var level1ProbabilityPercentage: Int? = null
        var level1Multiplier: Int? = null
        var level2ProbabilityPercentage: Int? = null
        var level2Multiplier: Int? = null

        var permission: String? = null
        var level1Permission: String? = null
        var level2Permission: String? = null

        fun loadConfig() {
            enable = plugin.config.getBoolean("frame-dupe.enable", true)
            fixShulkers = plugin.config.getBoolean("frame-dupe.fix-shulkers", true)
            needPermission = plugin.config.getBoolean("frame-dupe.need-permission", false)
            blacklistEnable = plugin.config.getBoolean("frame-dupe.blacklist.enable", false)
            whitelistEnable = plugin.config.getBoolean("frame-dupe.whitelist.enable", false)

            blacklistItems = plugin.config.getStringList("frame-dupe.blacklist.items")
            whitelistItems = plugin.config.getStringList("frame-dupe.whitelist.items")

            level1ProbabilityPercentage = plugin.config.getInt("frame-dupe.level1.probability-percentage", 20)
            level1Multiplier = plugin.config.getInt("frame-dupe.level1.multiplier", 1)
            level2ProbabilityPercentage = plugin.config.getInt("frame-dupe.level2.probability-percentage", 50)
            level2Multiplier = plugin.config.getInt("frame-dupe.level2.multiplier", 3)

            permission = plugin.config.getString("frame-dupe.permission")
            level1Permission = plugin.config.getString("frame-dupe.level1.permission")
            level2Permission = plugin.config.getString("frame-dupe.level2.permission")

        }

        init {
            loadConfig()
        }

        @EventHandler
        private fun onFrameBreak(event: EntityDamageByEntityEvent) {
            if (enable == true && event.entityType == EntityType.ITEM_FRAME) {
                val itemFrame = event.getEntity() as ItemFrame
                val item = itemFrame.item
                val player = event.damager
                val versionNumber: Int = plugin.getMinecraftVersionUtil.getMinecraftVersion()

                // Check if permission is needed
                if (needPermission == true) {
                    val permission: String? =
                        permission
                    permission?.let {
                        if (!event.getEntity().world.players[0].hasPermission(it)) {
                            return
                        }
                    }
                }

                // Whitelist and Blacklist Verification
                if (!isItemAllowed(item, "frame-dupe")) return

                // ShulkerBox Verification
                if ((fixShulkers == true) && versionNumber >= 9) {
                    if (!isShulkerAllowed(item, "frame-dupe")) return
                }

                // Probability of duplicate
                val rng = (Math.random() * 100).toInt()

                fun level1() {
                    if (rng < (level1ProbabilityPercentage ?: 20)) {
                        val multiplier: Int = level1Multiplier ?: 1
                        val count = multiplier.coerceAtLeast(0)
                        repeat(count) {
                            event.getEntity().world.dropItemNaturally(event.entity.location, item)
                        }
                    }
                }

                fun level2() {
                    if (rng < (level2ProbabilityPercentage ?: 50)) {
                        val multiplier: Int = level2Multiplier ?: 3
                        val count = multiplier.coerceAtLeast(0)
                        repeat(count) {
                            event.getEntity().world.dropItemNaturally(event.entity.location, item)
                        }
                    }
                }

                val hasL1 = level1Permission?.let { player.hasPermission(it) }
                val hasL2 = level2Permission?.let { player.hasPermission(it) }

                when {
                    hasL2 == true -> level2()
                    hasL1 == true -> level1()
                    else -> return
                }

            }
        }
    }


    inner class FrameSpecific : Listener {
        var enable: Boolean? = null
        var fixShulkers: Boolean? = null
        var needPermission: Boolean? = null
        var blacklistEnable: Boolean? = null
        var whitelistEnable: Boolean? = null

        var blacklistItems: List<String>? = null
        var whitelistItems: List<String>? = null

        var level1ProbabilityPercentage: Int? = null
        var level1Multiplier: Int? = null
        var level2ProbabilityPercentage: Int? = null
        var level2Multiplier: Int? = null

        var permission: String? = null
        var level1Permission: String? = null
        var level2Permission: String? = null

        fun loadConfig() {
            enable = plugin.config.getBoolean("glow-frame-dupe.enable", true)
            fixShulkers = plugin.config.getBoolean("glow-frame-dupe.fix-shulkers", true)
            needPermission = plugin.config.getBoolean("glow-frame-dupe.need-permission", false)
            blacklistEnable = plugin.config.getBoolean("glow-frame-dupe.blacklist.enable", false)
            whitelistEnable = plugin.config.getBoolean("glow-frame-dupe.whitelist.enable", false)

            blacklistItems = plugin.config.getStringList("glow-frame-dupe.blacklist.items")
            whitelistItems = plugin.config.getStringList("glow-frame-dupe.whitelist.items")

            level1ProbabilityPercentage = plugin.config.getInt("glow-frame-dupe.level1.probability-percentage", 20)
            level1Multiplier = plugin.config.getInt("glow-frame-dupe.level1.multiplier", 1)
            level2ProbabilityPercentage = plugin.config.getInt("glow-frame-dupe.level2.probability-percentage", 50)
            level2Multiplier = plugin.config.getInt("glow-frame-dupe.level2.multiplier", 3)

            permission = plugin.config.getString("glow-frame-dupe.permission")
            level1Permission = plugin.config.getString("glow-frame-dupe.level1.permission")
            level2Permission = plugin.config.getString("glow-frame-dupe.level2.permission")

        }

        init {
            loadConfig()
        }

        @EventHandler
        private fun onFrameBreak(event: EntityDamageByEntityEvent) {
            if (enable == true && event.entityType == EntityType.GLOW_ITEM_FRAME) {
                val itemFrame = event.getEntity() as GlowItemFrame
                val item = itemFrame.item
                val player = event.damager


                // Check if permission is needed
                if (needPermission == true) {
                    val permission: String? =
                        permission
                    permission?.let {
                        if (!player.hasPermission(it)) {
                            return
                        }
                    }
                }

                // Whitelist and Blacklist Verification
                if (!isItemAllowed(item, "glow-frame-dupe")) return

                // ShulkerBox Verification
                if (fixShulkers == true) {
                    if (!isShulkerAllowed(item, "glow-frame-dupe")) return
                }

                // Probability of duplicate
                val rng = (Math.random() * 100).toInt()

                fun level1() {
                    if (rng < (level1ProbabilityPercentage ?: 20)) {
                        val multiplier: Int = level1Multiplier ?: 1
                        val count = multiplier.coerceAtLeast(0)
                        repeat(count) {
                            event.getEntity().world.dropItemNaturally(event.entity.location, item)
                        }
                    }
                }

                fun level2() {
                    if (rng < (level2ProbabilityPercentage ?: 50)) {
                        val multiplier: Int = level2Multiplier ?: 3
                        val count = multiplier.coerceAtLeast(0)
                        repeat(count) {
                            event.getEntity().world.dropItemNaturally(event.entity.location, item)
                        }
                    }
                }

                val hasL1 = level1Permission?.let { player.hasPermission(it) }
                val hasL2 = level2Permission?.let { player.hasPermission(it) }

                when {
                    hasL2 == true -> level2()
                    hasL1 == true -> level1()
                    else -> return
                }

            }
        }
    }

    private fun isItemAllowed(item: ItemStack, path: String?): Boolean {
        if (plugin.config.getBoolean("$path.whitelist.enable")) {
            val whitelist = plugin.config.getStringList("$path.whitelist.items")
            if (!whitelist.contains(item.type.toString())) return false
        }

        if (plugin.config.getBoolean("$path.blacklist.enable")) {
            val blacklist = plugin.config.getStringList("$path.blacklist.items")
            if (blacklist.contains(item.type.toString())) return false
        }
        return true
    }

    private fun isShulkerAllowed(item: ItemStack, path: String?): Boolean {
        if (item.itemMeta is BlockStateMeta) {
            val blockStateMeta = item.itemMeta as BlockStateMeta
            if (blockStateMeta.blockState is ShulkerBox) {
                val shulkerBox = blockStateMeta.blockState as ShulkerBox
                val shulkerInventory = shulkerBox.inventory

                for (shulkerItem in shulkerInventory.contents) {
                    if (shulkerItem != null) {
                        val itemTypeName: String = shulkerItem.type.toString()

                        if (plugin.config.getBoolean("$path.blacklist.enable")) {
                            val blacklist = plugin.config.getStringList("$path.blacklist.items")
                            if (blacklist.contains(itemTypeName)) return false
                        }

                        if (plugin.config.getBoolean("$path.whitelist.enable")) {
                            val whitelist = plugin.config.getStringList("$path.whitelist.items")
                            if (!whitelist.contains(itemTypeName)) return false
                        }
                    }
                }
            }
        }
        return true
    }
}
