# SimpleJSONConfig

## Main features:

    - Create JSON configuration files for your plugins
    - Easly serialize your objects to json files with custom Serializer
    - Comment your configuration with @Comment annotation (Comments are also included in file)
    - Automatically wire your config objects to static fields wby using @Autowired annotation
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

## How to use

### Register SimpleJsonConfig in your plugin

```java
public class Main extends JavaPlugin {
    
    @Override
    public void onEnable() {
        // Default directory is YourPlugin/configuration/
        SimpleJSONConfig.INSTANCE.register(this);
        // Or Specify default directory
        SimpleJSONConfig.INSTANCE.register(this, new File("default/config/directory"));
    }
}

```

### Just create a new class which will be your configuration.

### Class should be annotated as `@Configuration` and should extend `Config` class

```java

@Configuration( "config" )        //OR
@Configuration( "config.json" )   //OR
@Configuration( "path/to/config" )

@SuppressWarnings( "FieldMayBeFinal" ) // SimpleJsonConfig not supporting final modifiers yet

@Getter //We recommend using lombok to access fields, but you can make all the fields public as well
public class MyConfig extends Config {
    private String joinMessage = "Default join message";
    private List<ItemStack> startingEquipment = new ArrayList<>(
            Collections.singletonList(new ItemStack(Material.DIRT)));
    @Comment( "This is Comment which appear in the config file as well" )
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
import com.twodevsstudio.simplejsonconfig.api.Config;
import com.twodevsstudio.simplejsonconfig.interfaces.Autowired;

public class Main {
    @Autowired
    private static MyConfig config;
    
    // ... your logic
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
    <groupId>com.github.slighterr12</groupId>
    <artifactId>SimpleJSONConfig</artifactId>
    <version>63f3cbf</version>
    <scope>compiled</scope> <!-- Better if only one plugin uses SimpleJsonConfig, no cross plugin config sharing -->
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
    implementation 'com.github.slighterr12:SimpleJSONConfig:63f3cbf' //Better if only one plugin uses SimpleJsonConfig, no cross plugin config sharing
    compileOnly 'com.github.slighterr12:SimpleJSONConfig:63f3cbf'    //Add SimpleJsonConfig to your plugins folder, enables cross plugin config sharing
}
```

### To exclude field from serialization just add a `transient` modifier

```java

@Getter
@Configuration( "playerConfig" )
public class MyConfig extends Config {
    private String playerName = "Slighter";
    private transient Player cachedPlayer; //<- this field is not included in the config
    
    public Player getCachedPlayer() {
        
        if (cachedPlayer == null) {
            this.cachedPlayer = Bukkit.getOfflinePlayer(playerName);
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
    }
    
    public static MyClass load(File sourceFile) {
        
        return SERIALIZER.loadConfig(MyClass.class, sourceFile);
    }
}
```

#### You can easily save and reload your config

```java

@Getter
@Setter
@Configuration( "config" )
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
