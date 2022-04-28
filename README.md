## Maven

For now use [JitPack](https://jitpack.io/#malkusch/broadlink-lb2-api).

## Usage

### Examples

#### Create a single known device

```java
var factory = new LB2LightFactory();
var light = factory.build(InetAddress.getByName("192.168.188.105"));
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
