package rocks.blackblock.polyvalent.networking;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.*;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.KeepAliveS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import rocks.blackblock.polyvalent.Polyvalent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PolyvalentHandshakeHandlerLogin implements PolyvalentHandshakeHandler, ServerPlayPacketListener {
    public static long MAGIC_VALUE = 0x47f83d2c74479974L;

    private final ClientConnection connection;
    private final MinecraftServer server;
    private final ServerPlayerEntity player;
    private final Consumer<PolyvalentHandshakeHandlerLogin> continueJoining;
    private String polyvalentVersion = "";
    private Object2IntMap<String> protocolVersions = null;
    private Object2LongMap<String> lastUpdate = new Object2LongOpenHashMap<>();

    public PolyvalentHandshakeHandlerLogin(MinecraftServer server, ServerPlayerEntity player, ClientConnection connection,
                                           Consumer<PolyvalentHandshakeHandlerLogin> continueJoining) {
        this.server = server;
        this.connection = connection;
        this.player = player;
        this.continueJoining = continueJoining;
        this.connection.setPacketListener(this);
        this.connection.setState(NetworkState.PLAY);

        Polyvalent.log("Sending magic handshake request to client...");

        ((TempPlayerLoginAttachments) player).polyvalent_setHandshakeHandler(this);
        this.connection.send(new KeepAliveS2CPacket(MAGIC_VALUE));
    }

    public void set(String polyvalentVersion, Object2IntMap<String> protocolVersions) {
        this.polyvalentVersion = polyvalentVersion;
        this.protocolVersions = protocolVersions;
    }

    @Override
    public boolean isPolyvalent() {
        return false;
    }

    @Override
    public String getPolyvalentVersion() {
        return null;
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        this.connection.send(packet);
    }

    @Override
    public MinecraftServer getServer() {
        return this.server;
    }

    @Override
    public @Nullable ServerPlayerEntity getPlayer() {
        return this.player;
    }

    public int getSupportedProtocol(String identifier) {
        return this.protocolVersions != null ? this.protocolVersions.getOrDefault(identifier, -1) : -1;
    }

    @Override
    public void setLastPacketTime(String identifier) {
        this.lastUpdate.put(identifier, System.currentTimeMillis());
    }

    @Override
    public long getLastPacketTime(String identifier) {
        return this.lastUpdate.getLong(identifier);
    }

    @Override
    public boolean shouldUpdateWorld() {
        return false;
    }

    @Override
    public void apply(ServerPlayNetworkHandler handler) {
        var polyvalentHandler = PolyvalentNetworkHandlerExtension.of(handler);
        polyvalentHandler.polyvalent_setVersion(this.getPolyvalentVersion());
    }

    @Override
    public void onCustomPayload(CustomPayloadC2SPacket packet) {
        var data = packet.getData();
        if (packet.getChannel().equals(ClientPackets.HANDSHAKE_ID)) {
            Polyvalent.log("Received handshake data from client...");
            PolyvalentServerProtocolHandler.handleHandshake(this, data.readVarInt(), data);
        }
    }

    @Override
    public void onKeepAlive(KeepAliveC2SPacket packet) {

        NetworkThreadUtils.forceMainThread(packet, this, this.server);

        // If the client also has the Polyvalent mod, it should actually prevent a response.
        // So when we do get the magic value back, this means that the client doesn't have the mod.
        // (Unless the server is running behind a proxy like Velocity, which interferes with keepalive packets)
        if (packet.getId() == MAGIC_VALUE) {
            Polyvalent.log("Received magic keepalive response from client.");
            this.continueJoining.accept(this);
        }
    }

    @Override
    public void onHandSwing(HandSwingC2SPacket packet) {

    }

    @Override
    public void onChatMessage(ChatMessageC2SPacket packet) {

    }

    @Override
    public void onClientStatus(ClientStatusC2SPacket packet) {

    }

    @Override
    public void onClientSettings(ClientSettingsC2SPacket packet) {

    }

    @Override
    public void onButtonClick(ButtonClickC2SPacket packet) {

    }

    @Override
    public void onClickSlot(ClickSlotC2SPacket packet) {

    }

    @Override
    public void onCraftRequest(CraftRequestC2SPacket packet) {

    }

    @Override
    public void onCloseHandledScreen(CloseHandledScreenC2SPacket packet) {

    }

    @Override
    public void onPlayerInteractEntity(PlayerInteractEntityC2SPacket packet) {

    }

    @Override
    public void onPlayerMove(PlayerMoveC2SPacket packet) {

    }

    @Override
    public void onPong(PlayPongC2SPacket packet) {

    }

    @Override
    public void onUpdatePlayerAbilities(UpdatePlayerAbilitiesC2SPacket packet) {

    }

    @Override
    public void onPlayerAction(PlayerActionC2SPacket packet) {

    }

    @Override
    public void onClientCommand(ClientCommandC2SPacket packet) {

    }

    @Override
    public void onPlayerInput(PlayerInputC2SPacket packet) {

    }

    @Override
    public void onUpdateSelectedSlot(UpdateSelectedSlotC2SPacket packet) {

    }

    @Override
    public void onCreativeInventoryAction(CreativeInventoryActionC2SPacket packet) {

    }

    @Override
    public void onUpdateSign(UpdateSignC2SPacket packet) {

    }

    @Override
    public void onPlayerInteractBlock(PlayerInteractBlockC2SPacket packet) {

    }

    @Override
    public void onPlayerInteractItem(PlayerInteractItemC2SPacket packet) {

    }

    @Override
    public void onSpectatorTeleport(SpectatorTeleportC2SPacket packet) {

    }

    @Override
    public void onResourcePackStatus(ResourcePackStatusC2SPacket packet) {

    }

    @Override
    public void onBoatPaddleState(BoatPaddleStateC2SPacket packet) {

    }

    @Override
    public void onVehicleMove(VehicleMoveC2SPacket packet) {

    }

    @Override
    public void onTeleportConfirm(TeleportConfirmC2SPacket packet) {

    }

    @Override
    public void onRecipeBookData(RecipeBookDataC2SPacket packet) {

    }

    @Override
    public void onRecipeCategoryOptions(RecipeCategoryOptionsC2SPacket packet) {

    }

    @Override
    public void onAdvancementTab(AdvancementTabC2SPacket packet) {

    }

    @Override
    public void onRequestCommandCompletions(RequestCommandCompletionsC2SPacket packet) {

    }

    @Override
    public void onUpdateCommandBlock(UpdateCommandBlockC2SPacket packet) {

    }

    @Override
    public void onUpdateCommandBlockMinecart(UpdateCommandBlockMinecartC2SPacket packet) {

    }

    @Override
    public void onPickFromInventory(PickFromInventoryC2SPacket packet) {

    }

    @Override
    public void onRenameItem(RenameItemC2SPacket packet) {

    }

    @Override
    public void onUpdateBeacon(UpdateBeaconC2SPacket packet) {

    }

    @Override
    public void onUpdateStructureBlock(UpdateStructureBlockC2SPacket packet) {

    }

    @Override
    public void onSelectMerchantTrade(SelectMerchantTradeC2SPacket packet) {

    }

    @Override
    public void onBookUpdate(BookUpdateC2SPacket packet) {

    }

    @Override
    public void onQueryEntityNbt(QueryEntityNbtC2SPacket packet) {

    }

    @Override
    public void onQueryBlockNbt(QueryBlockNbtC2SPacket packet) {

    }

    @Override
    public void onUpdateJigsaw(UpdateJigsawC2SPacket packet) {

    }

    @Override
    public void onJigsawGenerating(JigsawGeneratingC2SPacket packet) {

    }

    @Override
    public void onUpdateDifficulty(UpdateDifficultyC2SPacket packet) {

    }

    @Override
    public void onUpdateDifficultyLock(UpdateDifficultyLockC2SPacket packet) {

    }

    @Override
    public void onDisconnected(Text reason) {

    }

    @Override
    public ClientConnection getConnection() {
        return this.connection;
    }
}
