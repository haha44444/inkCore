package top.haha44444.inkCore.utils

import org.bukkit.ChatColor

fun String.colorize(): String =
    ChatColor.translateAlternateColorCodes('&', this)
