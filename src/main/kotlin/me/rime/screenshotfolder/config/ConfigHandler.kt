package me.rime.screenshotfolder.config

import dev.isxander.yacl3.api.OptionEventListener.Event
import dev.isxander.yacl3.config.v3.JsonFileCodecConfig
import dev.isxander.yacl3.config.v3.register
import dev.isxander.yacl3.dsl.YetAnotherConfigLib
import dev.isxander.yacl3.dsl.controller
import dev.isxander.yacl3.dsl.stringField
import dev.isxander.yacl3.dsl.tickBox
import me.rime.rimelib.util.LogUtil
import me.rime.rimelib.util.text
import me.rime.screenshotfolder.ScreenshotFolder
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Util
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories
import kotlin.io.path.notExists

val configPath: Path = FabricLoader.getInstance().configDir.resolve(ScreenshotFolder.NAMESPACE)
const val configFileName = "config.json"

@Suppress("UnstableApiUsage")
object ConfigHandler : JsonFileCodecConfig<ConfigHandler>(configPath.resolve(configFileName)) {
	val enabled by register(true, BOOL)
	private val screenshotsDir by register("", STRING)
	var actualDir: Path? = null
	private val LOGGER = LogUtil.createLogger("Config Handler", ScreenshotFolder.NAME)

	fun load() {
		val path = configPath.resolve(configFileName)
		if (path.notExists()) {
			path.createParentDirectories()
			path.createFile()
		}
		loadFromFile()
	}

	fun createGui(parent: Screen?): Screen = YetAnotherConfigLib(ScreenshotFolder.NAMESPACE) {
		save(::saveToFile)
		title("${ScreenshotFolder.NAME} Config".text)
		val general by categories.registering {
			name("General".text)
			val enabled = rootOptions.register(enabled) {
				name("Enabled".text)
				controller = tickBox()
			}
			val screenshotsDir = rootOptions.register(screenshotsDir) {
				name("Screenshots Directory".text)
				controller = stringField()
				addListener { option, event ->
					when (event) {
						Event.INITIAL, Event.STATE_CHANGE -> {
							actualDir = Path.of(option.pendingValue())
						}
						else -> {}
					}
				}
			}
			val exampleOption by rootOptions.registeringButton {
				name("Open the current screenshots directory".text)
				text("Click Here".text)
				action { _, _ ->
					Util.getOperatingSystem().open(screenshotsDir.pendingValue())
					MinecraftClient.getInstance().soundManager.play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_PLING, 1.0f))
				}
			}
		}
	}.generateScreen(parent)
}