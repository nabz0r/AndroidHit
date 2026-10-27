# Installed UI evidence and binary scope

The last production APK, `1.5.0-rc1` / code 6, SHA-256 **`33c4dbd6c14fa3e72ec52c7d98475b1a9738b175bad9216c4bc4c7c5bdd817a2`**, passed **14 EN/FR cases with 24 captures** on `emulator-5556`. [Executed text report](ui-smoke.txt) · [JSON identity, cases and capture paths](ui-smoke.json).

Both languages covered Home/Settings, the existing guest account page, all three collection tabs and their owned filters, the Lantern Leaf preview, both power-shop pages, a Spark offer closed without purchase, and all six Sealed passages menu entries without starting a board. The profile retained 235 local coins and three Spark items. The ×3 / 45-earned-coin confirmation agreed with its card. Selected home/preview/offer screenshots were inspected visually without finding blocking clipping or inconsistent amounts. The smoke never entered the existing Journey 2 run.

The explicit English setting and original `size=reset` / `density=reset` were restored with zero errors. No login, ad, purchase, equipment change, explicit reward claim, profile reset or private-preference edit was performed. Normal app entry may record a visit or persist an already-eligible local unlock.

Canonical final captures include [French home](../../screenshots/v1.5/home-fr.png), [equipment](../../screenshots/v1.5/collection-equipment-fr.png), [shop](../../screenshots/v1.5/shop-fr.png), [sealed menu](../../screenshots/v1.5/sealed-menu-fr.png), and their `-en.png` counterparts.

The earlier full pass remains [archived with its original APK hash](pre-share-image-fix/README.md): **58 cases / 96 captures**, six languages plus 720×1280 and 1280×720. It is not relabeled as a new execution on the final APK. The [source-manifest comparison](binary-source-diff.json) records exactly one changed game/build input, `ShareComposer.java`, between those binaries; the menu, strings, gear, powers, audio and engine sources were unchanged. Sharing has its own [final executed report](sealed-share-ui.txt).

The landscape app remains a centered reduced portrait column. Bounds and navigation passed in the earlier full run; physical readability, touch comfort, TalkBack behavior, native-speaker editorial quality and human retention are separate checks. This UI smoke does not qualify real identity, Play purchases or live advertising.
