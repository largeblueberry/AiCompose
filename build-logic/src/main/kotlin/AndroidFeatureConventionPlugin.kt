import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("aicompose.android.library")
                apply("aicompose.android.compose")
                apply("aicompose.hilt")
            }

            dependencies {
                add("implementation", catalog.findLibrary("kotlinx-coroutines-android").get())
                add("implementation", catalog.findLibrary("androidx-navigation-compose").get())
                add("implementation", catalog.findBundle("lifecycle").get())
            }
        }
    }
}
