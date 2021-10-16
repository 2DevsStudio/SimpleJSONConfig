package com.twodevsstudio.simplejsonconfig.data.repository;

import com.google.gson.reflect.TypeToken;
import com.twodevsstudio.simplejsonconfig.data.Identifiable;
import com.twodevsstudio.simplejsonconfig.def.StoreType;
import com.twodevsstudio.simplejsonconfig.def.Serializer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class FileRepository<ID, T extends Identifiable<ID>> implements Repository<ID, T> {
    
    public static final Serializer SERIALIZER = Serializer.getInst();
    private final TypeToken<T> typeToken;
    private final Path dataDirectory;
    private final StoreType storeType;
    
    @Override
    @SneakyThrows
    public void save(@NotNull T object) {
        
        Path file = findFileById(object.getId());
        if (!Files.exists(file)) {
            Files.createFile(file);
        }
        
        SERIALIZER.saveConfig(object, file.toFile(), storeType);
    }
    
    @Override
    @Nullable
    public T findById(@NotNull ID id) {
        
        Path file = findFileById(id);
        if (!Files.exists(file)) {
            return null;
        }
        
        return SERIALIZER.loadConfig(typeToken, file.toFile());
    }
    
    @Override
    @SneakyThrows
    @NotNull
    public List<T> findAll() {
        
        try (Stream<Path> walk = Files.walk(dataDirectory)) {
            return walk.filter(path -> !Files.isDirectory(path))
                    .filter(path -> path.endsWith(storeType.getExtension()))
                    .map(path -> SERIALIZER.loadConfig(typeToken, path.toFile()))
                    .collect(Collectors.toList());
        }
    }
    
    @Override
    @SneakyThrows
    public void deleteById(@NotNull ID id) {
        
        Files.deleteIfExists(findFileById(id));
    }
    
    @Override
    public void delete(@NotNull T object) {
        
        deleteById(object.getId());
    }
    
    @SneakyThrows
    @NotNull
    public Path findFileById(@NotNull ID id) {
        
        if (!Files.exists(dataDirectory)) {
            Files.createDirectories(dataDirectory);
        }
        
        String fileName = id + storeType.getExtension();
        return Paths.get(dataDirectory.toString(), fileName);
    }
}
