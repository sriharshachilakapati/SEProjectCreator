buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath 'de.richsource.gradle.plugins:gwt-gradle-plugin:0.6'
    }
}

allprojects {
    apply plugin: 'idea'
    apply plugin: 'eclipse'
}

subprojects {
    apply plugin: 'maven'

    repositories {
        jcenter()
        mavenCentral()
        maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
    }
}