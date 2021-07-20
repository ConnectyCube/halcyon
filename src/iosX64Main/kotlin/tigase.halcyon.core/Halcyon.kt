package tigase.halcyon.core

import tigase.halcyon.core.connector.AbstractConnector

actual class Halcyon actual constructor() : AbstractHalcyon() {
    override fun reconnect(immediately: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createConnector(): AbstractConnector {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}