# SimpleJSONConfig

## Main features:

    - Create JSON/YAML configuration files for your plugins
    - Persist your data in flat files using Data Stores
    - Easily serialize your objects to json files with custom Serializer
    - Shared adapters for cross plugin functionality by using SharedGsonBuilder
    - Comment your configuration with @Comment annotation (Comments are also included in file)
    - Automatically wire your config objects to static fields by using @Autowired annotation
    - Share your configurations between plugins
    - Specify configuration directory
    - Support for most crucial types in plugin development:
        * ItemStack
        * Location
        * World
        * Block
        * And more...
    - Interfaces and superclasses support
    - Reload specific config or all at once
    - LRU cache for faster access to objects for Data Stores
    - Your config files will automatically generate new fields if they are added to your config class and remove fields that are no longer present

## How to use

### Register SimpleJsonConfig in your plugin

```java
public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // Default directory is YourPlugin/configuration/
        SimpleJSONConfig.INSTANCE.register(this);
        // You can change your config type to YAML as well!
        SimpleJSONConfig.INSTANCE.register(this, StoreType.YAML);
        // Or Specify default directory
        SimpleJSONConfig.INSTANCE.register(this, new File("default/config/directory"));
    }
}

```

### Just create a new class which will be your configuration.

### Class should be annotated as `@Configuration` and should extend `Config` class

```java

@Configuration("config")        //OR
@Configuration("config.json")   //OR
@Configuration("path/to/config")

@SuppressWarnings("FieldMayBeFinal") // SimpleJsonConfig not supporting final modifiers yet

@Getter //We recommend using lombok to access fields, but you can make all the fields public as well
public class MyConfig extends Config {
    private String joinMessage = "Default join message";
    private List<ItemStack> startingEquipment = new ArrayList<>(
            Collections.singletonList(new ItemStack(Material.DIRT)));
    @Comment("This is Comment which appear in the config file as well")
    private YourType something;
}
```

### You have 2 ways to access your configuration in code:

    - Call for Config.getConfig(ConfigClass.class)
    - Annotate static field of your config type with @Autowired

```java
public class Main {
    private MyConfig config = Config.getConfig(MyConfig.class);

    // ... your logic
}
```

```java
public class Main {
    @Autowired
    private static MyConfig config;

    // ... your logic
}
```

## Data Stores

### Usage is very similar to the usage of configurations

#### Create a class you want to persist

#### Class have to be annotated with `@Stored` annotation and implement `Identifiable` interface

```java
//Second parameter is optional, JSON is the default value
@Stored(value = "directory", storeType = StoreType.JSON)
//Implement Identifiable interface and specify the type of ID
public class MyClass implements Identifiable<UUID> {
    //Add id field of specified type
    private final UUID id;
    private int count;

    //Implement getId method or simply add @Getter annotation on the field
    public UUID getId() {
        return id;
    }
}
```

#### After this you can access the service of your class and then you are ready to go!

#### Accessing the Service is identical to the accessing configurations

```java
public class Main {

    private static Service<UUID, MyClass> service = Service.getService(MyClass.class);

    public void foo() {
        MyClass myClass = new MyClass(UUID.randomUUID());

        // Serialize your object to the file in specified directory
        service.save(myClass);
        // Read your object by ID
        MyClass byId = service.getById(myClass.getId());
        // Get all objects which are matching the condition
        List<MyClass> matching = service.getMatching(aClass -> aClass.getCount() > 10);
        // Delete your object
        service.delete(myClass);
    }
}
```

### Adding dependency

Maven:

```xml

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependecies>
<dependency>
    <groupId>com.twodevsstudio</groupId>
    <artifactId>SimpleJSONConfig</artifactId>
    <version>1.4.1</version>
    <scope>compile</scope> <!-- Better if only one plugin uses SimpleJsonConfig, no cross plugin config sharing -->
    <scope>provided</scope> <!-- Add SimpleJsonConfig to your plugins folder, enables cross plugin config sharing -->
</dependency>
</dependecies>
```

Gradle:

```text
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.2DevsStudio:SimpleJSONConfig:1.4.1' //Better if only one plugin uses SimpleJsonConfig, no cross plugin config sharing
    compileOnly 'com.github.2DevsStudio:SimpleJSONConfig:1.4.1'    //Add SimpleJsonConfig to your plugins folder, enables cross plugin config sharing
}
```

