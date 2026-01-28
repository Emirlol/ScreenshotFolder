package me.ancientri.screenshotfolder

import com.llamalad7.mixinextras.sugar.ref.LocalRef
import kotlinx.io.IOException
import me.ancientri.rimelib.util.FabricLoader
import me.ancientri.rimelib.util.LoggerFactory
import me.ancientri.rimelib.util.color.ColorPalette
import me.ancientri.rimelib.util.command.IncompleteCommand
import me.ancientri.rimelib.util.command.register
import me.ancientri.rimelib.util.player
import me.ancientri.rimelib.util.text.sendText
import me.ancientri.rimelib.util.text.text
import me.ancientri.screenshotfolder.config.ConfigHandler
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.util.ScreenshotRecorder
import net.minecraft.screen.ScreenTexts
import java.io.File
import kotlin.io.path.createDirectories
import kotlin.io.path.isDirectory
import kotlin.io.path.notExists

object ScreenshotFolder {
	@Deprecated("This method will be called by the fabric loader, don't call it manually.", level = DeprecationLevel.ERROR)
	fun init() {
		ConfigHandler.init()
		ClientCommandRegistrationCallback.EVENT.register(NAMESPACE) {
			literal("config") {
				executes {
					source.client.let {
						it.send {
							it.setScreen(ConfigHandler.createGui(it.currentScreen))
						}
					}
				}
			}
			command = IncompleteCommand.getInstance()
		}
	}

	@JvmStatic
	fun mixinBody(getScreenshotFilename: (File) -> File, file2: LocalRef<File>) {
		if (!ConfigHandler.config.enabled) return
		var screenshotsDir = ConfigHandler.config.screenshotsDir
		if (screenshotsDir == null) {
			player?.sendText {
				+chatPrefix
				"The selected screenshot path is invalid! Falling back to default screenshots directory." colored ColorPalette.ERROR
				+ScreenTexts.SPACE
				"Click here to configure." styled {
					color = ColorPalette.TEXT
					underlined
					hoverEvent = showText {
						"Click here to open the config screen." colored ColorPalette.SUCCESS
					}
					clickEvent = runCommand("$NAMESPACE config")
				}
			}
			screenshotsDir = FabricLoader.gameDir.resolve(ScreenshotRecorder.SCREENSHOTS_DIRECTORY)
		}

		if (screenshotsDir.notExists()) {
			try {
				screenshotsDir.createDirectories()
			} catch (exception: IOException) {
				loggerFactory.createLogger("Mixin").error("Failed to create the selected screenshot path!", exception)
				return
			}
		} else if (!screenshotsDir.isDirectory()) {
			player?.sendText {
				+chatPrefix
				"The selected screenshot path is not a directory!" colored ColorPalette.ERROR
				+ScreenTexts.SPACE
				"Click here to configure." styled {
					color = ColorPalette.TEXT
					underlined
					hoverEvent = showText {
						"Click here to open the config screen." colored ColorPalette.SUCCESS
					}
					clickEvent = runCommand("$NAMESPACE config")
				}
			}
			return
		}
		file2.set(screenshotsDir.toFile())
	}

	val loggerFactory = LoggerFactory(NAME)
	const val NAME = "Screenshot Folder"
	const val NAMESPACE = "screenshotfolder"
	val chatPrefix = text {
		"[" colored ColorPalette.SURFACE2
		NAME colored ColorPalette.ACCENT
		"]" colored ColorPalette.SURFACE2
		+ScreenTexts.SPACE
	}
		get() = field.copy()
}