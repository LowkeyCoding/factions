package io.icker.factions.core

import io.icker.factions.api.persistents.User
import io.icker.factions.data.ToNbt
import io.icker.factions.data.DataStore
import io.icker.factions.data.FromNbt
import io.icker.factions.util.Message
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.EnderChestInventory
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtIntArray
import net.minecraft.nbt.NbtList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import java.util.UUID

class FactionStorage(private var isInUse: Boolean = false) : EnderChestInventory(), ToNbt<FactionStorage> {

    companion object : FromNbt<FactionStorage> {
        private const val chestMapFile = "chestMap.dat"

        private val factionChestMap: HashSet<Vec3i> = DataStore.loadObject(chestMapFile, {factionChestMapFromNbt(it)}) { HashSet() }
        private val factionInventories: HashMap<UUID, FactionStorage> by lazy { HashMap() }

        fun saveFactionStorageObjects() {
            DataStore.saveObject(chestMapFile, factionChestMap) { factionChestMapToNbt(it) }
            factionInventories.forEach { (uuid, factionStorage) ->
                DataStore.saveObject("inventories/${uuid}.dat", factionStorage)
            }
        }

        fun isFactionEnderChest(pos: BlockPos): Boolean = factionChestMap.contains(pos)

        fun toggleFactionEnderChest(pos: BlockPos) {
            if (factionChestMap.contains(pos)) {
                factionChestMap.remove(pos)
            } else {
                factionChestMap.add(pos)
            }
        }

        fun tryOpenFactionEnderChest(player: PlayerEntity): EnderChestInventory? {
            val user = User.get(player.uuid)
            val faction = user.faction

            if (faction == null) {
                Message("You cannot use faction storage when you are not in a faction.").send(player, false)
                return null
            }

            val inventory = if (faction.id in factionInventories) factionInventories[faction.id]!!
                else DataStore.loadObject("inventories/${faction.id}.dat", FactionStorage) { FactionStorage() }

            if (inventory.isInUse) {
                Message("Faction inventory is currently in use. Please try again later.")
                    .send(player, false)
                return null
            }

            inventory.isInUse = true

            return inventory
        }

        private fun factionChestMapFromNbt(element: NbtElement): HashSet<Vec3i> {
            if (element is NbtList)
                return HashSet(element.map {
                    val e = it as NbtIntArray
                    Vec3i(e[0].intValue(), e[1].intValue(), e[2].intValue())
                })
            return HashSet()
        }

        private fun factionChestMapToNbt(chestMap: HashSet<Vec3i>): NbtList {
            val list = NbtList()
            chestMap.forEach {
                NbtIntArray(intArrayOf(it.x, it.y, it.z))
                    .let { pos -> list.add(pos)}
            }

            return list
        }

        override fun fromNbt(nbt: NbtElement): FactionStorage {
            val inventory = FactionStorage()
            if (nbt is NbtList)
                inventory.readNbtList(nbt)

            return inventory
        }

    }

    override fun onClose(player: PlayerEntity?) {
        isInUse = false
        super.onClose(player)
    }


    override fun toNbt(): NbtElement {
        return toNbtList()
    }


}