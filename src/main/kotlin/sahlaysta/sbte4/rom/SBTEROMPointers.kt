package sahlaysta.sbte4.rom

internal class SBTEStringPointerGroup(val address: Int, val count: Int,
                                      val language: SBTEStringLanguage, val description: SBTEStringDescription)

internal val usROMStringPointers = arrayOf(

    //JAPANESE
    SBTEStringPointerGroup(0xEDDAA0, 2299, SBTEStringLanguage.JAPANESE, SBTEStringDescription.STORY_MODE),
    SBTEStringPointerGroup(0xED9758, 309,  SBTEStringLanguage.JAPANESE, SBTEStringDescription.EMERL_CARD_DESCRIPTIONS),
    SBTEStringPointerGroup(0xEDCD38, 17,   SBTEStringLanguage.JAPANESE, SBTEStringDescription.OPTIONS_MENU),
    SBTEStringPointerGroup(0xEDB9AC, 8,    SBTEStringLanguage.JAPANESE, SBTEStringDescription.BATTLE_MENU),
    SBTEStringPointerGroup(0xEDBED4, 35,   SBTEStringLanguage.JAPANESE, SBTEStringDescription.BATTLE_RULES_MENU),
    SBTEStringPointerGroup(0xEDD370, 16,   SBTEStringLanguage.JAPANESE, SBTEStringDescription.TRAINING_MODE_MENU),
    SBTEStringPointerGroup(0xEDC3BC, 9,    SBTEStringLanguage.JAPANESE, SBTEStringDescription.MINIGAME_MENU),
    SBTEStringPointerGroup(0xEDCF30, 40,   SBTEStringLanguage.JAPANESE, SBTEStringDescription.BATTLE_RECORD_MENU),
    SBTEStringPointerGroup(0xEDC9B8, 6,    SBTEStringLanguage.JAPANESE, SBTEStringDescription.CAPTURED_SKILL),
    SBTEStringPointerGroup(0xEDD5B0, 8,    SBTEStringLanguage.JAPANESE, SBTEStringDescription.STORY_MODE_MENU),
    SBTEStringPointerGroup(0xED8C18, 309,  SBTEStringLanguage.JAPANESE, SBTEStringDescription.EMERL_SKILLS),
    SBTEStringPointerGroup(0xED95C0, 16,   SBTEStringLanguage.JAPANESE, SBTEStringDescription.MULTIPLAYER_MESSAGES),
    SBTEStringPointerGroup(0xEDD73C, 51,   SBTEStringLanguage.JAPANESE, SBTEStringDescription.STORY_MODE_AREA_NAMES),

    //ENGLISH
    SBTEStringPointerGroup(0xEDFE8C, 2299, SBTEStringLanguage.ENGLISH,  SBTEStringDescription.STORY_MODE),
    SBTEStringPointerGroup(0xED9C2C, 309,  SBTEStringLanguage.ENGLISH,  SBTEStringDescription.EMERL_CARD_DESCRIPTIONS),
    SBTEStringPointerGroup(0xEDCD7C, 17,   SBTEStringLanguage.ENGLISH,  SBTEStringDescription.OPTIONS_MENU),
    SBTEStringPointerGroup(0xEDB9CC, 8,    SBTEStringLanguage.ENGLISH,  SBTEStringDescription.BATTLE_MENU),
    SBTEStringPointerGroup(0xEDBF60, 35,   SBTEStringLanguage.ENGLISH,  SBTEStringDescription.BATTLE_RULES_MENU),
    SBTEStringPointerGroup(0xEDD3B0, 16,   SBTEStringLanguage.ENGLISH,  SBTEStringDescription.TRAINING_MODE_MENU),
    SBTEStringPointerGroup(0xEDC3E0, 9,    SBTEStringLanguage.ENGLISH,  SBTEStringDescription.MINIGAME_MENU),
    SBTEStringPointerGroup(0xEDCFD0, 40,   SBTEStringLanguage.ENGLISH,  SBTEStringDescription.BATTLE_RECORD_MENU),
    SBTEStringPointerGroup(0xEDC9D0, 7,    SBTEStringLanguage.ENGLISH,  SBTEStringDescription.CAPTURED_SKILL),
    SBTEStringPointerGroup(0xEDD5D0, 8,    SBTEStringLanguage.ENGLISH,  SBTEStringDescription.STORY_MODE_MENU),
    SBTEStringPointerGroup(0xED90EC, 309,  SBTEStringLanguage.ENGLISH,  SBTEStringDescription.EMERL_SKILLS),
    SBTEStringPointerGroup(0xED9600, 16,   SBTEStringLanguage.ENGLISH,  SBTEStringDescription.MULTIPLAYER_MESSAGES),
    SBTEStringPointerGroup(0xEDD670, 51,   SBTEStringLanguage.ENGLISH,  SBTEStringDescription.STORY_MODE_AREA_NAMES),

    //FRENCH
    SBTEStringPointerGroup(0xEE2278, 2299, SBTEStringLanguage.FRENCH,   SBTEStringDescription.STORY_MODE),
    SBTEStringPointerGroup(0xEDA100, 309,  SBTEStringLanguage.FRENCH,   SBTEStringDescription.EMERL_CARD_DESCRIPTIONS),
    SBTEStringPointerGroup(0xEDCDC0, 17,   SBTEStringLanguage.FRENCH,   SBTEStringDescription.OPTIONS_MENU),
    SBTEStringPointerGroup(0xEDB9EC, 8,    SBTEStringLanguage.FRENCH,   SBTEStringDescription.BATTLE_MENU),
    SBTEStringPointerGroup(0xEDBFEC, 35,   SBTEStringLanguage.FRENCH,   SBTEStringDescription.BATTLE_RULES_MENU),
    SBTEStringPointerGroup(0xEDD3F0, 16,   SBTEStringLanguage.FRENCH,   SBTEStringDescription.TRAINING_MODE_MENU),
    SBTEStringPointerGroup(0xEDC404, 9,    SBTEStringLanguage.FRENCH,   SBTEStringDescription.MINIGAME_MENU),
    SBTEStringPointerGroup(0xEDD070, 40,   SBTEStringLanguage.FRENCH,   SBTEStringDescription.BATTLE_RECORD_MENU),
    SBTEStringPointerGroup(0xEDC9EC, 6,    SBTEStringLanguage.FRENCH,   SBTEStringDescription.CAPTURED_SKILL),
    SBTEStringPointerGroup(0xEDD5F0, 8,    SBTEStringLanguage.FRENCH,   SBTEStringDescription.STORY_MODE_MENU),
    SBTEStringPointerGroup(0xED9640, 16,   SBTEStringLanguage.FRENCH,   SBTEStringDescription.MULTIPLAYER_MESSAGES),

    //GERMAN
    SBTEStringPointerGroup(0xEE4664, 2299, SBTEStringLanguage.GERMAN,   SBTEStringDescription.STORY_MODE),
    SBTEStringPointerGroup(0xEDA5D4, 309,  SBTEStringLanguage.GERMAN,   SBTEStringDescription.EMERL_CARD_DESCRIPTIONS),
    SBTEStringPointerGroup(0xEDCE04, 17,   SBTEStringLanguage.GERMAN,   SBTEStringDescription.OPTIONS_MENU),
    SBTEStringPointerGroup(0xEDBA0C, 8,    SBTEStringLanguage.GERMAN,   SBTEStringDescription.BATTLE_MENU),
    SBTEStringPointerGroup(0xEDC078, 35,   SBTEStringLanguage.GERMAN,   SBTEStringDescription.BATTLE_RULES_MENU),
    SBTEStringPointerGroup(0xEDD430, 16,   SBTEStringLanguage.GERMAN,   SBTEStringDescription.TRAINING_MODE_MENU),
    SBTEStringPointerGroup(0xEDC428, 9,    SBTEStringLanguage.GERMAN,   SBTEStringDescription.MINIGAME_MENU),
    SBTEStringPointerGroup(0xEDD110, 40,   SBTEStringLanguage.GERMAN,   SBTEStringDescription.BATTLE_RECORD_MENU),
    SBTEStringPointerGroup(0xEDCA04, 6,    SBTEStringLanguage.GERMAN,   SBTEStringDescription.CAPTURED_SKILL),
    SBTEStringPointerGroup(0xEDD610, 8,    SBTEStringLanguage.GERMAN,   SBTEStringDescription.STORY_MODE_MENU),
    SBTEStringPointerGroup(0xED9680, 16,   SBTEStringLanguage.GERMAN,   SBTEStringDescription.MULTIPLAYER_MESSAGES),

    //SPANISH
    SBTEStringPointerGroup(0xEE6A50, 2299, SBTEStringLanguage.SPANISH,  SBTEStringDescription.STORY_MODE),
    SBTEStringPointerGroup(0xEDAAA8, 309,  SBTEStringLanguage.SPANISH,  SBTEStringDescription.EMERL_CARD_DESCRIPTIONS),
    SBTEStringPointerGroup(0xEDCE48, 17,   SBTEStringLanguage.SPANISH,  SBTEStringDescription.OPTIONS_MENU),
    SBTEStringPointerGroup(0xEDBA2C, 8,    SBTEStringLanguage.SPANISH,  SBTEStringDescription.BATTLE_MENU),
    SBTEStringPointerGroup(0xEDC104, 35,   SBTEStringLanguage.SPANISH,  SBTEStringDescription.BATTLE_RULES_MENU),
    SBTEStringPointerGroup(0xEDD470, 16,   SBTEStringLanguage.SPANISH,  SBTEStringDescription.TRAINING_MODE_MENU),
    SBTEStringPointerGroup(0xEDC44C, 9,    SBTEStringLanguage.SPANISH,  SBTEStringDescription.MINIGAME_MENU),
    SBTEStringPointerGroup(0xEDD1B0, 39,   SBTEStringLanguage.SPANISH,  SBTEStringDescription.BATTLE_RECORD_MENU),
    SBTEStringPointerGroup(0xEDCA34, 6,    SBTEStringLanguage.SPANISH,  SBTEStringDescription.CAPTURED_SKILL),
    SBTEStringPointerGroup(0xEDD630, 8,    SBTEStringLanguage.SPANISH,  SBTEStringDescription.STORY_MODE_MENU),
    SBTEStringPointerGroup(0xED96C0, 16,   SBTEStringLanguage.SPANISH,  SBTEStringDescription.MULTIPLAYER_MESSAGES),

    //ITALIAN
    SBTEStringPointerGroup(0xEE8E3C, 2299, SBTEStringLanguage.ITALIAN,  SBTEStringDescription.STORY_MODE),
    SBTEStringPointerGroup(0xEDAF7C, 309,  SBTEStringLanguage.ITALIAN,  SBTEStringDescription.EMERL_CARD_DESCRIPTIONS),
    SBTEStringPointerGroup(0xEDCE8C, 17,   SBTEStringLanguage.ITALIAN,  SBTEStringDescription.OPTIONS_MENU),
    SBTEStringPointerGroup(0xEDBA4C, 8,    SBTEStringLanguage.ITALIAN,  SBTEStringDescription.BATTLE_MENU),
    SBTEStringPointerGroup(0xEDC190, 35,   SBTEStringLanguage.ITALIAN,  SBTEStringDescription.BATTLE_RULES_MENU),
    SBTEStringPointerGroup(0xEDD4B0, 16,   SBTEStringLanguage.ITALIAN,  SBTEStringDescription.TRAINING_MODE_MENU),
    SBTEStringPointerGroup(0xEDC470, 9,    SBTEStringLanguage.ITALIAN,  SBTEStringDescription.MINIGAME_MENU),
    SBTEStringPointerGroup(0xEDD24C, 40,   SBTEStringLanguage.ITALIAN,  SBTEStringDescription.BATTLE_RECORD_MENU),
    SBTEStringPointerGroup(0xEDCA1C, 6,    SBTEStringLanguage.ITALIAN,  SBTEStringDescription.CAPTURED_SKILL),
    SBTEStringPointerGroup(0xEDD650, 8,    SBTEStringLanguage.ITALIAN,  SBTEStringDescription.STORY_MODE_MENU),
    SBTEStringPointerGroup(0xED9700, 16,   SBTEStringLanguage.ITALIAN,  SBTEStringDescription.MULTIPLAYER_MESSAGES)

)

