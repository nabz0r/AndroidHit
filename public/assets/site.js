(() => {
  "use strict";
  const config = window.LUMA_SITE_CONFIG || {};
  const email = /^[^\s@<>]+@[^\s@<>]+\.[^\s@<>]+$/.test(config.supportEmail || "") ? config.supportEmail : "";
  const emailLink = email ? `mailto:${encodeURIComponent(email).replace("%40", "@")}` : "";
  const edition = document.body?.dataset?.edition === "1.17" ? "1.17" : "1.16";
  const requestedLanguage = new URL(location.href).searchParams.get("lang");
  let language = requestedLanguage === "en" ? "en" : "fr";
  const syncConfiguration = () => {
    if (config.preparation === false) {
      document.querySelectorAll("[data-preparation-notice]").forEach(el => { el.hidden = true; });
    }
    if (config.publisherName && email) {
      document.querySelectorAll("[data-publisher]").forEach(el => {
        el.replaceChildren(document.createTextNode(`${config.publisherName} · `));
        const a = document.createElement("a");
        a.href = emailLink;
        a.textContent = email;
        el.append(a);
      });
      document.querySelectorAll("[data-contact]").forEach(el => {
        el.replaceChildren(document.createTextNode(language === "fr"
          ? `Pour une demande privée concernant Luma Drift, contacte ${config.publisherName} : `
          : `For a private Luma Drift request, contact ${config.publisherName}: `));
        const a = document.createElement("a");
        a.href = `${emailLink}?subject=Luma%20Drift%20%E2%80%94%20account%20%2F%20privacy`;
        a.textContent = email;
        el.append(a, document.createTextNode(language === "fr"
          ? ". N’envoie jamais ton mot de passe, un code de connexion ni tes coordonnées bancaires."
          : ". Never send a password, sign-in code or payment-card details."));
      });
    }
  };
  const setLanguage = next => {
    language = next === "en" ? "en" : "fr";
    document.documentElement.lang = language;
    document.querySelectorAll("[data-lang]").forEach(el => { el.hidden = el.dataset.lang !== language; });
    document.querySelectorAll("[data-set-lang]").forEach(el => {
      el.setAttribute("aria-pressed", String(el.dataset.setLang === language));
    });
    const url = new URL(location.href);
    if (language === "en") url.searchParams.set("lang", "en");
    else url.searchParams.delete("lang");
    history.replaceState(null, "", url);
    // Preserve the selected language between pages without cookies or localStorage.
    document.querySelectorAll("a[href]").forEach(link => {
      const destination = new URL(link.getAttribute("href"), location.href);
      if (destination.origin !== location.origin || !destination.pathname.endsWith(".html")) return;
      if (language === "en") destination.searchParams.set("lang", "en");
      else destination.searchParams.delete("lang");
      link.href = destination.pathname + destination.search + destination.hash;
    });
    document.querySelectorAll("[data-screenshot-fr]").forEach(image => {
      image.src = language === "en" ? image.dataset.screenshotEn : image.dataset.screenshotFr;
      image.alt = language === "en" ? image.dataset.altEn : image.dataset.altFr;
    });
    document.querySelectorAll("[data-alt-fr]").forEach(image => {
      image.alt = language === "en" ? image.dataset.altEn : image.dataset.altFr;
    });
    if (document.querySelector("#hero-title")) {
      document.title = edition === "1.17"
        ? (language === "en" ? "Luma Drift 1.17 — A world at your fingertips." : "Luma Drift 1.17 — Un monde à toucher.")
        : (language === "en" ? "Luma Drift 1.16 — Your home. Your universe." : "Luma Drift 1.16 — Ta maison. Ton univers.");
    }
    document.querySelectorAll("[data-content-fr]").forEach(meta => { meta.setAttribute("content", language === "en" ? meta.dataset.contentEn : meta.dataset.contentFr); });
    syncConfiguration();
    window.dispatchEvent(new CustomEvent("luma-language", { detail: language }));
  };
  document.querySelectorAll("[data-set-lang]").forEach(button => {
    button.addEventListener("click", () => setLanguage(button.dataset.setLang));
  });
  if (edition === "1.17") {
    const previews = [...document.querySelectorAll("audio[data-scene-preview], .audio-section audio")];
    previews.forEach(player => player.addEventListener("play", () => {
      previews.forEach(other => { if (other !== player && !other.paused) other.pause(); });
    }));
  }
  setLanguage(language);
})();
