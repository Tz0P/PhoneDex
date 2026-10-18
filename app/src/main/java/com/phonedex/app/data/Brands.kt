package com.phonedex.app.data

object Brands {
    val ALL: List<String> = listOf(
        "Samsung", "Apple", "Google", "OnePlus", "Xiaomi", "Redmi", "POCO", "Black Shark", "Huawei", "HONOR",
        "Sony", "Nothing", "CMF", "Motorola", "Lenovo", "OPPO", "vivo", "iQOO", "realme", "ASUS", "ROG",
        "Nokia", "HMD", "Fairphone", "TCL", "ZTE", "nubia", "RedMagic", "Meizu", "Infinix", "TECNO", "itel",
        "LG", "HTC", "Sharp", "Fujitsu", "Kyocera", "Panasonic", "Casio", "Alcatel", "Wiko", "BQ", "Gigaset",
        "Archos", "Energizer", "Vertu", "Essential", "Razer", "Light", "Punkt", "Teracube", "Murena", "Jolla",
        "Purism", "Pine64", "Unihertz", "Ricoh", "Hisense", "Coolpad", "Gionee", "Smartisan", "Meitu",
        "BlackBerry", "Palm", "Sonim", "Cat", "AGM", "Blackview", "Ulefone", "Doogee", "Oukitel", "Cubot",
        "UMIDIGI", "HOTWAV", "Fossibot", "Oscal", "Elephone", "Leagoo", "Vernee", "Bluboo", "HomTom",
        "Lava", "Micromax", "Karbonn", "Intex", "Celkon", "XOLO", "Spice", "iBall", "Jio", "Walton",
        "Symphony", "Condor", "Vestel", "General Mobile", "Reeder", "Highscreen", "Vsmart", "Nuu", "BLU",
        "Orbic", "Plum", "Maxwest", "Yezz", "Wileyfox", "Tambo", "Freedom", "Ayaneo",
        "Mudita", "Bittium", "Shift", "Gresso", "Hammer", "Crosscall", "Ruggear", "Microsoft"
    ).distinctBy { it.lowercase() }.sortedBy { it.lowercase() }
}
