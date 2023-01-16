package cc.trixey.invero.core.action

import cc.trixey.invero.core.session.Session
import java.util.concurrent.CompletableFuture

/**
 * Invero
 * cc.trixey.invero.core.action.Condition
 *
 * @author Arasple
 * @since 2023/1/14 12:20
 */
interface Expression {

    fun invoke(session: Session): CompletableFuture<Any?>

}