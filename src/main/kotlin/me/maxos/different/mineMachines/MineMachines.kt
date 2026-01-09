package me.maxos.different.mineMachines

import me.maxos.different.mineMachines.commands.PluginCommands
import me.maxos.different.mineMachines.commands.tabcomplete.CommandsTabComplete
import me.maxos.different.mineMachines.containers.MachinesContainer
import me.maxos.different.mineMachines.database.DatabaseManager
import me.maxos.different.mineMachines.events.card.CardBlockPlace
import me.maxos.different.mineMachines.events.entity.ShulkerDamage
import me.maxos.different.mineMachines.events.machine.BlockBreak
import me.maxos.different.mineMachines.events.machine.BlockExplodes
import me.maxos.different.mineMachines.events.machine.BlockPlace
import me.maxos.different.mineMachines.events.machine.actions.BanActionsBlock
import me.maxos.different.mineMachines.events.player.ClickOnBlock
import me.maxos.different.mineMachines.files.FileManager
import me.maxos.different.mineMachines.files.configs.ConfigManager
import me.maxos.different.mineMachines.files.configs.MessagesManager
import me.maxos.different.mineMachines.files.configs.menu.MenuConfigManager
import me.maxos.different.mineMachines.items.machines.MachineItemStack
import me.maxos.different.mineMachines.items.videocards.GpuItemStack
import me.maxos.different.mineMachines.menu.buttons.action.ClickButton
import me.maxos.different.mineMachines.visualizations.LightingMachine
import me.maxos.different.mineMachines.visualizations.scoreboard.ScoreBoardTeam
import net.j4c0b3y.api.menu.MenuHandler
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.command.PluginCommand
import org.bukkit.plugin.java.JavaPlugin

class MineMachines : JavaPlugin() {

	companion object {

		private const val SETTINGS_FILE = "settings.yml"
		private const val MENU_FILE = "menu.yml"
		private const val MESSAGES_FILE = "messages.yml"

		private const val MAIN_COMMAND = "minemachines"

		private const val MACHINES_KEY = "mine_machines_item"
		private const val CASH_PROFITS_KEY = "cash_profits"
		private const val GPU_AMOUNT_KEY = "gpu_amount"
		private const val MENU_ICON_REQUIRED_KEY = "required_icon"
		private const val GPU_KEY = "gpu_id"
		private const val GPU_PROFIT_MARGIN_KEY = "gpu_profit_margin"

		private const val SHULKER_KEY = "glowing_shulker"
		private const val TEAM_NAME = "lighting_machines"

	}

	private lateinit var settingsManager: FileManager
	private lateinit var menuSettingsManager: FileManager
	private lateinit var messagesSettingsManager: FileManager
	private lateinit var configManager: ConfigManager
	private lateinit var menuConfigManager: MenuConfigManager
	private lateinit var messagesManager: MessagesManager

	private lateinit var databaseManager: DatabaseManager

	private lateinit var machineItemStack: MachineItemStack
	private lateinit var gpuItemStack: GpuItemStack

	private lateinit var menuHandler: MenuHandler
	private lateinit var clickButton: ClickButton

	private lateinit var pluginCommands: PluginCommands
	private lateinit var commandsTabComplete: CommandsTabComplete

	private lateinit var machinesContainer: MachinesContainer

	private lateinit var blockPlace: BlockPlace
	private lateinit var blockBreak: BlockBreak
	private lateinit var cardBlockPlace: CardBlockPlace
	private lateinit var banActionsBlock: BanActionsBlock
	private lateinit var clickOnBlock: ClickOnBlock
	private lateinit var blockExplodes: BlockExplodes
	private lateinit var shulkerDamage: ShulkerDamage

	private lateinit var scoreBoardTeam: ScoreBoardTeam
	private lateinit var lightingMachine: LightingMachine

	private lateinit var command: PluginCommand

	lateinit var machinesKey: NamespacedKey
	lateinit var cashProfitsKey: NamespacedKey
	lateinit var gpuAmount: NamespacedKey

	lateinit var menuIconRequiredKey: NamespacedKey

	lateinit var gpuKey: NamespacedKey
	lateinit var gpuProfitMargin: NamespacedKey

	lateinit var glowShulkerKey: NamespacedKey


