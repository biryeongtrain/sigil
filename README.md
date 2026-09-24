# sigil

A barebone library for creating server-side skills. Provides basic frameworks for skill casting, cooldowns, and
containers. Does not include functional features like entity targeting.

> Note: Until version 1.0.0, the API may change without prior notice.

This fork backports upstream Sigil 0.3.0 to **Minecraft 1.21.8 / Fabric / Java 21**.
Use version `0.3.0+1.21.8`; the unsuffixed upstream versions target Minecraft 26.2.
The Java and Kotlin APIs are preserved except for Minecraft's version-specific names
(for example, `ResourceLocation` instead of `Identifier`).

## Gradle Setup

### Kotlin

```kotlin
repositories {
    maven("https://repo.biryeong.kim/releases")
}

dependencies {
    modImplementation("org.zuttomae:sigil:0.3.0+1.21.8")
}
```

### Groovy

```groovy
repositories {
    maven { url 'https://repo.biryeong.kim/releases' }
}

dependencies {
    modImplementation 'org.zuttomae:sigil:0.3.0+1.21.8'
}
```

Install Sigil and Fabric API on the server, or use Loom's `include` to bundle Sigil
in your mod. The Kotlin extension API also needs Kotlin's standard library at runtime.
Loom reads the injected-interface metadata, so consuming mods can call
`LivingEntity.getSkillContainer()`, `getSkillCooldownManager()`, and `getSkillManager()`.

## Verification and publishing

Run with JDK 21:

```sh
./gradlew build runGameTest --no-daemon
./gradlew publish --no-daemon
```

The GameTests exercise per-entity cast state and duration, server-tick completion,
cooldown expiry, cancellation, temporary attribute cleanup, entity attachment
serialization, entity removal, and server-player death. They do not test client visuals.
Publishing uses the same `qf-repo` Maven repository configuration as
[minigame-shader](https://github.com/biryeongtrain/minigame-shader/blob/master/build.gradle).
