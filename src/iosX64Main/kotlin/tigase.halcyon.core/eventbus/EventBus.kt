package tigase.halcyon.core.eventbus

import tigase.halcyon.core.AbstractHalcyon

actual class EventBus actual constructor(context: AbstractHalcyon) :
    AbstractEventBus(context) {
    override fun createHandlersMap(): MutableMap<String, MutableSet<EventHandler<*>>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createHandlersSet(): MutableSet<EventHandler<*>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}