package pe.chalk.bukkit

import de.robingrether.idisguise.api.DisguiseAPI
import org.bukkit.*
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author ChalkPE <chalk@chalk.pe>
 * @since 2017-06-12 08:43
 */
class LastMobStanding : JavaPlugin() {
    lateinit var api: DisguiseAPI
    lateinit var manager: Manager
    lateinit var prefix: String

    companion object {
        lateinit var instance: LastMobStanding
    }

    override fun onEnable() {
        instance = this
        prefix = "${ChatColor.DARK_AQUA}[$name]"

        api = Bukkit.getServicesManager().getRegistration(DisguiseAPI::class.java).provider
        manager = Manager(api)

        server.pluginManager.registerEvents(manager, this)
    }

    override fun onDisable() {
        manager.destroy()
    }

    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {
        if (sender !is Player) return true
        if (args == null || args.isEmpty()) return false

        when (args[0]) {
            "start" -> manager.startGame(sender)
            "stop" -> manager.stopGame(sender)
            else -> return false
        }

        return true
    }

    fun broadcastMessage(message: String, target: Player? = null) {
        val msg = "$prefix $message"
        if (target != null) target.sendMessage(msg) else server.broadcastMessage(msg)
    }
}