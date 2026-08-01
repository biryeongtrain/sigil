# sigil

A barebone library for creating server-side skills. Provides basic frameworks for skill casting, cooldowns, and
containers. Does not include functional features like entity targeting.

> Note: Until version 1.0.0, the API may change without prior notice.

## Gradle Setup

### Kotlin

```kotlin
repositories {
    maven("https://repo.biryeong.kim/releases")
}

dependencies {
    implementation("org.zuttomae:sigil:YOUR_VERSION")
}
```

### Groovy

```groovy
repositories {
    maven { url 'https://repo.biryeong.kim/releases' }
}

dependencies {
    implementation 'org.zuttomae:sigil:YOUR_VERSION'
}
```