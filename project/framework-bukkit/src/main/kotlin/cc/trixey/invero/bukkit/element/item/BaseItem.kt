package cc.trixey.invero.bukkit.element.item

import cc.trixey.invero.bukkit.BukkitWindow
import cc.trixey.invero.bukkit.element.Clickable
import cc.trixey.invero.common.Element
import cc.trixey.invero.common.Panel
import cc.trixey.invero.common.Pos
import cc.trixey.invero.common.Viewer
import cc.trixey.invero.common.event.ClickType
import cc.trixey.invero.common.panel.ElementalPanel
import cc.trixey.invero.common.util.locatingAbsoluteSlot
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import taboolib.platform.util.ItemBuilder
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Supplier

/**
 * Invero
 * cc.trixey.invero.bukkit.element.item.ItemElement
 *
 * @author Arasple
 * @since 2022/12/29 22:38
 */
abstract class BaseItem<T : Element>(override val panel: Panel) : Supplier<ItemStack>, Element, Clickable<T> {

    abstract var value: ItemStack

    abstract fun get(viewer: Viewer): ItemStack

    abstract fun modify(builder: ItemBuilder.() -> Unit)

    abstract fun build(material: Material, builder: ItemBuilder.() -> Unit = {})

    abstract fun buildAsync(supplier: Supplier<ItemStack>)

    abstract fun buildFuture(completable: CompletableFuture<ItemStack>, timeout: Long = 200L)

    override val clickCallback = CopyOnWriteArrayList<T.(ClickType, InventoryClickEvent?) -> Unit>()

    override fun get(): ItemStack {
        return value
    }

    open fun isVisible(): Boolean {
        return panel.parent.isPanelValid(panel) && panel.isElementValid(this)
    }

    override fun postRender(block: (Pos) -> Unit) {
        if (!isVisible()) return

        val elements = (panel as ElementalPanel).elements
        val positions = elements.locateElement(this)?.values ?: setOf()

        positions.forEach(block)
    }

    override fun push() {
        val window = panel.window as BukkitWindow
        postRender {
            val slot = locatingAbsoluteSlot(it, panel)
            if (slot >= 0) {
                window.inventory[slot] = value
            }
        }
    }

}