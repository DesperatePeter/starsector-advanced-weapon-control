import org.apache.tools.ant.taskdefs.condition.Os
// import org.jetbrains.dokka.DokkaConfiguration
// import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Variables {
    // Note: On Linux, if you installed Starsector into ~/something, you have to write /home/<user>/ instead of ~/
    val starsectorDirectory = System.getenv("STARSECTOR_DIRECTORY") ?: "/home/jannes/games/starsector"
    val modVersion = "1.19.1"
    val jarFileNameBase = "AdvancedGunneryControl-$modVersion"
    val jarFileName = "$jarFileNameBase.jar"
    val sourceJarFileName = "$jarFileNameBase-sources.jar"
    val javadocJarFileName = "$jarFileNameBase-javadoc.jar"

    val modId = "advanced_gunnery_control_dbeaa06e"
    val modName = "AdvancedGunneryControl"
    val author = "DesperatePeter"
    const val description = "A mod that allows fine-tuning of autofire. Press J-key in combat or on the campaign map to access."
    val gameVersion = "0.98a"
    val jarsDir = "jars/agc/AdvancedGunneryControl/$modVersion"
    val jars = arrayOf("$jarsDir/$jarFileName")
    val modPlugin = "com.dp.advancedgunnerycontrol.WeaponControlBasePlugin"
    val isUtilityMod = true
    val masterVersionFile = "https://raw.githubusercontent.com/DesperatePeter/starsector-advanced-weapon-control/master/$modId.version"
    val modThreadId = "21280"

    val modFolderName = modName.replace(" ", "-")

// Scroll down and change the "dependencies" part of mod_info.json, if needed
// LazyLib is needed to use Kotlin, as it provides the Kotlin Runtime
}
//////////////////////
val starsectorCoreDirectory = if(Os.isFamily(Os.FAMILY_WINDOWS)) "${Variables.starsectorDirectory}/starsector-core" else Variables.starsectorDirectory
val starsectorModDirectory = "${Variables.starsectorDirectory}/mods"
val modInModsFolder = File("$starsectorModDirectory/${Variables.modFolderName}")

plugins {
    kotlin("jvm") version "2.1.20"
    java
    // id("org.jetbrains.dokka") version "2.0.0"
}

version = Variables.modVersion

