package com.github.igotyou.FactoryMod.Factorys;

import org.bukkit.Location;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.interfaces.Recipe;
import com.github.igotyou.FactoryMod.properties.ProductionProperties;
import com.github.igotyou.FactoryMod.recipes.ProbabilisticEnchantment;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import com.github.igotyou.FactoryMod.utility.AreaEffect;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;
import static com.untamedears.citadel.Utility.getReinforcement;
import static com.untamedears.citadel.Utility.isReinforced;
import com.untamedears.citadel.entity.PlayerReinforcement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ProductionFactory extends BaseFactory
{

	private ProductionRecipe currentRecipe = null;//the recipe that is currently selected
	private ProductionProperties productionFactoryProperties;//the properties of the production factory
	public static final FactoryType FACTORY_TYPE = FactoryType.PRODUCTION;//the factory's type
	private List<ProductionRecipe> recipes;
	private int currentRecipeNumber = 0;//the array index of the current recipe
	
	/**
	 * Constructor
	 */
	public ProductionFactory (Location factoryLocation, Location factoryInventoryLocation, Location factoryPowerSource
			, String subFactoryType)
	{
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource, ProductionFactory.FACTORY_TYPE, subFactoryType);
		this.productionFactoryProperties = (ProductionProperties) factoryProperties;
		this.recipes=new ArrayList<ProductionRecipe> (productionFactoryProperties.getRecipes());
		this.setRecipeToNumber(0);
	}

	/**
	 * Constructor
	 */
	public ProductionFactory (Location factoryLocation, Location factoryInventoryLocation, Location factoryPowerSource,
			String subFactoryType, boolean active, int currentProductionTimer, int currentEnergyTimer,  List<ProductionRecipe> recipes,
			int currentRecipeNumber,double currentMaintenance,long timeDisrepair)
	{
		super(factoryLocation, factoryInventoryLocation, factoryPowerSource, ProductionFactory.FACTORY_TYPE, active, subFactoryType, currentProductionTimer, currentEnergyTimer, currentMaintenance, timeDisrepair);
		this.productionFactoryProperties = (ProductionProperties) factoryProperties;
		this.recipes=recipes;
		this.setRecipeToNumber(currentRecipeNumber);
	}
	
	@Override
	public boolean checkHasMaterials() {
		return currentRecipe.hasMaterials(getInventory());
	}
	
	@Override
	public boolean isRepairing() {
		return currentRecipe.getRepairs().size()!=0;
	}
	
	
	/**
	 * Returns either a success or error message.
	 * Called by the blockListener when a player left clicks the center block, with the InteractionMaterial
	 */
	@Override
	public List<InteractionResponse> getCentralBlockResponse()
	{
		List<InteractionResponse> responses=new ArrayList<InteractionResponse>();
		//Is the factory off
		if (!active)
		{
			//is the recipe is initiaed
			if (currentRecipe != null)
			{		
				//if we are at the end of the recipe array loop around
				if (currentRecipeNumber == recipes.size() - 1)
				{
					setRecipeToNumber(0);
					currentProductionTimer = 0;
				}
				//if we can just increment the recipe
				else
				{
					setRecipeToNumber(currentRecipeNumber + 1);
					currentProductionTimer = 0;
				}
			}
			//if the recipe for some reason is not initialised, initialise it to recipe 0
			else
			{
				setRecipeToNumber(0);
				currentProductionTimer = 0;
			}
			responses.add(new InteractionResponse(InteractionResult.SUCCESS, "-----------------------------------------------------"));
			responses.add(new InteractionResponse(InteractionResult.SUCCESS, "Switched recipe to: " + currentRecipe.getRecipeName()+"."));
			if(currentRecipeNumber != recipes.size() - 1)
			{
				responses.add(new InteractionResponse(InteractionResult.SUCCESS, "Next Recipe is: "+recipes.get(currentRecipeNumber+1).getRecipeName()+"."));
			}
			else
			{
				responses.add(new InteractionResponse(InteractionResult.SUCCESS, "Next Recipe is: "+recipes.get(0).getRecipeName()+"."));
			}
		}
		//if the factory is on, return error message
		else
		{
			responses.add(new InteractionResponse(InteractionResult.FAILURE, "You can't change recipes while the factory is on! Turn it off first."));
		}
		return responses;
	}
	
	@Override
	public ItemList<NamedItemStack> getFuel() {
		return productionFactoryProperties.getFuel();
	}

	/**
	 * Sets the factories current recipe.
	 * @param newRecipe the desired recipe
	 */
	public void setRecipe(Recipe newRecipe)
	{
		if (newRecipe instanceof ProductionRecipe)
		{
			currentRecipe = (ProductionRecipe) newRecipe;
		}
	}
	
	/**
	 * sets the recipe to the supplied index
	 * @param newRecipeNumber the desired recipeArray index
	 */
	public void setRecipeToNumber(int newRecipeNumber)
	{
		if (newRecipeNumber<recipes.size())
		{
			currentRecipe = recipes.get(newRecipeNumber);
			currentRecipeNumber = newRecipeNumber;
		}
		else
		{
			currentRecipe=recipes.get(0);
			currentRecipeNumber=0;
		}
	}
	/**
	 * Returns the currentRecipe
	 */
	public ProductionRecipe getCurrentRecipe()
	{
		return currentRecipe;
	}
	
	/**
	 * Returns the RecipeArray index
	 */
	public int getCurrentRecipeNumber()
	{
		return currentRecipeNumber;
	}
	
	/**
	 * Returns the factory's properties
	 */
	public ProductionProperties getProductionFactoryProperties()
	{
		return productionFactoryProperties;
	}
	
	public List<ProductionRecipe> getRecipes()
	{
		return recipes;
	}
	
	@Override
	public List<InteractionResponse> getChestResponse()
	{
		List<InteractionResponse> responses=new ArrayList<InteractionResponse>();
		String status=active ? "On" : "Off";
		String percentDone=status.equals("On") ? " - "+Math.round(currentProductionTimer*100/currentRecipe.getProductionTime())+"% done." : "";
		//Name: Status with XX% health.
		int health =(getProductionFactoryProperties().getRepair()==0) ? 100 : (int) Math.round(100*(1-currentRepair/(getProductionFactoryProperties().getRepair())));
		responses.add(new InteractionResponse(InteractionResult.SUCCESS, getProductionFactoryProperties().getName()+": "+status+" with "+String.valueOf(health)+"% health."));
		//RecipeName: X seconds(Y ticks)[ - XX% done.]
		responses.add(new InteractionResponse(InteractionResult.SUCCESS, currentRecipe.getRecipeName()+": "+currentRecipe.getProductionTime() + " seconds("+ currentRecipe.getProductionTime()*FactoryModPlugin.TICKS_PER_SECOND + " ticks)"+percentDone));
		//[Inputs: amount Name, amount Name.]
		if(!currentRecipe.getInputs().isEmpty())
		{
			responses.add(new InteractionResponse(InteractionResult.SUCCESS,"Input: "+currentRecipe.getInputs().toString()+"."));
		}
		//[Upgrades: amount Name, amount Name.]
		if(!currentRecipe.getUpgrades().isEmpty())
		{
			responses.add(new InteractionResponse(InteractionResult.SUCCESS,"Upgrades: "+currentRecipe.getUpgrades().toString()+"."));
		}
		//[Outputs: amount Name, amount Name.]
		if(!getOutputs().isEmpty())
		{
			responses.add(new InteractionResponse(InteractionResult.SUCCESS,"Output: "+getOutputs().toString()+"."));
		}
		//[Will repair XX% of the factory]
		if(!currentRecipe.getRepairs().isEmpty()&&getProductionFactoryProperties().getRepair()!=0)
		{
			int amountAvailable=currentRecipe.getRepairs().amountAvailable(getPowerSourceInventory());
			int amountRepaired=amountAvailable>currentRepair ? (int) Math.ceil(currentRepair) : amountAvailable;
			int percentRepaired=(int) (( (double) amountRepaired)/getProductionFactoryProperties().getRepair()*100);
			responses.add(new InteractionResponse(InteractionResult.SUCCESS,"Will repair "+String.valueOf(percentRepaired)+"% of the factory with "+currentRecipe.getRepairs().getMultiple(amountRepaired).toString()+"."));
		}
		//[Operates at XX% efficiency due to interference from: Location1, Location 2, Location 3, ...]
		if(currentRecipe.hasRecipeScaling())
		{
			String response;
			Set<ProductionFactory> scaledRecipeFactories=FactoryModPlugin.manager.getProductionManager().getScaledFactories(currentRecipe.getscaledRecipes());
			List<ProductionFactory> interferingFactories=new LinkedList<ProductionFactory>();
			for(ProductionFactory scaledRecipeFactory:scaledRecipeFactories)
			{
				if(scaledRecipeFactory.isWhole()&&scaledRecipeFactory!=this)
				{
					interferingFactories.add(scaledRecipeFactory);
				}
			}
			if(interferingFactories.size()==0)
			{
				response="Operates at 100% efficiency with no interference";
			}
			else
			{
				String factoryList="";
				for(ProductionFactory inteferingFactory:interferingFactories)
				{
					factoryList+=String.valueOf((int) this.getCenterLocation().distance(inteferingFactory.getCenterLocation()));
					factoryList+=inteferingFactory!=interferingFactories.get(interferingFactories.size()-1) ? ", " : "";
				}
				response="Operates at "+Math.round(100*currentRecipe.getRecipeScaling(this)) + "% efficiency: "+factoryList;
			}
			responses.add(new InteractionResponse(InteractionResult.SUCCESS,response));
		}
		return responses;
	}
	
	protected void recipeFinished() {
		//Remove upgrade and replace it with its upgraded form
		currentRecipe.getUpgrades().removeOneFrom(getInventory()).putIn(getInventory(),currentRecipe.getEnchantments());
	}

	@Override
	public ItemList<NamedItemStack> getInputs() {
		return currentRecipe.getInputs();
	}

	@Override
	public ItemList<NamedItemStack> getOutputs() {
		return currentRecipe.getOutputs(this);
	}

	@Override
	public ItemList<NamedItemStack> getRepairs() {
		return currentRecipe.getRepairs();
	}

	@Override
	public List<ProbabilisticEnchantment> getEnchantments() {
		return currentRecipe.getEnchantments();
	}

	@Override
	public double getEnergyTime() {
		return productionFactoryProperties.getEnergyTime();
	}

	@Override
	public double getProductionTime() {
		return currentRecipe.getProductionTime();
	}

	@Override
	public int getMaxRepair() {
		return productionFactoryProperties.getRepair();
	}
	
	@Override
	protected void updateAreaEffects() {
		for(AreaEffect areaEffect:productionFactoryProperties.getAreaEffects()) {
			//Replicates Mojang implementation of Beacons, unsure of the requirement of -2 and +2
			int xMin = factoryLocation.getBlockX()-2;
			int xMax = factoryLocation.getBlockX()+2;
			int zMin = factoryLocation.getBlockZ()-2;
			int zMax = factoryLocation.getBlockZ()+2;
			for(int x=xMin;x<=xMax;x+=16) {
				for(int z=zMin;z<=zMax;z+=16) {
					for(Entity entity:factoryLocation.getWorld().getChunkAt(x, z).getEntities()) {
						if(entity instanceof Player) {
							if(xMin<entity.getLocation().getBlockX()&&xMax>entity.getLocation().getBlockX()&&zMin<entity.getLocation().getBlockZ()&&zMax>entity.getLocation().getBlockZ())
								if((!FactoryModPlugin.CITADEL_ENABLED || FactoryModPlugin.CITADEL_ENABLED && !isReinforced(factoryLocation)) || 
										(((PlayerReinforcement) getReinforcement(factoryLocation)).isAccessible((Player)entity))){
									((Player)entity).addPotionEffect(areaEffect);
								}
						}
					}
				}
			}
		}
	}
}
