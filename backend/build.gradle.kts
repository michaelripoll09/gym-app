import java.security.SecureRandom
import java.util.Base64

plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.spring") version "2.3.21"
    kotlin("plugin.jpa") version "2.3.21"
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.gymapp"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.security:spring-security-crypto")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    val jwtSecret = ByteArray(32).also(SecureRandom()::nextBytes)
    environment("GYM_JWT_SECRET", Base64.getEncoder().encodeToString(jwtSecret))
}

springBoot {
    mainClass.set("com.gymapp.GymAppApplicationKt")
}

tasks.register<JavaExec>("runCatalogImport") {
    group = "application"
    description = "Imports the pinned exercise dataset into PostgreSQL."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.gymapp.catalog.CatalogImportCommandKt")
    doFirst {
        val dataset = project.findProperty("catalogDataset")?.toString()
            ?: error("Provide -PcatalogDataset=<path-to-exercises.json>")
        args(
            dataset,
            "hasaneyldrm/exercises-dataset",
            "7455efae41b330c265e7cd4b78dfa848e7ce5ebd",
            "656634224b8977b99a6d765470ee123260d4979715eaa4e7c0b7c8bb0d79f93d",
        )
    }
}
