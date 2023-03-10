package cc.trixey.invero.core.script

import cc.trixey.invero.core.icon.IconElement
import cc.trixey.invero.core.script.getRecursivePanels
import cc.trixey.invero.core.script.iconElementBy
import cc.trixey.invero.ui.common.panel.ElementalPanel
import taboolib.common.platform.function.submitAsync
import cc.trixey.invero.core.script.loader.InveroKetherParser
import taboolib.module.kether.combinationParser

/**
 * Invero
 * cc.trixey.invero.core.script.kether.ActionIcon
 *
 * @author Arasple
 * @since 2023/1/21 19:47
 */
object ActionIcon {

    /*
    icon [by <id>] [at <slot>] update/relocate/item

    refresh
    sub_index
    pause_update
    pause_relocate
    pause_frames
    resume_update
    resume_relocate
    resume_frames
     */

    @InveroKetherParser(["icon"])
    fun parser() = parser(null)

    fun parser(ref: ElementalPanel? = null) = combinationParser {
        it.group(
            command("by", then = text()).option().defaultsTo(null),
            command("at", then = int()).option().defaultsTo(-1),
            symbol(),
        ).apply(it) { by, at, action ->
            now {
                iconElementBy(by, at, ref).apply {
                    if (action == "item") return@now value
                    else handle(action)
                }
            }
        }
    }

    @InveroKetherParser(["icons"])
    fun parserIcons() = combinationParser { it ->
        it.group(symbol()).apply(it) { action ->
            now {
                getRecursivePanels()
                    .filterIsInstance<ElementalPanel>()
                    .flatMap { it.elements.value.keys }
                    .forEach {
                        if (it is IconElement) it.handle(action, true, 0L)
                    }
            }
        }
    }


    private fun IconElement.handle(action: String, now: Boolean = false, delay: Long = 2L) =
        submitAsync(now = now, delay = delay) {
            when (action) {
                "relocate" -> relocate()
                "update" -> update()
                "refresh" -> {
                    relocate()
                    update()
                }

                "index", "sub_index" -> iconIndex
                "pause_update" -> pauseUpdateTask()
                "pause_relocate" -> pauseRelocateTask()
                "pause_frames" -> pauseFramesTask()
                "resume_update" -> resumeUpdateTask()
                "resume_relocate" -> resumeRelocateTask()
                "resume_frames" -> resumeFramesTask()
                else -> error("Unsupported action for icon: $action")
            }
        }

}