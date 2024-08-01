plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.3.0"
}

group = "net.mine-asterisk.mc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // Paper API.
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")

    // Cloud.
    implementation("org.incendo:cloud-core:2.0.0-rc.2")
    implementation("org.incendo:cloud-paper:2.0.0-beta.9")

    // Hibernate.
    implementation("org.hibernate.orm:hibernate-core:6.5.2.Final")
    implementation("org.hibernate.orm:hibernate-hikaricp:6.5.2.Final")

    // HikariCP.
    implementation("com.zaxxer:HikariCP:5.1.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    runServer {
        minecraftVersion("1.21")
    }
}