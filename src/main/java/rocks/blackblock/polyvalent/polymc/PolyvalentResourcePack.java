package rocks.blackblock.polyvalent.polymc;

import io.github.theepicblock.polymc.api.resource.json.JModel;
import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.resource.ResourcePackImplementation;
import io.github.theepicblock.polymc.impl.resource.json.JModelImpl;
import io.github.theepicblock.polymc.impl.resource.json.JModelWrapper;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Objects;

public class PolyvalentResourcePack extends ResourcePackImplementation {

    @Override
    public JModel getOrDefaultVanillaItemModel(String namespace, String model) {

        if (this.getItemModel(namespace, model) == null) {
            if (Objects.equals(model, "shield")) {
                var newModel = Util.GSON.fromJson("{\"parent\":\"builtin/entity\",\"gui_light\":\"front\",\"textures\":{\"particle\":\"block/dark_oak_planks\"},\"display\":{\"thirdperson_righthand\":{\"rotation\":[0,90,0],\"translation\":[10,6,-4],\"scale\":[1,1,1]},\"thirdperson_lefthand\":{\"rotation\":[0,90,0],\"translation\":[10,6,12],\"scale\":[1,1,1]},\"firstperson_righthand\":{\"rotation\":[0,180,5],\"translation\":[-10,2,-10],\"scale\":[1.25,1.25,1.25]},\"firstperson_lefthand\":{\"rotation\":[0,180,5],\"translation\":[10,0,-10],\"scale\":[1.25,1.25,1.25]},\"gui\":{\"rotation\":[15,-25,-5],\"translation\":[2,3,0],\"scale\":[0.65,0.65,0.65]},\"fixed\":{\"rotation\":[0,180,0],\"translation\":[-2,4,-5],\"scale\":[0.5,0.5,0.5]},\"ground\":{\"rotation\":[0,0,0],\"translation\":[4,4,2],\"scale\":[0.25,0.25,0.25]}},\"overrides\":[{\"predicate\":{\"blocking\":1},\"model\":\"item/shield_blocking\"}]}", JModelImpl.class);
                this.setItemModel(namespace, model, new JModelWrapper(newModel));
            } else if (Objects.equals(model, "bow")) {
                var newModel = Util.GSON.fromJson("{\"parent\":\"builtin/entity\",\"gui_light\":\"front\",\"textures\":{\"particle\":\"block/dark_oak_planks\"},\"display\":{\"thirdperson_righthand\":{\"rotation\":[0,90,0],\"translation\":[10,6,-4],\"scale\":[1,1,1]},\"thirdperson_lefthand\":{\"rotation\":[0,90,0],\"translation\":[10,6,12],\"scale\":[1,1,1]},\"firstperson_righthand\":{\"rotation\":[0,180,5],\"translation\":[-10,2,-10],\"scale\":[1.25,1.25,1.25]},\"firstperson_lefthand\":{\"rotation\":[0,180,5],\"translation\":[10,0,-10],\"scale\":[1.25,1.25,1.25]},\"gui\":{\"rotation\":[15,-25,-5],\"translation\":[2,3,0],\"scale\":[0.65,0.65,0.65]},\"fixed\":{\"rotation\":[0,180,0],\"translation\":[-2,4,-5],\"scale\":[0.5,0.5,0.5]},\"ground\":{\"rotation\":[0,0,0],\"translation\":[4,4,2],\"scale\":[0.25,0.25,0.25]}},\"overrides\":[{\"predicate\":{\"blocking\":1},\"model\":\"item/shield_blocking\"}]}", JModelImpl.class);
                this.setItemModel(namespace, model, new JModelWrapper(newModel));
            } else {
                var newModel = new JModelWrapper();
                newModel.setParent("item/generated");
                if (Objects.equals(model, "stick")) {
                    newModel.setParent("item/handheld");
                }
                newModel.getTextures().put("layer0", "item/"+model);

                if (ArrayUtils.contains(new String[]{"leather_helmet", "leather_chestplate", "leather_leggings", "leather_boots"}, model)) {
                    newModel.getTextures().put("layer1", "item/"+model+"_overlay");
                }

                this.setItemModel(namespace, model, newModel);
            }
        }

        return this.getItemModel(namespace, model);
    }

}
