package io.github.petercrawley.minecraftstarshipplugin.customMaterials

import io.github.petercrawley.minecraftstarshipplugin.bit
import io.github.petercrawley.minecraftstarshipplugin.toByte
import org.bukkit.Bukkit.createBlockData
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.MultipleFacing
import org.bukkit.inventory.ItemStack

class MSPMaterial {
	companion object {
		var customBlocks = mapOf<Byte, String>()
	}

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

	constructor(material: BlockData) {
		if (material.material == Material.MUSHROOM_STEM) {
			val block = material as MultipleFacing

			val id: Byte = (
				block.hasFace(BlockFace.NORTH).toByte() * 32 +
				block.hasFace(BlockFace.EAST ).toByte() * 16 +
				block.hasFace(BlockFace.SOUTH).toByte() *  8 +
				block.hasFace(BlockFace.WEST ).toByte() *  4 +
				block.hasFace(BlockFace.UP   ).toByte() *  2 +
				block.hasFace(BlockFace.DOWN ).toByte()
			).toByte()

			val newMaterial: Any = if (customBlocks.containsKey(id)) id else Material.MUSHROOM_STEM

			when (newMaterial == Material.MUSHROOM_STEM) {
				true -> {
					this.materialType = MaterialType.Bukkit
					this.material = Material.MUSHROOM_STEM
				}
				false -> {
					this.materialType = MaterialType.CustomBlock
					this.material = newMaterial
				}
			}

		} else {
			this.materialType = MaterialType.Bukkit
			this.material = material
		}
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

				returnValue.setFace(BlockFace.NORTH, (material as Byte).bit(5))
				returnValue.setFace(BlockFace.EAST,  (material as Byte).bit(4))
				returnValue.setFace(BlockFace.SOUTH, (material as Byte).bit(3))
				returnValue.setFace(BlockFace.WEST,  (material as Byte).bit(2))
				returnValue.setFace(BlockFace.UP,    (material as Byte).bit(1))
				returnValue.setFace(BlockFace.DOWN,  (material as Byte).bit(0))

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

	override fun toString(): String {
		return when(materialType) {
			MaterialType.Bukkit -> material.toString()
			MaterialType.CustomBlock -> customBlocks[material as Byte]!!
			MaterialType.CustomItem -> throw NotImplementedError("Custom items are not yet supported")
		}
	}
}