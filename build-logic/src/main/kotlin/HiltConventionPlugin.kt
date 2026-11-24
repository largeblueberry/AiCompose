import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.devtools.ksp")
                apply("com.google.dagger.hilt.android")
            }

            dependencies {
                add("implementation", catalog.findLibrary("hilt-android").get())
                add("implementation", catalog.findLibrary("hilt-navigation-compose").get())
                add("ksp", catalog.findLibrary("hilt-compiler").get())
            }
        }
    }
}
