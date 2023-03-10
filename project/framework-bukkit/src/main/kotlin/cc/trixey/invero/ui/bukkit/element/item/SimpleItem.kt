package cc.trixey.invero.ui.bukkit.element.item

import cc.trixey.invero.ui.common.Panel
import cc.trixey.invero.ui.common.Viewer
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.submitAsync
import taboolib.platform.util.ItemBuilder
import taboolib.platform.util.buildItem
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

/**
 * Invero
 * cc.trixey.invero.ui.bukkit.element.chemdah.SimpleItem
 *
 * @author Arasple
 * @since 2022/12/29 22:37
 */
open class SimpleItem(panel: Panel, value: ItemStack = ItemStack(Material.AIR)) : BaseItem<SimpleItem>(panel) {

    override var value: ItemStack = value
        set(value) {
            field = value.clone()
            safePush()
        }

    override fun get(viewer: Viewer) = get()

    override fun modify(builder: ItemBuilder.() -> Unit) {
        value = buildItem(value, builder)
    }

    override fun build(material: Material, builder: ItemBuilder.() -> Unit) {
        value = buildItem(material, builder)
    }

    override fun buildAsync(supplier: Supplier<ItemStack>) {
        submitAsync { value = supplier.get() }
    }

    override fun buildFuture(completable: CompletableFuture<ItemStack>, timeout: Long) {
        submitAsync { value = completable.get(timeout, TimeUnit.MILLISECONDS) }
    }

    override fun getInstance(): SimpleItem {
        return this
    }

}