package rocks.blackblock.polyvalent;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.item.UnclampedModelPredicateProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.polyvalent.block.PolyvalentBlock;
import rocks.blackblock.polyvalent.client.PolyvalentBlockInfo;
import rocks.blackblock.polyvalent.client.PolyvalentItemInfo;
import rocks.blackblock.polyvalent.networking.ModPacketsS2C;

import java.util.*;

/**
 * The client-side Polyvalent class
 *
 * @author   Jelle De Loecker   <jelle@elevenways.be>
 * @since    0.1.0
 */
public class PolyvalentClient implements ClientModInitializer {

    public static HashMap<Integer, Identifier> actualBlockIdentifiers = new HashMap<>();
    public static HashMap<Integer, Identifier> actualItemIdentifiers = new HashMap<>();
    public static HashMap<Integer, PolyvalentBlockInfo> blockInfo = new HashMap<>();
    public static HashMap<Integer, PolyvalentItemInfo> itemInfo = new HashMap<>();
    public static HashMap<Identifier, PolyvalentItemInfo> itemInfoById = new HashMap<>();

    public static boolean connectedToPolyvalentServer = false;

    /**
     * Reset the Polyvalent state
     * (when disconnecting from a Polyvalent server, for example)
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     */
    public static void reset() {
        connectedToPolyvalentServer = false;
        actualBlockIdentifiers.clear();
        actualItemIdentifiers.clear();
        blockInfo.clear();
        itemInfoById.clear();
    }

    /**
     * Mark us as being connected to a Polyvalent server
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    connected_to_polyvalent_server   True if the server is Polyvalent
     */
    public static void markAsPolyvalentServer(boolean connected_to_polyvalent_server) {
        reset();
        connectedToPolyvalentServer = connected_to_polyvalent_server;
    }

    /**
     * Write all raw block ids and their state name to a PacketByteBuf buffer.
     * These will let the server know which polyvalent blocks it can use.
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    buffer   The buffer that will be written to
     */
    public static void writeBlockStateRawIds(PacketByteBuf buffer) {

        ArrayList<BlockState> polyvalent_states = new ArrayList<>();
        Set<Block> seen_blocks = new HashSet<>();
        HashMap<Identifier, Integer> polyvalent_block_ids = new HashMap<>();

        // Iterate over all the registered blockstates
        // and extract the ones that are polyvalent
        for (BlockState state : Block.STATE_IDS) {
            Block block = state.getBlock();

            if (block instanceof PolyvalentBlock) {
                polyvalent_states.add(state);

                if (!seen_blocks.contains(block)) {
                    seen_blocks.add(block);
                    Identifier block_identifier = Registry.BLOCK.getId(block);
                    polyvalent_block_ids.put(block_identifier, Registry.BLOCK.getRawId(block));
                }
            }
        }

        // Write the amount of polyvalent states to the buffer, just for info
        // (We won't write each state to the buffer anymore)
        buffer.writeVarInt(polyvalent_states.size());

        Polyvalent.log("There are " + polyvalent_states.size() + " client-side polyvalent states");

        HashMap<String, Integer> block_state_count = new HashMap<>();
        HashMap<String, Integer> block_state_start = new HashMap<>();

        // Iterate over all the extracted polyvalent states
        for (BlockState state : polyvalent_states) {

            // Get the client-side raw id
            int raw_id = Block.STATE_IDS.getRawId(state);

            Block block = state.getBlock();
            Identifier id = Registry.BLOCK.getId(block);
            String name = id.toString();

            Integer count = block_state_count.getOrDefault(name, 0);
            count++;
            block_state_count.put(name, count);

            Integer start_id = block_state_start.getOrDefault(name, null);

            if (start_id == null) {
                start_id = raw_id;
            } else if (start_id > raw_id) {
                start_id = raw_id;
            }

            block_state_start.put(name, start_id);
        }

        for (String name : block_state_count.keySet()) {
            Polyvalent.log("Block " + name + " has " + block_state_count.get(name) + " states");
            Integer count = block_state_count.get(name);
            Integer start_id = block_state_start.get(name);

            buffer.writeString(name);
            buffer.writeVarInt(count);
            buffer.writeVarInt(start_id);
        }

        // Write the amount of polyvalent blocks to the buffer
        buffer.writeInt(polyvalent_block_ids.size());

        // Write the block ids to the buffer too
        for (Identifier id : polyvalent_block_ids.keySet()) {
            int raw_id = polyvalent_block_ids.get(id);

            buffer.writeString(id.toString());
            buffer.writeVarInt(raw_id);
        }

        Polyvalent.log("Buffer is now: " + buffer.readableBytes() + " bytes");
    }

