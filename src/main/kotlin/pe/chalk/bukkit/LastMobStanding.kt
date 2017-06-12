package pe.chalk.bukkit

import de.robingrether.idisguise.api.DisguiseAPI
import de.robingrether.idisguise.disguise.Disguise
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author ChalkPE <chalk@chalk.pe>
 * @since 2017-06-12 08:43
 */
class LastMobStanding : JavaPlugin(), Listener {
    lateinit var prefix: String
    lateinit var api: DisguiseAPI
    lateinit var attacker: OfflinePlayer

    override fun onEnable() {
        prefix = "${ChatColor.AQUA}[$name]"
        server.pluginManager.registerEvents(this, this)
        api = Bukkit.getServicesManager().getRegistration(DisguiseAPI::class.java).provider
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        api.disguise(event.player, Disguise.fromString("pig"))

        event.player.inventory.clear()
        event.player.inventory.addItem(ItemStack(Material.WOOD_SWORD))

        event.player.sendMessage("$prefix Oh, pig!")
    }
}