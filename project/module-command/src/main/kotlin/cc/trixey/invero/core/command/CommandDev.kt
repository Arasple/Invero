package cc.trixey.invero.core.command

import cc.trixey.invero.common.Invero
import cc.trixey.invero.core.util.KetherHandler
import cc.trixey.invero.core.util.session
import cc.trixey.invero.ui.bukkit.InventoryPacket
import cc.trixey.invero.ui.bukkit.InventoryVanilla
import cc.trixey.invero.ui.bukkit.PanelContainer
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submitAsync
import taboolib.platform.util.bukkitPlugin
import taboolib.platform.util.isAir
import taboolib.platform.util.onlinePlayers

/**
 * Invero
 * cc.trixey.invero.core.command.CommandDev
 *
 * @author Arasple
 * @since 2023/1/15 15:44
 */
@CommandHeader(name = "idev")
object CommandDev {

    @CommandBody
    val main = mainCommand { createHelper() }

    @CommandBody
    val runKether = subCommand {
        execute<CommandSender> { sender, _, argument ->
            val player = if (sender is Player) sender else onlinePlayers.random()
            val script = argument.removePrefix("runKether ")

            submitAsync {
                KetherHandler.invoke(script, player, mapOf()).thenApply {
                    sender.sendMessage(
                        """
                            ------------------>
                            Script: $script
                            Result: $it
                        """.trimIndent()
                    )
                }.get()
            }
        }
    }

    @CommandBody
    val printSerailizedMenu = subCommand {
        execute<CommandSender> { _, _, argument ->
            val menuId = argument.split(" ").getOrNull(1) ?: return@execute
            val menuManager = Invero.api().getMenuManager()

            menuManager.getMenu(menuId)?.let { println(menuManager.serializeToJson(it)) }
        }
    }

    @CommandBody
    val printTasks = subCommand {
        execute<CommandSender> { _, _, _ ->
            val activeWorkers = Bukkit.getScheduler().activeWorkers.count { it.owner == bukkitPlugin }
            val pendingTasks = Bukkit.getScheduler().pendingTasks.count { it.owner == bukkitPlugin }

            println(
                """
                    Tasks: (activeWorkers=$activeWorkers, pendingTasks=$pendingTasks)
                """.trimIndent()
            )
        }
    }

    @CommandBody
    val printVariables = subCommand {
        execute<CommandSender> { _, _, argument ->
            val globaldata = Invero.api().getDataManager().getGlobalData().source
            println(
                """
                    
                    [I][Print] ------------------------------ [Variables]
                    GlobalData: (${globaldata.keys.size})
                """.trimIndent()
            )
            globaldata.forEach { (key, value) ->
                println("- [${key.removePrefix("global@")}] : $value")
            }
            onlinePlayers.forEach {
                println("[I][PLAYER: ${it.name}] ------------------------------ [Player_Variables]")
                Invero.api().getDataManager().getPlayerData(it).source.forEach { (key, value) ->
                    println("- [${key.removePrefix("player@")}] : $value")
                }
            }
        }
    }

    @CommandBody
    val printSession = subCommand {
        execute<CommandSender> { _, _, argument ->
            val session = onlinePlayers.first().session ?: run {
                println("No session valid")
                return@execute
            }

            println(
                """
                    
                    [I][Print] ------------------------------ [SESSION]
                    Date: ${session.createdTime}
                    
                    Viewer: ${session.viewer.name}
                    Window: ${session.window.type.name}
                    Menu: ${session.menu.id}
                    Variables: ${session.getVariables()}
                    TaskMgr: ${session.taskGroup}
                    --------------------------------------------------
                    
                """.trimIndent()
            )
        }
    }

    @CommandBody
    val printWindow = subCommand {
        execute<CommandSender> { _, _, argument ->
            val session = onlinePlayers.first().session ?: run {
                println("No session valid")
                return@execute
            }
            val window = session.window
            val inventory = window.inventory

            println(
                """
                    
                    [I][Print] ------------------------------ [WINDOW]
                    Object: $window (${window.type.name})
                    Virtualized: ${window.inventory.isVirtual()}
                    Hosted Panels: ${window.panels.size}
                    -->
                """.trimIndent()
            )

            fun dumpPanels(indent: String = " ", container: PanelContainer) {
                container.panels.forEachIndexed { index, panel ->
                    println(indent + "Panel#$index (${panel.scale} at ${panel.locate})")
                    if (panel is PanelContainer) {
                        println("$indent  > __SUB PANELS__ (${panel.panels.size})")
                        dumpPanels("$indent  ", panel)
                    }
                }
            }

            dumpPanels(container = window)

            if (inventory is InventoryPacket) {
                inventory.windowItems
            } else {
                (inventory as InventoryVanilla).container.contents
            }.filterNot { it.isAir }.joinToString(",") { it!!.type.name }.let {
                println("Storage: [ $it ]")
            }
        }
    }

}