package io.github.petercrawley.minecraftstarshipplugin.customMaterials

import io.github.petercrawley.minecraftstarshipplugin.MinecraftStarshipPlugin.Companion.customBlocks
import org.bukkit.Bukkit.createBlockData
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.MultipleFacing
import org.bukkit.inventory.ItemStack

class MSPMaterial {
	private var materialType: MaterialType = MaterialType.Bukkit
	private var material: Any = Material.AIR

	constructor(material: Material) {
		this.materialType = MaterialType.Bukkit
		this.material = material
	}

	constructor(material: Byte) {
		this.materialType = MaterialType.CustomBlock
		this.material = material
	}

	constructor(material: Int) {
		throw NotImplementedError("Custom items are not yet supported")

//		this.materialType = MaterialType.CustomItem
//		this.material = material
	}

	constructor(material: String) {
		for (i in customBlocks) {
			if (i.value == material) {
				this.materialType = MaterialType.CustomBlock
				this.material = i.key
				return
			}
		}

		this.material = Material.getMaterial(material) ?: Material.AIR
	}

	fun getBukkitMaterial(): Material {
		return when (materialType) {
			MaterialType.Bukkit -> material as Material
			MaterialType.CustomBlock -> Material.MUSHROOM_STEM
			MaterialType.CustomItem -> throw NotImplementedError("Custom items are not yet supported") // Material.STICK
		}
	}

	fun getBukkitBlockData(): BlockData {
		return when (materialType) {
			MaterialType.Bukkit -> createBlockData(material as Material)
			MaterialType.CustomBlock -> {
				val returnValue = createBlockData(Material.MUSHROOM_STEM) as MultipleFacing

				returnValue.setFace(BlockFace.NORTH, bitOfByte(material as Byte, 5))
				returnValue.setFace(BlockFace.EAST,  bitOfByte(material as Byte, 4))
				returnValue.setFace(BlockFace.SOUTH, bitOfByte(material as Byte, 3))
				returnValue.setFace(BlockFace.WEST,  bitOfByte(material as Byte, 2))
				returnValue.setFace(BlockFace.UP,    bitOfByte(material as Byte, 1))
				returnValue.setFace(BlockFace.DOWN,  bitOfByte(material as Byte, 0))

				returnValue
			}
			MaterialType.CustomItem -> throw NotImplementedError("Custom items are not yet supported") // createBlockData(Material.AIR)
		}
	}

	fun getBukkitItemStack(): ItemStack {
		return when (materialType) {
			MaterialType.Bukkit -> ItemStack(material as Material)
			MaterialType.CustomBlock -> {
				val returnValue = ItemStack(Material.STICK)

				val itemMeta = returnValue.itemMeta
				itemMeta.setCustomModelData(material as Int)
				returnValue.itemMeta = itemMeta

				returnValue
			}
			MaterialType.CustomItem -> {
				throw NotImplementedError("Custom items are not yet supported")

//				val returnValue = ItemStack(Material.STICK)
//
//				val itemMeta = returnValue.itemMeta
//				itemMeta.setCustomModelData(material as Int + 192) // Offset by 192 to avoid conflicts with custom blocks
//				returnValue.itemMeta = itemMeta
//
//				returnValue
			}
		}
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as MSPMaterial

		if (materialType != other.materialType) return false
		if (material != other.material) return false

		return true
	}

	override fun hashCode(): Int {
		return 31 * materialType.hashCode() + material.hashCode()
	}
}