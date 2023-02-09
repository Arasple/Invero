@file:OptIn(ExperimentalSerializationApi::class)

package cc.trixey.invero.core.api

import cc.trixey.invero.common.Invero
import cc.trixey.invero.common.Menu
import cc.trixey.invero.common.api.InveroSettings
import cc.trixey.invero.common.api.MenuManager
import cc.trixey.invero.common.api.SerializeResult
import cc.trixey.invero.common.api.SerializeResult.State.*
import cc.trixey.invero.common.util.letCatching
import cc.trixey.invero.common.util.listRecursively
import cc.trixey.invero.common.util.prettyPrint
import cc.trixey.invero.core.AgentPanel
import cc.trixey.invero.core.BaseMenu
import cc.trixey.invero.core.action.*
import cc.trixey.invero.core.menu.MenuBindings
import cc.trixey.invero.core.panel.PanelGenerator
import cc.trixey.invero.core.panel.PanelPaged
import cc.trixey.invero.core.panel.PanelStandard
import cc.trixey.invero.core.serialize.BaseMenuSerializer
import cc.trixey.invero.core.serialize.hocon.PatchedLoader
import cc.trixey.invero.core.util.session
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.bukkit.command.CommandSender
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submitAsync
import taboolib.common5.FileWatcher
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import taboolib.module.lang.sendLang
import taboolib.platform.util.bukkitPlugin
import taboolib.platform.util.onlinePlayers
import taboolib.platform.util.sendLang
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Invero
 * cc.trixey.invero.core.api.DefaultMenManager
 *
 * @author Arasple
 * @since 2023/2/1 17:17
 */
class DefaultMenManager : MenuManager {

