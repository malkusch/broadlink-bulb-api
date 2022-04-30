# Broadlink Bulb Java API

This is a Java API for the Broadlight Smart bulbs (LB1). This is an addition to [`broadlink-java-api`](https://github.com/mob41/broadlink-java-api)
which doesn't support this device. Also [`python-broadlink`](https://github.com/mjg59/python-broadlink/) was a great resource to implement this API.

## Maven

For now use [JitPack](https://jitpack.io/#malkusch/broadlink-bulb-api).

## Usage

Use `BroadlinkBulbFactory` to build `BroadlinkBulb` objects.

### Examples

#### Create a single known device

```java
var factory = new BroadlinkBulbFactory();
var light = factory.build("192.168.188.105");
light.turnOn();
```

#### Automatic Discovery of devices

```java
var factory = new BroadlinkBulbFactory();
var lights = factory.discover();
for (var light : lights) {
    light.turnOn();
}
```
