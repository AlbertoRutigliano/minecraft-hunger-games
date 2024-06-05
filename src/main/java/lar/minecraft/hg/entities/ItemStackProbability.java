package lar.minecraft.hg.entities;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemStackProbability extends ItemStack {
	
	private double probability;
	private int minAmount;
	private int maxAmount;
	
	/**
	 * Constructs an ItemStackProbability with a given material, probability, and a range for the amount.
	 * The constructor will set the amount to a random value between minAmount and maxAmount (inclusive).
	 * If the probability check fails, the amount is set to 0.
	 *
	 * @param material The material of the item stack.
	 * @param probability The probability (0.0 to 1.0) that the item will be included in the chest.
	 * @param minAmount The minimum amount of the item stack.
	 * @param maxAmount The maximum amount of the item stack.
	 */
	public ItemStackProbability(Material material, double probability, int minAmount, int maxAmount) {
	    super(material, minAmount == maxAmount ? minAmount : new Random().nextInt((maxAmount - minAmount) + 1) + minAmount);

	    this.probability = probability;
	    this.minAmount = minAmount;
	    this.maxAmount = maxAmount;
	    
	    // If the probability check fails, set the amount to 0
	    if (probability != 1.0 && new Random().nextDouble() > probability) {
	        this.setAmount(0);
	    }
	}
	
	/**
	 * Constructs an ItemStackProbability with a given material and probability.
	 * This constructor sets the item stack amount to 1.
	 * If the probability check fails, the amount is set to 0.
	 *
	 * @param material The material of the item stack.
	 * @param probability The probability (0.0 to 1.0) that the item will be included in the chest.
	 */
	public ItemStackProbability(Material material, double probability) {
		this(material, probability, 1, 1);
	}
	
	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}

	public int getMinAmount() {
		return minAmount;
	}

	public void setMinAmount(int minAmount) {
		this.minAmount = minAmount;
	}

	public int getMaxAmount() {
		return maxAmount;
	}

	public void setMaxAmount(int maxAmount) {
		this.maxAmount = maxAmount;
	}
	
	
}
