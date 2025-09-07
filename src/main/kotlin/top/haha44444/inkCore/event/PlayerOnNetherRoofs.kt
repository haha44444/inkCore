package top.haha44444.inkCore.event

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityPlaceEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin
import top.haha44444.inkCore.utils.colorize

class PlayerOnNetherRoofs(private val plugin: Plugin) : Listener {
    // config
    var allowPlace: Boolean? = null

    var upper: Int = 127
    var fallBackSafeY: Int = 120

    var bypassPermission: String? = null
    var noPermissionMsg: String? = null
    var disallowPlaceMsg: String? = null

    fun loadConfig() {
        allowPlace = plugin.config.getBoolean("height-limits.nether.allow-place", false)

        upper = plugin.config.getInt("height-limits.nether.upper")
        fallBackSafeY = plugin.config.getInt("height-limits.nether.fall-back-safe-y")

        bypassPermission = plugin.config.getString("height-limits.nether.permission")
        noPermissionMsg = plugin.config.getString("height-limits.nether.messages.no-permission")?.colorize()
        disallowPlaceMsg = plugin.config.getString("height-limits.nether.messages.disallow-place")?.colorize()
    }

    init {
        loadConfig()
    }

    @EventHandler
    private fun onPlayerMove(e: PlayerMoveEvent) {
        val p = e.player
        bypassPermission?.let { if (p.hasPermission(it)) return }
        if (!isOnNetherRoof(p.location)) return

        noPermissionMsg?.let { p.sendMessage(it) }
        e.isCancelled = true
        p.scheduler.run(plugin, { _: ScheduledTask ->
            correctEntityDownwards(p)
        }, null)
    }

    @EventHandler
    private fun onPlayerTeleport(e: PlayerTeleportEvent) {
        val p = e.player
        bypassPermission?.let { if (p.hasPermission(it)) return }
        val to = e.to
        if (!isOnNetherRoof(to)) return

        noPermissionMsg?.let { p.sendMessage(it) }
        e.isCancelled = true
        p.scheduler.run(plugin, { _: ScheduledTask ->
            correctEntityDownwards(p)
        }, null)
    }

    @EventHandler
    private fun onBlockPlace(e: BlockPlaceEvent) {
        val p = e.player
        val loc = e.blockPlaced.location
        if (!isOnNetherRoof(loc)) return

        fun cancelAndSendDisallowPlaceMsg() {
            disallowPlaceMsg?.let { p.sendMessage(it) }
            e.isCancelled = true
        }

        fun cancelAndSendNoPermissionMsg() {
            noPermissionMsg?.let { p.sendMessage(it) }
            e.isCancelled = true
        }

        // Allow place or not
        val hasBPhasAP = bypassPermission?.let { p.hasPermission(it) } == true && allowPlace == true
        val nHasBPhasAP = bypassPermission?.let { p.hasPermission(it) } == true && allowPlace == false
        val hasBPnHasAP = bypassPermission?.let { p.hasPermission(it) } == false && allowPlace == true
        val nHasBPnHasAP = bypassPermission?.let { p.hasPermission(it) } == false && allowPlace == false

        when {
            hasBPhasAP -> return
            nHasBPhasAP -> cancelAndSendDisallowPlaceMsg()
            hasBPnHasAP -> cancelAndSendNoPermissionMsg()
            nHasBPnHasAP -> cancelAndSendNoPermissionMsg()
        }
    }

    @EventHandler
    private fun onHangingPlace(e: HangingPlaceEvent) {
        val p = e.player
        val loc = e.entity.location
        if (!isOnNetherRoof(loc)) return

        fun cancelAndSendDisallowPlaceMsg() {
            disallowPlaceMsg?.let { p?.sendMessage(it) }
            e.isCancelled = true
        }

        fun cancelAndSendNoPermissionMsg() {
            noPermissionMsg?.let { p?.sendMessage(it) }
            e.isCancelled = true
        }

        // Allow place or not
        val hasBPhasAP = bypassPermission?.let { p?.hasPermission(it) } == true && allowPlace == true
        val nHasBPhasAP = bypassPermission?.let { p?.hasPermission(it) } == true && allowPlace == false
        val hasBPnHasAP = bypassPermission?.let { p?.hasPermission(it) } == false && allowPlace == true
        val nHasBPnHasAP = bypassPermission?.let { p?.hasPermission(it) } == false && allowPlace == false

        when {
            hasBPhasAP -> return
            nHasBPhasAP -> cancelAndSendDisallowPlaceMsg()
            hasBPnHasAP -> cancelAndSendNoPermissionMsg()
            nHasBPnHasAP -> cancelAndSendNoPermissionMsg()
        }
    }

    @EventHandler
    private fun onEntityPlace(e: EntityPlaceEvent) {
        val p = e.player
        val loc = e.entity.location
        if (!isOnNetherRoof(loc)) return

        fun cancelAndSendDisallowPlaceMsg() {
            disallowPlaceMsg?.let { p?.sendMessage(it) }
            e.isCancelled = true
        }

        fun cancelAndSendNoPermissionMsg() {
            noPermissionMsg?.let { p?.sendMessage(it) }
            e.isCancelled = true
        }

        // Allow place or not
        val hasBPhasAP = bypassPermission?.let { p?.hasPermission(it) } == true && allowPlace == true
        val nHasBPhasAP = bypassPermission?.let { p?.hasPermission(it) } == true && allowPlace == false
        val hasBPnHasAP = bypassPermission?.let { p?.hasPermission(it) } == false && allowPlace == true
        val nHasBPnHasAP = bypassPermission?.let { p?.hasPermission(it) } == false && allowPlace == false

        when {
            hasBPhasAP -> return
            nHasBPhasAP -> cancelAndSendDisallowPlaceMsg()
            hasBPnHasAP -> cancelAndSendNoPermissionMsg()
            nHasBPnHasAP -> cancelAndSendNoPermissionMsg()
        }
    }

    fun isOnNetherRoof(loc: Location): Boolean {
        val w = loc.world ?: return false
        return w.environment == World.Environment.NETHER && loc.y >= upper
    }

    fun correctEntityDownwards(entity: Entity) {
        if (entity is LivingEntity) entity.isGliding = false
        entity.leaveVehicle()

        val from = entity.location
        val target = findNearestSafeBelow(from)
            ?: from.clone().apply { y = fallBackSafeY.toDouble() }

        entity.teleportAsync(target.toCenterLocation())
    }


    private fun findNearestSafeBelow(from: Location): Location? {
        val w = from.world ?: return null
        val x = from.blockX + 0.5
        val z = from.blockZ + 0.5
        val startY = kotlin.math.min(from.blockY, upper)

        for (yy in startY downTo 5) {
            val feet = Location(w, x, yy.toDouble(), z)
            val head = feet.clone().add(0.0, 1.0, 0.0)
            val below = feet.clone().add(0.0, -1.0, 0.0)

            val airAtFeet = feet.block.type.isAir
            val airAtHead = head.block.type.isAir
            val solidBelow = below.block.type.isSolid

            if (airAtFeet && airAtHead && solidBelow) {
                return feet
            }
        }
        return null
    }

}