package sahlaysta.sbte4.gui

import sahlaysta.sbte4.gui.GUIBackgroundQueue.Waiter
import javax.swing.SwingUtilities
import javax.swing.SwingWorker

/* the background queue for background-thread actions. enqueue an action with enqueue().
   every action has a key, and the key can be used to cancel or replace the action. */
internal class GUIBackgroundQueue {

    interface ActionState { fun actionIsCanceled(): Boolean }

    private class BackgroundFunction(val fn: (actionState: ActionState) -> Any?)

    private class CallbackFunction(val fn: (value: Any?) -> Unit)

    private class ErrorCallbackFunction(val fn: (exception: Exception) -> Unit)

    private class BackgroundAction(val key: Any?,
                                   val backgroundFunction: BackgroundFunction,
                                   val callbackFunction: CallbackFunction,
                                   val errorCallbackFunction: ErrorCallbackFunction,
                                   var canceled: Boolean = false)

    private fun interface Waiter { fun waitFor() }

    private val lock = Any()
    private var executing = false
    private var waiter: Waiter? = null
    private var queue = LinkedHashMap<Any?, BackgroundAction>()

    fun <T> enqueue(backgroundFunction: (actionState: ActionState) -> T,
                    callbackFunction: (value: T) -> Unit,
                    errorCallbackFunction: (exception: Exception) -> Unit) {
        enqueue(Any(), backgroundFunction, callbackFunction, errorCallbackFunction)
    }

    fun <T> enqueue(key: Any?,
                    backgroundFunction: (actionState: ActionState) -> T,
                    callbackFunction: (value: T) -> Unit,
                    errorCallbackFunction: (exception: Exception) -> Unit) {
        require(SwingUtilities.isEventDispatchThread()) { "Must only enqueue on the Event Dispatch Thread" }
        val bgfn = BackgroundFunction { actionState -> backgroundFunction(actionState) }
        val cbfn = CallbackFunction { value: Any? -> callbackFunction(@Suppress("UNCHECKED_CAST") (value as T)) }
        val ecbfn = ErrorCallbackFunction { exception: Exception -> errorCallbackFunction(exception) }
        enqueueBackgroundAction(BackgroundAction(key, bgfn, cbfn, ecbfn))
    }

    private fun enqueueBackgroundAction(bga: BackgroundAction) {
        synchronized(lock) {
            val replaced = queue.put(bga.key, bga)
            replaced?.canceled = true
            if (!executing)
                executeBackgroundWorker()
        }
    }

    private fun executeBackgroundWorker() {
        executing = true
        val worker = object : SwingWorker<Unit, Unit>() {
            override fun doInBackground() {
                while (true) {
                    val key: Any?
                    val bga: BackgroundAction
                    synchronized(lock) {
                        if (queue.isEmpty()) {
                            waiter = null
                            return
                        }
                        key = queue.keys.first()
                        bga = queue[key]!!
                    }
                    val actionState = object : ActionState {
                        override fun actionIsCanceled() = synchronized(lock) { bga.canceled }
                    }
                    var ret: Any? = null
                    var exception: Exception? = null
                    try {
                        ret = bga.backgroundFunction.fn(actionState)
                    } catch (e: Exception) {
                        exception = e
                    }
                    if (!synchronized(lock) { bga.canceled }) {
                        try {
                            if (exception == null) {
                                bga.callbackFunction.fn(ret)
                            } else {
                                bga.errorCallbackFunction.fn(exception)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    synchronized(lock) {
                        if (queue[key] === bga) queue.remove(key)
                        if (queue.isEmpty()) {
                            waiter = null
                            return
                        }
                    }
                }
            }
            override fun done() {
                synchronized(lock) {
                    executing = false
                    if (queue.isEmpty()) {
                        trimQueue()
                    } else {
                        executeBackgroundWorker()
                    }
                }
            }
        }
        worker.execute()
        waiter = Waiter { worker.get() }
    }

    private fun trimQueue() {
        queue = LinkedHashMap(queue)
    }

    fun waitFor() {
        require(SwingUtilities.isEventDispatchThread()) { "Must only wait on the Event Dispatch Thread" }
        synchronized(lock) { waiter }?.waitFor()
    }

    fun cancel(key: Any?) {
        require(SwingUtilities.isEventDispatchThread()) { "Must only cancel on the Event Dispatch Thread" }
        synchronized(lock) { queue.remove(key)?.canceled = true }
    }

}