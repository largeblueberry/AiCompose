import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.plugin.compose")

                withPlugin("com.android.application") {
                    extensions.configure<ApplicationExtension> {
                        buildFeatures {
                            compose = true
                        }
                    }
                }
                withPlugin("com.android.library") {
                    extensions.configure<LibraryExtension> {
                        buildFeatures {
                            compose = true
                        }
                    }
                }
            }

            dependencies {
                val bom = catalog.findLibrary("androidx-compose-bom").get()
                add("implementation", platform(bom))
                add("androidTestImplementation", platform(bom))
                add("implementation", catalog.findLibrary("androidx-compose-ui").get())
                add("implementation", catalog.findLibrary("androidx-compose-ui-graphics").get())
                add("implementation", catalog.findLibrary("androidx-compose-foundation").get())
                add("implementation", catalog.findLibrary("androidx-compose-material3").get())
                add("implementation", catalog.findLibrary("androidx-compose-material-icons-extended").get())
                add("debugImplementation", catalog.findLibrary("androidx-compose-ui-tooling").get())
                add("implementation", catalog.findLibrary("androidx-compose-ui-tooling-preview").get())
            }
        }
    }
}