repositories {
    maven(url = uri("$projectDir/libs"))
    jcenter()
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
    compileOnly(fileTree("$starsectorModDirectory/Console Commands/jars"){include("*.jar")})

    implementation(fileTree("$starsectorModDirectory/LazyLib/jars") { include("*.jar") })
    implementation(fileTree("$starsectorModDirectory/MagicLib/jars") { include("*.jar") })
    implementation(fileTree("$starsectorModDirectory/LunaLib/jars") { include("*.jar") })
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
//    named<DokkaTask>("dokkaJavadoc"){
//        dokkaSourceSets{
//            named("main"){
//                documentedVisibilities.set(
//                    setOf(
//                        DokkaConfiguration.Visibility.PUBLIC,
//                        DokkaConfiguration.Visibility.PROTECTED
//                    )
//                )
//                sourceRoots.from(sourceSets.main.get().allSource)
//            }
//        }
//    }
//    named<Jar>("javadocJar"){
//        dependsOn(dokkaJavadoc)
//        from(dokkaJavadoc.get().outputs)
//        archiveClassifier.set("javadoc")
//        destinationDirectory.set(file(Variables.jarsDir))
//        archiveFileName.set(Variables.javadocJarFileName)
//    }

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
                        "directDownloadURL": "https://github.com/DesperatePeter/starsector-advanced-weapon-control/releases/download/${version[0]}.${version[1]}.${version[2]}/AdvancedGunneryControl-${version[0]}.${version[1]}.${version[2]}.zip",
                        "changelogURL": "https://raw.githubusercontent.com/DesperatePeter/starsector-advanced-weapon-control/master/changelog.txt"
                    }
                """.trimIndent()
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
                   |   # Allowed values are: (replace N with a number, usually between 0 and 100)
                   |   # "PD", "NoPD", "NoMissiles", "PD(Flux>N%)", "PrioritisePD", "Fighter", "NoFighters", "AvoidShields", "TargetShields",
                   |   # "AvdShields+", "TgtShields+", "AvdShieldsFT", "TgtShieldsFT", "AvdArmor(N%)", "AvoidDebris", "ShieldsOff",
                   |   # "Opportunist", "Hold(Flux>N%)", "ConserveAmmo", "CnsrvPDAmmo", "ShipTarget", "AvoidPhased", "TargetPhase",
                   |   # "BigShips", "SmallShips", "Panic(H<N%)", "AvoidPhased", "Range<N%", "ForceF(Flux<N%)", "Overloaded", "Merge",
                   |   # "PrioFighter", "PrioMissile", "PrioShips", "PrioWounded", "PrioHealthy", "CustomAI", "LowRoF(N%)"
                   |   
                   |   # Note: The word Flux in parentheses may be abbreviated by skipping any of the non-capitalized letters, e.g.: F, Fx, Flx
                   |   
                   |   # Choose which of the following lists will be used. Valid options are: "classic", "novice" and "complete"
                   |   # If you are using LunaSettings, adjust the lists below
                   |   "listVariant" : "classic"
                   |   
                   |   ,"completeTagList" : [
                   |                "PD", "NoPD", "PD(Flx>50%)", "PD(Flx>10%)",
                   |                "AvoidShields", "TargetShields", "AvdShields+", "TgtShields+", "AvdArmor(33%)", "AvdArmor(75%)",
                   |                "Hold(Flx>90%)", "Hold(Flx>75%)", "Hold(Flx>50%)", "Merge",
                   |                "AvoidPhased", "TargetPhase", "ShipTarget", 
                   |                "ForceAF", "ForceF(F<25%)", "ForceF(F<50%)", "ForceF(F<75%)",
                   |                "PrioritisePD", "PrioFighter", "PrioMissile", "PrioShips", "PrioWounded", "PrioHealthy",
                   |                "Fighter", "AvdShieldsFT", "TgtShieldsFT", "AvoidDebris",
                   |                "NoMissiles", "NoFighters",
                   |                "Opportunist", "Panic(H<25%)", "Range<60%", "Range<80%",
                   |                "ConserveAmmo", "CnsrvPDAmmo",
                   |                "BigShips", "SmallShips",
                   |                "Overloaded", "LowRoF(200%)", "CustomAI", "PrioDense"
                   |                ]  
                   |   
                   |   ,"classicTagList" : [
                   |                "PD", "PD(Flx>50%)",
                   |                "AvoidShields", "TargetShields", "AvdArmor(33%)", 
                   |                "Hold(Flx>90%)", "Hold(Flx>75%)", "Merge",
                   |                "AvoidPhased", "TargetPhase", "ShipTarget", 
                   |                "ForceAF", "ForceF(F<50%)",
                   |                "PrioritisePD", "PrioFighter", "PrioMissile",
                   |                "NoMissiles", "NoFighters",
                   |                "Opportunist", "Panic(H<25%)", "Range<60%",
                   |                "ConserveAmmo", "CnsrvPDAmmo",
                   |                "AvdShields+", "TgtShields+"
                   |                ]  
                   |                
                   |   ,"noviceTagList" : [
                   |                "PD",
                   |                "AvoidShields", "TargetShields", "AvdArmor(33%)", 
                   |                "Hold(Flx>90%)",
                   |                "AvoidPhased", "ShipTarget", 
                   |                "ForceAF", "ForceF(F<50%)",
                   |                "NoMissiles", "NoFighters",
                   |                "Range<60%"
                   |                ]   
                   |   
                   |   
                   |   # If set to true, any tags that are not in the tagList that are assigned to a weapon group will pop up as buttons in advanced mode
                   |   ,"allowHotLoadingTags" : true
                   |   
                   |   # Tags to display in simple mode. 
                   |   , "simpleTagList" : [ "PD", "AvoidShields", "TargetShields", "AvdArmor(33%)", "Hold(Flx>90%)", "NoFighters" ]
                   |   
                   |   # Determines which ship modes will be shown in the GUIs. Modes that do not exist will be discarded
                   |   # Allowed Values: "DEFAULT", "LowShields", "ShieldsUp", "Vent(Flx>75%)", "VntA(Flx>25%)", "Run(HP<50%)", "NoSystem", "SpamSystem", "Charge", "ForceAF", "NeverVent", "FarAway", "StayAway"
                   |   # Note that "DEFAULT" is not a real mode but instead a shortcut to disable all other modes. It's kind of deprecated and only still exists for compatibility reasons.
                   |   
                   |   ,"shipModeList" : ["LowShields", "ShieldsUp", "Vent(Flx>75%)", "VntA(Flx>25%)", "Run(HP<50%)", "NoSystem", "SpamSystem", "Charge", "FarAway", "StayAway", "NeverVent"]
                   |   
                   |   # How tags are stored. Valid values are: "Index", "WeaponComposition", "WeaponCompositionGlobal"
                   |   # This affects how tags are persisted. When changing modes, you will have to redo all tags!
                   |
                   |   # [Index]: This is the default behavior. Tags are stored per ship and weapon group index. This is the most precise method, but if you refit a ship, you will need to redo tags lest you have nonsensical tags.
                   |   # [WeaponComposition]: Tags are stored per ship by weapon composition. If you e.g. have a weapon group with railguns and light autocannons on a ship and refit it, such that the weapon group with those weapons gets a new index, you won't have to redo tags. On the other hand, if you e.g. remove the light autocannon, you will have to redo tags, since now the group will be composed of railguns rather than railguns and light autocannons.
                   |   # [WeaponCompositionGlobal]: The same as WeaponComposition, but tags are NOT stored per ship. So if you e.g. set a weapon group containing only PD Lasers to PD, it will affect all your ships that have a weapon group containing only PD Lasers. This storage mode takes the least amount of effort, but also offers the least flexibility."
                   |   ,"tagStorageMode" : "Index"
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
                   |   # Switch this off if you want to reset modes every battle (campaign-GUI only works when enabled)
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
                   |   
                   |   , "maxLoadouts" : 3 # <---- EDIT HERE ----
                   |   , "loadoutNames" : [ "Normal", "Special", "AllDefault" ]
                   |   
                   |   # If you disable this, you will have to use the Load/Save-Buttons to save/load weapon modes
                   |   # This can't be enabled when enablePersistentFireModes is off
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
                   |   ,"targetShields_threshold" : 0.1     # i.e. Attack if target flux < 90% (simplified, see above)
                   |   ,"avoidShields_threshold" : 0.2      # i.e. Attack if target flux > 80% (simplified, see above)
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
                   |   # Vent (Flux>75%)
                   |   ,"vent_flux" : 0.75 # vent if flux level > X
                   |   ,"vent_safetyFactor" : 2.0 # vent only if ship thinks it will survive venting X times (positive non-zero number)
                   |   
                   |   # VentAggressive (Flux>25%)
                   |   ,"aggressiveVent_flux" : 0.25 # vent if flux level > X
                   |   ,"aggressiveVent_safetyFactor" : 0.25 # (positive non-zero number)
                   |   
                   |   ,"retreat_hull" : 0.5 # retreat if hull level < X
                   |   ,"retreat_shouldDirectRetreat" : false
                   |   
                   |   ,"shieldsOff_flux" : 0.5 # In ShieldsOff (Flux>50%) mode, turn off shields if flux level > X
                   |   
                   |   ,"conserveAmmo_ammo" : 0.5 # Start conserving ammo when ammoLevel < X
                   |   ,"conservePDAmmo_ammo" : 0.8 # Only allow firing at fighters and missiles when ammo < X
                   |   
                   |   # If true, the BigShips/SmallShips tags will exclusively target Destroyers and bigger/smaller
                   |   ,"strictBigSmallShipMode" : false
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
                   |   # Sets the flux threshold for "TgtShieldsFT" tag below which all targets are viable, regardless of their shield factor
                   |   ,"targetShieldsAtFT_flux" : 0.2
                   |   
                   |   # Sets the flux threshold for "AvdShieldsFT" tag below which all targets are viable, regardless of their shield factor
                   |   ,"avoidShieldsAtFT_flux" : 0.2
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

    register("create-everything"){
        dependsOn(jar, kotlinSourcesJar, "write-settings-file", "create-metadata-files", "javadocJar")
    }

    // If enabled, will copy your mod to the /mods directory when run (and whenever gradle syncs).
    // Disabled by default, as it is not needed if your mod directory is symlinked into your /mods folder.
    register<Copy>("install-mod") {

        dependsOn(jar)
        dependsOn(kotlinSourcesJar)
        val enabled = false;

        if (!enabled) return@register

        println("Installing mod into Starsector mod folder...")

        val destinations = listOf(modInModsFolder)

        destinations.forEach { dest ->
            copy {
                from(projectDir)
                into(dest)
                exclude(".git", ".github", ".gradle", ".idea", ".run", "gradle")
            }
        }
    }
}

//tasks.test {
//    useJUnitPlatform()
//}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17
