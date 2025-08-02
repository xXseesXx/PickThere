package org.xXseesXx.pickthere.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.xXseesXx.pickthere.client.SelectedInventoryManager;

import java.util.List;

public class InventoryRenderer {
    private static VertexBuffer vertexBuffer;
    public static boolean requestedRefresh = false;

    public static void renderSelectedInventories(RenderLevelStageEvent event) {
        List<BlockPos> regularPositions = SelectedInventoryManager.getSelectedPositions();
        List<BlockPos> sameItemOnlyPositions = SelectedInventoryManager.getSameItemOnlyPositions();
        
        if (regularPositions.isEmpty() && sameItemOnlyPositions.isEmpty()) {
            return;
        }

        if (vertexBuffer == null || requestedRefresh) {
            requestedRefresh = false;
            vertexBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);

            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder buffer = tessellator.getBuilder();

            var opacity = 0.8F;

            buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

            // Render regular selections in gold/yellow
            regularPositions.forEach(pos -> {
                if (pos == null) return;
                renderInventoryOutline(buffer, pos, 1.0f, 0.8f, 0.0f, opacity); // Gold
            });

            // Render same-item-only selections in red
            sameItemOnlyPositions.forEach(pos -> {
                if (pos == null) return;
                renderInventoryOutline(buffer, pos, 1.0f, 0.0f, 0.0f, opacity); // Red
            });

            vertexBuffer.bind();
            vertexBuffer.upload(buffer.end());
            VertexBuffer.unbind();
        }

        if (vertexBuffer != null) {
            Vec3 view = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition();

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glDisable(GL11.GL_DEPTH_TEST);

            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            PoseStack matrix = event.getPoseStack();
            matrix.pushPose();
            matrix.translate(-view.x, -view.y, -view.z);

            vertexBuffer.bind();
            vertexBuffer.drawWithShader(matrix.last().pose(), new Matrix4f(event.getProjectionMatrix()), RenderSystem.getShader());
            VertexBuffer.unbind();
            matrix.popPose();

            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
        }
    }

    private static void renderInventoryOutline(BufferBuilder buffer, BlockPos pos, float red, float green, float blue, float opacity) {
        final float size = 1.0f;
        final double x = pos.getX(), y = pos.getY(), z = pos.getZ();

        // TOP face
        buffer.vertex(x, y + size, z).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y + size, z).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y + size, z).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y + size, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y + size, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y + size, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y + size, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y + size, z).color(red, green, blue, opacity).endVertex();

        // BOTTOM face
        buffer.vertex(x + size, y, z).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y, z).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y, z).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y, z).color(red, green, blue, opacity).endVertex();

        // Vertical edges
        buffer.vertex(x + size, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y + size, z + size).color(red, green, blue, opacity).endVertex();

        buffer.vertex(x + size, y, z).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x + size, y + size, z).color(red, green, blue, opacity).endVertex();

        buffer.vertex(x, y, z + size).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y + size, z + size).color(red, green, blue, opacity).endVertex();

        buffer.vertex(x, y, z).color(red, green, blue, opacity).endVertex();
        buffer.vertex(x, y + size, z).color(red, green, blue, opacity).endVertex();
    }

    public static void requestRefresh() {
        requestedRefresh = true;
    }
}