internal val jpROMStringPointers = arrayOf(

    //JAPANESE
    SBTEStringPointerGroup(0xE4BDD8, 2299, SBTEStringLanguage.JAPANESE, SBTEStringDescription.STORY_MODE),
    SBTEStringPointerGroup(0xE49710, 309,  SBTEStringLanguage.JAPANESE, SBTEStringDescription.EMERL_CARD_DESCRIPTIONS),
    SBTEStringPointerGroup(0xE4B5AC, 17,   SBTEStringLanguage.JAPANESE, SBTEStringDescription.OPTIONS_MENU),
    SBTEStringPointerGroup(0xE4A614, 8,    SBTEStringLanguage.JAPANESE, SBTEStringDescription.BATTLE_MENU),
    SBTEStringPointerGroup(0xE4AA9C, 35,   SBTEStringLanguage.JAPANESE, SBTEStringDescription.BATTLE_RULES_MENU),
    SBTEStringPointerGroup(0xE4B828, 16,   SBTEStringLanguage.JAPANESE, SBTEStringDescription.TRAINING_MODE_MENU),
    SBTEStringPointerGroup(0xE4AD3C, 9,    SBTEStringLanguage.JAPANESE, SBTEStringDescription.MINIGAME_MENU),
    SBTEStringPointerGroup(0xE4B67C, 40,   SBTEStringLanguage.JAPANESE, SBTEStringDescription.BATTLE_RECORD_MENU),
    SBTEStringPointerGroup(0xE4B244, 6,    SBTEStringLanguage.JAPANESE, SBTEStringDescription.CAPTURED_SKILL),
    SBTEStringPointerGroup(0xE4B968, 8,    SBTEStringLanguage.JAPANESE, SBTEStringDescription.STORY_MODE_MENU),
    SBTEStringPointerGroup(0xE48CE8, 309,  SBTEStringLanguage.JAPANESE, SBTEStringDescription.EMERL_SKILLS),
    SBTEStringPointerGroup(0xE49690, 16,   SBTEStringLanguage.JAPANESE, SBTEStringDescription.MULTIPLAYER_MESSAGES),
    SBTEStringPointerGroup(0xE4BA74, 51,   SBTEStringLanguage.JAPANESE, SBTEStringDescription.STORY_MODE_AREA_NAMES),

    //ENGLISH
    SBTEStringPointerGroup(0xE4E1C4, 2299, SBTEStringLanguage.ENGLISH,  SBTEStringDescription.STORY_MODE),
    SBTEStringPointerGroup(0xE49BE4, 309,  SBTEStringLanguage.ENGLISH,  SBTEStringDescription.EMERL_CARD_DESCRIPTIONS),
    SBTEStringPointerGroup(0xE4B5F0, 17,   SBTEStringLanguage.ENGLISH,  SBTEStringDescription.OPTIONS_MENU),
    SBTEStringPointerGroup(0xE4A634, 8,    SBTEStringLanguage.ENGLISH,  SBTEStringDescription.BATTLE_MENU),
    SBTEStringPointerGroup(0xE4AB28, 35,   SBTEStringLanguage.ENGLISH,  SBTEStringDescription.BATTLE_RULES_MENU),
    SBTEStringPointerGroup(0xE4B868, 16,   SBTEStringLanguage.ENGLISH,  SBTEStringDescription.TRAINING_MODE_MENU),
    SBTEStringPointerGroup(0xE4AD60, 9,    SBTEStringLanguage.ENGLISH,  SBTEStringDescription.MINIGAME_MENU),
    SBTEStringPointerGroup(0xE4B71C, 40,   SBTEStringLanguage.ENGLISH,  SBTEStringDescription.BATTLE_RECORD_MENU),
    SBTEStringPointerGroup(0xE4B25C, 7,    SBTEStringLanguage.ENGLISH,  SBTEStringDescription.CAPTURED_SKILL),
    SBTEStringPointerGroup(0xE4B988, 8,    SBTEStringLanguage.ENGLISH,  SBTEStringDescription.STORY_MODE_MENU),
    SBTEStringPointerGroup(0xE491BC, 309,  SBTEStringLanguage.ENGLISH,  SBTEStringDescription.EMERL_SKILLS),
    SBTEStringPointerGroup(0xE496D0, 16,   SBTEStringLanguage.ENGLISH,  SBTEStringDescription.MULTIPLAYER_MESSAGES),
    SBTEStringPointerGroup(0xE4B9A8, 51,   SBTEStringLanguage.ENGLISH,  SBTEStringDescription.STORY_MODE_AREA_NAMES)

)

