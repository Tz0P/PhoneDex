# Brand logo files

Put 128×128 PNGs (transparent background) in `app/src/main/res/drawable-nodpi/`.
Name = lowercase brand name; spaces and symbols become `_` (Android resource rules: a-z, 0-9, `_`).
Missing files are fine: the brand's initial is shown instead.

| Brand | File |
|---|---|
| AGM | `agm.png` |
| Alcatel | `alcatel.png` |
| Apple | `apple.png` |
| Archos | `archos.png` |
| ASUS | `asus.png` |
| Ayaneo | `ayaneo.png` |
| Bittium | `bittium.png` |
| Black Shark | `black_shark.png` |
| BlackBerry | `blackberry.png` |
| Blackview | `blackview.png` |
| BLU | `blu.png` |
| Bluboo | `bluboo.png` |
| BQ | `bq.png` |
| Casio | `casio.png` |
| Cat | `cat.png` |
| Celkon | `celkon.png` |
| CMF | `cmf.png` |
| Condor | `condor.png` |
| Coolpad | `coolpad.png` |
| Crosscall | `crosscall.png` |
| Cubot | `cubot.png` |
| Doogee | `doogee.png` |
| Elephone | `elephone.png` |
| Energizer | `energizer.png` |
| Essential | `essential.png` |
| Fairphone | `fairphone.png` |
| Fossibot | `fossibot.png` |
| Freedom | `freedom.png` |
| Fujitsu | `fujitsu.png` |
| General Mobile | `general_mobile.png` |
| Gigaset | `gigaset.png` |
| Gionee | `gionee.png` |
| Google | `google.png` |
| Gresso | `gresso.png` |
| Hammer | `hammer.png` |
| Highscreen | `highscreen.png` |
| Hisense | `hisense.png` |
| HMD | `hmd.png` |
| HomTom | `homtom.png` |
| HONOR | `honor.png` |
| HOTWAV | `hotwav.png` |
| HTC | `htc.png` |
| Huawei | `huawei.png` |
| iBall | `iball.png` |
| Infinix | `infinix.png` |
| Intex | `intex.png` |
| iQOO | `iqoo.png` |
| itel | `itel.png` |
| Jio | `jio.png` |
| Jolla | `jolla.png` |
| Karbonn | `karbonn.png` |
| Kyocera | `kyocera.png` |
| Lava | `lava.png` |
| Leagoo | `leagoo.png` |
| Lenovo | `lenovo.png` |
| LG | `lg.png` |
| Light | `light.png` |
| Maxwest | `maxwest.png` |
| Meitu | `meitu.png` |
| Meizu | `meizu.png` |
| Micromax | `micromax.png` |
| Microsoft | `microsoft.png` |
| Motorola | `motorola.png` |
| Mudita | `mudita.png` |
| Murena | `murena.png` |
| Nokia | `nokia.png` |
| Nothing | `nothing.png` |
| nubia | `nubia.png` |
| Nuu | `nuu.png` |
| OnePlus | `oneplus.png` |
| OPPO | `oppo.png` |
| Orbic | `orbic.png` |
| Oscal | `oscal.png` |
| Oukitel | `oukitel.png` |
| Palm | `palm.png` |
| Panasonic | `panasonic.png` |
| Pine64 | `pine64.png` |
| Plum | `plum.png` |
| POCO | `poco.png` |
| Punkt | `punkt.png` |
| Purism | `purism.png` |
| Razer | `razer.png` |
| realme | `realme.png` |
| RedMagic | `redmagic.png` |
| Redmi | `redmi.png` |
| Reeder | `reeder.png` |
| Ricoh | `ricoh.png` |
| ROG | `rog.png` |
| Ruggear | `ruggear.png` |
| Samsung | `samsung.png` |
| Sharp | `sharp.png` |
| Shift | `shift.png` |
| Smartisan | `smartisan.png` |
| Sonim | `sonim.png` |
| Sony | `sony.png` |
| Spice | `spice.png` |
| Symphony | `symphony.png` |
| Tambo | `tambo.png` |
| TCL | `tcl.png` |
| TECNO | `tecno.png` |
| Teracube | `teracube.png` |
| Ulefone | `ulefone.png` |
| UMIDIGI | `umidigi.png` |
| Unihertz | `unihertz.png` |
| Vernee | `vernee.png` |
| Vertu | `vertu.png` |
| Vestel | `vestel.png` |
| vivo | `vivo.png` |
| Vsmart | `vsmart.png` |
| Walton | `walton.png` |
| Wiko | `wiko.png` |
| Wileyfox | `wileyfox.png` |
| Xiaomi | `xiaomi.png` |
| XOLO | `xolo.png` |
| Yezz | `yezz.png` |
| ZTE | `zte.png` |

Custom brands you add in the app work the same way, e.g. `Foo Bar` -> `foo_bar.png`.
If you ever turn on resource shrinking for release builds, keep the logos with a `res/raw/keep.xml`: `<resources xmlns:tools="http://schemas.android.com/tools" tools:keep="@drawable/*" />`.
