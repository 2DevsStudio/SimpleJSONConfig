package com.twodevsstudio.simplejsonconfig.def;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.twodevsstudio.simplejsonconfig.api.CommentProcessor;
import com.twodevsstudio.simplejsonconfig.def.adapters.*;
import com.twodevsstudio.simplejsonconfig.def.strategies.SuperclassExclusionStrategy;
import com.twodevsstudio.simplejsonconfig.interfaces.PostProcessable;
import java.io.*;
import java.lang.ref.Reference;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

@Getter
public class Serializer {
  private final CommentProcessor commentProcessor = new CommentProcessor();
  private final JsonParser jsonParser = new JsonParser();

  private final SharedGsonBuilder jsonBuilder;

  /** Please use {@link SharedGsonBuilder} to avoid Gson Override */
  @Setter(onParam_ = @NotNull, value = AccessLevel.PUBLIC)
  private Gson gson;

  /**
   * You can use this class to serialize and deserialize your object to and from JSON format This
   * class uses Google json library to perform serialization and deserialization Fields with
   * modifiers {final, static, transient} wont be serialized In order to serialize your class you
   * have to create default (empty) constructor
   *
   * <p>If some necessary operations have to be performed after deserialization (like assign
   * transient fields) use the {@code PostProcessable} interface, that'll provide you {@code
   * gsonPostProcess()} method You can create your business logic, and that method will be called
   * after the deserialization process complete
   */
  private Serializer() {

    this.jsonBuilder = new SharedGsonBuilder(this);
    jsonBuilder
        .registerTypeHierarchyAdapter(Class.class, new ClassAdapter())
        .registerTypeHierarchyAdapter(ChronoUnit.class, new ChronoUnitAdapter())
        .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackAdapter())
        .registerTypeHierarchyAdapter(World.class, new WorldAdapter())
        .registerTypeHierarchyAdapter(Reference.class, new ReferenceAdapter())
        .registerTypeAdapter(BlockState.class, new InterfaceAdapter())
        .addDeserializationExclusionStrategy(new SuperclassExclusionStrategy())
        .addSerializationExclusionStrategy(new SuperclassExclusionStrategy());

    try {
      Class.forName("net.kyori.adventure.text.Component");

      jsonBuilder.registerTypeHierarchyAdapter(Component.class, new AdventureComponentAdapter());
    } catch (Throwable ignored) {
    }

    this.jsonBuilder.build();
  }

  /**
   * Get the instance of {@code Serializer}
   *
   * @return The instance of {@code Serializer}
   */
  @Contract(pure = true)
  public static Serializer getInst() {

    return Serializer.SingletonHelper.INSTANCE;
  }

  public SharedGsonBuilder toBuilder() {

    return this.jsonBuilder;
  }

  @SneakyThrows
  public String getYamlString(String jsonString) {

    JsonNode jsonNodeTree = new ObjectMapper().readTree(jsonString);
    return new YAMLMapper().writeValueAsString(jsonNodeTree);
  }

  public String getFileContent(Object object, StoreType type) {

    String jsonString = gson.toJson(object);
    if (type == StoreType.YAML) {
      return getYamlString(jsonString);
    }
    return jsonString;
  }

  /**
   * Serialize parameterized object to JSON format and save it into the file
   *
   * @param object Object that is going to be serialized
   * @param file File where serialized object will be stored
   */
  @SneakyThrows
  public void saveConfig(Object object, @NotNull File file) {

    saveConfig(object, file, StoreType.JSON, StandardCharsets.UTF_8);
  }

  @SneakyThrows
  public void saveConfig(Object object, @NotNull File file, StoreType storeType, Charset encoding) {

    try {
      if (!file.createNewFile()) {
        Files.deleteIfExists(file.toPath());
        file.createNewFile();
      }
      try (PrintWriter out = new PrintWriter(file, encoding)) {
        out.println(getFileContent(object, storeType));
      }
      commentProcessor.includeComments(file, object);
    } catch (MalformedInputException exception) {
      saveConfig(
          object,
          file,
          storeType,
          encoding == StandardCharsets.US_ASCII
              ? StandardCharsets.ISO_8859_1
              : StandardCharsets.US_ASCII);
    }
  }

  public <T> T loadConfig(TypeToken<T> token, @NotNull File file) {

    return loadConfig(token, file, StoreType.JSON);
  }

  public <T> T loadConfig(TypeToken<T> token, @NotNull File file, StoreType configType) {

    file = commentProcessor.getFileWithoutComments(file);

    try {

      String json = readJsonString(file, configType);
      T deserializedObject = gson.fromJson(json, token.getType());

      PostProcessable.deepPostProcess(deserializedObject);
      if (deserializedObject instanceof PostProcessable) {
        ((PostProcessable) deserializedObject).gsonPostProcess();
      }

      return deserializedObject;
    } catch (IOException e) {

      e.printStackTrace();
      return null;
    }
  }

  private String readJsonString(File file, StoreType configType) throws IOException {

    String json;
    if (configType == StoreType.YAML) {
      Yaml yaml = new Yaml();
      Object loadedYaml =
          yaml.load(
              new BufferedReader(
                  new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)));
      json = gson.toJson(loadedYaml, LinkedHashMap.class);
    } else {
      json = new String(Files.readAllBytes(file.toPath()));
    }
    return json;
  }

  /**
   * Deserialize your object from JSON format It also call {@code gsonPostProcess()} method if the
   * class implements {@code PostProcessable} interface
   *
   * @param clazz The class of serialized object
   * @param file It's the file where serialized object is stored
   * @param <T> It's the return type of deserialized object
   * @return Deserialized object of parameterized type or null when any exception occurs
   */
  @Nullable
  public <T> T loadConfig(Class<T> clazz, @NotNull File file) {

    return loadConfig(TypeToken.get(clazz), file, StoreType.JSON);
  }

  @Nullable
  public <T> T loadConfig(Class<T> clazz, @NotNull File file, StoreType type) {

    return loadConfig(TypeToken.get(clazz), file, type);
  }

  private static class SingletonHelper {

    private static final Serializer INSTANCE = new Serializer();
  }
}
