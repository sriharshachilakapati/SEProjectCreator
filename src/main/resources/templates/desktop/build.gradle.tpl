apply plugin: 'java'

project.ext.lwjglVersion = "3.0.0-SNAPSHOT"

dependencies {
    compile files("../libs/silenceengine.jar")
    compile files("../libs/silenceengine-resources.jar")
    compile files("../libs/backend-lwjgl.jar")
    compile project(":${className}Core")

    compile "org.lwjgl:lwjgl:${lwjglVersion}"
    compile "org.lwjgl:lwjgl-platform:${lwjglVersion}:natives-windows"
    compile "org.lwjgl:lwjgl-platform:${lwjglVersion}:natives-linux"
    compile "org.lwjgl:lwjgl-platform:${lwjglVersion}:natives-osx"
}