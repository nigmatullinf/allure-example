plugins {
    java
    id("io.qameta.allure") version "2.9.5"
}

group = "io.eroshenkoam"
version = version

allure {
    report {
        version.set("2.18.1")
    }
    adapter {
        autoconfigure.set(true)
        aspectjWeaver.set(true)
        frameworks {
            junit5 {
                adapterVersion.set("2.18.1")
            }
        }
    }
}

tasks.withType(JavaCompile::class) {
    sourceCompatibility = "${JavaVersion.VERSION_1_8}"
    targetCompatibility = "${JavaVersion.VERSION_1_8}"
    options.encoding = "UTF-8"
}

tasks.withType(Test::class) {
    ignoreFailures = true
    useJUnitPlatform {

    }
    systemProperty("junit.jupiter.execution.parallel.enabled", "true")
    systemProperty("junit.jupiter.execution.parallel.config.strategy", "dynamic")

    systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
    
    // Playwright configuration
    systemProperty("playwright.tracing", "true")
    systemProperty("playwright.tracing.dir", "build/playwright-traces")
    systemProperty("playwright.tracing.screenshots", "true")
    systemProperty("playwright.tracing.snapshots", "true")
    systemProperty("playwright.tracing.sources", "true")
}

// Task to install Playwright browsers
tasks.register("playwrightInstall") {
    group = "playwright"
    description = "Install Playwright browsers"
    doLast {
        com.microsoft.playwright.Playwright.create().let { playwright ->
            try {
                playwright.chromium().launch()
                playwright.firefox().launch()
                playwright.webkit().launch()
                println("Playwright browsers installed successfully")
            } finally {
                playwright.close()
            }
        }
    }
}


repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("commons-io:commons-io:2.6")
    implementation("io.qameta.allure:allure-java-commons:2.14.0")
    implementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    implementation("org.junit.jupiter:junit-jupiter-engine:5.7.2")
    implementation("org.junit.jupiter:junit-jupiter-params:5.7.2")
    
    // Playwright dependencies
    implementation("com.microsoft.playwright:playwright:1.40.0")
    implementation("io.qameta.allure:allure-playwright:2.18.1")
}
