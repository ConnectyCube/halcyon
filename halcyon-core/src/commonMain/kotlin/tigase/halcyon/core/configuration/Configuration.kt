/*
 * halcyon-core
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
package tigase.halcyon.core.configuration

import tigase.halcyon.core.builder.ModulesConfigBuilder
import tigase.halcyon.core.exceptions.HalcyonException
import tigase.halcyon.core.xmpp.BareJID
import tigase.halcyon.core.xmpp.forms.JabberDataForm

interface SaslConfig

interface DomainProvider {

	val domain: String

}

data class Registration(
	val domain: String,
	val formHandler: ((JabberDataForm) -> Unit)?,
	val formHandlerWithResponse: ((JabberDataForm) -> JabberDataForm)?,
)

interface Connection

data class Configuration(
	val sasl: SaslConfig?,
	val connection: Connection,
	val registration: Registration? = null,
	internal val modulesConfigurator: (ModulesConfigBuilder.() -> Unit)? = null,
)

val Configuration.domain: String
	get() = if (this.sasl is DomainProvider) {
		this.sasl.domain
	} else if (this.registration != null) {
		this.registration.domain
	} else throw HalcyonException("Cannot determine domain.")

val Configuration.userJID: BareJID?
	get() = null