# Advanced Weapon Control - Starsector Mod #

![Cover Image](imgs/agc.png "Cover Image")

This is a utility mod that allows you to set your auto-fire weapon groups to different modes.
For example, in PD-Only mode, weapons will ONLY fire at missiles and fighters, not at enemy ships.
There are many modes, check out the full list of available modes below!

Sections of this readme are roughly ordered by importance. For the most important stuff, stop after Settings.

Note: If you don't have a markdown renderer handy, you can read the online version at 
<https://github.com/DesperatePeter/starsector-advanced-weapon-control/blob/master/README.md>

Also visit the forums post: <https://fractalsoftworks.com/forum/index.php?topic=21280.0>

## TL;DR Instructions ##

- Unzip the archive in your Starsector/mods folder
- Enable the NUMPAD-Numlock on your keyboard ![Numlock](imgs/numlock.png "Numlock")
- Play the game and press NUMPAD-Keys during combat to cycle fire modes
- (optional) after that, add suffixes to that mode by pressing "-"
- Target an ally (R-Key) in combat to cycle their firing modes
- By default, firing modes are automatically saved/loaded between combats (per ship)
- (optional) edit Starsector/mods/AdvancedGunneryControl/Settings.editme and add/remove modes you don't like etc.
- (optional) press "J" in the campaign map to open a GUI

## Installation ##

Simply download the latest release from <https://github.com/DesperatePeter/starsector-advanced-weapon-control/releases> 
and unzip it in your mods folder. 
If you install a new version, please make sure to delete the old folder before doing so.

## Controls ##

Press the NUMPAD Keys 1-7, to cycle between firing modes for that group. **Make sure to enable Num-Lock!**
Press the "-"-Key after 1-7 to cycle suffixes for that group. 

Target an ally ("R"-Key) to instead adjust their modes.

Whenever you cycle modes, you will see a message like this:

```Group 2: [_X__] Missiles (custom AI) 2/3 HoldFire(Flux>90%)```

In order, this lets you know that a) group 2 is in b) the second out of 4 modes, 
c) the current mode is Missiles, c) it's using custom AI when base AI wants to target something else,
d) 2 out of 3 weapons are eligible for that mode (the non-eligible weapon will use mode Default)
and e) (optional) the mode suffix prevents the weapon from firing if ship flux >= 90%.

When you deploy a ship in combat, its last used fire modes will be loaded automatically. You can disable this behaviour
in the settings.

Hotkeys (rebindable in Settings.editme):
- NUMPAD 1-7 - Cycle firing modes for weapon groups 1-7 for targeted or player ship (not rebindable)
- "J" - Show info about current firing modes (and load/save modes if automatic saving/loading is disabled)
- "/" - Reset all modes back to default for current ship (for current loadout)
- "*" - Manually load firing modes for all deployed ships
- "-" - Cycle suffix for the last group you cycled modes for
- "+" - Cycle loadouts for **all** ships
- "J" - Open the Gunnery Control GUI (only campaign mode)

Technical Note: For NUMPAD 1-7, any key that represents the numbers 1 to 7 and isn't used by the base game will work.
So, if you rebind your weapon group keys (to e.g. F1-F7), you should be able to use the normal number keys.
If you bind the numpad numbers as secondary weapon group keys, this mod won't work at all.
If this becomes an issue for you, please let me know, and I will try to implement a solution.

### Gunnery Control GUI ###

If you don't like having to set up your ships firing modes during (simulated) combat, there is also a dialog interface available.
Simply press the "J"-Key while on the campaign map, and the interface will guide you through configuring your
firing modes. Unfortunately, I **can't directly interface with the ship refit screen**, which would be much better,
so this is the best I can do.

### Loadouts ###

You can define (by default 3) different mode loadouts for each ship. You can then cycle through these loadouts for all
ships by pressing the "+"-Key in combat. Doing so will switch all firing modes, suffixes and ship modes
to those defined in the next loadout.

You can configure the number of available loadouts, and their names in the Settings.editme file.

I would recommend leaving one loadout blank (i.e. everything default) for your entire fleet to give you a fallback option.

## Fire Modes ##

Note: Please refer to GUI tooltips for more details

