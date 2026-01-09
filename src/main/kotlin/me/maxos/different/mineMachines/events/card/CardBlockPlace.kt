package me.maxos.different.mineMachines.events.card

import me.maxos.different.mineMachines.MineMachines
import me.maxos.different.mineMachines.files.configs.ConfigManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

class CardBlockPlace(

	private val configManager: ConfigManager,
	private val plugin: MineMachines

): Listener {

	@EventHandler(ignoreCancelled = true)
	fun cardPlace(e: BlockPlaceEvent) {

		val block = e.blockPlaced
		val material = block.type

		if (material !in configManager.cardsMaterials) {
			return
		} else {
			val itemPdc = e.itemInHand.itemMeta.persistentDataContainer
			if (itemPdc.has(plugin.gpuKey)) e.isCancelled = true
		}

	}

}