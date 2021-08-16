package com.the9grounds.aeadditions.network.packets

import appeng.helpers.InventoryAction
import com.the9grounds.aeadditions.container.chemical.ChemicalTerminalContainer
import io.netty.buffer.Unpooled
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.PacketBuffer

class MEInteractionPacket : BasePacket {

    private var windowId: Int = 0
    private var slot: Int = 0
    private var action: InventoryAction? = null
    
    constructor(packetBuffer: PacketBuffer) {
        windowId = packetBuffer.readInt()
        slot = packetBuffer.readVarInt()
        action = packetBuffer.readEnumValue(InventoryAction::class.java)
    }
    
    constructor(windowId: Int, slot: Int, action: InventoryAction) {
        this.windowId = windowId
        this.slot = slot
        this.action = action
        
        val packet = PacketBuffer(Unpooled.buffer())
        
        packet.writeInt(getPacketId())
        packet.writeInt(windowId)
        packet.writeVarInt(slot)
        packet.writeEnumValue(action)
        configureWrite(packet)
    }

    override fun serverPacketData(player: PlayerEntity?) {
        val container = player!!.openContainer
        if (container is ChemicalTerminalContainer) {
            if (container.windowId != windowId) {
                return
            }
            
            container.handleInteraction(slot, action!!)
        }
    }
}