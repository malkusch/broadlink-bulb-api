# Broadlink LB2 Java API

This is a Java API for the Broadlight LB2 Smart bulbs. This is an addition to [`broadlink-java-api`](https://github.com/mob41/broadlink-java-api)
which doesn't support this device.

## Maven

For now use [JitPack](https://jitpack.io/#malkusch/broadlink-lb2-api).

## Usage

Use `LB2LightFactory` to build `LB2Light` objects.

### Examples

#### Create a single known device

```java
var factory = new LB2LightFactory();
var light = factory.build("192.168.188.105");
light.turnOn();
```

#### Automatic Discovery of devices

```java
var factory = new LB2LightFactory();
var lights = factory.discover();
for (var light : lights) {
    light.turnOn();
}
```
