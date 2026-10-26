/* Public configuration only. Never put service-account keys, tokens or secrets here.
 * Keep preparation=true until the publication checklist in docs/PUBLIC_PAGES.md passes.
 * A Firebase WEB app configuration is required for external account deletion;
 * the Android app ID in google-services.json cannot replace the Web app ID.
 */
window.LUMA_SITE_CONFIG = Object.freeze({
  preparation: true,
  publisherName: "",
  supportEmail: "",
  walletApiBaseUrl: "",
  firebase: Object.freeze({
    apiKey: "",
    authDomain: "",
    projectId: "luma-drift-production",
    appId: ""
  })
});
