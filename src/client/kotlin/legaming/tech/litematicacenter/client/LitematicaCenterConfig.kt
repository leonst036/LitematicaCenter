package legaming.tech.litematicacenter.client

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import java.io.File

object LitematicaCenterConfig {
    private val configFile = File(FabricLoader.getInstance().configDir.toFile(), "litematicacenter.json")
    private val gson = GsonBuilder().setPrettyPrinting().create()

    data class ConfigData(
        var similarityThreshold: Double = 0.8
    )

    var data = ConfigData()
        private set

    init {
        load()
    }

    fun load() {
        if (configFile.exists()) {
            try {
                configFile.reader().use { reader ->
                    val loaded = gson.fromJson(reader, ConfigData::class.java)
                    if (loaded != null) {
                        data = loaded
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            save()
        }
    }

    fun save() {
        try {
            configFile.writer().use { writer ->
                gson.toJson(data, writer)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
