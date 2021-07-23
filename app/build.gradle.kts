import com.android.build.api.instrumentation.*
import com.android.build.api.variant.AndroidComponentsExtension
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

plugins {
    id("com.android.application")
    kotlin("android")
}

apply<VariableFontInjectionPlugin>()

android {
    compileSdk = 30
    buildToolsVersion = "30.0.3"

    buildFeatures.compose = true

    defaultConfig {
        applicationId = "com.mxalbert.compose.variablefonts"
        minSdk = 23
        targetSdk = 30
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = rootProject.extra["compose_version"] as String
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.appcompat:appcompat:1.3.0")
    implementation("androidx.compose.material:material:${rootProject.extra["compose_version"]}")
    implementation("androidx.compose.ui:ui-tooling:${rootProject.extra["compose_version"]}")
    implementation("androidx.activity:activity-compose:1.3.0-rc02")
    implementation("com.google.android.material:material:1.4.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${rootProject.extra["compose_version"]}")
}

class VariableFontInjectionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)

        androidComponents.onVariants { variant ->
            variant.transformClassesWith(
                ClassVisitorFactory::class.java,
                InstrumentationScope.ALL
            ) {}
            variant.setAsmFramesComputationMode(FramesComputationMode.COPY_FRAMES)
        }
    }

    abstract class ClassVisitorFactory : AsmClassVisitorFactory<InstrumentationParameters.None> {

        override fun createClassVisitor(
            classContext: ClassContext,
            nextClassVisitor: ClassVisitor
        ): ClassVisitor = TargetClassVisitor(nextClassVisitor)

        override fun isInstrumentable(classData: ClassData): Boolean = classData.className ==
                "androidx.compose.ui.text.platform.extensions.TextPaintExtensions_androidKt"

    }

    private class TargetClassVisitor(classVisitor: ClassVisitor) :
        ClassVisitor(Opcodes.ASM7, classVisitor) {

        override fun visitMethod(
            access: Int,
            name: String?,
            descriptor: String?,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            val methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions)
            return if (name == "applySpanStyle") {
                println("Found target method: $name$descriptor")
                TargetMethodVisitor(api, methodVisitor, access, name, descriptor)
            } else methodVisitor
        }

    }

    private class TargetMethodVisitor(
        api: Int,
        methodVisitor: MethodVisitor?,
        access: Int,
        name: String?,
        descriptor: String?
    ) : AdviceAdapter(api, methodVisitor, access, name, descriptor) {

        override fun onMethodExit(opcode: Int) {
            mv.visitVarInsn(Opcodes.ALOAD, 0)
            mv.visitVarInsn(Opcodes.ALOAD, 1)
            mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "com/mxalbert/compose/variablefonts/ui/theme/TypeKt",
                "applyFontWeight",
                "(Landroid/graphics/Paint;Landroidx/compose/ui/text/SpanStyle;)V",
                false
            )
        }

    }

}
