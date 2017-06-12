package pe.chalk.bukkit

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author ChalkPE <chalk@chalk.pe>
 * @since 2017-06-12 08:43
 */
class LastMobStanding : JavaPlugin(), Listener {
    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
    }

    @EventHandler
    fun onPlayerJoin() {

    }
}