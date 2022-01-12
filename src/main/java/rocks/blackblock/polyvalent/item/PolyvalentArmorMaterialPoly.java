package rocks.blackblock.polyvalent.item;

import io.github.theepicblock.polymc.api.resource.ResourcePackMaker;
import io.github.theepicblock.polymc.impl.poly.item.ArmorMaterialPoly;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.util.Identifier;

public class PolyvalentArmorMaterialPoly extends ArmorMaterialPoly {

    public PolyvalentArmorMaterialPoly(ArmorMaterial material) {
        super(material);
    }

    /**
     * Use the FancyPants way of adding these armor materials
     */
    @Override
    public boolean shouldUseFancyPants() {
        return false;
    }

    /**
     * Use the FancyPants way of adding these armor materials
     */
    @Override
    public void addToResourcePack(ResourcePackMaker pack) {

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
                var path = "assets/minecraft/textures/models/armor/" + modelPath.getPath() + "_layer_" + (i + 1) + ".png";
                byte[] data = pack.getFileStream(modelPath.getNamespace(), path).readAllBytes();

                pack.writeToPath("assets/minecraft/textures/models/armor/polyvalent_armor_" + suffix + "_layer_" + (i + 1) + ".png", data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
