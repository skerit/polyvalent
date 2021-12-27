package rocks.blackblock.polyvalent.mixin;

import io.github.theepicblock.polymc.api.PolyMap;
import net.minecraft.block.Block;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import rocks.blackblock.polyvalent.PolyvalentServer;
import rocks.blackblock.polyvalent.networking.PolyvalentHandshakeHandler;
import rocks.blackblock.polyvalent.networking.TempPlayerLoginAttachments;
import rocks.blackblock.polyvalent.polymc.PolyvalentMap;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements TempPlayerLoginAttachments {

    @Unique
    private PolyvalentMap polyMap = null;

    @Unique
    private boolean has_polyvalent = false;

    @Unique
    private boolean polyvalent_requireWorldReload;
    private PolyvalentHandshakeHandler polyvalent_handshakeHandler;

    @Override
    public void polyvalent_setWorldReload(boolean value) {
        this.polyvalent_requireWorldReload = value;
    }

    @Override
    public boolean polyvalent_getWorldReload() {
        return this.polyvalent_requireWorldReload;
    }

    @Override
    public PolyvalentHandshakeHandler polyvalent_getAndRemoveHandshakeHandler() {
        var handler = this.polyvalent_handshakeHandler;
        this.polyvalent_handshakeHandler = null;
        return handler;
    }

    @Override
    public void polyvalent_setHandshakeHandler(PolyvalentHandshakeHandler handler) {
        this.polyvalent_handshakeHandler = handler;
    }

    public void setHasPolyvalent(boolean has_polyvalent) {
        this.has_polyvalent = has_polyvalent;
    }

    @Override
    public boolean hasPolyvalent() {
        return this.has_polyvalent;
    }

    @Override
    public void setPolyvalentMap(PolyvalentMap map) {
        System.out.println("Setting polyvalent map");
        this.polyMap = map;
    }

    @Override
    public PolyvalentMap getPolyvalentMap() {
        System.out.println("Getting polyvalent map...");
        try {
            return this._getPolyvalentMap();
        } catch (Exception e) {
            System.out.println("Error loading polyvalent map: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private PolyvalentMap _getPolyvalentMap() {
        if (this.polyMap == null) {
            this.polyMap = PolyvalentServer.getMainMap().createPlayerMap((ServerPlayerEntity) (Object) this);
        }

        System.out.println("Returning polyvalent map: " + this.polyMap);

        return this.polyMap;
    }

    @Override
    public void loadPolyvalentStates(int amount, PacketByteBuf buffer) {
        System.out.println("Loading...");
        try {
            this._loadPolyvalentStates(amount, buffer);
        } catch (Exception e) {
            System.out.println("Error loading polyvalent states: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void _loadPolyvalentStates(int amount, PacketByteBuf buffer) {

        int i = 0;

        System.out.println("Loading " + amount + " polyvalent states");

        PolyvalentMap map = this.getPolyvalentMap();

        for (i = 0; i < amount; i++) {

            // Get the data as it is known on the client
            int client_raw_id = buffer.readVarInt();
            String client_name = buffer.readString();

            // And now get the raw id from the server
            int server_raw_id = PolyvalentServer.BLOCK_STATE_ID_MAP.get(client_name);

            if (client_raw_id != server_raw_id) {
                map.setServerToClientId(server_raw_id, client_raw_id);
            }

            //int raw_id = Block.STATE_IDS.getRawId(state);
            //buffer.writeVarInt(raw_id);
            //buffer.writeString(state_id);

        }

        System.out.println("Finished loading " + i + " polyvalent states");

    }
}