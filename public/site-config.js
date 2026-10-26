/* Public configuration only. Never put service-account keys, tokens or secrets here.
 * Keep preparation=true until the publication checklist in docs/PUBLIC_PAGES.md passes.
 * A Firebase WEB app configuration is required for external account deletion;
 * the Android app ID in google-services.json cannot replace the Web app ID.
 */
window.LUMA_SITE_CONFIG = Object.freeze({
  preparation: true,
  publisherName: "nabilux",
  supportEmail: "nabz0r+gaming@gmail.com",
  walletApiBaseUrl: "https://luma-wallet-369472826910.europe-west1.run.app",
  firebase: Object.freeze({
    apiKey: "AIzaSyBspttjrAwSD1az3zlXjDg0nwB5sFhq9sk",
    authDomain: "luma-drift-production.firebaseapp.com",
    projectId: "luma-drift-production",
    appId: "1:369472826910:web:47e79b67e7a3abd0c3080f"
  })
});