internal val euROMStringPointers = arrayOf(

    //JAPANESE
    SBTEStringPointerGroup(0xEDEBD0, 2299, SBTEStringLanguage.JAPANESE, SBTEStringDescription.STORY_MODE),
    SBTEStringPointerGroup(0xEDA884, 309,  SBTEStringLanguage.JAPANESE, SBTEStringDescription.EMERL_CARD_DESCRIPTIONS),
    SBTEStringPointerGroup(0xEDDE64, 17,   SBTEStringLanguage.JAPANESE, SBTEStringDescription.OPTIONS_MENU),
    SBTEStringPointerGroup(0xEDCAD8, 8,    SBTEStringLanguage.JAPANESE, SBTEStringDescription.BATTLE_MENU),
    SBTEStringPointerGroup(0xEDD000, 35,   SBTEStringLanguage.JAPANESE, SBTEStringDescription.BATTLE_RULES_MENU),
    SBTEStringPointerGroup(0xEDE4A0, 16,   SBTEStringLanguage.JAPANESE, SBTEStringDescription.TRAINING_MODE_MENU),
    SBTEStringPointerGroup(0xEDD4E8, 9,    SBTEStringLanguage.JAPANESE, SBTEStringDescription.MINIGAME_MENU),
    SBTEStringPointerGroup(0xEDE05C, 40,   SBTEStringLanguage.JAPANESE, SBTEStringDescription.BATTLE_RECORD_MENU),
    SBTEStringPointerGroup(0xEDDAE4, 6,    SBTEStringLanguage.JAPANESE, SBTEStringDescription.CAPTURED_SKILL),
    SBTEStringPointerGroup(0xEDE6E0, 8,    SBTEStringLanguage.JAPANESE, SBTEStringDescription.STORY_MODE_MENU),
    SBTEStringPointerGroup(0xED9D44, 309,  SBTEStringLanguage.JAPANESE, SBTEStringDescription.EMERL_SKILLS),
    SBTEStringPointerGroup(0xEDA6EC, 16,   SBTEStringLanguage.JAPANESE, SBTEStringDescription.MULTIPLAYER_MESSAGES),
    SBTEStringPointerGroup(0xEDE86C, 51,   SBTEStringLanguage.JAPANESE, SBTEStringDescription.STORY_MODE_AREA_NAMES),

    //ENGLISH
    SBTEStringPointerGroup(0xEE0FBC, 2299, SBTEStringLanguage.ENGLISH,  SBTEStringDescription.STORY_MODE),
    SBTEStringPointerGroup(0xEDAD58, 309,  SBTEStringLanguage.ENGLISH,  SBTEStringDescription.EMERL_CARD_DESCRIPTIONS),
    SBTEStringPointerGroup(0xEDDEA8, 17,   SBTEStringLanguage.ENGLISH,  SBTEStringDescription.OPTIONS_MENU),
    SBTEStringPointerGroup(0xEDCAF8, 8,    SBTEStringLanguage.ENGLISH,  SBTEStringDescription.BATTLE_MENU),
    SBTEStringPointerGroup(0xEDD08C, 35,   SBTEStringLanguage.ENGLISH,  SBTEStringDescription.BATTLE_RULES_MENU),
    SBTEStringPointerGroup(0xEDE4E0, 16,   SBTEStringLanguage.ENGLISH,  SBTEStringDescription.TRAINING_MODE_MENU),
    SBTEStringPointerGroup(0xEDD50C, 9,    SBTEStringLanguage.ENGLISH,  SBTEStringDescription.MINIGAME_MENU),
    SBTEStringPointerGroup(0xEDE0FC, 40,   SBTEStringLanguage.ENGLISH,  SBTEStringDescription.BATTLE_RECORD_MENU),
    SBTEStringPointerGroup(0xEDDAFC, 7,    SBTEStringLanguage.ENGLISH,  SBTEStringDescription.CAPTURED_SKILL),
    SBTEStringPointerGroup(0xEDE700, 8,    SBTEStringLanguage.ENGLISH,  SBTEStringDescription.STORY_MODE_MENU),
    SBTEStringPointerGroup(0xEDA218, 309,  SBTEStringLanguage.ENGLISH,  SBTEStringDescription.EMERL_SKILLS),
    SBTEStringPointerGroup(0xEDA72C, 16,   SBTEStringLanguage.ENGLISH,  SBTEStringDescription.MULTIPLAYER_MESSAGES),
    SBTEStringPointerGroup(0xEDE7A0, 51,   SBTEStringLanguage.ENGLISH,  SBTEStringDescription.STORY_MODE_AREA_NAMES),

    //FRENCH
    SBTEStringPointerGroup(0xEE33A8, 2299, SBTEStringLanguage.FRENCH,   SBTEStringDescription.STORY_MODE),
    SBTEStringPointerGroup(0xEDB22C, 309,  SBTEStringLanguage.FRENCH,   SBTEStringDescription.EMERL_CARD_DESCRIPTIONS),
    SBTEStringPointerGroup(0xEDDEEC, 17,   SBTEStringLanguage.FRENCH,   SBTEStringDescription.OPTIONS_MENU),
    SBTEStringPointerGroup(0xEDCB18, 8,    SBTEStringLanguage.FRENCH,   SBTEStringDescription.BATTLE_MENU),
    SBTEStringPointerGroup(0xEDD118, 35,   SBTEStringLanguage.FRENCH,   SBTEStringDescription.BATTLE_RULES_MENU),
    SBTEStringPointerGroup(0xEDE520, 16,   SBTEStringLanguage.FRENCH,   SBTEStringDescription.TRAINING_MODE_MENU),
    SBTEStringPointerGroup(0xEDD530, 9,    SBTEStringLanguage.FRENCH,   SBTEStringDescription.MINIGAME_MENU),
    SBTEStringPointerGroup(0xEDE19C, 40,   SBTEStringLanguage.FRENCH,   SBTEStringDescription.BATTLE_RECORD_MENU),
    SBTEStringPointerGroup(0xEDDB18, 6,    SBTEStringLanguage.FRENCH,   SBTEStringDescription.CAPTURED_SKILL),
    SBTEStringPointerGroup(0xEDE720, 8,    SBTEStringLanguage.FRENCH,   SBTEStringDescription.STORY_MODE_MENU),
    SBTEStringPointerGroup(0xEDA76C, 16,   SBTEStringLanguage.FRENCH,   SBTEStringDescription.MULTIPLAYER_MESSAGES),

    //GERMAN
    SBTEStringPointerGroup(0xEE5794, 2299, SBTEStringLanguage.GERMAN,   SBTEStringDescription.STORY_MODE),
    SBTEStringPointerGroup(0xEDB700, 309,  SBTEStringLanguage.GERMAN,   SBTEStringDescription.EMERL_CARD_DESCRIPTIONS),
    SBTEStringPointerGroup(0xEDDF30, 17,   SBTEStringLanguage.GERMAN,   SBTEStringDescription.OPTIONS_MENU),
    SBTEStringPointerGroup(0xEDCB38, 8,    SBTEStringLanguage.GERMAN,   SBTEStringDescription.BATTLE_MENU),
    SBTEStringPointerGroup(0xEDD1A4, 35,   SBTEStringLanguage.GERMAN,   SBTEStringDescription.BATTLE_RULES_MENU),
    SBTEStringPointerGroup(0xEDE560, 16,   SBTEStringLanguage.GERMAN,   SBTEStringDescription.TRAINING_MODE_MENU),
    SBTEStringPointerGroup(0xEDD554, 9,    SBTEStringLanguage.GERMAN,   SBTEStringDescription.MINIGAME_MENU),
    SBTEStringPointerGroup(0xEDE23C, 40,   SBTEStringLanguage.GERMAN,   SBTEStringDescription.BATTLE_RECORD_MENU),
    SBTEStringPointerGroup(0xEDDB30, 6,    SBTEStringLanguage.GERMAN,   SBTEStringDescription.CAPTURED_SKILL),
    SBTEStringPointerGroup(0xEDE740, 8,    SBTEStringLanguage.GERMAN,   SBTEStringDescription.STORY_MODE_MENU),
    SBTEStringPointerGroup(0xEDA7AC, 16,   SBTEStringLanguage.GERMAN,   SBTEStringDescription.MULTIPLAYER_MESSAGES),

    //SPANISH
    SBTEStringPointerGroup(0xEE7B80, 2299, SBTEStringLanguage.SPANISH,  SBTEStringDescription.STORY_MODE),
    SBTEStringPointerGroup(0xEDBBD4, 309,  SBTEStringLanguage.SPANISH,  SBTEStringDescription.EMERL_CARD_DESCRIPTIONS),
    SBTEStringPointerGroup(0xEDDF74, 17,   SBTEStringLanguage.SPANISH,  SBTEStringDescription.OPTIONS_MENU),
    SBTEStringPointerGroup(0xEDCB58, 8,    SBTEStringLanguage.SPANISH,  SBTEStringDescription.BATTLE_MENU),
    SBTEStringPointerGroup(0xEDD230, 35,   SBTEStringLanguage.SPANISH,  SBTEStringDescription.BATTLE_RULES_MENU),
    SBTEStringPointerGroup(0xEDE5A0, 16,   SBTEStringLanguage.SPANISH,  SBTEStringDescription.TRAINING_MODE_MENU),
    SBTEStringPointerGroup(0xEDD578, 9,    SBTEStringLanguage.SPANISH,  SBTEStringDescription.MINIGAME_MENU),
    SBTEStringPointerGroup(0xEDE2DC, 40,   SBTEStringLanguage.SPANISH,  SBTEStringDescription.BATTLE_RECORD_MENU),
    SBTEStringPointerGroup(0xEDDB60, 6,    SBTEStringLanguage.SPANISH,  SBTEStringDescription.CAPTURED_SKILL),
    SBTEStringPointerGroup(0xEDE760, 8,    SBTEStringLanguage.SPANISH,  SBTEStringDescription.STORY_MODE_MENU),
    SBTEStringPointerGroup(0xEDA7EC, 16,   SBTEStringLanguage.SPANISH,  SBTEStringDescription.MULTIPLAYER_MESSAGES),

    //ITALIAN
    SBTEStringPointerGroup(0xEE9F6C, 2299, SBTEStringLanguage.ITALIAN,  SBTEStringDescription.STORY_MODE),
    SBTEStringPointerGroup(0xEDC0A8, 309,  SBTEStringLanguage.ITALIAN,  SBTEStringDescription.EMERL_CARD_DESCRIPTIONS),
    SBTEStringPointerGroup(0xEDDFB8, 17,   SBTEStringLanguage.ITALIAN,  SBTEStringDescription.OPTIONS_MENU),
    SBTEStringPointerGroup(0xEDCB78, 8,    SBTEStringLanguage.ITALIAN,  SBTEStringDescription.BATTLE_MENU),
    SBTEStringPointerGroup(0xEDD2BC, 35,   SBTEStringLanguage.ITALIAN,  SBTEStringDescription.BATTLE_RULES_MENU),
    SBTEStringPointerGroup(0xEDE5E0, 16,   SBTEStringLanguage.ITALIAN,  SBTEStringDescription.TRAINING_MODE_MENU),
    SBTEStringPointerGroup(0xEDD59C, 9,    SBTEStringLanguage.ITALIAN,  SBTEStringDescription.MINIGAME_MENU),
    SBTEStringPointerGroup(0xEDE37C, 40,   SBTEStringLanguage.ITALIAN,  SBTEStringDescription.BATTLE_RECORD_MENU),
    SBTEStringPointerGroup(0xEDDB48, 6,    SBTEStringLanguage.ITALIAN,  SBTEStringDescription.CAPTURED_SKILL),
    SBTEStringPointerGroup(0xEDE780, 8,    SBTEStringLanguage.ITALIAN,  SBTEStringDescription.STORY_MODE_MENU),
    SBTEStringPointerGroup(0xEDA82C, 16,   SBTEStringLanguage.ITALIAN,  SBTEStringDescription.MULTIPLAYER_MESSAGES)

)
