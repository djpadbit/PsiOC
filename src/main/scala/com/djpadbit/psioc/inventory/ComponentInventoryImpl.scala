package com.djpadbit.psioc.inventory

import net.minecraft.item.ItemStack
import net.minecraft.entity.player.EntityPlayer

import li.cil.oc.common.inventory.ComponentInventory
import li.cil.oc.common.item.data.MicrocontrollerData
import li.cil.oc.api.machine.MachineHost
import li.cil.oc.api.network.ManagedEnvironment
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.api.network.Node
import li.cil.oc.api.network.Message

class ComponentInventoryImpl(val cHost: MachineHost,val cData: MicrocontrollerData, val cStack: ItemStack, val cCAD: ManagedEnvironment) extends ComponentInventory {

	override def host: EnvironmentHost = cHost

	override def items: Array[ItemStack] = cData.components

	override def getSizeInventory: Int = cData.components.length

	override def markDirty() {
		cData.save(cStack)
	}

	override def isItemValidForSlot(slot: Int, stack: ItemStack) = false

	override def isUsableByPlayer(player: EntityPlayer) = true

	override def node: Node = Option(cHost.machine).map(_.node).orNull

	override def onConnect(node: Node) {
		if (node == this.node) {
			connectComponents()
			node.connect(cCAD.node)
		}
	}

	override def onDisconnect(node: Node) {
		if (node == this.node) {
			disconnectComponents()
			cCAD.node.remove()
		}
	}

	override def onMessage(message: Message) {}

}