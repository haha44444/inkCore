package top.haha44444.inkCore.utils

import org.bukkit.Bukkit

class GetMinecraftVersionUtil {
    fun getMinecraftVersion(): Int {
        val version = Bukkit.getBukkitVersion().split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        val parts = version.replace("[^0-9.]".toRegex(), "").split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        return try {
            parts[1].toInt()
        } catch (_: NumberFormatException) {
            7
        } catch (_: ArrayIndexOutOfBoundsException) {
            7
        }
    }
}