package cc.trixey.invero.core.compat

import cc.trixey.invero.common.Invero
import cc.trixey.invero.common.supplier.ItemSourceProvider
import cc.trixey.invero.core.compat.item.*
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake

/**
 * Invero
 * cc.trixey.invero.core.compat.Compat
 *
 * @author Arasple
 * @since 2023/1/29 15:43
 */
object Compat {

    @Awake(LifeCycle.INIT)
    fun inject() {
        registerItemSources()
    }

    internal fun registerItemSources() {
        ZaphkielItemProvider().register("zaphkiel", "zap")
        OraxenItemProvider().register("oraxen")
        ItemsAdderItemProvider().register("itemsadder", "ia")
        HDBItemProvider().register("headdatabase", "hdb")
        SerializedItemProvider().register("base64", "serialized")
        ScriptItemProvider().register("kether", "script")
    }

    private fun ItemSourceProvider.register(vararg namespace: String) {
        if (this is PluginHook && !isHooked) return
        namespace.forEach { Invero.api().registerItemSourceProvider(it, this) }
    }

}