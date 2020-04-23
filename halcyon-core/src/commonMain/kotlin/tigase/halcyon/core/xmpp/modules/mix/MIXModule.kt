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
package tigase.halcyon.core.xmpp.modules.mix

import kotlinx.serialization.Serializable
import tigase.halcyon.core.Context
import tigase.halcyon.core.currentTimestamp
import tigase.halcyon.core.eventbus.Event
import tigase.halcyon.core.exceptions.HalcyonException
import tigase.halcyon.core.modules.Criteria
import tigase.halcyon.core.modules.Criterion
import tigase.halcyon.core.modules.XmppModule
import tigase.halcyon.core.requests.IQRequestBuilder
import tigase.halcyon.core.requests.MessageRequestBuilder
import tigase.halcyon.core.xml.Element
import tigase.halcyon.core.xml.element
import tigase.halcyon.core.xml.getChildContent
import tigase.halcyon.core.xmpp.BareJID
import tigase.halcyon.core.xmpp.JID
import tigase.halcyon.core.xmpp.modules.BindModule
import tigase.halcyon.core.xmpp.modules.mam.MAMMessageEvent
import tigase.halcyon.core.xmpp.modules.mix.MIXModule.Companion.MISC_XMLNS
import tigase.halcyon.core.xmpp.modules.roster.RosterItemAnnotation
import tigase.halcyon.core.xmpp.modules.roster.RosterItemAnnotationProcessor
import tigase.halcyon.core.xmpp.modules.roster.RosterModule
import tigase.halcyon.core.xmpp.stanzas.*
import tigase.halcyon.core.xmpp.toBareJID
import tigase.halcyon.core.xmpp.toJID

@Serializable
data class MIXRosterItemAnnotation(val participantId: String) : RosterItemAnnotation

@Serializable
data class MIXInvitation(val inviter: BareJID, val invitee: BareJID, val channel: BareJID, val token: String?)

data class MIXMessageEvent(val channel: BareJID, val stanza: Message, val timestamp: Long) : Event(TYPE) {

	companion object {
		const val TYPE = "tigase.halcyon.core.xmpp.modules.mix.MIXMessageEvent"
	}
}

class MIXModule(override val context: Context) : XmppModule, RosterItemAnnotationProcessor {

	companion object {
		const val XMLNS = "urn:xmpp:mix:core:1"
		const val MISC_XMLNS = "urn:xmpp:mix:misc:0"
		const val TYPE = XMLNS
	}

	override val criteria: Criteria? = Criterion.element(this@MIXModule::isMIXMessage)
	override val type = TYPE
	override val features = arrayOf(XMLNS)

	private lateinit var rosterModule: RosterModule

	override fun initialize() {
		rosterModule = context.modules.getModule(RosterModule.TYPE)
		context.eventBus.register<MAMMessageEvent>(MAMMessageEvent.TYPE, this@MIXModule::process)
	}

	private fun isMIXMessage(message: Element): Boolean {
		if (message.name != Message.NAME || message.attributes["type"] != MessageType.Groupchat.value) return false
		val fromJid = message.attributes["from"]?.toBareJID() ?: return false
		val item = rosterModule.store.getItem(fromJid) ?: return false
		return item.annotations.any { it is MIXRosterItemAnnotation }
	}

	override fun process(element: Element) {
		process(wrap(element), currentTimestamp())
	}

	private fun process(event: MAMMessageEvent) {
		if (!isMIXMessage(event.forwardedStanza.stanza)) return
		process(event.forwardedStanza.stanza, event.forwardedStanza.timestamp ?: currentTimestamp())
	}

	private fun process(message: Message, time: Long) {
		context.eventBus.fire(MIXMessageEvent(message.from!!.bareJID, message, time))
	}

	private fun myJID(): JID =
		context.modules.getModule<BindModule>(BindModule.TYPE).boundJID ?: throw HalcyonException("Resource not bound.")

