import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

fun loadPropertiesFile(file: File, required: Boolean = true): Properties {
    val props = Properties()
    if (!file.exists()) {
        if (required) {
            error("Missing developer configuration file: ${file.absolutePath}")
        }
        return props
    }
    file.inputStream().use(props::load)
    return props
}

val developmentConfigFile = rootProject.file("development.properties")
val developmentProperties = loadPropertiesFile(developmentConfigFile)
val localProperties = loadPropertiesFile(rootProject.file("local.properties"), required = false)

fun configuredProperty(name: String): String =
    developmentProperties.getProperty(name)?.takeIf { it.isNotBlank() }
        ?: error("Missing required developer configuration value: $name in ${developmentConfigFile.absolutePath}")

fun optionalConfiguredProperty(name: String): String? =
    developmentProperties.getProperty(name)?.takeIf { it.isNotBlank() }

fun configuredList(name: String): List<String> =
    configuredProperty(name)
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

fun optionalConfiguredList(name: String): List<String> =
    optionalConfiguredProperty(name)
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        .orEmpty()

fun localProperty(name: String): String? =
    localProperties.getProperty(name)?.takeIf { it.isNotBlank() }

fun gradleProperty(name: String): String? =
    (findProperty(name) as String?)?.takeIf { it.isNotBlank() }

fun environmentVariable(name: String): String? =
    System.getenv(name)?.takeIf { it.isNotBlank() }

fun expandUserPath(path: String): String {
    val home = System.getProperty("user.home")
        ?: environmentVariable("HOME")
        ?: environmentVariable("USERPROFILE")
    return when {
        home == null -> path
        path == "~" -> home
        path.startsWith("~/") || path.startsWith("~\\") -> File(home, path.substring(2)).path
        else -> path
    }
}

fun hasStarsectorCoreJars(dir: File): Boolean =
    File(dir, "starfarer.api.jar").isFile &&
            File(dir, "starfarer_obf.jar").isFile &&
            File(dir, "fs.common_obf.jar").isFile

fun existingStarsectorRoot(path: String): String? {
    val root = file(expandUserPath(path))
    if (!root.isDirectory) return null
    val nestedCore = File(root, "starsector-core")
    return when {
        hasStarsectorCoreJars(nestedCore) || hasStarsectorCoreJars(root) -> root.absolutePath
        else -> null
    }
}

val defaultStarsectorDirectoryPaths =
    optionalConfiguredList("starsector.defaultDirs") +
            optionalConfiguredProperty("starsector.defaultDir").let { if (it == null) emptyList() else listOf(it) }

val detectedDefaultStarsectorDirectoryPath =
    defaultStarsectorDirectoryPaths.firstNotNullOfOrNull(::existingStarsectorRoot)

val starsectorDirectoryPath: String =
    gradleProperty("starsectorDir")
        ?: environmentVariable("STARSECTOR_DIRECTORY")
        ?: localProperty("starsector.dir")
        ?: detectedDefaultStarsectorDirectoryPath
        ?: error(
            """
            Starsector directory not configured.

            Set one of:
              - Gradle property: -PstarsectorDir=C:\Path\To\Starsector
              - Environment variable: STARSECTOR_DIRECTORY=C:\Path\To\Starsector
              - local.properties: starsector.dir=C:/Path/To/Starsector

            Or edit the shared default in:
              ${developmentConfigFile.absolutePath}

            These default local paths are also used automatically if one exists:
              ${defaultStarsectorDirectoryPaths.joinToString("\n              ").ifBlank { "(none configured)" }}

            The directory must contain either starsector-core/ or the Starsector core jars.
            """.trimIndent()
        )

fun resolveStarsectorCoreDirectory(root: File): File {
    val nested = File(root, "starsector-core")
    return when {
        hasStarsectorCoreJars(nested) -> nested
        hasStarsectorCoreJars(root) -> root
        else -> error(
            """
            Could not find Starsector core jars.

            Checked:
              ${nested.absolutePath}
              ${root.absolutePath}

            Expected jars include starfarer.api.jar, starfarer_obf.jar, and fs.common_obf.jar.
            """.trimIndent()
        )
    }
}

data class RequiredMod(
    val displayName: String,
    val folderNames: List<String>,
    val modIds: List<String> = emptyList(),
)

class BuildVariables(
    val modVersion: String,
    val modId: String,
    val modName: String,
    val author: String,
    val description: String,
    val gameVersion: String,
    val modPlugin: String,
    val isUtilityMod: String,
    val repoOwner: String,
    val repoName: String,
    val modThreadId: String,
    val outputJarRoot: String,
    configuredReleaseTag: String?,
) {
    val jarFileNameBase = "AdvancedGunneryControl-$modVersion"
    val jarFileName = "$jarFileNameBase.jar"
    val sourceJarFileName = "$jarFileNameBase-sources.jar"
    val jarsDir = "$outputJarRoot/$modVersion"
    val jars = arrayOf("$jarsDir/$jarFileName")
    val masterVersionFile = "https://raw.githubusercontent.com/$repoOwner/$repoName/master/$modId.version"
    val modFolderName = modName.replace(" ", "-")
    val changelogUrl = "https://raw.githubusercontent.com/$repoOwner/$repoName/master/changelog.txt"
    val releaseTag = configuredReleaseTag ?: modVersion
    val directDownloadUrl = "https://github.com/$repoOwner/$repoName/releases/download/$releaseTag/${modFolderName}-$modVersion.zip"

// Scroll down and change the "dependencies" part of mod_info.json, if needed
// LazyLib is needed to use Kotlin, as it provides the Kotlin Runtime
}

