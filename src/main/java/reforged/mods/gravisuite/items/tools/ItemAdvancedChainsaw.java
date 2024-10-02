package reforged.mods.gravisuite.items.tools;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ic2.core.IC2;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.item.ElectricItem;
import ic2.core.util.StackUtil;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import org.jetbrains.annotations.Nullable;
import reforged.mods.gravisuite.GraviSuiteMainConfig;
import reforged.mods.gravisuite.items.tools.base.ItemBaseElectricItem;
import reforged.mods.gravisuite.utils.Helpers;
import reforged.mods.gravisuite.utils.Refs;
import reforged.mods.gravisuite.utils.pos.BlockPos;

import java.util.*;

public class ItemAdvancedChainsaw extends ItemBaseElectricItem {

    public int energyPerOperation = 100;
    public Set<Block> mineableBlocks = new HashSet<Block>();
    public static boolean wasEquipped = false;
    public static AudioSource audioSource;

    public static final String NBT_SHEARS = "shears", NBT_TCAPITATOR = "capitator";

    public ItemAdvancedChainsaw() {
        super(GraviSuiteMainConfig.ADVANCED_CHAINSAW_ID, "advanced_chainsaw", 2, 500, 15000, EnumToolMaterial.EMERALD);
        this.efficiencyOnProperMaterial = 35.0F;
        MinecraftForge.setToolClass(this, "axe", 4);
        MinecraftForge.EVENT_BUS.register(this);
        this.setIconIndex(Refs.TOOLS_ID + 1);
        init();
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean par4) {
        super.addInformation(stack, player, tooltip, par4);
        boolean isShearsOn = readToolMode(stack, NBT_SHEARS);
        boolean isCapitatorOn = readToolMode(stack, NBT_TCAPITATOR);
        String modeShear = Helpers.getStatusMessage(isShearsOn);
        String modeCapitator = Helpers.getStatusMessage(isCapitatorOn);
        tooltip.add(Refs.tool_mode_shear_gold + " " + modeShear);
        if (GraviSuiteMainConfig.CHAINSAW_TREE_CAPITATOR) {
            tooltip.add(Refs.tool_mode_capitator_gold + " " + modeCapitator);
        }
        if (Helpers.isShiftKeyDown()) {
            tooltip.add(Helpers.pressXAndYForZ(Refs.to_enable_2, "Mode Switch Key", "Right Click", Refs.SHEAR_MODE + ".stat"));
            if (GraviSuiteMainConfig.CHAINSAW_TREE_CAPITATOR) {
                tooltip.add(Helpers.pressXAndYForZ(Refs.to_enable_2, Refs.SNEAK_KEY, "Right Click", Refs.CAPITATOR_MODE + ".stat"));
            }
        } else {
            tooltip.add(Helpers.pressForInfo(Refs.SNEAK_KEY));
        }
    }

    public boolean canHarvestBlock(Block block, ItemStack stack) {
        return ((Item.axeDiamond.canHarvestBlock(block))
                || (Item.axeDiamond.getStrVsBlock(stack, block) > 1.0F)
                || (Item.swordDiamond.canHarvestBlock(block))
                || (Item.swordDiamond.getStrVsBlock(stack, block) > 1.0F)) || (this.mineableBlocks.contains(block));
    }

