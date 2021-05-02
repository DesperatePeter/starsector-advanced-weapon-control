# Advanced Weapon Control - Starsector Mod #

![Cover Image](imgs/agc.png "Cover Image")

This is a utility mod, that allows you to set your (auto-fire) weapon groups to different modes.
For example, in PD-Only mode, weapons will ONLY fire at missiles and fighters, not at enemy ships.
This is especially useful for e.g. Bust PD Lasers, to not waste charges to deal soft-flux to enemy shields.
Check out the list of available modes below!

Note: If you don't have a markdown renderer handy, you can read the online version at 
<https://github.com/DesperatePeter/starsector-advanced-weapon-control/blob/master/README.md>

Also visit the forums post: <https://fractalsoftworks.com/forum/index.php?topic=21280.0>

## TL;DR Instructions ##

- Unzip the archive in your Starsector/mods folder
- Enable the NUMPAD-Numlock on your keyboard
- Play the game and press NUMPAD-Keys during combat to cycle fire modes
- (optional) edit Starsector/mods/AdvancedGunneryControl/Settings.editme and remove modes you don't like etc.

## Installation ##

Simply download the release from <https://github.com/DesperatePeter/starsector-advanced-weapon-control/releases> and unzip it in your mods folder. If you install a new version, please make sure to
delete the old folder before doing so.

## Controls ##

Press #, where # is the Weapon Group Number on the NUMPAD, to cycle between firing modes for that group. **Make sure to enable Num-Lock!**

Whenever you cycle modes, you will see a message like this:

```Group 2: [_X__] Missiles (custom AI) 2/3```

In order, this let's you know that a) group 2 is in b) the second out of 4 modes, 
c) the current mode is Missiles, c) it's using custom AI when base AI wants to target something else
and d) 2 out of 3 weapons are eligible for that mode (the non-eligible weapon will use mode Default).

Press the J-Key (re-bindable in Settings.editme, saveLoadInfoHotkey) to see complete info about current firing modes.
Pressing this key will also store the current firing modes for the ship. When you first press the J-Key or try to cycle
weapon modes during the next combat, it will load the stored fire modes for you. (you can opt-out in the settings)

Technical Note: Any key that represents the numbers 1 to 7 and isn't used by the base game will work.
So, if you rebind your weapon group keys (to e.g. F1-F7), you should be able to use the normal number keys.
If you bind the numpad numbers as secondary weapon group keys, this mod won't work at all.
If this becomes an issue for you, please let me know, and I will try to implement a solution.

### Setting fire modes for allied ships (experimental) ###

Note: You will have to unbind the NUMPAD2, NUMPAD4 and NUMPAD6 keys under the section Fleet Command in the 
Starsector Settings Menu to use this feature properly.

While in combat, target an allied ship (hover over it with your mouse cursor and press the R-Key).
Open the Command UI (TAB-Key) and press the J-Key. This will display information about the target's weapon groups.

While you are in the Command UI and have that ship targeted, you can modify that ship's weapon groups in the same way you
can normally modify your own.

I opted to limit this feature to the Command UI only, as I want to prevent this from happening accidentally.

## Fire Modes ##

Mode | Targets | Prioritizes | Requirements | Can use Custom AI | Weapon Example | Enabled by Default
:---: | :---   | :---        | :---         | :---:             | :---: | :---:
Default | Same as base AI | Same as base AI | None | No | All weapons | Yes
PD | Fighters/Missiles | Fighters/Missiles | PD Weapon | No | Flak | Yes
Fighters | Fighters | Fighters | None | Yes | Devastator Cannon | Yes
Missiles | Missiles (Mines/Flares) | Missiles | PD Weapon | Yes | Burst PD | Yes 
NoFighters | Anything but Fighters | Same as base AI | None | No | Hellbore Cannon | Yes
BigShips | Destroyers to Capitals | Bigger=Better | None | Yes | Squall MLRM | No
SmallShips | Fighters to Destroyers | Smaller=Better | None | Yes | Phase Lance | No
Mining | Asteroids | Asteroids | None | Yes | Mining Blaster | No

Note: If a weapon is not eligible for a certain mode, it will use its base AI as a fallback mode

Note: You need to manually add modes that are not enabled by default in the settings

## Settings ##

The settings allow you to configure many aspects of the mod, most prominently: Whether to use custom AI or base AI only
and which fire modes you want to have access to and in which order you want to cycle through them.
There are more settings available, but you can ignore those unless you are feeling adventurous.

Simply open the file ***Settings.editme*** (located in the folder of this mod) in a text editor of your choice and modify the lines marked with <---- EDIT HERE ----

Please be careful to adhere to the syntax and allowed values. If your settings file contains errors, the mod will use
the default settings instead! Make sure to check the log (Starsector/starsector.log) if your settings don't apply!

### Weapon Mode Cycle Order ###

There are a lot of available firing modes in this mod. Especially if you want to switch weapon modes in the heat of battle,
having to cycle through 5 different weapon modes can be a bit unwieldy.

