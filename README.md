# Advanced Weapon Control - Starsector Mod #

![Cover Image](imgs/agc.png "Cover Image")

This is a basic utility mod, that allows you to set your (auto-fire) weapon groups to different modes.
For example, in PD-Only mode, weapons will ONLY fire at missiles and fighters, not at enemy ships.
This is especially useful for e.g. Bust PD Lasers, to not waste charges to deal soft-flux to enemy shields.

Note: If you don't have a markdown renderer handy, you can read the online version at <https://github.com/DesperatePeter/starsector-advanced-weapon-control/blob/master/README.md>

## Installation ##

Simply download the release from <https://github.com/DesperatePeter/starsector-advanced-weapon-control/releases> and unzip it in your mods folder. If you install a new version, please make sure to
delete the old folder before doing so.

## Controls ##

Press #, where # is the Weapon Group Number on the NUMPAD, to cycle between firing modes for that group. **Make sure to enable Num-Lock!**

Technical Note: Any key that represents the numbers 1 to 7 and isn't used by the base game will work.
So, if you rebind your weapon group keys (to e.g. F1-F7), you should be able to use the normal number keys.
If you bind the numpad numbers as secondary weapon group keys, this mod won't work at all.
If this becomes an issue for you, please let me know, and I will try to implement a solution.

## Fire Modes ##

Mode | Description | Notes | Affected by Custom AI | Suitable Weapon Example
:---: | :--- | :--- | :---: | :---
Default | Behaves exactly like normal | - | No | All weapons
PD | Weapons will ONLY fire at missiles and fighters. | Will only work for PD-weapons | No | Flak
Missiles | Weapons will ONLY fire at missiles/mines | Will only work for PD-weapons | Yes | Burst PD
Fighters | Weapons will ONLY fire at fighters | - | Yes | Devastator Cannon
NoFighters | Weapon will NOT fire at fighters | Will otherwise work normally | No | Hellbore Cannon

Note: If a weapon is not eligible for a certain mode, it will use its vanilla AI as a fallback mode

## Settings ##

Currently, the settings allow you to configure two aspects of the mod: Whether to use custom AI or vanilla AI only
and which fire modes you want to have access to and in which order you want to cycle through them.

Simply open the file ***Settings.editme*** (located in the folder of this mod) in a text editor of your choice and modify the lines marked with <---- EDIT HERE ----

Please be careful to adhere to the syntax and allowed values. If your settings file contains errors, the mod will use
the default settings instead! Make sure to check the log (Starsector/starsector.log) if your settings don't apply!

### Weapon Mode Cycle Order ###

There are a lot of available firing modes in this mod. Especially if you want to switch weapon modes in the heat of battle,
having to cycle through 6 different weapon modes can be a bit unwieldy.

In order to remedy that problem, you can customize which modes you want to use.
Please note that the mode "Default" (i.e. same behaviour as without this mod) will always be the first mode.
When you press the #-key corresponding to a weapon group, it will select the next mode in the list. After the last mode,
it goes back to the first mode (i.e. "Default").

If you find the number of options overwhelming, try the following setting:

```"cycleOrder" : ["PD"]```

### Enable Custom AI ###

Whether you enable the custom AI or not, all weapon fire modes will first attempt to use the vanilla AI 
(i.e. the AI that the weapon has without this mod) to select a target and firing solution.
Only when the target selected by the vanilla AI does not match the type specified by the weapon mode, will there be a difference.

If the custom AI is **disabled**, the weapon simply won't fire at all.

If the custom AI is **enabled**, the weapon will try to acquire a new target of the chosen type and a fitting firing solution,
based on an algorithm designed by me.

You should **disable** the custom AI, if:

- You have performance issues (as enabling it might have to calculate two firing solutions)
- You want an experience that is as close to vanilla Starsector as possible
- You absolutely hate it when your weapons occasionally fire at weird stuff (as my algorithm is still relatively unrefined)

You should **enable** the custom AI, if:

- You want to set weapons to prioritize targets they normally wouldn't (e.g. phase lances as anti-fighter weapons)
- You hate it when your weapons don't fire even if there is a reasonable target
- You want to help me improve my custom AI by sending me written reports/video snippets of glitchy weapon behaviour

## How does the mod work? ##

In Starsector, each Weapon has a so-called AutofireAIPlugin. When that weapon is on autofire, this plugin will make the
decision where the weapon should aim and whether it should fire or not.

When you first toggle the autofire mode of a weapon, this mod will extract the original AutofireAIPlugin (AKA the base/vanilla AI)
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

## Roadmap ##

This is still an early alpha version. It has not yet been tested sufficiently. If you encounter any bugs/crashes,
please let me know.

After some more testing, bug-fixing and implementing requested features,
I would like to add a feature where you can merge other weapon groups into your active weapon group.

### Requested Features ###

- Add IgnoreFighters mode **DONE**
- Add ability to issue fire modes to AI-controlled allied ships
- Add weapon-blacklist for other mods such that their weapons stay unaffected by fire modes from this mod 

## Known Issues ##

- Depending on the machine/OS(?), keyboard inputs will not be accepted while holding SHIFT-Key.
- Depending on the machine/OS(?), non-existent weapon groups will still toggle their mode (with no effect)

If you happen to have any clues to fixing these issues, please let me know.

## Changelog ## 

- 0.1.0: Initial release
- 0.2.0: Added IgnoreFighters mode, added custom AI, added settings

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
(just copy & paste the entire repository into your mods folder) and give me life feedback (you can DM me on Discord @Jannes#9184)
