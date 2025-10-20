package top.haha44444.inkCore.event

import org.bukkit.event.Listener

interface ChatEventHandler : Listener {
    var greenNamePermission: String?
    var greenMessagePermission: String?
}