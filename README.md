# SimpleJSONConfig


Maven:

```xml
   <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
   </repository>
    
   <dependency>
	<groupId>com.github.slighterr12</groupId>
	<artifactId>SimpleJSONConfig</artifactId>
	<version>a5c8083</version>
   </dependency>
```

Gradle:


```xml
	repositories {
		maven { url 'https://jitpack.io' }
	}
    
	dependencies {
	        implementation 'com.github.slighterr12:SimpleJSONConfig:32e62403f6'
	}
```


Usage:


#Config class:
```java

@Getter # If you have Lombok, there you can just use annotation called @Getter <lombok.Getter> instead of handmade Getters, much faster
@Configuration( "general-config.json" )  # there you specify name of file, you don not have to remember about extension
public class GeneralConfig extends Config { # remember to extend Config!
    
    private String test = "There is default value for test";
    
    # you can store how much fields do you want, 
    # objects, maps, lists, itemstacks, worlds, whatever you want, it will be serialized and easily deserialized.
}
```

#Main Class of your Plugin
```java

    @Override
    public void onEnable() {

        SimpleJSONConfig.INSTANCE.register(this);

    }
```

#How to get instance of your config
```java

Config.getConfig(<ClassName>.class);

# or by using annotation @Autowired

@Autowired
private static ConfigClass config; # remember about static

```