### To exclude field from serialization just add a `transient` modifier

```java

@Getter
@Configuration("playerConfig")
public class MyConfig extends Config {
    private String playerName = "Slighter";
    private transient Player cachedPlayer; //<- this field is not included in the config

    public Player getCachedPlayer() {
        if (cachedPlayer == null) {
            this.cachedPlayer = Bukkit.getPlayer(playerName);
        }
        return this.cachedPlayer;
    }
}

```

## Additional Features

#### You can also serialize objects which are not configuration using `Serializer`

```java
public class MyClass {

    private static final Serializer SERIALIZER = Serializer.getInst();

    private String aString = "Text";
    private int anInt = 10;

    public void save(File targetFile) {
        SERIALIZER.saveConfig(this, targetFile);
        SERIALIZER.saveConfig(this, targetFile, StoreType.YAML); //You can also specify type of serialization
    }

    public static MyClass load(File sourceFile) {
        return SERIALIZER.loadConfig(MyClass.class, sourceFile);
        //If you previously serialized as YAML you have to remember to specify type on loading
        return SERIALIZER.loadConfig(MyClass.class, sourceFile, StoreType.YAML);
    }
}
```

#### You can easily save and reload your config

```java

@Getter
@Setter
@Configuration("config")
public class MyConfig extends Config {
    private String joinMessage = "Default join message";
}
```

```java
public class MyClass {

    @Autowired
    public static MyConfig config;

    public void foo() {
        config.setJoinMessage("Better Join Message");
        config.save(); //Save config file
    }
}
```

```java
public class ReloadCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
        Config.getConfig(MyConfig.class).reload(); // Reload single config
        Config.reloadAll(); //Reload all configs
        return true;
    }
}
```

###You can enable LRU cache for your data store

- Available optional parameters:
    - `cacheLifespanSeconds` - time after which the entry will be removed from the cache, default value is -1 which
      means that the entry will never be removed
    - `cacheScanIntervalSeconds` - Interval between cache scans for removing expired entries, default value is 120
      seconds
    - `cacheMaxSize` - maximum size of the cache, cache is NOT limited by default

```java

@Stored(value = "directory", storeType = StoreType.JSON, cacheLifespanSeconds = 60, cacheScanIntervalSeconds = 10, cacheMaxSize = 1000)
public class MyClass implements Identifiable<UUID> {
    private final UUID id;
    private int count;

    public UUID getId() {
        return id;
    }
}
```

### You can customize the serializer

- Get the serializer instance

```java
Serializer serializer = Serializer.getInst();
```

- And get internal GsonBuilder instance

```java
SharedGsonBuilder gsonBuilder = serializer.toBuilder();
``` 

- You can add your own type adapters

```java
gsonBuilder.registerTypeAdapter(MyClass .class, new MyClassTypeAdapter());
```

- Remember to build it after you are done

```java
gsonBuilder.build();
```

#### SharedGsonBuilder will allow you to share the same GsonBuilder instance between all plugins using SimpleJsonConfig without worrying about overriding the type adapters

### Automatic config file update

- Your config files will be automatically updated when you change the config class by adding new fields or
  removing old ones

## Tips

### Working with more complex data types

If you want to use more complex data types the easiest way is to extract a common interface and use it in your
config class, ie:

```java
public class MyClass {
    protected int number;
}

public class EnhancedMyClass extends MyClass {
    protected String text;
}
```

should be changed to:

```java
public interface MyInterface { // This can be just a Marker Interface

}

public class MyClass implements MyInterface {
    protected int number;

}

public class EnhancedMyClass extends MyClass {
    protected String text;
}
```

And you should change your config from:

```java

@Getter
@Configuration("config")
public class MyConfig extends Config {
    private List<MyClass> myClasses = new ArrayList<>();
}
```

To:

```java

@Getter
@Configuration("config")
public class MyConfig extends Config {
    private List<MyInterface> myClasses = new ArrayList<>();
}
```

Last step is to register your interface using InterfaceAdapter in onEnable method in the main class:

```java
Serializer.getInst().
        .

toBuilder()
        .

registerTypeAdapter(MyInterface .class, new InterfaceAdapter())
        .

build();
```

This will allow you to use your interface in your config class and serialize/deserialize it properly by injecting
class information into the file.
