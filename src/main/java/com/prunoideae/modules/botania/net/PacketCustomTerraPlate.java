package com.prunoideae.modules.botania.net;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.joml.Vector3f;

import java.util.function.Supplier;

public record PacketCustomTerraPlate(BlockPos pos, int color1, int color2, float progress) {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("kubejs_botania", "terra_plate_particle");
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ID,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(color1);
        buf.writeInt(color2);
        buf.writeFloat(progress);
    }

    public static PacketCustomTerraPlate decode(FriendlyByteBuf buf) {
        return new PacketCustomTerraPlate(buf.readBlockPos(), buf.readInt(), buf.readInt(), buf.readFloat());
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            // 客户端处理
            handleClient();
        });
        ctx.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        int ticks = (int) (progress * 100);
        int totalSpiritCount = 3;
        double tickIncrement = 360.0D / (double) totalSpiritCount;
        int speed = 5;
        double wticks = (double) (ticks * speed) - tickIncrement;
        double r = Math.sin((double) (ticks - 100) / 10.0D) * 2.0D;
        double g = Math.sin(wticks * Math.PI / 180.0D * 0.55D);

        for (int ix = 0; ix < totalSpiritCount; ++ix) {
            double x = pos.getX() + Math.sin(wticks * Math.PI / 180.0D) * r + 0.5D;
            double y = pos.getY() + 0.25D + Math.abs(r) * 0.7D;
            double z = pos.getZ() + Math.cos(wticks * Math.PI / 180.0D) * r + 0.5D;
            wticks += tickIncrement;

            int color = lerpColor(color1, color2, progress);
            float red = ((color >> 16) & 0xFF) / 255f;
            float green = ((color >> 8) & 0xFF) / 255f;
            float blue = (color & 0xFF) / 255f;

            level.addParticle(new DustParticleOptions(new Vector3f(red, green, blue), 1.0f),
                    x, y, z, 0, 0, 0);
            level.addParticle(new DustParticleOptions(new Vector3f(red, green, blue), 0.5f),
                    x + Math.random() * 0.2 - 0.1, y + Math.random() * 0.2 - 0.1, z + Math.random() * 0.2 - 0.1,
                    0, 0, 0);

            if (ticks == 100) {
                for (int j = 0; j < 15; ++j) {
                    level.addParticle(new DustParticleOptions(new Vector3f(red, green, blue), 0.7f),
                            pos.getX() + 0.5 + Math.random() * 0.5 - 0.25,
                            pos.getY() + 0.5 + Math.random() * 0.5 - 0.25,
                            pos.getZ() + 0.5 + Math.random() * 0.5 - 0.25,
                            0, 0, 0);
                }
            }
        }
    }

    private static int lerpColor(int color1, int color2, float progress) {
        float r1 = ((color1 >> 16) & 0xFF) / 255f;
        float g1 = ((color1 >> 8) & 0xFF) / 255f;
        float b1 = (color1 & 0xFF) / 255f;
        float r2 = ((color2 >> 16) & 0xFF) / 255f;
        float g2 = ((color2 >> 8) & 0xFF) / 255f;
        float b2 = (color2 & 0xFF) / 255f;
        float r = r1 + (r2 - r1) * progress;
        float g = g1 + (g2 - g1) * progress;
        float b = b1 + (b2 - b1) * progress;
        return ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
    }

    // 静态注册方法（可选）
    public static void register() {
        CHANNEL.registerMessage(0, PacketCustomTerraPlate.class,
                PacketCustomTerraPlate::encode,
                PacketCustomTerraPlate::decode,
                PacketCustomTerraPlate::handle);
    }
}