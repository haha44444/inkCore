package top.haha44444.inkCore.storage

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.IOException
import java.util.*
import java.util.logging.Level

class HomeStorage(private val plugin: Plugin) {
    private lateinit var file: File
    private lateinit var data: FileConfiguration


    fun init() {
        if (!plugin.dataFolder.exists()) plugin.dataFolder.mkdirs()
        file = File(plugin.dataFolder, "data.yml")
        if (!file.exists()) {
            file.createNewFile()
        }
        data = YamlConfiguration.loadConfiguration(file)
    }


    fun save() {
        try {
            data.save(file)
        } catch (e: IOException) {
            plugin.logger.log(Level.SEVERE, "无法保存 data.yml", e)
        }
    }


    fun saveAsync() {
        Bukkit.getAsyncScheduler().runNow(plugin) { _: ScheduledTask ->
            save()
        }
    }
    fun reloadFromDisk() {
        data = YamlConfiguration.loadConfiguration(file)
    }


    fun setHome(uuid: UUID, index: Int, loc: Location) {
        val base = "players.$uuid.$index"
        data.set("$base.world", loc.world?.name)
        data.set("$base.x", loc.x)
        data.set("$base.y", loc.y)
        data.set("$base.z", loc.z)
        data.set("$base.yaw", loc.yaw)
        data.set("$base.pitch", loc.pitch)
    }


    fun getHome(uuid: UUID, index: Int): Location? {
        val base = "players.$uuid.$index"
        val worldName = data.getString("$base.world") ?: return null
        val world = Bukkit.getWorld(worldName) ?: return Location(null, 0.0, 0.0, 0.0)
        val x = data.getDouble("$base.x")
        val y = data.getDouble("$base.y")
        val z = data.getDouble("$base.z")
        val yaw = data.getDouble("$base.yaw").toFloat()
        val pitch = data.getDouble("$base.pitch").toFloat()
        return Location(world, x, y, z, yaw, pitch)
    }


    fun deleteHome(uuid: UUID, index: Int): Boolean {
        val base = "players.$uuid.$index"
        if (!data.contains(base)) return false
        data.set(base, null)
        return true
    }


    fun listHomeIndices(uuid: UUID): List<Int> {
        val section = data.getConfigurationSection("players.$uuid") ?: return emptyList()
        return section.getKeys(false)
            .mapNotNull { it.toIntOrNull() }
            .sorted()
    }
}