    /**
     * Write all raw item ids to a PacketByteBuf buffer.
     * These will let the server know which polyvalent items it can use.
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     *
     * @param    buffer   The buffer that will be written to
     */
    public static void writeItemRawIds(PacketByteBuf buffer) {

        // Write the amount of Polyvalent items to the buffer
        buffer.writeVarInt(Polyvalent.ITEMS.size());

        // Now iterate over all the Polyvalent items
        for (Map.Entry<Identifier, Item> entry : Polyvalent.ITEMS.entrySet()) {
            Identifier id = entry.getKey();
            Item item = entry.getValue();

            // Get the raw id this client uses to identify this item
            int block_item_id = Item.getRawId(item);

            buffer.writeString(id.toString());
            buffer.writeVarInt(block_item_id);
        }
    }

    /**
     * Initialize Polyvalent on the client-side
     *
     * @author   Jelle De Loecker   <jelle@elevenways.be>
     * @since    0.1.0
     */
    @Override
    public void onInitializeClient() {

        // Register the Polyvalent packets
        ModPacketsS2C.register();

        // Register the Polyvalent blocks that are translucent
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(),
                Polyvalent.GLASS_BLOCK_ONE,
                Polyvalent.GLASS_BLOCK_TWO,
                Polyvalent.GLASS_BLOCK_THREE,
                Polyvalent.PORTAL_BLOCK_ONE,
                Polyvalent.NO_COLLISION_TRANSPARENT_CARPET_BLOCK
        );

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(),
                Polyvalent.CUTOUT_BLOCK_ONE,
                Polyvalent.SAPLING_ONE
        );

        // Register the Polyvalent blocks that have cutouts
        // (The backside of these blocks can be seen when looking at them)
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutoutMipped(),
                Polyvalent.LEAVES_BLOCK_ONE,
                Polyvalent.LEAVES_BLOCK_TWO,
                Polyvalent.LEAVES_BLOCK_THREE,
                Polyvalent.PLANT_CLIMBABLE_ONE
        );

        FabricModelPredicateProviderRegistry.register(Polyvalent.COMPASS_ITEM, new Identifier("angle"), new UnclampedModelPredicateProvider(){
            private final AngleInterpolator aimedInterpolator = new AngleInterpolator();
            private final AngleInterpolator aimlessInterpolator = new AngleInterpolator();

            @Override
            public float unclampedCall(ItemStack itemStack, @Nullable ClientWorld clientWorld, @Nullable LivingEntity livingEntity, int i) {
                double g;
                Entity entity;
                Entity entity2 = entity = livingEntity != null ? livingEntity : itemStack.getHolder();
                if (entity == null) {
                    return 0.0f;
                }
                if (clientWorld == null && entity.world instanceof ClientWorld) {
                    clientWorld = (ClientWorld)entity.world;
                }
                BlockPos blockPos = CompassItem.hasLodestone(itemStack) ? this.getLodestonePos(clientWorld, itemStack.getOrCreateNbt()) : this.getSpawnPos(clientWorld);
                long l = clientWorld.getTime();
                if (blockPos == null || entity.getPos().squaredDistanceTo((double)blockPos.getX() + 0.5, entity.getPos().getY(), (double)blockPos.getZ() + 0.5) < (double)1.0E-5f) {
                    if (this.aimlessInterpolator.shouldUpdate(l)) {
                        this.aimlessInterpolator.update(l, Math.random());
                    }
                    double d = this.aimlessInterpolator.value + (double)((float)this.scatter(i) / 2.14748365E9f);
                    return MathHelper.floorMod((float)d, 1.0f);
                }
                boolean d = livingEntity instanceof PlayerEntity && ((PlayerEntity)livingEntity).isMainPlayer();
                double e = 0.0;
                if (d) {
                    e = livingEntity.getYaw();
                } else if (entity instanceof ItemFrameEntity) {
                    e = this.getItemFrameAngleOffset((ItemFrameEntity)entity);
                } else if (entity instanceof ItemEntity) {
                    e = 180.0f - ((ItemEntity)entity).getRotation(0.5f) / ((float)Math.PI * 2) * 360.0f;
                } else if (livingEntity != null) {
                    e = livingEntity.bodyYaw;
                }
                e = MathHelper.floorMod(e / 360.0, 1.0);
                double f = this.getAngleToPos(Vec3d.ofCenter(blockPos), entity) / 6.2831854820251465;
                if (d) {
                    if (this.aimedInterpolator.shouldUpdate(l)) {
                        this.aimedInterpolator.update(l, 0.5 - (e - 0.25));
                    }
                    g = f + this.aimedInterpolator.value;
                } else {
                    g = 0.5 - (e - 0.25 - f);
                }
                return MathHelper.floorMod((float)g, 1.0f);
            }

            /**
             * Scatters a seed by integer overflow in multiplication onto the whole
             * int domain.
             */
            private int scatter(int seed) {
                return seed * 1327217883;
            }

            @Nullable
            private BlockPos getSpawnPos(ClientWorld world) {
                return world.getDimension().isNatural() ? world.getSpawnPos() : null;
            }

            @Nullable
            private BlockPos getLodestonePos(World world, NbtCompound nbt) {
                Optional<RegistryKey<World>> optional;
                boolean bl = nbt.contains("LodestonePos");
                boolean bl2 = nbt.contains("LodestoneDimension");
                if (bl && bl2 && (optional = CompassItem.getLodestoneDimension(nbt)).isPresent() && world.getRegistryKey() == optional.get()) {
                    return NbtHelper.toBlockPos(nbt.getCompound("LodestonePos"));
                }
                return null;
            }

            private double getItemFrameAngleOffset(ItemFrameEntity itemFrame) {
                Direction direction = itemFrame.getHorizontalFacing();
                int i = direction.getAxis().isVertical() ? 90 * direction.getDirection().offset() : 0;
                return MathHelper.wrapDegrees(180 + direction.getHorizontal() * 90 + itemFrame.getRotation() * 45 + i);
            }

            private double getAngleToPos(Vec3d pos, Entity entity) {
                return Math.atan2(pos.getZ() - entity.getZ(), pos.getX() - entity.getX());
            }
        });

        // Old test for leaves
        Random r = new Random();
        int low = 0x00b359;
        int high = 0xffff59;

        ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
            //return 0xebb359;
            return r.nextInt(high-low) + low;
        }, Polyvalent.LEAVES_BLOCK_ONE, Polyvalent.LEAVES_BLOCK_TWO, Polyvalent.LEAVES_BLOCK_THREE);
    }

    @Environment(value= EnvType.CLIENT)
    static class AngleInterpolator {
        double value;
        private double speed;
        private long lastUpdateTime;

        AngleInterpolator() {
        }

        boolean shouldUpdate(long time) {
            return this.lastUpdateTime != time;
        }

        void update(long time, double target) {
            this.lastUpdateTime = time;
            double d = target - this.value;
            d = MathHelper.floorMod(d + 0.5, 1.0) - 0.5;
            this.speed += d * 0.1;
            this.speed *= 0.8;
            this.value = MathHelper.floorMod(this.value + this.speed, 1.0);
        }
    }
}