    private val module = SerializersModule {

        polymorphic(Menu::class) {
            subclass(BaseMenu::class)
        }

        polymorphic(AgentPanel::class) {
            subclass(PanelStandard::class)
            subclass(PanelGenerator::class)
            subclass(PanelPaged::class)
        }

        polymorphic(Action::class) {
            subclass(ActionKether::class)
            subclass(ConditionIf::class)
            subclass(ConditionAll::class)
            subclass(ConditionAny::class)
            subclass(ConditionNone::class)
            subclass(ConditionIfNot::class)
            subclass(ConditionCase::class)
            subclass(StructureActionKether::class)
            subclass(FunctionalActionCatcher::class)
            subclass(FunctionalActionCatchers::class)
            subclass(NetesedAction::class)
        }

    }

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        serializersModule = module
        explicitNulls = false
    }

    private val defaultWorkspace = File(bukkitPlugin.dataFolder, "workspace")

    private val menus = ConcurrentHashMap<String, BaseMenu>()

    private val bindings = ConcurrentHashMap<String, MenuBindings>()

    override fun getMenu(id: String, ignoreCase: Boolean): BaseMenu? {
        return menus.entries.find { it.key.equals(id, ignoreCase) }?.value
    }

    override fun getMenus(): List<BaseMenu> {
        return menus.values.toList()
    }

    override fun deserialize(workspace: File): List<SerializeResult> {
        val matcher = InveroSettings.fileFilter.toRegex()
        val results = mutableListOf<SerializeResult>()
        val configurations = workspace
            .listRecursively()
            .filter { it.name.matches(matcher) }
            .mapNotNull {
                runCatching {
                    val conf = PatchedLoader.loadFromFile(it)
                    val keys = conf.getKeys(false)
                    if (menuDeclarations.any { it in keys }) conf else null
                }.onFailure { failure ->
                    results += SerializeResult(
                        file = it,
                        state = FAILURE_FILE,
                        throwable = failure
                    )
                }.getOrNull()
            }

        configurations.forEach { conf ->
            val file = conf.file ?: error("No valid file of configuration")
            val menu = runCatching {
                deserializeToMenu(conf, conf.name).also { it.register() }
            }.onFailure {
                results += SerializeResult(
                    file = file,
                    state = FAILURE_MENU,
                    throwable = it
                )
            }.getOrNull()

            if (menu != null) {
                if (getMenu(conf.name, true) != null) {
                    results += SerializeResult(
                        file = file,
                        state = FAILURE_DUPLICATED
                    )
                } else {
                    menus[conf.name] = menu

                    results += SerializeResult(
                        menu,
                        file,
                        SUCCESS
                    )
                }
            }
        }

        return results
    }

    override fun reload(receiver: CommandSender) {
        // mark time start
        val start = System.currentTimeMillis()
        // unregister all menus
        if (menus.isNotEmpty()) {
            onlinePlayers.forEach { player -> player.session?.menu?.close(player) }
            menus.values.forEach { it.unregister() }
            menus.clear()
        }
        // init workspaces
        val workspaces = getWorkspaces()

        if (workspaces.isEmpty()) {
            receiver.sendLang("menu-loader-workspace-empty")
        } else {
            receiver.sendLang("menu-loader-workspace-inited", workspaces.size)
            workspaces
                .flatMap { deserialize(it) }
                .forEach {
                    val file = it.file
                    when (it.state) {
                        FAILURE_FILE -> receiver.sendLang("menu-loader-file-errored", file.name)
                        FAILURE_MENU -> receiver.sendLang("menu-loader-menu-errored", file.name)
                        FAILURE_DUPLICATED -> receiver.sendLang("menu-loader-menu-duplicate", file.name)
                        SUCCESS -> registerListener(file, it.menu as BaseMenu)
                    }
                    it.print()
                }
            if (menus.isNotEmpty()) {
                val took = (System.currentTimeMillis() - start).div(1000.0)
                receiver.sendLang("menu-loader-menu-finished", menus.size, took)
            }
        }
    }

    override fun initMenuBindings(menu: Menu) {
        (menu as BaseMenu).bindings?.let {
            bindings[menu.id!!] = it
        }
    }

    override fun findBound(itemStack: ItemStack): BaseMenu? {
        bindings
            .entries
            .find { it.value.inferItem?.isItem(itemStack) == true }
            ?.key
            ?.let {
                return getMenu(it)
            }
        return null
    }

    override fun findBound(chat: String): BaseMenu? {
        bindings
            .entries
            .find { entry -> entry.value.chat.any { chat == it } }
            ?.key
            ?.let {
                return getMenu(it)
            }
        return null
    }

    private fun registerListener(file: File, menu: BaseMenu) {
        val menuId = menu.id!!

        if (FileWatcher.INSTANCE.hasListener(file)) return
        FileWatcher.INSTANCE.addSimpleListener(file) {
            submitAsync {
                if (!file.exists() || !menus.containsKey(menuId)) {
                    FileWatcher.INSTANCE.removeListener(file)
                } else runCatching {
                    deserializeToMenu(PatchedLoader.loadFromFile(file), menuId)
                }.onFailure {
                    it.prettyPrint()
                    console().sendLang("menu-loader-auto-reload-errored", menuId)
                }.getOrNull()?.let { loaded ->
                    // replace in memory
                    menus[menuId]?.unregister()
                    menus[menuId] = loaded
                    letCatching { loaded.register() }
                    // auto open
                    onlinePlayers
                        .filter { it.session?.menu?.id == menuId }
                        .also {
                            if (it.isNotEmpty()) console().sendLang("menu-loader-auto-reload-successed", menuId)
                        }
                        .forEach {
                            loaded.open(player = it, vars = it.session?.getVariables() ?: emptyMap())
                        }
                }
            }
        }
    }

    override fun deserializeToMenu(configuration: Configuration, name: String?): BaseMenu {
        configuration.changeType(Type.JSON)
        return json
            .decodeFromString(BaseMenuSerializer, configuration.saveToString())
            .also { if (name != null && it.id == null) it.id = name }
    }

    override fun serializeToJson(menu: Menu): String {
        return json.encodeToString(value = menu)
    }

    override fun <T> getJsonSerializer(): T {
        @Suppress("UNCHECKED_CAST")
        return json as T
    }

    private fun getWorkspaces(): List<File> {
        val settings = InveroSettings.workspaces

        return if (settings.isEmpty()) {
            listOf(defaultWorkspace.also { it.mkdirs() })
        } else {
            settings.map { path -> File(path) }
        }.filter { it.isDirectory }
    }

    private val menuDeclarations = mutableSetOf("menu", "title")

    companion object {

        @Awake(LifeCycle.INIT)
        fun init() {
            PlatformFactory.registerAPI<MenuManager>(DefaultMenManager())
        }

        @Awake(LifeCycle.ACTIVE)
        fun load() {
            Invero.API.getMenuManager().reload()
        }

    }

}