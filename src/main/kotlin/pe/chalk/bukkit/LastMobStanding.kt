package pe.chalk.bukkit

import de.robingrether.idisguise.api.DisguiseAPI
import de.robingrether.idisguise.disguise.Disguise
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import kotlin.concurrent.schedule

/**
 * @author ChalkPE <chalk@chalk.pe>
 * @since 2017-06-12 08:43
 */
class LastMobStanding : JavaPlugin(), Listener {
    val entities = setOf(EntityType.PLAYER, EntityType.PIG)

    val primaryColor = ChatColor.DARK_AQUA.toString()
    val accentColor = ChatColor.AQUA.toString()

    lateinit var prefix: String
    lateinit var api: DisguiseAPI
    lateinit var attacker: Player

    var playing = false
    var livingPlayers = mutableSetOf<Player>()

    override fun onEnable() {
        prefix = "$primaryColor[$name]"
        server.pluginManager.registerEvents(this, this)
        api = Bukkit.getServicesManager().getRegistration(DisguiseAPI::class.java).provider
    }

    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {
        if (args == null || args.isEmpty()) return false

        when (args[0]) {
            "start" -> start()
            "stop", "end" -> stop()
            else -> return false
        }

        return true
    }

    fun start() {
        if (playing) {
            server.broadcastMessage("$prefix Already playing!")
            return
        }

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
            it.allowFlight = false
            it.gameMode = GameMode.ADVENTURE
            it.health = it.getAttribute(Attribute.GENERIC_MAX_HEALTH).value

            val message = if (it == attacker) "You are attacker. Kill everyone!" else "Oh, pig!"
            it.sendMessage("$prefix $message")
        }

        attacker.world.time = 6000
        attacker.world.setGameRuleValue("doMobLoot", "false")
        attacker.world.setGameRuleValue("doMobSpawning", "false")
        attacker.world.setGameRuleValue("doDaylightCycle", "false")
        attacker.world.setGameRuleValue("keepInventory", "true")
        attacker.world.setGameRuleValue("naturalRegeneration", "false")
        attacker.world.entities.filter { !entities.contains(it.type) }.forEach { it.remove() }

        playing = true
        livingPlayers.clear()
        livingPlayers.addAll(list)

        server.broadcastMessage("$prefix Game started!")
        server.broadcastMessage("$prefix Attacker is $accentColor${attacker.displayName}")
    }

    fun stop() {
        if (!playing) {
            server.broadcastMessage("$prefix Not started!")
            return
        }

        server.onlinePlayers.forEach {
            api.undisguise(it)

            it.inventory.clear()
            it.allowFlight = false
        }

        playing = false
        server.broadcastMessage("$prefix Game ended!")
    }

    @EventHandler
    fun onPlayerDamageByPlayer(event: EntityDamageByEntityEvent) {
        if (!playing) return
        if (event.entity !is Player || event.damager !is Player) return

        val victim = event.entity as Player
        val damager = event.damager as Player

        if (!livingPlayers.contains(damager) || !livingPlayers.contains(victim)) event.isCancelled = true
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        if (!playing) return

        val attackerName = "$accentColor${attacker.displayName}$primaryColor"
        event.deathMessage = "$prefix " + event.deathMessage.replace(attacker.displayName, attackerName)

        event.entity.inventory.clear()
        event.entity.allowFlight = true
        event.entity.isFlying = true

        livingPlayers.remove(event.entity)
        api.disguise(event.entity, Disguise.fromString("bat"))

        if (event.entity == attacker) {
            server.broadcastMessage("$prefix $attackerName dead. Pigs win!")
            return stop()
        }

        val livingPigCount = livingPlayers.filter { it != attacker }.size
        if (livingPigCount > 0) server.broadcastMessage("$prefix Now $livingPigCount pigs left!")

        if (livingPlayers.all { it == attacker }) {
            server.broadcastMessage("$prefix All pigs dead. $attackerName win!")
            return stop()
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        api.undisguise(event.player)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        api.undisguise(event.player)
    }
}