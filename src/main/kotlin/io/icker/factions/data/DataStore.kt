package io.icker.factions.data

import io.icker.factions.FactionsMod
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtIo
import java.io.IOException

object DataStore {
    private val rootPath = FabricLoader.getInstance().gameDir.resolve("world/factions")
    private const val nbtRootKey = "root"

    fun <T> loadObject(path: String, fromNbt: FromNbt<T>, default: () -> T): T {
        val file = rootPath.resolve(path).toFile()

        try {
            return NbtIo.readCompressed(file)
                .get(nbtRootKey)!!
                .let { fromNbt.fromNbt(it) }
        } catch (e: IOException) {
            FactionsMod.LOGGER.error("Failed to read NBT data ({})", file, e)
        }

        return default()
    }

    fun <T> saveObject(path: String, value: T) where T: ToNbt<T> {
        saveObject(path, value.toNbt())
    }

    fun <T> saveObject(path: String, value: T, toNbt: (T) -> NbtElement) {
        saveObject(path, toNbt(value))
    }

    fun saveObject(path: String, nbt: NbtElement) {
        val file = rootPath.resolve(path).toFile()

        try {
            val parent = file.parentFile
            if (!parent.exists()) {
                parent.mkdirs()
            }

            val data = NbtCompound()
            data.put(nbtRootKey, nbt)
            NbtIo.writeCompressed(data, file)
        } catch (e: IOException) {
            FactionsMod.LOGGER.error("Failed to write NBT data ({})", file, e)
        }

    }

}

fun interface ToNbt<in T> {
    fun toNbt(): NbtElement
}

fun interface FromNbt<out T> {
    fun fromNbt(nbt: NbtElement): T
}