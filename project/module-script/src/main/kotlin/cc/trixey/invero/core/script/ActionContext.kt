package cc.trixey.invero.core.script

import cc.trixey.invero.core.script.session
import taboolib.common5.cdouble
import cc.trixey.invero.core.script.loader.InveroKetherParser
import taboolib.module.kether.combinationParser

/**
 * Invero
 * cc.trixey.invero.core.script.kether.ActionContext
 *
 * @author Arasple
 * @since 2023/1/23 11:02
 */
object ActionContext {

    @InveroKetherParser(["context", "ctx"])
    fun parser() = combinationParser {
        it.group(
            // get, has, set, del, inc, dec
            symbol(),
            // key
            text().option(),
            // value
            command("to", "by", then = action()).option().defaultsTo(null)
        ).apply(it) { action, key, mod ->
            future {
                val value = if (mod != null) newFrame(mod).run<Any>() else null
                if (key == null || action == "update") {
                    return@future completedFuture(session()?.updateVariables())
                }

                when (action) {
                    "get" -> completedFuture(session()?.getVariable(key))
                    "has" -> completedFuture(session()?.hasVariable(key) == true)
                    "no", "without" -> completedFuture(session()?.hasVariable(key) == false)
                    "rem", "del", "delete" -> {
                        variables().remove(key)
                        completedFuture(session()?.removeVariable(key))
                    }
                    "set" -> {
                        (value ?: error("No valid value")).thenApply {
                            variables().set(key, it)
                            session()?.setVariable(key, it)
                        }
                    }
                    "inc", "increase", "+=" -> {
                        (value ?: error("No valid value")).thenApply {
                            session()?.apply {
                                val result = getVariable(key).cdouble + it.cdouble
                                variables().set(key, result)
                                setVariable(key, result)
                            }
                        }
                    }

                    "dec", "decrease", "-=" -> {
                        (value ?: error("No valid value")).thenApply {
                            session()?.apply {
                                val result = getVariable(key).cdouble - it.cdouble
                                variables().set(key, result)
                                setVariable(key, result)
                            }
                        }
                    }

                    else -> error("Unknown action $action")
                }
            }
        }
    }

}