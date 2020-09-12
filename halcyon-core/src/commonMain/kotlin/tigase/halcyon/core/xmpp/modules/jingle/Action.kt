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
package tigase.halcyon.core.xmpp.modules.jingle

enum class Action(val value: String) {
    contentAccept("content-accept"),
    contentAdd("content-add"),
    contentModify("content-modify"),
    contentReject("content-reject"),
    descriptionInfo("description-info"),
    securityInfo("security-info"),
    sessionAccept("session-accept"),
    sessionInfo("session-info"),
    sessionInitiate("session-initiate"),
    sessionTerminate("session-terminate"),
    transportAccept("transport-accept"),
    transportInfo("transport-info"),
    transportReject("transport-reject"),
    transportReplace("transport-replace");

    companion object {
        fun fromValue(value: String) = values().find { it.value == value }
    }
}