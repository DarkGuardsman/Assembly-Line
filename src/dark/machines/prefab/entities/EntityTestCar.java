package dark.machines.prefab.entities;

import net.minecraft.world.World;

import com.builtbroken.assemblyline.ALRecipeLoader;

public class EntityTestCar extends EntityVehicle
{

    public EntityTestCar(World world)
    {
        super(world);
    }

    public EntityTestCar(World world, float xx, float yy, float zz)
    {
        super(world, xx, yy, zz);
    }

    @Override
    public void updateClients()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void dropAsItem()
    {
        this.dropItemWithOffset(ALRecipeLoader.itemVehicleTest.itemID, 1, 0.0F);
    }

}