	private fun invitationToElement(invitation: MIXInvitation): Element {
		return element("invitation") {
			"inviter"{ +invitation.inviter.toString() }
			"invitee"{ +invitation.invitee.toString() }
			"channel"{ +invitation.channel.toString() }
			invitation.token?.let {
				"token"{ +it }
			}
		}
	}

	fun join(invitation: MIXInvitation, nick: String): IQRequestBuilder<JoinResponse> =
		join(invitation.channel, nick, invitation)

	fun join(channel: BareJID, nick: String, invitation: MIXInvitation? = null): IQRequestBuilder<JoinResponse> {
		return context.request.iq {
			type = IQType.Set
			to = myJID().bareJID.toJID()
			"client-join"{
				xmlns = "urn:xmpp:mix:pam:2"
				attributes["channel"] = channel.toString()
				"join"{
					xmlns = XMLNS
					"nick"{
						+nick
					}
					"subscribe"{ attributes["node"] = "urn:xmpp:mix:nodes:messages" }
					"subscribe"{ attributes["node"] = "urn:xmpp:mix:nodes:presence" }
					"subscribe"{ attributes["node"] = "urn:xmpp:mix:nodes:participants" }
					"subscribe"{ attributes["node"] = "urn:xmpp:mix:nodes:info" }
					invitation?.let {
						addChild(invitationToElement(it))
					}
				}
			}
		}.resultBuilder { iq ->
			val join = iq.getChildrenNS("client-join", "urn:xmpp:mix:pam:2")!!.getChildrenNS("join", XMLNS)!!
			val nodes = join.getChildren("subscribe").map { it.attributes["node"]!! }.toTypedArray()
			val nck = join.getChildren("subscribe").firstOrNull()?.value ?: nick
			JoinResponse(join.attributes["jid"]!!.toJID(), nck, nodes)
		}
	}

	data class JoinResponse(val jid: JID, val nick: String, val nodes: Array<String>)

	fun leave(channel: BareJID): IQRequestBuilder<Unit> {
		return context.request.iq {
			type = IQType.Set
			"client-leave"{
				xmlns = "urn:xmpp:mix:pam:2"
				attributes["channel"] = channel.toString()
				"leave"{
					xmlns = XMLNS
				}
			}
		}
	}

	fun message(channel: BareJID, message: String): MessageRequestBuilder {
		return context.request.message {
			to = channel.toJID()
			type = MessageType.Groupchat
			body = message
		}
	}

	override fun prepareRosterGetRequest(stanza: IQ) {
		stanza.getChildrenNS("query", RosterModule.XMLNS)?.add(element("annotate") {
			xmlns = "urn:xmpp:mix:roster:0"
		})
	}

	override fun processRosterItem(item: Element): RosterItemAnnotation? {
		return item.getChildrenNS("channel", "urn:xmpp:mix:roster:0")?.let { channel ->
			MIXRosterItemAnnotation(channel.attributes["participant-id"]!!)
		}
	}

}

data class MixAnnotation(val nick: String, val jid: BareJID?)

fun Element.isMixMessage(): Boolean {
	if (this.name != Message.NAME || this.attributes["type"] != MessageType.Groupchat.value) return false
	return this.getChildrenNS("mix", MIXModule.XMLNS) != null
}

fun Element.getMixAnnotation(): MixAnnotation? {
	if (this.name != Message.NAME || this.attributes["type"] != MessageType.Groupchat.value) return null
	return this.getChildrenNS("mix", MIXModule.XMLNS)?.let {
		val nick = it.getFirstChild("nick")!!.value!!
		val jid = it.getFirstChild("jid")?.value?.toBareJID()
		MixAnnotation(nick, jid)
	}
}

fun Element.getMixInvitation(): MIXInvitation? = this.getChildrenNS("invitation", MISC_XMLNS)?.let {
	val inviter = it.getChildContent("inviter") ?: return null
	val invitee = it.getChildContent("invitee") ?: return null
	val channel = it.getChildContent("channel") ?: return null
	val token = it.getChildContent("token")

	MIXInvitation(inviter.toBareJID(), invitee.toBareJID(), channel.toBareJID(), token)
}


