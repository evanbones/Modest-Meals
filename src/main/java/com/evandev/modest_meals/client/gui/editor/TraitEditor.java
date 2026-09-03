package com.evandev.modest_meals.client.gui.editor;

import com.evandev.modest_meals.trait.FoodTrait;

public interface TraitEditor<T extends FoodTrait> {
    void initFrom(T trait);

    void buildForm(FormBuilder form);

    T createTrait();
}