Mode | Targets | Prioritizes | Requirements | Can use Custom AI | Weapon Example | Enabled by Default
:---: | :---   | :---        | :---         | :---:             | :---: | :---:
Default | Same as base AI | Same as base AI | None | No | All weapons | Yes
PD | Fighters/Missiles | Fighters/Missiles | PD Weapon | No | Flak | Yes
Fighters | Fighters | Fighters | None | Yes | Devastator Cannon | Yes
Missiles | Missiles (Mines/Flares) | Missiles | PD Weapon | Yes | Burst PD | Yes 
NoFighters | Anything but Fighters | Same as base AI | None | No | Hellbore Cannon | Yes
Opportunist | Ignores fighters/missiles | Special* | None | Always | Missiles | Yes
BigShips | Destroyers to Capitals | Bigger=Better | None | Yes | Squall MLRM | No
SmallShips | Fighters to Destroyers | Smaller=Better | None | Yes | Phase Lance | No
Mining | Asteroids | Asteroids | None | Yes | Mining Blaster | No
AvoidShields | Ships (no missiles) | ships without shields or high flux | None | Always | High-intensity Laser | Yes
TargetShields | Ships (no missiles) | ships with shields and low flux | None | Always | Needlers | Yes
PD(Flux>50%) | Varies | Same as default when flux < 50%, otherwise same as PD | PD Weapon | No | PD Laser | Yes
PD(Ammo<50%) | Varies | Same as default when ammo > 50%, otherwise same as PD | PD Weapon with ammo | No | Burst PD | No

*Depending on damage type, will try to only fire when the shot is likely to be effective. Will try to avoid
targets that move too fast or are too far away. Mainly intended for missiles with limited ammo.

Note: If a weapon is not eligible for a certain mode, it will use its base AI as a fallback mode

Note: You need to manually add modes that are not enabled by default in the settings

### Mode Suffixes ###

Suffixes modify the behaviour of the selected fire mode in some way. Only one suffix may be applied.

Suffix | Effect
:---: | :---
NONE | None
HoldFire(Flux>90%) | Weapon will hold fire if ship flux >= 90%
HoldFire(Flux>75%) | Weapon will hold fire if ship flux >= 75%
HoldFire(Flux>50%) | Weapon will hold fire if ship flux >= 50%
ConserveAmmo* | Weapon will behave similar to Opportunist mode when ammo < 50%
PanicFire* | When ship hull drops below 50%, this weapon will fire hail mary shots. Useful for _guided_ missiles.

*these suffixes rely on custom AI and will not work well with custom AI disabled. 
They will work best when forcing custom AI in the settings.

### Ship Modes ###

Ship modes only affect AI-controlled ships. Ship modes can only be set in the GUI. Multiple modes can be set.

Mode | Effect | Notes
:---: | :--- | :---
Default | Base game ship AI | -
ForceAutofire | Force deselect all weapon groups and set them to autofire | Combine with HoldFire-suffixes to prevent fluxing out
Retreat(Hull<50%) | Issue retreat command for the ship if hull < 50% | This WILL use a command point
ShieldsOff(Flux>50%) | Force turn off shield at ship flux > 50% | Only recommended for high armor ships with good PD
Vent(Flux>50%) | Force vent at ship flux > 50% | Beware of Harpoons! (Use secondary loadout)

## Settings ##

The settings allow you to configure many aspects of the mod, most prominently: Whether to use custom AI or base AI only
and which fire modes you want to have access to and in which order you want to cycle through them.
There are more settings available, but you can ignore those unless you are feeling adventurous.

Simply open the file ***Settings.editme*** (located in the folder of this mod) in a text editor of your choice 
and modify the lines marked with <---- EDIT HERE ----

Please be careful to adhere to the syntax and allowed values. If your settings file contains errors, the mod will use
the default settings instead! Make sure to check the log (Starsector/starsector.log) if your settings don't apply!

### Enable Custom AI ###

There are three different AI settings:

- If the custom AI is **disabled**, the weapon will use the baseAI to acquire a target. If the target doesn't match
  the mode, the weapon won't fire. (base AI)
- (default) If the custom AI is **enabled**, the weapon will first try the base AI. If the target doesn't 
  match the selected mode, the custom AI will take over. (custom AI)
