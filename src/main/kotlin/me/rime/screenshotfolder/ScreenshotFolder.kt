package me.rime.screenshotfolder

import me.rime.rimelib.util.register
import me.rime.screenshotfolder.config.ConfigHandler
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback

object ScreenshotFolder {
	@Deprecated("This method will be called by the fabric loader, don't call it manually.", level = DeprecationLevel.ERROR)
	fun init() {
		ConfigHandler.load()
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
			incomplete
		}
	}

	const val NAME = "Screenshot Folder"
	const val NAMESPACE = "screenshotfolder"
}