package pe.chalk.bukkit

import de.robingrether.idisguise.api.DisguiseAPI
import de.robingrether.idisguise.disguise.Disguise
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.*

/**
 * @author ChalkPE <chalk@chalk.pe>
 * @since 2017-06-13 20:44
 */
class Manager(val api: DisguiseAPI) : Listener {
    val plugin = LastMobStanding.Companion.instance
    val gameRules = mapOf(
            "doMobLoot" to "false",
            "doMobSpawning" to "false",
            "doDaylightCycle" to "false",
            "keepInventory" to "true",
            "naturalRegeneration" to "false"
    )

    var playing = false
    lateinit var attacker: Player
    lateinit var livingPlayers: MutableList<Player>

    fun init(world: World) {
        gameRules.forEach { k, v -> world.setGameRuleValue(k, v) }
        val spawnLocation = world.spawnLocation.add(Vector(0, 3, 0))

        world.players.forEach {
            api.undisguise(it)

            it.gameMode = GameMode.ADVENTURE
            it.allowFlight = false
            it.isFlying = false

            it.inventory.clear()
            it.inventory.addItem(ItemStack(Material.APPLE, 4))
            it.health = it.getAttribute(Attribute.GENERIC_MAX_HEALTH).value

            it.teleport(spawnLocation)
        }

        livingPlayers = world.players.toMutableList()
    }

    fun destroy() {
        api.undisguiseAll()
    }

    fun startGame(sender: Player) {
        if (playing) return plugin.broadcastMessage("The game already started.", sender)
        playing = true

        init(sender.world)
        if (livingPlayers.size <= 1) return plugin.broadcastMessage("We need at least 2 people to start the game.")

        attacker = livingPlayers[Random().nextInt(livingPlayers.size)]
        livingPlayers.forEach {
            val item = if (it == attacker) Material.DIAMOND_SWORD else Material.WOOD_SWORD
            val message = if (it == attacker) "You are attacker. Kill everyone!" else "Oh, pig!"

            it.inventory.addItem(ItemStack(item))
            plugin.broadcastMessage(message, it)

            if (it == attacker) api.undisguise(it)
            else api.disguise(it, Disguise.fromString("pig"))
        }

        plugin.broadcastMessage("Game started!")
        plugin.broadcastMessage("Attacker is ${attacker.displayName}")
    }

    fun stopGame(sender: Player, delay: Long = -1) {
        if (delay > 0) {
            object: BukkitRunnable() {
                override fun run() = plugin.manager.stopGame(sender)
            }.runTaskLater(plugin, delay)

            return
        }

        if (!playing) return plugin.broadcastMessage("There is no game to stop.", sender)
        playing = false

        init(sender.world)
        plugin.broadcastMessage("Game ended!")
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        api.undisguise(event.player)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        api.undisguise(event.player)
    }

    @EventHandler
    fun onPlayerDamageByPlayer(event: EntityDamageByEntityEvent) {
        if (!playing) return
        if (event.entity !is Player || event.damager !is Player) return

        val victim = event.entity as Player
        val damager = event.damager as Player

        if (!livingPlayers.contains(victim) || !livingPlayers.contains(damager)) {
            event.isCancelled = true
            return // others can't attack dead players, dead players can't attack others
        }

        if (victim.health - event.damage < 1) {
            event.isCancelled = true
            killPlayer(victim, damager)
        }
    }

    fun killPlayer(victim: Player, killer: Player) {
        livingPlayers.remove(victim)

        plugin.broadcastMessage("You are dead!", victim)
        plugin.broadcastMessage("${victim.displayName} was killed by ${killer.displayName}")

        if (victim == attacker) {
            plugin.broadcastMessage("${attacker.displayName} dead. Pigs win!")
            return stopGame(attacker, 20 * 3)
        }

        victim.inventory.clear()
        victim.allowFlight = true
        victim.isFlying = true

        api.disguise(victim, Disguise.fromString("bat"))

        val livingPigCount = livingPlayers.filter { it != attacker }.size
        if (livingPigCount > 0) plugin.broadcastMessage("Now $livingPigCount pigs left!")

        if (livingPlayers.all { it == attacker }) {
            plugin.broadcastMessage("All pigs dead. ${attacker.displayName} win!")
            return stopGame(attacker, 20 * 3)
        }
    }
}