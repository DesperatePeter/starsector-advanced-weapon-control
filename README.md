# Advanced Weapon Control - Starsector Mod

This is a basic utility mod, that allows you to set (auto-fire) weapon groups to different modes.
In PD-Only mode, weapons will ONLY fire at missiles and fighters, not at enemy ships.
This is especially useful for e.g. Bust PD Lasers, to not waste charges to deal soft-flux to enemy shields.

## Controls

Press #, where # is the Weapon Group Number on the NUMPAD to cycle between modes. Make sure to enable Num-Lock!

Technical Note: Any key that represents the numbers 1 to 7 and isn't used by the base game will work.
So, if you rebind your weapon group keys (to e.g. F1-F7), you should be able to use the normal number keys.
If you bind the numpad numbers as secondary weapon group keys, this mod won't work at all.

## Fire Modes

Mode | Description | Notes | Weapon Example
:---: | :--- | :--- | ---
Default | Behaves exactly like vanilla Starsector auto-fire | - | -
PD | Weapons will ONLY fire at missiles and fighters. | Weapons still acquire targets normally, but only fire when a missile/fighter is targeted | Flak
Missile (experimental) | Weapons will ONLY fire at missiles | Weapons still acquire targets normally, but only fire when a missile is targeted | Burst PD
Fighter (experimental) | Weapons will ONLY fire at fighters | Weapons still acquire targets normally, but only fire when a fighter is targeted | Devastator Cannon

## Roadmap

This is still a very early alpha version. The code is still very messy, and it has barely been tested yet!
First off I need to clean up a little and add version checker support and automatic releases.

After that, I would like to add a feature where you can merge other weapon groups into your active weapon group.

## Acknowledgements

Many thanks to Wisp for answering my endless questions about Kotlin and Starsector modding and for providing
an awesome repository template.

Thanks to LazyWizard for providing the LazyLib.

Thanks to stormbringer951 for inspiring me to create this mod by creating his mod Weapons Group Controls.