- If you **force and enable** the custom AI, the weapon will immediately try to acquire a target via custom AI. (override AI)

You should **disable** the custom AI, if:

- You want an experience that is as close to vanilla Starsector as possible
- You absolutely hate it when your weapons occasionally fire at weird stuff (as my algorithm is still undergoing development, though mostly complete)

You should **enable or force-enable** the custom AI, if:

- You want to set weapons to prioritize targets they normally wouldn't (e.g. phase lances as anti-fighter weapons)
- You dislike it when your weapons don't fire even if there is a reasonable target
- You want to be able to customize the AI behaviour (in Settings.editme)
- You want to use advanced modes/suffixes (Opportunist etc.)
- You want to get the "full experience"
- You want to help me improve my custom AI by sending me written reports/video snippets of glitchy weapon behaviour

### Performance Considerations ###

This mod will have a negative effect on performance. That effect will range from barely noticeable to considerable,
depending on the settings. On my machine (which is ~9 years old), the mod generally doesn't have a noticeable impact unless
I go crazy in the settings. Below I will list a few options for improving performance:

- Either enable & force customAI, or disable it (as this prevents the occasional computation of two firing solutions).
- Try not to set every weapon group for every ship to a special fire mode.
- Leave the AI recursion level and friendly fire complexity at 1.
- Consider turning off auto save/load and instead manually save ("J"-Key) and load ("*"-Key).
- Stick to ship mode Default

## How does the mod work? ##

In Starsector, each Weapon has a so-called AutofireAIPlugin. When that weapon is on autofire, this plugin will make the
decision where the weapon should aim and whether it should fire or not.

When you first toggle the autofire mode of a weapon, this mod will extract the original AutofireAIPlugin (AKA the base AI)
from the weapon and store it in a new Plugin called the AdjustableAIPlugin. Additionally, it will create a new Plugin for
every available autofire mode and also store it in the AdjustableAIPlugin. Then, whenever you toggle the autofire mode,
the AdjustableAIPlugin will check whether the plugin corresponding to the selected mode is compatible with the weapon.
If it is compatible, it will set the active Plugin to that plugin. Otherwise, it will simply set the default AutofireAIPlugin as active.

Each Plugin corresponding to an autofire mode also contains a reference to the base Plugin. Each time the plugin has to make
a decision, it first asks the base plugin what it would like to do. If that behaviour is in line with the selected mode,
the plugin will simply let the base AI do its thing. Otherwise, depending on whether customAI is enabled or not, it will
tell the weapon to not fire, or try to come up with its own firing solution.

Similarly, when setting ship AI modes, the mod will replace the base ship AI plugin with a custom plugin that will perform
some actions and then let the base AI take back over.

### Compatibility with other mods ###

This mod should be compatible with other mods that provide custom AIs for their weapons, as long as they don't try to
manipulate the weapon AI mid-combat. This mod will simply use the custom AI of that weapon as the base AI.

If you are a mod-author and want to explicitly tell my mod to not tweak the AI of your weapon(s), include the weapon id
into your mod's modSettings.json:

```
{
  "AdvancedGunneryControl": {
    "weaponBlacklist": [
      "weapon_id_1", "weapon_id_2"
    ]
  }
}
```

This mod doesn't affect anything outside of combat, so it's very unlikely to cause problems on the campaign level.

## Roadmap ##

To me, the mod feels pretty feature complete. I feel like the mod already does enough stuff and adding more features 
(e.g. fighter controls) would make the mod too big.
I will still try to fix reported bugs and maybe do some usability/control improvements.

## Known Issues ##

- Versions before 0.8.2 saved custom classes as persistent data, meaning it was not possible to remove the mod.

## Changelog ## 

