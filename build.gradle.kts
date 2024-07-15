plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
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

    // Hibernate.
    implementation("org.hibernate.orm:hibernate-core:6.5.2.Final")
    implementation("org.hibernate.orm:hibernate-hikaricp:6.5.2.Final")

    // HikariCP.
    implementation("com.zaxxer:HikariCP:5.1.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}