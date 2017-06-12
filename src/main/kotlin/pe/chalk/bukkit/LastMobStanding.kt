package pe.chalk.bukkit

import de.robingrether.idisguise.api.DisguiseAPI
import de.robingrether.idisguise.disguise.Disguise
import de.robingrether.idisguise.disguise.DisguiseType
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

/**
 * @author ChalkPE <chalk@chalk.pe>
 * @since 2017-06-12 08:43
 */
class LastMobStanding : JavaPlugin(), Listener {
    val entities = setOf(EntityType.DROPPED_ITEM, EntityType.EXPERIENCE_ORB)

    lateinit var prefix: String
    lateinit var api: DisguiseAPI
    lateinit var attacker: Player

    var playing = false
    var livingPlayers = mutableSetOf<Player>()

    override fun onEnable() {
        prefix = "${ChatColor.AQUA}[$name]"
        server.pluginManager.registerEvents(this, this)
        api = Bukkit.getServicesManager().getRegistration(DisguiseAPI::class.java).provider
    }

    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {
        if (sender == null) return true
        if (command?.name != "lms") return true

        if (sender !is Player) {
            sender.sendMessage("$prefix You are not a player")
            return true
        }

        if (!sender.isOp) {
            sender.sendMessage("$prefix Permission denied")
            return true
        }

        when (args?.get(0)) {
            "start" -> start()
            "end" -> end()
        }

        return true
    }

    fun start() {
        val list = server.onlinePlayers.toTypedArray()
        if (list.size <= 1) {
            server.broadcastMessage("$prefix Too few people to start!")
            return
        }

        attacker = list[Random().nextInt(list.size)]

        list.forEach {
            if (it == attacker) api.undisguise(it)
            else api.disguise(it, Disguise.fromString("pig"))

            it.inventory.clear()
            it.inventory.addItem(ItemStack(if (it == attacker) Material.DIAMOND_SWORD else Material.WOOD_SWORD))

            it.foodLevel = 20
            it.gameMode = GameMode.ADVENTURE
            it.health = it.getAttribute(Attribute.GENERIC_MAX_HEALTH).value

            it.sendMessage("$prefix " + (if (it == attacker) "Kill everyone!" else "Oh, pig!"))
        }

        playing = true

        attacker.world.time = 6000
        attacker.world.setGameRuleValue("keepInventory", "true")
        attacker.world.entities.filter { entities.contains(it.type) }.forEach { it.remove() }

        livingPlayers.clear()
        livingPlayers.addAll(list)

        server.broadcastMessage("$prefix Game started!")
        server.broadcastMessage("$prefix Attacker is now ${attacker.displayName}")
    }

    fun end() {
        playing = false
        server.broadcastMessage("$prefix Game ended!")

        server.onlinePlayers.forEach {
            it.inventory.clear()
            it.teleport(it.world.spawnLocation)
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        if (!playing) return

        if (event.entity == attacker) {
            server.broadcastMessage("$prefix Attacker ${attacker.displayName} dead! Pigs won!")
            return end()
        }

        livingPlayers.remove(event.entity)
        server.broadcastMessage("$prefix Now ${livingPlayers.filter { it != attacker }.size} pigs living!")

        if (livingPlayers.all { it == attacker }) {
            server.broadcastMessage("$prefix All pigs dead! Attacker ${attacker.displayName} won!")
            return end()
        }
    }
}