- 0.1.0: Initial release
- 0.2.0: Added IgnoreFighters mode, added custom AI, added settings
- 0.2.1: Significant improvements to custom AI, including settings for custom AI
- 0.3.0: further improvements to custom AI (friendly fire), added weapon blacklist
- 0.3.1: minor polish and bugfixes
- 0.4.0: Experimental support for setting allied ship's fire modes, UI Settings
- 0.4.1: bugfix (allied ships were still referencing the player ship weapons), setting for info hotkey
- 0.5.0-ALPHA: fire modes are now exclusively stored on a per-ship-basis, meaning the data can be saved/loaded between combats
- 0.5.1: fire mode settings are now saved between saving/reloading, several bugfixes.
- 0.5.2: fixed bug where customAIFriendlyFireCaution had a much higher effect than intended
  changed it so that pressing J-Key is no longer required to initialize other ships.
- 0.5.3: In accordance with Wisp, removed dependency on Questgiver lib, 
  as that might cause compatibility issues with PerseanChronicles.
  fixed issue where refitting could cause weird behaviour (hopefully)
- 0.6.0: added 3 new fire modes, added reset function, fixed blacklist-bug, fixed several AI bugs, fixed issues with persistent storage
- 0.7.0: automatically load fire modes for all ships on combat start (opt-out in settings), 
  no longer need to be in Command UI to set friendly modes,  added hotkey to load fire modes for all ships
- 0.7.1: fix issue with reset key, adjusted readme
- 0.7.2: IPDAI is now considered for PD/Missiles mode, invalid modes are now skipped (opt-out in settings)
- 0.8.0 (pre-release): added mode suffixes, added gunnery control GUI
- 0.8.1 (pre-release): cleaned up GUI, display weapon mode suffixes
- 0.8.2: add ability to cycle suffixes during combat, fix issues with persistence and save game corruption
- 0.8.3: minor polish
- 0.8.4: pre-deployed ships (e.g. when trying to disengage) will now also automatically load firing modes
- 0.8.5: automatically purge incompatible persistent data, changed GUI hotkey to "J" and made it rebindable
- 0.9.0 (pre-release): Added loadouts, ship modes & additional modes/suffixes
- 0.9.1 (pre-release): Fixed an issue that could lead to a nullptr-exception
- 0.9.2: Fixed issues (suffixes didn't properly affect targeting priority, ship modes got occasionally reset),
  added additional ship modes.
- 0.10.1: Added targetShields/avoidShields modes, possibility to activate multiple ship AI modes, reworked opportunist, 
  added mode parameters to options
- 0.10.2: Yet another Opportunist overhaul (now less conservative for weapons with unlimited ammo, new setting), minor bugfixes
- 0.10.4: Fixed issues with friendly fire, fixed several issues with target acquisition, turrets now assume a neutral position
- 0.11.0: Reworked Target/AvoidShields, reworked most of the custom AI to fix some issues (improved aim, friendly fire, target prio)
- 0.12.0: Converted PD(Flux>50%) and PD(Ammo<50%) to fire modes, the latter disabled by default. GUI polish. 
  Added suggested weapon modes (prototypical). Added setting for strict/lenient BigShip/SmallShip modes.
- 0.12.1: Add variant mode copying, help hotkey, additional settings (technically, this should have been 0.13.0)
- 0.13.0: add noPD mode (disabled by default), polish

## Acknowledgements ##

Many thanks to Wisp(borne) for answering my endless questions about Kotlin and Starsector modding and for providing
an awesome repository template.

Thanks to LazyWizard for providing the LazyLib.

Thanks to stormbringer951 for inspiring me to create this mod by creating his mod Weapons Group Controls.

Last but not least: Thanks to everyone using this mod and giving me feedback!

## Support me ##

If you'd like to review my code and give me some hints what I could improve, please do! Also, feel free to create PRs!

If you happen to know how to make good videos, I'd very much appreciate if you could make a nice video showcasing the features
of this mod. If you came here from the Starsector mod forum, you know why I'm asking for this xD

As you might know, writing the code is the easy part. Making sure that it works properly is where the challenge lies.
I'm grateful for any help with testing this mod.

Do you have an idea for a cool new firing mode? Please feel free to contribute them!
Just follow the following steps:
- Create XyzAI class that inherits from (extends) AdjustableAIPlugin
- Extend FireMode.kt such that your fire mode appears in all relevant fields
- Add to readme-table
- Add to Settings.editme allowed-values comment (please refrain from adding to default list)
- Test that the mode works as intended!

On the off-chance that you want to support me financially, please don't :P 
