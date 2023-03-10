package cc.trixey.invero.ui.bukkit.nms

/**
 * Invero
 * cc.trixey.invero.ui.bukkit.nms.WindowProperty
 *
 * @author Arasple
 * @since 2023/2/24 13:10
 *
 * ref: https://wiki.vg/Protocol#Set_Container_Property
 */
enum class WindowProperty(val index: Int) {

    FURANCE_FIRE_ICON(0),

    FURANCE_MAX_BURN_TIME(1),

    FURANCE_PROGRESS_ARROW(2),

    FURANCE_MAX_PROGRESS(3),

    ENCHANTMENT_TABLE_LEVEL_REQUIREMENT_TOP(0),

    ENCHANTMENT_TABLE_LEVEL_REQUIREMENT_MIDDLE(1),

    ENCHANTMENT_TABLE_LEVEL_REQUIREMENT_BOTTOM(2),

    ENCHANTMENT_TABLE_SEED(3),

    ENCHANTMENT_TABLE_ENCHANTMENT_ID_TOP(4),

    ENCHANTMENT_TABLE_ENCHANTMENT_ID_MIDDLE(5),

    ENCHANTMENT_TABLE_ENCHANTMENT_ID_BOTTOM(6),

    ENCHANTMENT_TABLE_ENCHANTMENT_LEVEL_TOP(7),

    ENCHANTMENT_TABLE_ENCHANTMENT_LEVEL_MIDDLE(8),

    ENCHANTMENT_TABLE_ENCHANTMENT_LEVEL_BOTTOM(9),

    BEACON_POWER_LEVEL(0),

    BEACON_FIRST_POTION_EFFECT(1),

    BEACON_SECOND_POTION_EFFECT(2),

    ANVIL_REPAIR_COST(0),

    BREWING_STAND_BREW_TIME(0),

    BREWING_STAND_FUEL_TIME(1),

    STONECUTTER_SELECTED_RECIPE(0),

    LOOM_SELECTED_PATTERN(0),

    LECTERN_PAGE_NUMBER(0),

}