val Variables = BuildVariables(
    modVersion = configuredProperty("mod.version"),
    modId = configuredProperty("mod.id"),
    modName = configuredProperty("mod.name"),
    author = configuredProperty("mod.author"),
    description = configuredProperty("mod.description"),
    gameVersion = configuredProperty("mod.gameVersion"),
    modPlugin = configuredProperty("mod.plugin"),
    isUtilityMod = configuredProperty("mod.utility"),
    repoOwner = configuredProperty("repo.owner"),
    repoName = configuredProperty("repo.name"),
    modThreadId = configuredProperty("mod.threadId"),
    outputJarRoot = configuredProperty("output.jarRoot"),
    configuredReleaseTag = optionalConfiguredProperty("repo.releaseTag"),
)
//////////////////////
val starsectorDirectory = file(starsectorDirectoryPath)
val starsectorCoreDirectory = resolveStarsectorCoreDirectory(starsectorDirectory)
val starsectorModDirectory = File(starsectorDirectory, "mods")
val modInModsFolder = File(starsectorModDirectory, Variables.modFolderName)

fun modInfoContainsId(candidate: File, modId: String): Boolean {
    val modInfo = File(candidate, "mod_info.json")
    if (!modInfo.isFile) return false
    val text = modInfo.readText()
    return Regex(""""id"\s*:\s*"${Regex.escape(modId)}"""", RegexOption.IGNORE_CASE)
        .containsMatchIn(text)
}

fun resolveDependencyModDirectory(mod: RequiredMod): File {
    if (!starsectorModDirectory.isDirectory) {
        error("Could not find Starsector mods directory: ${starsectorModDirectory.absolutePath}")
    }

    val candidates = starsectorModDirectory.listFiles()
        ?.filter { it.isDirectory }
        .orEmpty()
        .sortedByDescending { it.name }

    candidates.firstOrNull { candidate ->
        mod.folderNames.any { folder -> candidate.name.equals(folder, ignoreCase = true) }
    }?.let { return it }

    candidates.firstOrNull { candidate ->
        mod.folderNames.any { folder -> candidate.name.startsWith("$folder-", ignoreCase = true) }
    }?.let { return it }

    candidates.firstOrNull { candidate ->
        mod.modIds.any { id -> modInfoContainsId(candidate, id) }
    }?.let { return it }

    error(
        """
        Missing required dependency mod: ${mod.displayName}

        Install it into:
          ${starsectorModDirectory.absolutePath}

        Accepted folder names:
          ${mod.folderNames.joinToString(", ")}
        Accepted mod ids:
          ${mod.modIds.joinToString(", ").ifBlank { "(none listed)" }}
        """.trimIndent()
    )
}

val lazyLibDirectory = resolveDependencyModDirectory(
    RequiredMod(
        configuredProperty("dependency.lazyLib.displayName"),
        configuredList("dependency.lazyLib.folderNames"),
        configuredList("dependency.lazyLib.modIds")
    )
)
val magicLibDirectory = resolveDependencyModDirectory(
    RequiredMod(
        configuredProperty("dependency.magicLib.displayName"),
        configuredList("dependency.magicLib.folderNames"),
        configuredList("dependency.magicLib.modIds")
    )
)
val lunaLibDirectory = resolveDependencyModDirectory(
    RequiredMod(
        configuredProperty("dependency.lunaLib.displayName"),
        configuredList("dependency.lunaLib.folderNames"),
        configuredList("dependency.lunaLib.modIds")
    )
)
val consoleCommandsDirectory = resolveDependencyModDirectory(
    RequiredMod(
        configuredProperty("dependency.consoleCommands.displayName"),
        configuredList("dependency.consoleCommands.folderNames"),
        configuredList("dependency.consoleCommands.modIds")
    )
)

fun dependencyJarFiles(dir: File): Array<File> =
    File(dir, "jars").listFiles { file ->
        file.isFile && file.extension.equals("jar", ignoreCase = true)
    } ?: emptyArray()

plugins {
    kotlin("jvm") version "2.1.20"
    java
    // id("org.jetbrains.dokka") version "2.0.0"
}

version = Variables.modVersion

repositories {
    maven(url = uri("$projectDir/libs"))
    mavenCentral()
}

dependencies {
    // implementation("org.junit.jupiter:junit-jupiter:5.7.0")
    // implementation("junit:junit:4.13.1")
    val kotlinVersionInLazyLib = "2.1.20"

    implementation(fileTree("libs") { include("*.jar") })
    // testImplementation(kotlin("test"))

    // Get kotlin sdk from LazyLib during runtime, only use it here during compile time
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersionInLazyLib")
    // compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersionInLazyLib")
    compileOnly(fileTree("${consoleCommandsDirectory.path}/jars"){include("*.jar")})

    implementation(fileTree("${lazyLibDirectory.path}/jars") { include("*.jar") })
    implementation(fileTree("${magicLibDirectory.path}/jars") { include("*.jar") })
    implementation(fileTree("${lunaLibDirectory.path}/jars") { include("*.jar") })
    //compileOnly(fileTree("$starsectorModDirectory/Console Commands/jars") { include("*.jar") })

    // Starsector jars and dependencies
    implementation(fileTree(starsectorCoreDirectory) {
        include(
            "starfarer.api.jar",
            //"starfarer.api-sources.jar",
            "starfarer_obf.jar",
            "fs.common_obf.jar",
            "json.jar",
            "xstream-1.4.10.jar",
            "log4j-1.2.9.jar",
            "lwjgl.jar",
            "lwjgl_util.jar"
        )
    })
    compileOnly(fileTree("$projectDir/api/src/com/fs/starfarer/api"){
        include("*.java")
    })
}

//java{
//    withSourcesJar()
//    withJavadocJar()
//}

tasks {
    register("validateLocalBuildEnvironment") {
        group = "verification"
        description = "Checks Starsector and dependency mod paths used for local AGC builds."

        doLast {
            println("Starsector directory: ${starsectorDirectory.absolutePath}")
            println("Starsector core: ${starsectorCoreDirectory.absolutePath}")
            println("LazyLib: ${lazyLibDirectory.absolutePath}")
            println("MagicLib: ${magicLibDirectory.absolutePath}")
            println("LunaLib: ${lunaLibDirectory.absolutePath}")
            println("Console Commands: ${consoleCommandsDirectory.absolutePath}")

            require(starsectorDirectory.isDirectory) { "Missing Starsector directory: ${starsectorDirectory.absolutePath}" }
            require(starsectorCoreDirectory.isDirectory) { "Missing Starsector core directory: ${starsectorCoreDirectory.absolutePath}" }
            require(hasStarsectorCoreJars(starsectorCoreDirectory)) {
                "Missing required Starsector core jars in: ${starsectorCoreDirectory.absolutePath}"
            }

            listOf(
                "LazyLib" to lazyLibDirectory,
                "MagicLib" to magicLibDirectory,
                "LunaLib" to lunaLibDirectory,
                "Console Commands" to consoleCommandsDirectory,
            ).forEach { (name, dir) ->
                require(dir.isDirectory) { "Missing dependency directory for $name: ${dir.absolutePath}" }
                val jarsDir = File(dir, "jars")
                require(jarsDir.isDirectory) {
                    "Dependency mod has no jars directory for $name: ${jarsDir.absolutePath}"
                }
                require(dependencyJarFiles(dir).isNotEmpty()) {
                    "Dependency mod has no jar files for $name: ${jarsDir.absolutePath}"
                }
            }
        }
    }

    named<Jar>("jar")
    {
        // dependsOn(dokkaJavadoc)
        from(sourceSets.main.get().output)
        destinationDirectory.set(file(Variables.jarsDir))
        archiveFileName.set(Variables.jarFileName)
    }
    named<org.gradle.jvm.tasks.Jar>("kotlinSourcesJar") {
        from(sourceSets.main.get().allSource)
        destinationDirectory.set(file(Variables.jarsDir))
        archiveFileName.set(Variables.sourceJarFileName)
        archiveClassifier.set("sources")
    }
    register("create-metadata-files") {
        val version = Variables.modVersion.split(".")
        System.setProperty("line.separator", "\n") // Use LF instead of CRLF like a normal person

        File(projectDir, "mod_info.json")
            .writeText(
                """
                    # THIS FILE IS GENERATED BY build.gradle.kts. (Note that Starsector's json parser permits `#` for comments)
                    {
                        "id": "${Variables.modId}",
                        "name": "${Variables.modName}",
                        "author": "${Variables.author}",
                        "utility": "${Variables.isUtilityMod}",
                        "version": { "major":"${version[0]}", "minor": "${version[1]}", "patch": "${version[2]}" },
                        "description": "${Variables.description}",
                        "gameVersion": "${Variables.gameVersion}",
                        "jars":[${Variables.jars.joinToString() { "\"$it\"" }}],
                        "modPlugin":"${Variables.modPlugin}",
                        "dependencies": [
                            {
                                "id": "lw_lazylib",
                                "name": "LazyLib"
                            },
                            {
                                "id" : "MagicLib",
                                "name" : "MagicLib"
                            }
                        ]
                    }
                """.trimIndent()
            )

        File(projectDir, "data/config/version/version_files.csv")
            .writeText(
                """
                    version file
                    ${Variables.modId}.version

                """.trimIndent()
            )

        File(projectDir, "${Variables.modId}.version")
            .writeText(
                """
                    # THIS FILE IS GENERATED BY build.gradle.kts.
                    {
                        "masterVersionFile":"${Variables.masterVersionFile}",
                        "modName":"${Variables.modName}",
                        "modThreadId":${Variables.modThreadId},
                        "modVersion":
                        {
                            "major":${version[0]},
                            "minor":${version[1]},
                            "patch":${version[2]}
                        },
                        "directDownloadURL": "${Variables.directDownloadUrl}",
                        "changelogURL": "${Variables.changelogUrl}"
                    }
                """.trimIndent() + "\n"
            )


        File(projectDir, ".github/workflows/mod-folder-name.txt")
            .writeText(Variables.modFolderName)

        mkdir(Variables.jarsDir)

        doLast {
            File(Variables.jarsDir, "${Variables.jarFileNameBase}.pom")
                .writeText(
                    """
                    <project>
                        <modelVersion>4.0.0</modelVersion>

                        <groupId>agc</groupId>
                        <artifactId>AdvancedGunneryControl</artifactId>
                        <version>${Variables.modVersion}</version>
                    </project>
                """.trimIndent()
                )
        }

    }

    register("write-settings-file") {
        System.setProperty("line.separator", "\n")
        File(projectDir, "Settings.editme")
            .writeText(
                """
                   | # NOTE: If LunaLib is enabled, LunaSettings override these settings! Use of LunaSettings is recommended.
                   | # When using LunaSettings, the main area of interest in this file are the tag lists, as they cannot be set via LunaSettings
                   |
                   | # NOTE: If the mod fails to parse these settings, it will fall back to default settings
                   | #       Check starsector.log (in the Starsector folder) for details (ctrl+f for advancedgunnerycontrol)
                   | {
                   |   #                                 #### TAG LIST ####
                   |   # Determines which tags will be shown in the GUIs. Feel free to add/remove tags as you see fit.
                   |   # Allowed values are: (replace N with a number; percent forms usually use 0-100, raw-value forms use literal values like H<145)
                   |   # "PD", "NoPD", "NoMissile", "PD(TF>N%)", "PD(SF>N%)", "PD(HF>N%)", "PrioSmall", "PrioBig", "TargetFighter", "NoFighter",
                   |   # "AvoidShield", "TargetShield", "AvoidShield(S<N%)", "TargetShield(S>N%)", "AvoidShield+", "TargetShield+",
                   |   # "AvoidShield(TF>N%)", "AvoidShield(SF>N%)", "AvoidShield(HF>N%)", "TargetShield(TF>N%)", "TargetShield(SF>N%)", "TargetShield(HF>N%)",
                   |   # "AvoidArmor(N%)", "AvoidDebris", "NoShield", "Opportunist", "HoldFire(TF>N%)", "HoldFire(SF>N%)", "HoldFire(HF>N%)",
                   |   # "Opportunist(A<N%)", "PD(A<N%)", "ConserveAmmo", "ConserveAmmo(A<N%)", "ConservePDAmmo", "ConservePDAmmo(A<N%)", "CnsrvPDAmmo", "ShipTarget", "AvoidPhased", "TargetPhase", "SyncWindow", "SyncWindow(X)", "SyncVolley", "SyncVolley(X)", "Ambush", "Ambush(X)",
                   |   # "AvoidPD(Waste>N%)", "AvoidPD(H<N)", "IgnoreMinorPD", "IgnoreMinorPD(H<N>)", "TargetBig", "TargetSmall", "Panic(H<N%)", "Range<N%", "ForceAutoFire", "DoNotShoot", "Force(TF<N%)", "Force(SF<N%)", "Force(HF<N%)",
                   |   # "TargetOverloaded", "Merge", "DisableTags", "PrioFighter", "PrioMissile", "PrioShip", "PrioWounded", "PrioWoundedPD", "PrioHealthy", "PrioFocused", "PrioShields", "PrioHull", "PrioClose", "PrioFar",
                   |   # "BlockBeams", "CustomAI", "LowRoF(N%)", "PrioDense"
                   |
                   |   # Flux notation inside parentheses: TF = total flux, HF = hard flux, SF = soft flux.
                   |   # Legacy tags such as Hold(TF>N%), Hold(SF>N%), Hold(HF>N%), Hold(Flx>N%), HoldSFT(F>N%), ForceAF, PrioPD, PrioritisePD, PrioritizePD, BigShip, BigShips, SmallShip, SmallShips, Fighter, Overloaded, AvoidFighter, AvoidMissile, NoPD(Waste>N%), NoPD(H<N), and AvShldFT(F<N%) are still accepted for saved-loadout compatibility.
                   |
                   |   # Choose which of the following lists will be used. Valid options are: "classic", "novice" and "complete"
                   |   # LunaSettings no longer selects this list; use the AGC GUI tag-list selector instead.
                   |   "listVariant" : "classic"
                   |
                   |   ,"completeTagList" : [
                   |                "TargetShield", "TargetShield+", "TargetShield(S>50%)", "TargetShield(TF>10%)", "TargetShield(SF>10%)", "TargetShield(HF>10%)",
                   |                "TargetPhase", "TargetBig", "TargetSmall", "TargetFighter",
                   |                "TargetOverloaded", "ShipTarget", "Opportunist",
                   |                "Opportunist(A<80%)",
                   |
                   |                "AvoidShield", "AvoidShield+", "AvoidShield(S<50%)", "AvoidShield(TF>10%)", "AvoidShield(SF>10%)", "AvoidShield(HF>10%)",
                   |                "AvoidArmor(33%)", "AvoidArmor(75%)", "AvoidPhased",
                   |                "AvoidPD(Waste>40%)", "AvoidPD(H<145)", "AvoidDebris",
                   |
                   |                "NoShield", "NoMissile", "NoFighter", "NoPD", "DoNotShoot",
                   |
                   |                "ForceAutoFire", "Force(TF<10%)", "Force(TF<25%)",
                   |                "Force(TF<50%)", "Force(SF<10%)", "Force(SF<25%)", "Force(HF<25%)",
                   |
                   |                "HoldFire(TF>90%)", "HoldFire(TF>75%)", "HoldFire(SF>25%)", "HoldFire(HF>25%)",
                   |
                   |                "PD", "PD(TF>50%)", "PD(SF>10%)", "PD(SF>25%)",
                   |                "PD(HF>25%)", "PD(A<80%)",
                   |
                   |                "PrioFighter", "PrioMissile", "PrioSmall", "PrioBig",
                   |                "PrioShip", "PrioWounded", "PrioWoundedPD", "PrioHealthy",
                   |                "PrioShields", "PrioHull", "PrioFocused", "PrioClose",
                   |                "PrioFar", "PrioDense",
                   |
                   |                "Ambush", "SyncWindow", "SyncVolley",
                   |
                   |                "CustomAI", "Merge", "DisableTags", "Range<60%",
                   |                "Range<90%", "LowRoF(200%)", "Panic(H<25%)", "BlockBeams"
                   |                ]
                   |
                   |   ,"classicTagList" : [
                   |                "TargetShield", "TargetShield+", "TargetShield(TF>10%)",
                   |                "TargetPhase", "TargetBig", "TargetSmall", "TargetFighter",
                   |                "TargetOverloaded", "ShipTarget", "Opportunist",
                   |                "Opportunist(A<80%)",
                   |
                   |                "AvoidShield", "AvoidShield+", "AvoidShield(TF>10%)",
                   |                "AvoidArmor(33%)", "AvoidArmor(75%)", "AvoidPhased",
                   |                "AvoidPD(Waste>40%)", "AvoidPD(H<145)",
                   |
                   |                "NoMissile", "NoFighter", "DoNotShoot",
                   |
                   |                "ForceAutoFire", "Force(TF<10%)", "Force(TF<25%)",
                   |                "Force(TF<50%)", "Force(SF<10%)", "Force(SF<25%)",
                   |
                   |                "HoldFire(TF>90%)", "HoldFire(TF>75%)",
                   |
                   |                "PD", "PD(TF>50%)", "PD(SF>10%)", "PD(SF>25%)",
                   |                "PD(A<80%)",
                   |
                   |                "PrioFighter", "PrioMissile", "PrioSmall", "PrioBig",
                   |                "PrioShip", "PrioWounded", "PrioWoundedPD", "PrioHealthy",
                   |                "PrioShields", "PrioHull", "PrioFocused", "PrioClose",
                   |                "PrioFar", "PrioDense",
                   |
                   |                "Ambush", "SyncWindow", "SyncVolley",
                   |
                   |                "CustomAI", "Merge", "DisableTags", "Range<60%",
                   |                "Range<90%", "LowRoF(200%)", "Panic(H<25%)"
                   |                ]
                   |
                   |   ,"noviceTagList" : [
                   |                "TargetShield",
                   |
                   |                "AvoidShield", "AvoidArmor(33%)", "AvoidPhased",
                   |
                   |                "NoMissile", "NoFighter",
                   |
                   |                "ForceAutoFire", "Force(TF<50%)", "Force(SF<10%)",
                   |
                   |                "HoldFire(TF>90%)",
                   |
                   |                "PD",
                   |
                   |                "PrioFighter", "PrioMissile", "PrioShip",
                   |
                   |                "Range<60%"
                   |                ]
                   |   # Tags to display in simple mode.
                   |   , "simpleTagList" : [ "PD", "AvoidShield", "TargetShield", "AvoidArmor(33%)", "HoldFire(TF>90%)", "NoFighter" ]
                   |
                   |   # Determines which ship modes will be shown in the GUIs. Modes that do not exist will be discarded
                   |   # Allowed Values: "DEFAULT", "Personality(Steady)", "PreAim", "LowShield(TF>N%)", "ShieldUp(TF<N%)", "Vent(TF>N%,S=N)", "RunCR(CR<N%)", "RunCR(CR<A/B/C%)", "RunHP(HP<N%)", "RunHP(HP<A/B/C%)", "DisableSystem", "SpamSystem", "Charge", "ForceAutoFire", "NeverVent", "FarAway", "StayAway"
                   |   # Legacy "NoSystem" entries are still accepted and canonicalized to "DisableSystem".
                   |   # Note that "DEFAULT" is not a real mode but instead a shortcut to disable all other modes. It's kind of deprecated and only still exists for compatibility reasons.
                   |
                   |   ,"shipModeList" : ["PreAim", "Vent(TF>75%,S=2.0)", "Vent(TF>25%,S=0.25)", "LowShield(TF>50%)", "ShieldUp(TF<90%)", "RunCR(CR<50%)", "RunHP(HP<50%)", "Personality(Steady)", "DisableSystem", "SpamSystem", "Charge", "FarAway", "StayAway", "NeverVent"]
                   |   ,"noviceShipModeList" : ["PreAim", "Vent(TF>75%,S=2.0)", "Vent(TF>25%,S=0.25)", "LowShield(TF>50%)", "ShieldUp(TF<90%)", "RunCR(CR<50%)", "RunHP(HP<50%)"]
                   |
                   |   #                                 #### CUSTOM AI ####
                   |   # If you set this to true, if the base AI would have weapons in weapon groups target something invalid for the selected tags,
                   |   # they will try to acquire a fitting target using custom targeting AI.
                   |   # If you set this to false, they will use exclusively vanilla AI (base AI) and simply not fire in that situation.
                   |   # Update: I made quite a lot of improvements to the customAI, so I feel like it's safe to use now.
                   |   # Beware though that enabling (but not forcing) it will have a negative effect on game performance.
                   |   # Note that modes that rely on custom AI will be very janky when turning off custom AI
                   |   # Allowed values: true/false
                   |   ,"enableCustomAI" : true # <---- EDIT HERE ----
                   |
                   |   # Enabling this will always use the customAI (for applicable modes)
                   |   # Note that forcing & enabling custom AI should actually be beneficial for performance over just enabling it.
                   |   # Note that setting enableCustomAI to false and this to true is not a brilliant idea and will be overridden :P
                   |   ,"forceCustomAI" : false # <---- EDIT HERE ----
                   |
                   |   #                                 #### UI SETTINGS ####
                   |
                   |   , "suppressHudWarning" : false
                   |   # Master switch for campaign-persistent weapon tags and ship modes. Disable only if you want assignments to be temporary and are willing to disable campaign GUI storage.
                   |   , "enablePersistentFireModes" : true # <---- EDIT HERE ----
                   |   # If set to false, changes are only persistent if made in the campaign-GUI or manually saved
                   |   , "persistChangesInCombat" : true # <---- EDIT HERE ----
                   |   # Number of frames messages will be displayed before fading. -1 for infinite
                   |   , "messageDisplayDuration" : 250 # <---- EDIT HERE ----
                   |   # X/Y Position (from bottom left) where messages/tooltips will be displayed (refpoint: top left corner of message)
                   |   # Values between 0 and 1, x = 0.0 means left side of the screen, y = 0.0 means bottom of the screen
                   |   # Note: These values will automatically get adjusted by your scaling multiplier
                   |   , "messagePositionX" : 0.2 # <---- EDIT HERE ----
                   |   , "messagePositionY" : 0.4 # <---- EDIT HERE ----
                   |   # X/Y Position where the anchor (top left corner of first weapon group button row) of the combat GUI will be placed
                   |   , "combatUiAnchorX" : 0.025
                   |   , "combatUiAnchorY" : 0.8
                   |   # A key that can be represented by a single character that's not bound to anything in combat in the Starsector settings
                   |   , "inCombatGuiHotkey" : "j" # <---- EDIT HERE ----
                   |   # Campaign GUI
                   |   , "GUIHotkey" : "j" # <---- EDIT HERE ----
                   |   , "mergeHotkey" : "k" # <---- EDIT HERE ----
                   |   , "disableTagsHotkey" : "l" # <---- EDIT HERE ----
                   |
                   |   , "maxLoadouts" : 3 # <---- EDIT HERE ----
                   |   , "loadoutNames" : [ "Normal", "Special", "AllDefault" ]
                   |
                   |   # Automatically applies saved weapon tags and ship modes to player ships as they deploy in combat.
                   |   # This is forced off when enablePersistentFireModes is off.
                   |   , "enableAutoSaveLoad" : true # <---- EDIT HERE ----
                   |
                   |   #                                  #### TROUBLESHOOTING ####
                   |   # These flags disable certain parts of the UI that might cause issues on specific systems
                   |   , "enableRefitScreenIntegration" : true # <---- EDIT HERE ----
                   |   , "showRefitScreenButton" : true # <---- EDIT HERE ----
                   |   , "enableWeaponHighlighting" : true # <---- EDIT HERE ----
                   |   , "enableHoverTooltips" : true # <---- EDIT HERE ----
                   |   , "enableHoverTooltipBoxes" : true # <---- EDIT HERE ----
                   |   , "enableButtonHoverSound" : true # <---- EDIT HERE ----
                   |   , "enableButtonHoverEffects" : true # <---- EDIT HERE ----
                   |   , "enableButtonOutlines": true # <---- EDIT HERE ----
                   |   # Shows the Advanced Info section in Customize Suggested Tags weapon cards.
                   |   , "showSuggestedTagAdvancedInfo": false # <---- EDIT HERE ----
                   |
                   |   #                                 #### CUSTOM AI CONFIGURATION  ####
                   |   # NOTE: All the stuff here is mainly here to facilitate testing. But feel free to play around with the settings here!
                   |
                   |   # Define the number of calculation steps the AI should perform per time frame to compute firing solutions.
                   |   # higher values -> slightly better AI but worse performance (0 means just aim at current target position).
                   |   # performance cost increases linearly, firing solution accuracy approx. logarithmically (recommended: 1-2)
                   |   # I.e. doubling this value doubles the time required to compute firing solutions but only increases their
                   |   # accuracy a little bit.
                   |   # I believe that 1 is the value used in Vanilla
                   |   ,"customAIRecursionLevel" : 1 # <---- EDIT HERE (maybe)----
                   |
                   |   # If set to false, ships will be approximated as circles. This might lead to questionable firing decisions
                   |   # with very precise weapons (e.g. beams) against decidedly non-circular ships. On the other hand, using exact
                   |   # bounds will use significantly more performance
                   |   # Note: Even if set to true, exact bounds will only be used for making a firing decision, not for target selection
                   |   #       and aiming. Since weapons will always try to aim at the center of a ship,
                   |   #       using it universally seemed like a waste of performance.
                   |   ,"useExactBoundsForFiringDecision" : true # <---- EDIT HERE (maybe)----
                   |
                   |   # Any positive or negative float possible, reasonable values: between 0.7 ~ 2.0 or so
                   |   # 1.0 means "fire if shot will land within 1.0*(targetHitbox+10)"
                   |   # (the +10 serves to compensate for very small targets such as missiles and fighters)
                   |   ,"customAITriggerHappiness" : 1.0 # <---- EDIT HERE (maybe) ----
                   |
                   |   # Set this to true if you want the custom AI to perform better :P
                   |   ,"customAIAlwaysUsesBestTargetLeading" : false # <---- EDIT HERE (maybe) ----
                   |   # For purposes of determining whether a shot will hit, assume collision radius to be multiplied
                   |   # by this factor. This is to compensate for the fact that most ships aren't spherical.
                   |   ,"collisionRadiusMultiplier" : 0.8 # <---- EDIT HERE (maybe) ----
                   |
                   |   #                                 #### FRIENDLY FIRE AI CONFIGURATION ####
                   |   # "magic number" to choose how complex the friendly fire calculation should be
                   |   # The number entered here roughly corresponds to the big O notation (i.e. runtime of friendly fire algorithm ~ n^i,
                   |   # where n is the number of entities (ships/missiles) in range of the ship and i is the number chosen here)
                   |   # Valid numbers are:
                   |   #     - 0 : No friendly fire computation, weapons won't care about hitting allies
                   |   #     - 1 : Weapons won't consider friendly fire for target selection, only for deciding whether to fire or not
                   |   #     - 2 : Weapon will only select targets that don't risk friendly fire (potentially high performance cost)
                   |   ,"customAIFriendlyFireAlgorithmComplexity" : 1 # <---- EDIT HERE (maybe) ----
                   |
                   |   # Essentially the same as triggerHappiness, but used to prevent firing if ally would be hit
                   |   # 1.0 should be enough to not hit allies if they don't change their course, but it's nice to have a little buffer
                   |   ,"customAIFriendlyFireCaution" : 1.25 # <---- EDIT HERE (maybe) ----
                   |
                   |   #                                 #### TAG CUSTOMIZATION ####
                   |   # NOTE: Unless stated otherwise, numbers in this section should be positive values between (exclusively) 0 and 1 and represent fractions (i.e. 0.01 to 0.99)
                   |   # NOTE: Using invalid values might cause very odd behaviour and/or crashes!
                   |
                   |   # Shield thresholds: When not flanking shields and shields are on, the shield factor is simply
                   |   # equal to (1 - fluxLevel) of the target. When flanking shields, shield factor == 0.
                   |   # When shields are off but the enemy ship could raise them in time, the shield factor is equal to (1 - fluxLevel)*0.75
                   |   # When omni-shields are off, it's considered as half-flanking (subject to change)
                   |   # For frontal shields, unfold time and projectile travel time are considered to determine flanking
                   |   # For modes that want to hit shields, reducing the threshold makes them more likely to fire
                   |   # For modes that want to avoid shields, the opposite is true
                   |
                   |   ,"targetShields_threshold" : 0.1     # Default shield-factor threshold for TargetShield tags that do not include S>N%.
                   |   ,"avoidShields_threshold" : 0.2      # Default shield-factor threshold for AvoidShield tags that do not include S<N%.
                   |
                   |   # Opportunist AND conserveAmmo tag: (shield thresholds for opportunist mode, depending on damage type)
                   |   ,"opportunist_kineticThreshold" : 0.5    # i.e. Attack if target flux < 50% (simplified, see above)
                   |   ,"opportunist_HEThreshold" : 0.15        # i.e. Attack if target flux > 85% (simplified, see above)
                   |
                   |    # increasing this value will increase the likelihood of opportunist/conserveAmmo firing (positive non-zero number)
                   |    # Note: Relatively small changes to this value will have a considerable impact. So I'd recommend values between 0.9 and 1.2 or so
                   |   ,"opportunist_triggerHappinessModifier" : 1.0
                   |
                   |   # Vent ship modes:
                   |   # Vent(TF>75%,S=2.0)
                   |   ,"vent_flux" : 0.75 # vent if flux level > X
                   |   ,"vent_safetyFactor" : 2.0 # vent only if ship thinks it will survive venting X times (positive non-zero number)
                   |
                   |   # Legacy VentA(TF>25%) aliases are accepted as Vent(TF>25%,S=0.25,A=T)
                   |   ,"aggressiveVent_flux" : 0.25 # vent if flux level > X
                   |   ,"aggressiveVent_safetyFactor" : 0.25 # (positive non-zero number)
                   |
                   |   ,"retreat_hull" : 0.5 # retreat if hull level < X
                   |   ,"retreat_shouldDirectRetreat" : false
                   |
                   |   ,"shieldsOff_flux" : 0.5 # In LowShield mode, turn off shields if total flux level > X
                   |
                   |   ,"conserveAmmo_ammo" : 0.5 # Start conserving ammo when ammoLevel < X
                   |   ,"conservePDAmmo_ammo" : 0.8 # Only allow firing at fighters and missiles when ammo < X
                   |   ,"noPDWasteCleanupDamageCap" : 100 # AvoidPD(Waste>N%) ignores waste filtering for weapons at or below this estimated attack packet damage
                   |   # Advanced edit-tag controls. These are hidden and inactive unless enabled here or in LunaLib.
                   |   ,"showEnergyDamageTypeExclusionOption" : false
                   |   ,"showMissileDamageTypeExclusionOption" : false
                   |   ,"showProjectileDamageTypeExclusionOption" : false
                   |
                   |   # When set to true, the mod will periodically (~1/s) check if the custom ship AI has been stripped
                   |   # from player-fleet ships. Stripping happens when transferring control or turning on/off autopilot
                   |   # on the player-controlled ship
                   |   ,"automaticallyReapplyPlayerShipModes" : true
                   |
                   |   # Other mods can apply tags to ships in enemy fleets. Set this to false to opt out.
                   |   ,"allowEnemyShipModeApplication" : true
                   |
                   |   # Target/avoid shield tags allow targetting of fighters, regardless of their shield factor
                   |   # (Note that these modes will still prioritise targets based on shield factor)
                   |   ,"ignoreFighterShields" : true
                   |
                   |   # Sets the legacy free-fire threshold for old "TgtShieldsFT"; canonical tags should use TargetShield(TF>N%).
                   |   ,"targetShieldsAtFT_flux" : 0.2
                   |
                   |   # Sets the legacy free-fire threshold for old "AvdShieldsFT"; canonical tags should use AvoidShield(TF>N%).
                   |   ,"avoidShieldsAtFT_flux" : 0.2
                   |
                   |   # Soft-flux tags such as "HoldFire(SF>N%)" will still stop firing once total flux reaches this value.
                   |   ,"SFTUpperFluxLimit" : 0.9
                   |
                   |   # The PrioFighters/Missiles/Ships/PD tags will multiply the priority of the target type by this value.
                   |   # For reference: ShipTarget modifies priority by a factor of 1000, most other things by 2~1000
                   |   ,"prioXModifier" : 10000.0
                   |
                   |   # If a weapon's spread value would exceeds this value when firing the next burst, friendly fire computations will use a cone
                   |   # and eclipsing logic instead of a line to determine if the shot is likely to cause friendly fire.
                   |   ,"useConeFFAboveSpread" : 4.0
                   |
                   | }

                """.trimMargin()
            )
    }

    register("buildMod") {
        group = "build"
        description = "Validates the local Starsector install, compiles Kotlin, builds the jar, and regenerates metadata/settings."
        dependsOn("validateLocalBuildEnvironment", "compileKotlin", "jar", "create-metadata-files", "write-settings-file")
    }

    register("create-everything"){
        description = "Legacy compatibility task; prefer buildMod for normal local builds."
        dependsOn("buildMod")
    }

    register<Copy>("deployLocalMod") {
        group = "deployment"
        description = "Copies the built AGC fork into the configured Starsector mods directory."
        dependsOn("buildMod")
        from(projectDir) {
            exclude(
                ".git/**",
                ".github/**",
                ".gradle/**",
                ".idea/**",
                ".run/**",
                "gradle/**",
                "build/**",
                "local.properties",
                "*.bak",
            )
        }
        into(modInModsFolder)
    }
}

//tasks.test {
//    useJUnitPlatform()
//}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn("validateLocalBuildEnvironment")
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17
