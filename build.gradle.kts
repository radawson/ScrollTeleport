plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.papermc.paperweight.userdev") version "1.5.11"
}

group = "org.clockworx"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")
}

tasks {
    // Configure reobfuscation to use Mojang mappings for production
    paperweight {
        reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION
    }

    // Make the reobfJar task run on build
    assemble {
        dependsOn(reobfJar)
    }

    // Configure shadowJar
    shadowJar {
        archiveClassifier.set("")
        minimize()
    }

    // Configure jar task
    jar {
        manifest {
            attributes(
                "Name" to project.name,
                "Version" to project.version,
                "Description" to "A modern scroll teleportation plugin",
                "Author" to "Clockworx",
                "Main" to "org.clockworx.scrollteleportation.ScrollTeleportation"
            )
        }
        from("LICENSE") {
            rename { "${it}_${project.name}" }
        }
        dependsOn("shadowJar")
    }

    clean {
        delete(layout.buildDirectory)
    }
} 