package cc.trixey.invero.core.animation

/**
 * TrMenu
 * cc.trixey.invero.core.animation.CycleSingle
 *
 * @author Arasple
 * @since 2023/1/13 12:27
 */
@JvmInline
value class CycleSingle(val value: String) : Cycle<String> {

    override fun get(): String {
        return value
    }

    override fun getMode(): CycleMode {
        return CycleMode.LOOP
    }

    override fun cycle() {
        error("CycleSingle not support cycle()")
    }

}