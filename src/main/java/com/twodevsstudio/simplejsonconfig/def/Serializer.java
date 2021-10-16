package com.twodevsstudio.simplejsonconfig.def;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.twodevsstudio.simplejsonconfig.api.CommentProcessor;
import com.twodevsstudio.simplejsonconfig.interfaces.PostProcessable;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;

@Getter
public class Serializer {
    private final CommentProcessor commentProcessor = new CommentProcessor();
    private final JsonParser jsonParser = new JsonParser();
    
    @Setter( onParam_ = @NotNull )
    private Gson gson;
    
    /**
     * You can use this class to serialize and deserialize your object to and from JSON format This class uses Google
     * json library to perform serialization and deserialization Fields with modifiers {final, static, transient} wont
     * be serialized In order to serialize your class you have to create default (empty) constructor
     * <p>
     * If some necessary operations have to be performed after deserialization (like assign transient fields) use the
     * {@code PostProcessable} interface, that'll provide you {@code gsonPostProcess()} method You can create your
     * business logic, and that method will be called after the deserialization process complete
     */
    private Serializer() {
        
        this.gson = new DefaultGsonBuilder().getGsonBuilder().create();
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
     * @param file   File where serialized object will be stored
     */
    @SneakyThrows
    public void saveConfig(Object object, @NotNull File file) {
        
        saveConfig(object, file, StoreType.JSON);
    }
    
    @SneakyThrows
    public void saveConfig(Object object, @NotNull File file, StoreType configType) {
        
        if (!file.createNewFile()) {
            Files.deleteIfExists(file.toPath());
            file.createNewFile();
        }
        
        try (PrintWriter out = new PrintWriter(file, "UTF-8")) {
            out.println(getFileContent(object, configType));
        }
        
        commentProcessor.includeComments(file, object);
        
    }
    
    public <T> T loadConfig(TypeToken<T> token, @NotNull File file) {
        
        return loadConfig(token, file, StoreType.JSON);
    }
    
    public <T> T loadConfig(TypeToken<T> token, @NotNull File file, StoreType configType) {
        
        file = commentProcessor.getFileWithoutComments(file);
        
        try {
            
            String json = readJsonString(file, configType);
            T deserializedObject = gson.fromJson(json, token.getType());
            
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
            Object loadedYaml = yaml.load(
                    new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)));
            json = gson.toJson(loadedYaml, LinkedHashMap.class);
        } else {
            json = new String(Files.readAllBytes(file.toPath()));
        }
        return json;
    }
    
    /**
     * Deserialize your object from JSON format It also call {@code gsonPostProcess()} method if the class implements
     * {@code PostProcessable} interface
     *
     * @param clazz The class of serialized object
     * @param file  It's the file where serialized object is stored
     * @param <T>   It's the return type of deserialized object
     *
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
    
    /**
     * Get the instance of {@code Serializer}
     *
     * @return The instance of {@code Serializer}
     */
    @Contract( pure = true )
    public static Serializer getInst() {
        
        return Serializer.SingletonHelper.INSTANCE;
    }
    
    private static class SingletonHelper {
        
        private static final Serializer INSTANCE = new Serializer();
        
    }
}
