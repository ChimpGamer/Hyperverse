plugins {
    id("io.papermc.paperweight.userdev") version "1.3.1"
}

dependencies {
    paperDevBundle("1.18-R0.1-SNAPSHOT")
    compileOnly(projects.hyperverseNmsCommon)
}
