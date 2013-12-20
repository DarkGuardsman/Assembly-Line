package dark.machines.items;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import universalelectricity.core.item.ItemElectric;

import com.dark.DarkCore;
import com.dark.IndustryTabs;
import com.dark.IExtraInfo.IExtraItemInfo;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dark.machines.CoreMachine;

/** Simple battery to store energy
 *
 * @author DarkGuardsman */
public class ItemBattery extends ItemElectric implements IExtraItemInfo
{
    public ItemBattery()
    {
        super(CoreMachine.CONFIGURATION.getItem("Battery", DarkCore.getNextItemId()).getInt());
        this.setUnlocalizedName(CoreMachine.getInstance().PREFIX + "Battery");
        this.setCreativeTab(IndustryTabs.tabIndustrial());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IconRegister iconRegister)
    {
        this.itemIcon = iconRegister.registerIcon(this.getUnlocalizedName().replace("item.", ""));
    }

    @Override
    public float getVoltage(ItemStack itemStack)
    {
        return 25;
    }

    @Override
    public float getMaxElectricityStored(ItemStack theItem)
    {
        return 5000;
    }

    @Override
    public boolean hasExtraConfigs()
    {
        return false;
    }

    @Override
    public void loadExtraConfigs(Configuration config)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void loadOreNames()
    {
        OreDictionary.registerOre("Battery", this);
    }
}