    @Override
    public float getStrVsBlock(ItemStack stack, Block block, int meta) {
        if (!ElectricItem.canUse(stack, this.energyPerOperation))
            return 0.5F;
        if (canHarvestBlock(block, stack))
            return this.efficiencyOnProperMaterial;
        return 1.0F;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, int x, int y, int z, EntityPlayer player) {
        if (IC2.platform.isRendering()) {
            return false;
        }
        World world = player.worldObj;
        Block block = Block.blocksList[world.getBlockId(x, y, z)];
        if (block instanceof IShearable && readToolMode(stack, NBT_SHEARS)) {
            IShearable target = (IShearable) block;
            if ((target.isShearable(stack, player.worldObj, x, y, z))
                    && (ElectricItem.use(stack, this.energyPerOperation, player))) {
                ArrayList<ItemStack> drops = target.onSheared(stack, player.worldObj, x, y, z,
                        EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, stack));
                for (ItemStack drop : drops) {
                    float f = 0.7F;
                    double d = itemRand.nextFloat() * f + (1.0F - f) * 0.5D;
                    double d1 = itemRand.nextFloat() * f + (1.0F - f) * 0.5D;
                    double d2 = itemRand.nextFloat() * f + (1.0F - f) * 0.5D;
                    EntityItem entityitem = new EntityItem(player.worldObj, x + d, y + d1, z + d2, drop);
                    entityitem.delayBeforeCanPickup = 10;
                    player.worldObj.spawnEntityInWorld(entityitem);
                }
                player.addStat(net.minecraft.stats.StatList.mineBlockStatArray[world.getBlockId(x, y, z)], 1);
            }
        }
        if (GraviSuiteMainConfig.CHAINSAW_TREE_CAPITATOR && readToolMode(stack, NBT_TCAPITATOR)) {
            ItemStack blockStack = new ItemStack(block, 1, 32767);
            boolean isLog = false;
            List<ItemStack> logs = Helpers.getStackFromOre("log");
            logs.addAll(Helpers.getStackFromOre("wood")); // just in case some mod uses old oredict name
            for (ItemStack check : logs) {
                if (Helpers.areStacksEqual(check, blockStack)) {
                    isLog = true;
                    break;
                }
            }

            if (isLog) {
                BlockPos origin = new BlockPos(x, y, z);
                LinkedList<BlockPos> connectedLogs = scanForTree(world, origin, player.isSneaking() ? 0 : 256);
                for (BlockPos coord : connectedLogs) {
                    if (coord.equals(origin)) {
                        continue;
                    }
                    if (!ElectricItem.canUse(stack, this.energyPerOperation)) {
                        break;
                    }
                    if (ElectricItem.canUse(stack, this.energyPerOperation)) {
                        if (canHarvestBlock(block, stack) && harvestBlock(world, coord.getX(), coord.getY(), coord.getZ(), player) && !player.capabilities.isCreativeMode) {
                            ElectricItem.use(stack, this.energyPerOperation, player);
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (IC2.platform.isSimulating()) {
            if (IC2.keyboard.isModeSwitchKeyDown(player)) {
                boolean shears = false;
                if (!readToolMode(stack, NBT_SHEARS)) {
                    saveToolMode(stack, NBT_SHEARS, true);
                    shears = true;
                } else {
                    saveToolMode(stack, NBT_SHEARS, false);
                }
                IC2.platform.messagePlayer(player, Refs.tool_mode_shear + " " + Helpers.getStatusMessage(shears));
            }
            if (GraviSuiteMainConfig.CHAINSAW_TREE_CAPITATOR) {
                if (IC2.keyboard.isSneakKeyDown(player)) {
                    boolean capitator = false;
                    if (!readToolMode(stack, NBT_TCAPITATOR)) {
                        saveToolMode(stack, NBT_TCAPITATOR, true);
                        capitator = true;
                    } else {
                        saveToolMode(stack, NBT_TCAPITATOR, false);
                    }
                    IC2.platform.messagePlayer(player, Refs.tool_mode_capitator + " " + Helpers.getStatusMessage(capitator));
                }
            }
        }
        return stack;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack par1ItemStack, World par2World, int block, int xPos, int yPos, int zPos, EntityLiving entity) {
        if (Block.blocksList[block].getBlockHardness(par2World, xPos, yPos, zPos) != 0.0D) {
            if (entity != null) {
                ElectricItem.use(par1ItemStack, this.energyPerOperation, null);
            } else {
                ElectricItem.discharge(par1ItemStack, this.energyPerOperation, this.TIER, true, false);
            }
        }
        return true;
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLiving entity, EntityLiving attacker) {
        if (ElectricItem.use(stack, this.energyPerOperation, null)) {
            entity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) attacker), 13);
        } else {
            entity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) attacker), 1);
        }
        if (attacker != null && entity instanceof EntityCreeper && entity.getHealth() <= 0.0F) {
            IC2.achievements.issueAchievement((EntityPlayer)attacker, "killCreeperChainsaw");
        }
        return false;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int i, boolean flag) {
        boolean isEquipped = flag && entity instanceof EntityLiving;
        if (IC2.platform.isRendering()) {
            if (isEquipped && !wasEquipped) {
                if (audioSource == null) {
                    audioSource = IC2.audioManager.createSource(entity, PositionSpec.Hand, "Tools/Chainsaw/ChainsawIdle.ogg", true, false, IC2.audioManager.defaultVolume);
                }
                if (audioSource != null) {
                    audioSource.play();
                }
            } else if (!isEquipped && audioSource != null) {
                audioSource.stop();
                audioSource.remove();
                audioSource = null;
                if (entity instanceof EntityLiving) {
                    IC2.audioManager.playOnce(entity, PositionSpec.Hand, "Tools/Chainsaw/ChainsawStop.ogg", true, IC2.audioManager.defaultVolume);
                }
            } else if (audioSource != null) {
                audioSource.updatePosition();
            }
            wasEquipped = isEquipped;
        }
    }

    @ForgeSubscribe
    public void onEntityInteract(EntityInteractEvent event) {
        if (IC2.platform.isSimulating()) {
            Entity entity = event.target;
            EntityPlayer player = event.entityPlayer;
            ItemStack stack = player.inventory.getStackInSlot(player.inventory.currentItem);
            if ((stack != null) && (stack.getItem() == this) && ((entity instanceof IShearable))
                    && (readToolMode(stack, NBT_SHEARS))
                    && (ElectricItem.use(stack, this.energyPerOperation, player))) {
                IShearable target = (IShearable) entity;
                if (target.isShearable(stack, entity.worldObj, (int) entity.posX, (int) entity.posY, (int) entity.posZ)) {
                    ArrayList<ItemStack> drops = target.onSheared(stack, entity.worldObj, (int) entity.posX, (int) entity.posY, (int) entity.posZ,
                            EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, stack));
                    for (ItemStack drop : drops) {
                        EntityItem ent = entity.entityDropItem(drop, 1.0F);
                        ent.motionY += itemRand.nextFloat() * 0.05F;
                        ent.motionX += (itemRand.nextFloat() - itemRand.nextFloat()) * 0.1F;
                        ent.motionZ += (itemRand.nextFloat() - itemRand.nextFloat()) * 0.1F;
                    }
                }
            }
        }
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
        if (audioSource != null) {
            audioSource.stop();
            audioSource.remove();
            audioSource = null;
        }
        return true;
    }

    public static boolean readToolMode(ItemStack stack, String mode) {
        NBTTagCompound tag = StackUtil.getOrCreateNbtData(stack);
        return tag.getBoolean(mode);
    }

    public static void saveToolMode(ItemStack stack, String mode, boolean value) {
        NBTTagCompound tag = StackUtil.getOrCreateNbtData(stack);
        tag.setBoolean(mode, value);
    }

    public void init() {
        this.mineableBlocks.add(Block.planks);
        this.mineableBlocks.add(Block.bookShelf);
        this.mineableBlocks.add(Block.woodSingleSlab);
        this.mineableBlocks.add(Block.woodDoubleSlab);
        this.mineableBlocks.add(Block.chest);
        this.mineableBlocks.add(Block.lockedChest);
        this.mineableBlocks.add(Block.leaves);
        this.mineableBlocks.add(Block.web);
        this.mineableBlocks.add(Block.cloth);
        this.mineableBlocks.add(Block.pumpkin);
        this.mineableBlocks.add(Block.melon);
        this.mineableBlocks.add(Block.cactus);
        this.mineableBlocks.add(Block.snow);
    }

    private interface BlockAction {
        boolean onBlock(BlockPos pos, Block block, boolean isRightBlock);
    }

    public LinkedList<BlockPos> scanForTree(final World world, final BlockPos startPos, int limit) {
        Block block = Block.blocksList[world.getBlockId(startPos.getX(), startPos.getY(), startPos.getZ())];
        ItemStack blockStack = new ItemStack(block, 1, 32767);
        boolean isLog = false;
        List<ItemStack> logs = Helpers.getStackFromOre("log");
        logs.addAll(Helpers.getStackFromOre("wood")); // just in case some mod uses old oredict name
        for (ItemStack check : logs) {
            if (Helpers.areStacksEqual(check, blockStack)) {
                isLog = true;
                break;
            }
        }
        if (!isLog) {
            return new LinkedList<BlockPos>();
        }
        final boolean[] leavesFound = new boolean[1];
        LinkedList<BlockPos> result = recursiveSearch(world, startPos, new BlockAction() {
            @Override
            public boolean onBlock(BlockPos pos, Block block, boolean isRightBlock) {
                int metadata = Helpers.getBlockMetadata(world, pos) | 8;
                boolean isLeave = metadata >= 8 && metadata <= 11;
                if (block.isLeaves(world, startPos.getX(), startPos.getY(), startPos.getZ()) && isLeave || getBOPStatus(world, pos)) leavesFound[0] = true;
                return true;
            }
        }, limit);
        return leavesFound[0] ? result : new LinkedList<BlockPos>();
    }

    // TODO: might need some adjustments
    private boolean getBOPStatus(World world, BlockPos pos) {
        int meta = Helpers.getBlockMetadata(world, pos) | 8;
        Block block = Helpers.getBlock(world, pos);
        if (Loader.isModLoaded("BiomesOPlenty")) {
            if (Helpers.instanceOf(block, "biomesoplenty.blocks.BlockBOPPetals") ||
                    Helpers.instanceOf(block, "biomesoplenty.blocks.BlockBOPLeaves") ||
                    Helpers.instanceOf(block, "biomesoplenty.blocks.BlockBOPColorizedLeaves") ||
                    Helpers.instanceOf(block, "biomesoplenty.blocks.BlockBOPAppleLeaves")) {
                return meta >= 8 && meta <= 15;
            }
        }
        return false;
    }

    // Recursively scan 3x3x3 cubes while keeping track of already scanned blocks to avoid cycles.
    private static LinkedList<BlockPos> recursiveSearch(final World world, final BlockPos start, @Nullable final BlockAction action, int limit) {
        Block wantedBlock = Helpers.getBlock(world, start);
        boolean abort = false;
        final LinkedList<BlockPos> result = new LinkedList<BlockPos>();
        final Set<BlockPos> visited = new HashSet<BlockPos>();
        final LinkedList<BlockPos> queue = new LinkedList<BlockPos>();
        queue.push(start);

        while (!queue.isEmpty()) {
            final BlockPos center = queue.pop();
            final int x0 = center.getX();
            final int y0 = center.getY();
            final int z0 = center.getZ();
            for (int z = z0 - 1; z <= z0 + 1 && !abort; ++z) {
                for (int y = y0 - 1; y <= y0 + 1 && !abort; ++y) {
                    for (int x = x0 - 1; x <= x0 + 1 && !abort; ++x) {
                        final BlockPos pos = new BlockPos(x, y, z);
                        Block checkBlock = Helpers.getBlock(world, pos);
                        if ((Helpers.isAir(world, pos) || !visited.add(pos))) {
                            continue;
                        }
                        final boolean isRightBlock = checkBlock.blockID == wantedBlock.blockID;
                        if (isRightBlock) {
                            result.add(pos);
                            if (queue.size() > limit) {
                                abort = true;
                                break;
                            }
                            queue.push(pos);
                        }
                        if (action != null) {
                            abort = !action.onBlock(pos, checkBlock, isRightBlock);
                        }
                    }
                }
            }
        }
        return !abort ? result : new LinkedList<BlockPos>();
    }
}
