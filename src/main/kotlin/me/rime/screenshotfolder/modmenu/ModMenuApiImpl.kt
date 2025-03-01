package me.rime.screenshotfolder.modmenu

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import me.rime.screenshotfolder.config.ConfigHandler

class ModMenuApiImpl: ModMenuApi {
    override fun getModConfigScreenFactory() = ConfigScreenFactory(ConfigHandler::createGui)
}