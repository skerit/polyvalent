package rocks.blackblock.polyvalent.item;

import io.github.theepicblock.polymc.api.resource.ModdedResources;
import io.github.theepicblock.polymc.api.resource.PolyMcResourcePack;
import io.github.theepicblock.polymc.api.resource.TextureAsset;
import io.github.theepicblock.polymc.impl.misc.logging.SimpleLogger;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.util.Identifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class PolyvalentArmorMaterialPoly {

    private ArmorMaterial material;
    private Identifier modelPath;
    private int number;

    public PolyvalentArmorMaterialPoly(ArmorMaterial material) {
        this.material = material;
        // New ArmorMaterial textures are probably put in the `minecraft` namespace
        this.setModelPath(new Identifier("minecraft", material.getName()));
    }

    /**
     * Set the path to the original model texture
     */
    public void setModelPath(Identifier modelPath) {
        this.modelPath = modelPath;
    }

    /**
     * Get the path to the original model texture
     */
    public Identifier getModelPath() {
        return this.modelPath;
    }

    /**
     * Set the number to use for this armor
     * @param number
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * Get the number to use for this armor
     */
    public Integer getNumber() {
        return this.number;
    }

    /**
     * Use the FancyPants way of adding these armor materials
     */
    public void addToResourcePack(ModdedResources resources, PolyMcResourcePack pack, SimpleLogger logger) {

        Identifier modelPath = this.getModelPath();
        Integer number = this.getNumber();

        try {

            String suffix = "";

            if (number < 10) {
                suffix = "0" + number;
            } else {
                suffix = "" + number;
            }

            for (int i = 0; i <= 1; i++) {
                var path = "textures/models/armor/" + modelPath.getPath() + "_layer_" + (i + 1) + ".png";
                byte[] data = resources.getInputStream(modelPath.getNamespace(), path).readAllBytes();
                addToPack(pack, "textures/models/armor/polyvalent_armor_" + suffix + "_layer_" + (i + 1) + ".png", data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addToPack(PolyMcResourcePack pack, String path, byte[] data) {

        pack.setAsset("minecraft", path, (location, gson) -> {
            Files.write(location, data);
        });

    }
}
