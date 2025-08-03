package org.xXseesXx.pickthere.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.xXseesXx.pickthere.Pickthere;
import org.xXseesXx.pickthere.items.PickThereItem;
import org.xXseesXx.pickthere.util.InventoryManager;
import java.util.List;

@SideOnly(Side.CLIENT)
public class DebugRenderer {
    
    // Cache for performance optimization
    private ItemStack lastRenderedItem = null;
    private List<InventoryManager.Position> cachedRegularPositions = null;
    private List<InventoryManager.Position> cachedSameItemOnlyPositions = null;
    
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player == null) {
            return;
        }
        
        try {
            // Check if player is holding PickThere item in hand and render selected inventories
            ItemStack pickThereItem = findPickThereItemInHand(player);
            if (pickThereItem != null) {
                boolean pickupEnabled = PickThereItem.isPickupEnabled(pickThereItem);
                
                if (pickupEnabled && org.xXseesXx.pickthere.config.PickThereConfig.enableVisualEffects) {
                    renderSelectedInventories(player, pickThereItem, event.partialTicks);
                }
            }
            
        } catch (Exception e) {
            // Log error for debugging but don't spam console
            if (org.xXseesXx.pickthere.config.PickThereConfig.enableVisualEffects) {
                System.err.println("PickThere: Rendering error - " + e.getMessage());
            }
        }
    }
    
    private ItemStack findPickThereItemInHand(EntityPlayer player) {
        // Check only the currently held item (selected item in hotbar)
        ItemStack heldItem = player.getHeldItem();
        if (heldItem != null && heldItem.getItem() == Pickthere.pickThereItem) {
            return heldItem;
        }
        return null;
    }
    
    private void renderSelectedInventories(EntityPlayer player, ItemStack pickThereItem, float partialTicks) {
        try {
            // Use caching to avoid repeated NBT parsing
            List<InventoryManager.Position> regularPositions;
            List<InventoryManager.Position> sameItemOnlyPositions;
            
            if (lastRenderedItem != pickThereItem) {
                // Cache miss - update cache
                regularPositions = InventoryManager.getSelectedPositions(pickThereItem);
                sameItemOnlyPositions = InventoryManager.getSameItemOnlyPositions(pickThereItem);
                
                lastRenderedItem = pickThereItem;
                cachedRegularPositions = regularPositions;
                cachedSameItemOnlyPositions = sameItemOnlyPositions;
            } else {
                // Cache hit - use cached data
                regularPositions = cachedRegularPositions;
                sameItemOnlyPositions = cachedSameItemOnlyPositions;
            }
            
            // Calculate render position relative to player
            double playerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
            double playerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
            double playerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
            
            // Render regular inventory outlines (orange)
            for (InventoryManager.Position pos : regularPositions) {
                if (isWithinRenderDistance(pos, playerX, playerY, playerZ)) {
                    renderInventoryOutline(pos.x, pos.y, pos.z, playerX, playerY, playerZ, 
                                         1.0F, 0.6F, 0.0F, 0.8F); // Orange
                }
            }
            
            // Render same-item-only inventory outlines (red)
            for (InventoryManager.Position pos : sameItemOnlyPositions) {
                if (isWithinRenderDistance(pos, playerX, playerY, playerZ)) {
                    renderInventoryOutline(pos.x, pos.y, pos.z, playerX, playerY, playerZ, 
                                         1.0F, 0.0F, 0.0F, 0.8F); // Red
                }
            }
            
        } catch (Exception e) {
            // Silently handle rendering errors
        }
    }
    
    private boolean isWithinRenderDistance(InventoryManager.Position pos, double playerX, double playerY, double playerZ) {
        // If maxRenderDistance is -1, render at unlimited distance
        double maxDistance = org.xXseesXx.pickthere.config.PickThereConfig.maxRenderDistance;
        if (maxDistance < 0) {
            return true;
        }
        
        double dx = pos.x - playerX;
        double dy = pos.y - playerY;
        double dz = pos.z - playerZ;
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        return distanceSquared <= maxDistance * maxDistance;
    }
    
    private void renderInventoryOutline(double x, double y, double z, double playerX, double playerY, double playerZ, 
                                       float red, float green, float blue, float alpha) {
        // Calculate render position relative to player
        double renderX = x - playerX;
        double renderY = y - playerY;
        double renderZ = z - playerZ;
        
        try {
            GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);  // X-ray effect
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glLineWidth(3.0F);
            
            // Set color
            GL11.glColor4f(red, green, blue, alpha);
            
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawing(GL11.GL_LINES);
            
            // Draw the 12 edges of a cube
            // Bottom face edges
            tessellator.addVertex(renderX, renderY, renderZ);
            tessellator.addVertex(renderX + 1, renderY, renderZ);
            tessellator.addVertex(renderX + 1, renderY, renderZ);
            tessellator.addVertex(renderX + 1, renderY, renderZ + 1);
            tessellator.addVertex(renderX + 1, renderY, renderZ + 1);
            tessellator.addVertex(renderX, renderY, renderZ + 1);
            tessellator.addVertex(renderX, renderY, renderZ + 1);
            tessellator.addVertex(renderX, renderY, renderZ);
            
            // Top face edges
            tessellator.addVertex(renderX, renderY + 1, renderZ);
            tessellator.addVertex(renderX + 1, renderY + 1, renderZ);
            tessellator.addVertex(renderX + 1, renderY + 1, renderZ);
            tessellator.addVertex(renderX + 1, renderY + 1, renderZ + 1);
            tessellator.addVertex(renderX + 1, renderY + 1, renderZ + 1);
            tessellator.addVertex(renderX, renderY + 1, renderZ + 1);
            tessellator.addVertex(renderX, renderY + 1, renderZ + 1);
            tessellator.addVertex(renderX, renderY + 1, renderZ);
            
            // Vertical edges
            tessellator.addVertex(renderX, renderY, renderZ);
            tessellator.addVertex(renderX, renderY + 1, renderZ);
            tessellator.addVertex(renderX + 1, renderY, renderZ);
            tessellator.addVertex(renderX + 1, renderY + 1, renderZ);
            tessellator.addVertex(renderX + 1, renderY, renderZ + 1);
            tessellator.addVertex(renderX + 1, renderY + 1, renderZ + 1);
            tessellator.addVertex(renderX, renderY, renderZ + 1);
            tessellator.addVertex(renderX, renderY + 1, renderZ + 1);
            
            tessellator.draw();
            
            // Restore OpenGL state
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);  // Reset color to white
            GL11.glPopMatrix();
            
        } catch (Exception e) {
            // Silently handle rendering errors
        }
    }
}