# UUIDProvider
A Bukkit/Sponge plugin that provides players UUID support for other plugins.  
This plugin has been tested on Cauldron 1.6.4/1.7.10, Spigot 1.8, 1.10, 1.11, and Sponge 1.10.2

All my plugin's builds can be downloaded from http://kaikk.net/mc/#bukkit

##Install
A MySQL database is required. Also [Kai's Commons](https://github.com/KaiKikuchi/KaisCommons/releases) is required.  
Edit plugins/UUIDProvider/config.yml and set your database account.  
Sponge version also requires [JSON-simple](https://mvnrepository.com/artifact/com.googlecode.json-simple/json-simple/1.1.1).  
If you're running multiple servers, be sure to use the same MySQL database to improve performances!

##Developers: how to use it

Add this plugin in your project's build path. Maven:
```xml
<repository>
  <id>net.kaikk.mc</id>
  <url>http://kaikk.net/mc/repo/</url>
</repository>
```
```xml
<dependency>
  <groupId>net.kaikk.mc</groupId>
  <artifactId>UUIDProvider</artifactId>
  <version>2.5</version>
  <type>jar</type>
  <scope>provided</scope>
</dependency>
```

Most important methods:
- (UUID) UUIDProvider.get(OfflinePlayer)
- (OfflinePlayer) UUIDProvider.get(UUID)

Please report any issue! Suggestions are well accepted!

##Support my life!
If you like this plugin and you run it fine on your server, please <a href='http://kaikk.net/mc/#donate'>consider a donation</a>! It means a lot to me! :)