import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
            }

            extensions.configure<LibraryExtension> {
                compileSdk = 37

                defaultConfig {
                    minSdk = 26
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    consumerProguardFiles("consumer-rules.pro")
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_11
                    targetCompatibility = JavaVersion.VERSION_11
                }
            }

            dependencies {
                add("implementation", catalog.findLibrary("androidx-core-ktx").get())
                add("implementation", catalog.findLibrary("androidx-appcompat").get())
                add("implementation", catalog.findLibrary("material").get())
                add("testImplementation", catalog.findLibrary("junit").get())
                add("androidTestImplementation", catalog.findLibrary("androidx-junit").get())
                add("androidTestImplementation", catalog.findLibrary("androidx-espresso-core").get())
            }
        }
    }
}
