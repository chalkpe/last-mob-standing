package pe.chalk.bukkit.task

import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import pe.chalk.bukkit.Manager

/**
 * @author ChalkPE <chalk@chalk.pe>
 * @since 2017-06-14 08:59
 */
class StopTask(val manager: Manager, val attacker: Player) : BukkitRunnable() {
    override fun run() {
        manager.stopGame(attacker)
    }
}