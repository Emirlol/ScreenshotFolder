package me.ancientri.screenshotfolder.config

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.isxander.yacl3.dsl.YetAnotherConfigLib
import dev.isxander.yacl3.dsl.controller
import dev.isxander.yacl3.dsl.stringField
import dev.isxander.yacl3.dsl.tickBox
import me.ancientri.rimelib.config.ConfigBuilder
import me.ancientri.rimelib.config.dfu.JsonCodecConfigManager
import me.ancientri.rimelib.util.FabricLoader
import me.ancientri.rimelib.util.text.text
import me.ancientri.screenshotfolder.ScreenshotFolder
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Util
import org.slf4j.Logger
import java.nio.file.Path

data class Config(
	@JvmField
	val enabled: Boolean = true,
	val screenshotsDir: Path? = null
) {
	class Builder(val config: Config) : ConfigBuilder<Config> {
		var enabled: Boolean = config.enabled
		var screenshotsDir: Path? = config.screenshotsDir

		override fun build(): Config = config.copy(
			enabled = enabled,
			screenshotsDir = screenshotsDir
		)
	}
}

object ConfigHandler : JsonCodecConfigManager<Config, Config.Builder>() {
	override val codec: Codec<Config> = RecordCodecBuilder.create { instance ->
		instance.group(
			Codec.BOOL.fieldOf("enabled").forGetter(Config::enabled),
			Codec.STRING.xmap(
				{
					if (it.isBlank()) null else try {
						Path.of(it)
					} catch (_: Exception) {
						null // Fallback to null if the path is invalid
					}
				},
				{ it?.toString() ?: "" }
			).fieldOf("screenshotsDir").forGetter(Config::screenshotsDir)
		).apply(instance, ::Config)
	}

	override fun builder(config: Config): Config.Builder = Config.Builder(config)

	override val configPath: Path = FabricLoader.configDir.resolve(ScreenshotFolder.NAMESPACE).resolve("config.json")
	override val default: Config = Config()
	override val logger: Logger = ScreenshotFolder.loggerFactory.createLogger("Config Handler")

	fun createGui(parent: Screen?): Screen = YetAnotherConfigLib(ScreenshotFolder.NAMESPACE) {
		val builder = builder(config)
		save {
			saveConfig(applyBuilder(builder))
		}
		title("${ScreenshotFolder.NAME} Config".text)
		val general by categories.registering {
			name("General".text)
			val enabled by rootOptions.registering {
				name("Enabled".text)
				controller = tickBox()
				binding(
					builder.enabled,
					{ builder.enabled },
					{ value -> builder.enabled = value }
				)
			}
			val screenshotsDir by rootOptions.registering {
				name("Screenshots Directory".text)
				controller = stringField()
				binding(
					builder.screenshotsDir?.toString() ?: "",
					{ builder.screenshotsDir?.toString() ?: "" },
					{ value -> builder.screenshotsDir = try { if (value.isBlank()) null else Path.of(value) } catch (_: Exception) { null } }
				)
			}
			val openButton by rootOptions.registeringButton {
				name("Open the current screenshots directory".text)
				text("Click Here".text)
				action { _, _ ->
					Util.getOperatingSystem().open(screenshotsDir.pendingValue())
					MinecraftClient.getInstance().soundManager.play(PositionedSoundInstance.ui(SoundEvents.BLOCK_NOTE_BLOCK_PLING, 1.0f))
				}
			}
		}
	}.generateScreen(parent)
}