In order to remedy that problem, you can customize which modes you want to use.
Please note that the mode "Default" (i.e. same behaviour as without this mod) will always be the first mode.
When you press the #-key corresponding to a weapon group, it will select the next mode in the list. After the last mode,
it goes back to the first mode (i.e. "Default").

If you find the number of options overwhelming, try the following setting:

```"cycleOrder" : ["PD"]```

### Enable Custom AI ###

Whether you enable the custom AI or not, all weapon fire modes will first attempt to use the base AI 
(i.e. the AI that the weapon has without this mod) to select a target and firing solution, unless you use the forceCustomAI setting.
Only when the target selected by the base AI does not match the type specified by the weapon mode, will there be a difference.

If the custom AI is **disabled**, the weapon simply won't fire at all.

If the custom AI is **enabled**, the weapon will try to acquire a new target of the chosen type and a fitting firing solution,
based on an algorithm designed by me.

You should **disable** the custom AI, if:

- You have performance issues (as enabling it might have to calculate two firing solutions)
- You want an experience that is as close to vanilla Starsector as possible
- You absolutely hate it when your weapons occasionally fire at weird stuff (as my algorithm is still undergoing development, though mostly complete)

You should **enable** the custom AI, if:

- You want to set weapons to prioritize targets they normally wouldn't (e.g. phase lances as anti-fighter weapons)
- You dislike it when your weapons don't fire even if there is a reasonable target
- You want to be able to customize the AI behaviour (in Settings.editme)
- You want to get the "full experience"
- You want to use Fighter/Missile-only modes
- You want to help me improve my custom AI by sending me written reports/video snippets of glitchy weapon behaviour

Note: Weapon groups will display "(base AI)" when custom AI is off/not applicable, "(custom AI)" when using the
custom AI as a fallback and "(override AI)" when using only the custom AI.

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

### Compatibility with other mods ###

This mod should be compatible with other mods that provide custom AIs for their weapons, as long as they don't try to
manipulate the weapon AI mid-combat. This mod will simply use the custom AI of that weapon as the base AI for that weapon.

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

This mod doesn't affect anything outside the current combat. So it's unlikely to cause problems on the campaign level.

## Roadmap ##

This is still an early version. It has not yet been tested sufficiently. If you encounter any bugs/crashes,
please let me know.

After some more testing, bug-fixing and implementing requested features,
I would like to add a feature where you can merge other weapon groups into your active weapon group.
Update: 0.95a-RC16 will probably fix the issue that was blocking this feature.

After that I want to implement some more fringe fire modes, that will be disabled by default. I was thinking about an
anti-small-craft (fighters and frigates) mode and a mining (target only asteroids for mining blaster roleplay) mode.

### Requested Features ###

- Add IgnoreFighters mode **DONE**
- Add ability to issue fire modes to AI-controlled allied ships **DONE**
- Add weapon-blacklist for other mods such that their weapons stay unaffected by fire modes from this mod **DONE**

## Known Issues ##

- Depending on the machine/OS(?), keyboard inputs will not be accepted while holding SHIFT-Key.

If you happen to have any clues to fixing these issues, please let me know.

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
- 0.5.4: fixed blacklist-bug, 

## Acknowledgements ##

Many thanks to Wisp for answering my endless questions about Kotlin and Starsector modding and for providing
an awesome repository template.

Thanks to LazyWizard for providing the LazyLib.

Thanks to stormbringer951 for inspiring me to create this mod by creating his mod Weapons Group Controls.

Last but not least: Thanks to everyone using my mod and giving me feedback!

## Support me ##

If you'd like to review my code and give me some hints what I could improve, please do! Also, feel free to create PRs!

If you happen to know how to make good videos, I'd very much appreciate if you could make a nice video showcasing the features
of this mod. If you came here from the Starsecor mod forum, you know why I'm asking for this xD

As you might know, writing the code is the easy part. Making sure that it works properly is where the challenge lies.
I'm grateful for any help with testing this mod. If you're willing to test unstable versions, while I'm working on the mode,
I usually push to the current feature-branch somewhat regularly. So feel free to check out/download the latest commit 
(just copy & paste the entire repository into a folder called AdvancedGunneryControl in your mods folder)
and give me life feedback (you can DM me on Discord @Jannes#9184)

Do you have an idea for a cool new firing mode? Please feel free to contribute them!
Just follow the following steps:
- Create XyzAI class that inherits from (extends) AdustableAIPlugin
- Extend FireMode.kt such that your fire mode appears in all relevant fields
- Add to readme-table
- Add to Settings.editme allowed-values comment (please refrain from adding to default list)
- Test that the mode works as intended!

On the off-chance that you want to support me financially, please don't :P My day-job as an engineer
pays enough to cover my living expenses.

I believe there are better places where you can donate your money to, check out <https://fractalsoftworks.com/forum/index.php?topic=19739.0> for instance