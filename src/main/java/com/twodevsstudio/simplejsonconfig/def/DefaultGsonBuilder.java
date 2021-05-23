package com.twodevsstudio.simplejsonconfig.def;

import com.google.gson.GsonBuilder;
import com.twodevsstudio.simplejsonconfig.def.adapters.InterfaceAdapter;
import com.twodevsstudio.simplejsonconfig.def.adapters.ItemStackAdapter;
import com.twodevsstudio.simplejsonconfig.def.adapters.ReferenceAdapter;
import com.twodevsstudio.simplejsonconfig.def.adapters.WorldAdapter;
import com.twodevsstudio.simplejsonconfig.def.strategies.SuperclassExclusionStrategy;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.reflect.Modifier;

@Getter( onMethod_ = @NotNull )
@Setter( onParam_ = @NotNull )
public class DefaultGsonBuilder {
    
    private GsonBuilder gsonBuilder;
    
    public DefaultGsonBuilder() {
        
        this.gsonBuilder = new GsonBuilder().setPrettyPrinting()
                .disableHtmlEscaping()
                .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
                .serializeNulls()
                .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackAdapter())
                .registerTypeAdapter(Reference.class, new ReferenceAdapter())
                .registerTypeAdapter(World.class, new WorldAdapter())
                .registerTypeAdapter(BlockState.class, new InterfaceAdapter())
                .addDeserializationExclusionStrategy(new SuperclassExclusionStrategy())
                .addSerializationExclusionStrategy(new SuperclassExclusionStrategy());
    }
    
}