	override fun onEnable() {

		//!! Конфигурация плагина
		settingsManager = FileManager(this, SETTINGS_FILE) // менеджер файла с хранящейся в нём конфигурацией
		configManager = ConfigManager(settingsManager) // основной менеджер для работы с готовыми конфигурациями
		menuSettingsManager = FileManager(this, MENU_FILE)
		menuConfigManager = MenuConfigManager(menuSettingsManager)
		messagesSettingsManager = FileManager(this, MESSAGES_FILE)
		messagesManager = MessagesManager(messagesSettingsManager)

		databaseManager = DatabaseManager(this, configManager)

		machinesKey = NamespacedKey(this, MACHINES_KEY)
		cashProfitsKey = NamespacedKey(this, CASH_PROFITS_KEY)
		gpuAmount = NamespacedKey(this, GPU_AMOUNT_KEY)
		menuIconRequiredKey = NamespacedKey(this, MENU_ICON_REQUIRED_KEY)
		gpuKey = NamespacedKey(this, GPU_KEY)
		gpuProfitMargin = NamespacedKey(this, GPU_PROFIT_MARGIN_KEY)
		glowShulkerKey = NamespacedKey(this, SHULKER_KEY)

		scoreBoardTeam = ScoreBoardTeam(this, TEAM_NAME, configManager)

		machineItemStack = MachineItemStack(configManager, this) // хранилище наших машин в виде предметов
		gpuItemStack = GpuItemStack(configManager, this)

		lightingMachine = LightingMachine(this, scoreBoardTeam, configManager)
		machinesContainer = MachinesContainer(machineItemStack, databaseManager, this, configManager, lightingMachine)

		menuHandler = MenuHandler(this)
		clickButton = ClickButton(configManager, this, machinesContainer, menuConfigManager, messagesManager)

		pluginCommands = PluginCommands(machineItemStack, gpuItemStack, this, messagesManager) // обработка команд плагина
		commandsTabComplete = CommandsTabComplete(machineItemStack, gpuItemStack) // табуляция для команд выше

		command = this.getCommand(MAIN_COMMAND)!! // регистрируем команду
		command.setExecutor(pluginCommands)
		command.tabCompleter = commandsTabComplete
		setCommandMessages()

		blockPlace = BlockPlace(machineItemStack, this, machinesContainer, messagesManager)
		blockBreak = BlockBreak(machinesContainer, gpuItemStack, configManager, messagesManager)
		cardBlockPlace = CardBlockPlace(configManager, this)
		banActionsBlock = BanActionsBlock(machinesContainer, configManager)
		clickOnBlock = ClickOnBlock(menuConfigManager, clickButton, machinesContainer, this, configManager, databaseManager, gpuItemStack, messagesManager)
		blockExplodes = BlockExplodes(machinesContainer, configManager, gpuItemStack, messagesManager)
		shulkerDamage = ShulkerDamage(this)

		val manager = Bukkit.getPluginManager()
		manager.registerEvents(blockPlace, this)
		manager.registerEvents(blockBreak, this)
		manager.registerEvents(cardBlockPlace, this)
		manager.registerEvents(banActionsBlock, this)
		manager.registerEvents(clickOnBlock, this)
		manager.registerEvents(blockExplodes, this)
		manager.registerEvents(shulkerDamage, this)

	}

	fun onReload() {

		settingsManager.reloadConfig()
		menuSettingsManager.reloadConfig()
		messagesSettingsManager.reloadConfig()
		setCommandMessages()

		configManager.reloadMainSettings()
		messagesManager.reloadConfig()

		scoreBoardTeam.reloadTeams()
		machinesContainer.refreshShulkers()

		machineItemStack.reloadMachinesItems()
		gpuItemStack.reloadGpuItems()

		commandsTabComplete.fillingList()

		menuConfigManager.reloadMenus()

		machinesContainer.reloadAllMachines()
	}

	private fun setCommandMessages() {
		command.setPermissionMessage(messagesManager.getMessage("no-perms", ""))
		command.usage = messagesManager.getMessage("usage-command", "")
	}

	override fun onDisable() {

		machinesContainer.delShulkers()
		scoreBoardTeam.clearTeams()

	}
}
