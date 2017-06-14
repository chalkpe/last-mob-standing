package pe.chalk.bukkit

import de.robingrether.idisguise.api.DisguiseAPI
import de.robingrether.idisguise.disguise.Disguise
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.*

/**
 * @author ChalkPE <chalk@chalk.pe>
 * @since 2017-06-13 20:44
 */
class GameManager(val api: DisguiseAPI) : Listener {
    val plugin = LastMobStanding.Companion.instance
    val gameRules = mapOf(
            "doMobLoot" to "false",
            "doMobSpawning" to "false",
            "doDaylightCycle" to "false",
            "keepInventory" to "true",
            "naturalRegeneration" to "false"
    )

    var playing = false
    var gameTime: Long = 0

    lateinit var attacker: Player
    var livingPlayers = mutableListOf<Player>()

    init {
        plugin.server.scheduler.runTaskTimer(plugin, {
            if (++gameTime < 0) plugin.broadcastMessage("`${Math.abs(gameTime)}' seconds left!")
        }, 20, 20)
    }

    fun init(world: World, teleport: Boolean = true) {
        gameRules.forEach { k, v -> world.setGameRuleValue(k, v) }
        val spawnLocation = world.spawnLocation.add(Vector(0, 1, 0)) // 1 block higher

        gameTime = -20 // seconds
        livingPlayers = world.players.toMutableList()

        livingPlayers.forEach {
            api.undisguise(it)

            it.gameMode = GameMode.ADVENTURE
            it.allowFlight = false
            it.isFlying = false

            it.inventory.clear()
            it.inventory.addItem(ItemStack(Material.APPLE, 4))
            it.health = it.getAttribute(Attribute.GENERIC_MAX_HEALTH).value

            if (teleport) it.teleport(spawnLocation)
        }
    }

    fun destroy() {
        api.undisguiseAll()
    }

    fun startGame(sender: Player) {
        if (playing) return plugin.log("The game already started.", sender)
        playing = true

        init(sender.world)
        if (livingPlayers.size <= 1) return plugin.log("We need at least 2 people to start the game.", sender)

        attacker = livingPlayers[Random().nextInt(livingPlayers.size)]
        val attackerName = attacker.name.toLowerCase(Locale.ENGLISH)

        val pig: Disguise = Disguise.fromString("pig")
        pig.visibility = Disguise.Visibility.ONLY_LIST
        pig.setVisibilityParameter(attackerName)

        val zombie: Disguise = Disguise.fromString("zombie")
        zombie.visibility = Disguise.Visibility.NOT_LIST
        zombie.setVisibilityParameter(attackerName)

        livingPlayers.forEach {
            val dis = if (it == attacker) zombie else pig
            val weapon = if (it == attacker) Material.DIAMOND_SWORD else Material.WOOD_SWORD
            val message = if (it == attacker) "`You are attacker.' Kill everyone!" else "`Oh, pig.' Kill an attacker!"

            api.disguise(it, dis)
            it.inventory.addItem(ItemStack(weapon))
            plugin.broadcastMessage(message, it)
        }

        attacker.location.pitch = -90f // look upward
        attacker.inventory.chestplate = ItemStack(Material.GOLD_CHESTPLATE)
        attacker.inventory.boots = ItemStack(Material.GOLD_BOOTS)

        plugin.broadcastMessage("Attacker is `${attacker.displayName}'")
        plugin.broadcastMessage("Game started!")
    }

    fun stopGame(sender: Player) {
        if (!playing) return plugin.log("There is no game to stop.", sender)
        playing = false

        init(sender.world, false)
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
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (!playing) return

        // attacker cannot move before game starts
        if (event.player == attacker && gameTime < 0) event.isCancelled = true
    }

    @EventHandler
    fun onPlayerDamageByPlayer(event: EntityDamageByEntityEvent) {
        if (!playing) return
        if (event.entity !is Player || event.damager !is Player) return

        val victim = event.entity as Player
        val damager = event.damager as Player

        // others can't attack dead players, dead players can't attack others
        if (!livingPlayers.contains(victim) || !livingPlayers.contains(damager)) event.isCancelled = true

        // pigs cannot attack other pigs
        else if (victim != attacker && damager != attacker) event.isCancelled = true
    }

    fun onPlayerDeath(event: PlayerDeathEvent) {
        if (!playing) return

        val victim = event.entity
        if (livingPlayers.contains(victim)) return
        livingPlayers.remove(victim)

        plugin.warn(event.deathMessage)
        event.deathMessage = null

        victim.inventory.clear()
        victim.allowFlight = true
        victim.isFlying = true

        api.disguise(victim, Disguise.fromString("bat"))

        val pigsWin = victim == attacker
        val attackerWin = livingPlayers.all { it == attacker }

        if (pigsWin || attackerWin) {
            plugin.server.scheduler.runTaskLater(plugin, { stopGame(attacker) }, 60)
            plugin.warn(if (pigsWin) "`${attacker.displayName}' dead. Pigs win!" else "All pigs dead. `${attacker.displayName}' win!")
        }

        val livingPigCount = livingPlayers.filter { it != attacker }.size
        if (livingPigCount > 0) plugin.warn("Now `$livingPigCount' pigs left!")
    }
}