package com.evandev.modest_meals.compat.parcool;

//? if parcool {
import com.alrex.parcool.api.stamina.AbstractLocalStamina;
import com.evandev.modest_meals.stamina.PlayerStamina;
import com.evandev.modest_meals.stamina.StaminaHelper;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class ModestMealsStamina extends AbstractLocalStamina {

    public ModestMealsStamina(Player owner, @Nullable AbstractLocalStamina previous) {
        super(owner);
    }

    @Override
    public double max() {
        return ParCoolCompat.usesStamina(this.owner) ? Math.max(1, StaminaHelper.get(this.owner).getMaxLevel()) : 1.0;
    }

    @Override
    public double value() {
        if (!ParCoolCompat.usesStamina(this.owner)) {
            return 1.0;
        }
        PlayerStamina stamina = StaminaHelper.get(this.owner);
        return stamina.getData().getStamina();
    }

    @Override
    public boolean isExhausted() {
        return false;
    }

    @Override
    public boolean imposePenalty() {
        return false;
    }

    @Override
    public boolean isInfinite() {
        return !ParCoolCompat.usesStamina(this.owner) || super.isInfinite();
    }

    @Override
    public void setValue(double value) {
    }

    @Override
    public void consume(double value) {
    }

    @Override
    public void recover(double value) {
    }
}
//?}
