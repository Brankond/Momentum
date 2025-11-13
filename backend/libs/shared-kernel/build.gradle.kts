plugins {
    `java-library`
}

java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:3.5.7"))

    implementation("org.springframework:spring-context")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.assertj:assertj-core:3.26.3")
}
