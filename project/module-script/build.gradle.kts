dependencies {
    compileTabooLib()
    compileCore(11903)
    framework()
    serialization()

    compileModule("module-common")
    compileModule("module-core")
    compileModule("module-compats")

    compileOnly("com.mojang:datafixerupper:4.0.26")
}