package pe.chalk.bukkit.task

import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import pe.chalk.bukkit.Manager

/**
 * @author ChalkPE <chalk@chalk.pe>
 * @since 2017-06-14 08:59
 */
class TimerTask(val manager: Manager) : BukkitRunnable() {
    override fun run() {
        val time = ++manager.gameTime
        if (time < 0) manager.plugin.broadcastMessage("`$time' seconds left!")
    }
}