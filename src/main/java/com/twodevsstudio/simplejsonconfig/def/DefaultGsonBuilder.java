package com.twodevsstudio.simplejsonconfig.def;

import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.twodevsstudio.simplejsonconfig.def.adapters.ChronoUnitAdapter;
import com.twodevsstudio.simplejsonconfig.def.adapters.ClassAdapter;
import com.twodevsstudio.simplejsonconfig.def.adapters.InterfaceAdapter;
import com.twodevsstudio.simplejsonconfig.def.adapters.ItemStackAdapter;
import com.twodevsstudio.simplejsonconfig.def.adapters.RecordTypeAdapterFactory;
import com.twodevsstudio.simplejsonconfig.def.adapters.ReferenceAdapter;
import com.twodevsstudio.simplejsonconfig.def.adapters.WorldAdapter;
import com.twodevsstudio.simplejsonconfig.def.strategies.SuperclassExclusionStrategy;
import java.lang.ref.Reference;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.time.temporal.ChronoUnit;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Getter(onMethod_ = @NotNull)
@Setter(onParam_ = @NotNull)
public class DefaultGsonBuilder {

  private GsonBuilder gsonBuilder;

  public DefaultGsonBuilder() {

    this.gsonBuilder = new GsonBuilder().setPrettyPrinting()
        .disableHtmlEscaping()
        .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
        .serializeNulls()
        .registerTypeHierarchyAdapter(Class.class, new ClassAdapter())
        .registerTypeHierarchyAdapter(ChronoUnit.class, new ChronoUnitAdapter())
        .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
        .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackAdapter())
        .registerTypeHierarchyAdapter(World.class, new WorldAdapter())
        .registerTypeHierarchyAdapter(Reference.class, new ReferenceAdapter())
        .registerTypeAdapter(BlockState.class, new InterfaceAdapter())
        .addDeserializationExclusionStrategy(new SuperclassExclusionStrategy())
        .addSerializationExclusionStrategy(new SuperclassExclusionStrategy());
  }


  public DefaultGsonBuilder registerTypeHierarchyAdapter(Class<?> baseType, Object typeAdapter) {

    gsonBuilder.registerTypeHierarchyAdapter(baseType, typeAdapter);
    return this;
  }

  public DefaultGsonBuilder registerTypeAdapter(Type type, Object typeAdapter) {

    gsonBuilder.registerTypeAdapter(type, typeAdapter);
    return this;
  }

  public DefaultGsonBuilder addDeserializationExclusionStrategy(ExclusionStrategy strategy) {

    gsonBuilder.addDeserializationExclusionStrategy(strategy);
    return this;
  }

  public DefaultGsonBuilder addSerializationExclusionStrategy(ExclusionStrategy strategy) {

    gsonBuilder.addSerializationExclusionStrategy(strategy);
    return this;
  }

  public Gson build() {

    return gsonBuilder.create();
  }
}
