package cc.trixey.invero.core.command

import cc.trixey.invero.core.InveroManager
import cc.trixey.invero.core.Menu
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.component.CommandComponentDynamic
import taboolib.common.platform.command.component.CommandComponentLiteral
import taboolib.module.lang.asLangText

fun String.retrievePlayer(): Player? {
    return Bukkit.getPlayerExact(this)
}

fun String.retrieveMenu(): Menu? {
    return InveroManager.getMenu(this)
}

fun CommandComponentDynamic.suggestMenuIds(uncheck: Boolean = false) {
    suggestion<CommandSender>(uncheck = uncheck) { _, _ -> InveroManager.getMenus().keys.toList() }
}

/**
 * modified from
 *
 * https://github.com/TabooLib/taboolib/blob/master/expansion/expansion-command-helper
 */
internal fun CommandComponent.createHelper() = execute<ProxyCommandSender> { sender, context, _ ->
    val command = context.command
    val builder = StringBuilder("§cUsage: /${command.name}")
    var newline = false

    fun print(
        compound: CommandComponent,
        index: Int,
        size: Int,
        offset: Int = 8,
        level: Int = 0,
        end: Boolean = false,
        optional: Boolean = false
    ) {
        var option = optional
        var comment = 0
        when (compound) {
            is CommandComponentLiteral -> {
                if (size == 1) {
                    builder.append(" ").append("§c${compound.aliases[0]}")
                } else {
                    newline = true
                    builder.appendLine()
                    builder.append(space(offset))
                    if (level > 1) builder.append(if (end) " " else "§7│")
                    builder.append(space(level))
                    if (index + 1 < size) builder.append("§7├── ")
                    else builder.append("§7└── ")
                    builder.append("§c${compound.aliases[0]}")
                }
                option = false
                comment = compound.aliases[0].length
            }

            is CommandComponentDynamic -> {
                val value = if (compound.comment.startsWith("@")) {
                    sender.asLangText(compound.comment.substring(1))
                } else {
                    compound.comment
                }
                comment = if (compound.optional || option) {
                    option = true
                    builder.append(" ").append("§8[<$value>]")
                    compound.comment.length + 4
                } else {
                    builder.append(" ").append("§7<$value>")
                    compound.comment.length + 2
                }
            }
        }
        if (level > 0) comment += 1
        compound.children.forEachIndexed { i, children ->
            // 因 literal 产生新的行
            if (newline) {
                print(children, i, compound.children.size, offset, level + comment, end, option)
            } else {
                val length = if (offset == 8) command.name.length + 1 else comment + 1
                print(children, i, compound.children.size, offset + length, level, end, option)
            }
        }
    }

    val size = context.commandCompound.children.size
    context.commandCompound.children.forEachIndexed { index, children ->
        print(children, index, size, end = index + 1 == size)
    }
    builder.lines().forEach { sender.sendMessage(it) }
}

private fun space(space: Int) = (1..space).joinToString("") { " " }