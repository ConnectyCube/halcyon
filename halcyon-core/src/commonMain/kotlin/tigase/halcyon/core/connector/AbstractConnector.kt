/*
 * Tigase Halcyon XMPP Library
 * Copyright (C) 2018 Tigase, Inc. (office@tigase.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
package tigase.halcyon.core.connector

import tigase.halcyon.core.xmpp.SessionController

abstract class AbstractConnector(val context: tigase.halcyon.core.Context) {

	var state: State = State.Disconnected
		protected set(value) {
			val old = field
			field = value
			if (old != field) context.eventBus.fire(ConnectorStateChangeEvent(old, field))
		}

	abstract fun createSessionController(): SessionController

	abstract fun send(data: CharSequence)

	abstract fun start()

	abstract fun stop()

}