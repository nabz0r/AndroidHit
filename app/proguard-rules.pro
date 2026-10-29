# Keep the public, pure art API available for regressions against the signed release.
# No preference, commerce, engine or Activity interface is exposed by these rules.
-keep,allowoptimization,includedescriptorclasses class com.androidhit.lumadrift.ui.CompanionPortrait { public *; }
-keep,allowoptimization,includedescriptorclasses class com.androidhit.lumadrift.ui.CharacterSprites { public *; }
-keep,allowoptimization,includedescriptorclasses class com.androidhit.lumadrift.ui.ArtLibrary { public *; }
-keep,allowoptimization,includedescriptorclasses class com.androidhit.lumadrift.ui.AvatarStage$Pose { public *; }
-keep,allowoptimization,includedescriptorclasses class com.androidhit.lumadrift.data.Catalog { public *; }
-keep,allowoptimization,includedescriptorclasses class com.androidhit.lumadrift.data.Catalog$CharacterItem { public *; }
-keep,allowoptimization,includedescriptorclasses class com.androidhit.lumadrift.data.CosmeticCatalog { public *; }
-keep,allowoptimization,includedescriptorclasses class com.androidhit.lumadrift.data.CosmeticCatalog$* { public *; }
-keep,allowoptimization,includedescriptorclasses class com.androidhit.lumadrift.data.SignatureOutfits { public *; }
-keep,allowoptimization,includedescriptorclasses class com.androidhit.lumadrift.data.SignatureOutfits$Outfit { public *; }
-keep,allowoptimization,includedescriptorclasses class com.androidhit.lumadrift.data.AvatarStyles { public *; }
-keep,allowoptimization,includedescriptorclasses class com.androidhit.lumadrift.data.AvatarStyles$* { public *; }
# v1.18 pure motion/render snapshots remain inspectable on the signed prototype.
-keep,allowoptimization,includedescriptorclasses class com.androidhit.lumadrift.ui.CompanionMotion { public *; }
-keep,allowoptimization,includedescriptorclasses class com.androidhit.lumadrift.ui.CompanionMotion$* { public *; }
-keep,allowoptimization,includedescriptorclasses class com.androidhit.lumadrift.ui.CompanionArt118 { public *; }
-keep,allowoptimization,includedescriptorclasses class com.androidhit.lumadrift.ui.CompanionArt118$Entry { public *; }
-keep,allowoptimization,includedescriptorclasses class com.androidhit.lumadrift.data.AvatarLoadout { public *; }
-keep,allowoptimization,includedescriptorclasses class com.androidhit.lumadrift.data.AvatarGearCatalog { public *; }
-keep,allowoptimization,includedescriptorclasses class com.androidhit.lumadrift.data.AvatarGearCatalog$* { public *; }
