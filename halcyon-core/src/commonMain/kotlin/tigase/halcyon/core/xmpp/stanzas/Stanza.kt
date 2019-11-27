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
package tigase.halcyon.core.xmpp.stanzas

import getTypeAttr
import tigase.halcyon.core.xml.Element
import tigase.halcyon.core.xmpp.JID
import tigase.halcyon.core.xmpp.StanzaType

abstract class Stanza protected constructor(private val element: Element) {

	private fun getJID(attName: String): JID? {
		val att = element.attributes[attName]
		return if (att == null) null else JID.parse(att)
	}

	private fun setAtt(attName: String, value: String?) {
		if (value == null) {
			element.attributes.remove(attName)
		} else {
			element.attributes[attName] = value
		}
	}

	var to: JID?
		get() = getJID("to")
		set(value) = setAtt("to", value?.toString())

	var from: JID?
		get() = getJID("from")
		set(value) = setAtt("from", value?.toString())

	var type: StanzaType?
		set(value) = setAtt("type", value?.name?.toLowerCase())
		get() = element.getTypeAttr()
}