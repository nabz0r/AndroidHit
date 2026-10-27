package com.androidhit.lumadrift.social;

import com.androidhit.lumadrift.data.Strings;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Complete social-screen copy; original French/English pairs are stable accessibility labels. */
public final class SocialStrings {
    private static final Map<String,String[]> COPY=new LinkedHashMap<>();
    static {
        add("Home","Accueil","Start","Inicio","Início","Home");
        add("Stars find each other","Les étoiles se retrouvent","Sterne finden zueinander","Las estrellas se reencuentran","As estrelas encontram-se","Le stelle si ritrovano");
        add("Add a friend with their code. Send a little hint for their next board.","Ajoute un ami avec son code. Offre un petit indice pour son prochain plateau.","Füge Freunde mit ihrem Code hinzu. Schicke ihnen einen kleinen Hinweis für das nächste Rätsel.","Añade amigos con su código. Envíales una pista para su próximo tablero.","Adiciona amigos pelo código. Envia-lhes uma dica para o próximo tabuleiro.","Aggiungi amici con il loro codice. Invia un indizio per il prossimo livello.");
        add("Connecting…","Connexion…","Verbindung wird hergestellt…","Conectando…","A ligar…","Connessione…");
        add("Your adventure stays playable without an account. Sign in with Google to create your circle.","Ton aventure reste jouable sans compte. Connecte-toi à Google pour créer ton cercle.","Dein Abenteuer bleibt ohne Konto spielbar. Melde dich mit Google an, um deinen Kreis zu gründen.","Puedes seguir jugando sin cuenta. Inicia sesión con Google para crear tu círculo.","Podes continuar a jogar sem conta. Inicia sessão com Google para criar o teu círculo.","Puoi continuare a giocare senza account. Accedi con Google per creare la tua cerchia.");
        add("My Google account","Mon compte Google","Mein Google-Konto","Mi cuenta Google","A minha conta Google","Il mio account Google");
        add("Refresh","Actualiser","Aktualisieren","Actualizar","Atualizar","Aggiorna");
        add("YOUR FRIEND CODE","TON CODE AMI","DEIN FREUNDESCODE","TU CÓDIGO DE AMIGO","O TEU CÓDIGO DE AMIGO","IL TUO CODICE AMICO");
        add("Copy code","Copier le code","Code kopieren","Copiar código","Copiar código","Copia codice");
        add("Code copied","Code copié","Code kopiert","Código copiado","Código copiado","Codice copiato");
        add("Your public nickname","Ton surnom public","Dein öffentlicher Spitzname","Tu apodo público","A tua alcunha pública","Il tuo soprannome pubblico");
        add("Share my map position","Partager ma position sur la carte","Meinen Kartenstand teilen","Compartir mi posición en el mapa","Partilhar a minha posição no mapa","Condividi la mia posizione sulla mappa");
        add("Only accepted friends see your nickname and companion. Your position stays hidden until you enable it. Progress is reported by your device.","Seuls tes amis acceptés voient ton surnom et ton compagnon. Ta position est masquée tant que tu ne l'actives pas. La progression est déclarée par ton appareil.","Nur bestätigte Freunde sehen deinen Spitznamen und Begleiter. Dein Kartenstand bleibt verborgen, bis du ihn freigibst. Dein Gerät meldet den Fortschritt.","Solo los amigos aceptados ven tu apodo y compañero. Tu posición permanece oculta hasta que la compartas. Tu dispositivo declara el progreso.","Só os amigos aceites veem a tua alcunha e companheiro. A posição fica oculta até ativares a partilha. O progresso é comunicado pelo teu dispositivo.","Solo gli amici accettati vedono soprannome e compagno. La posizione resta nascosta finché non la condividi. I progressi sono dichiarati dal dispositivo.");
        add("Save my profile","Enregistrer mon profil","Mein Profil speichern","Guardar mi perfil","Guardar o meu perfil","Salva il mio profilo");
        add("A new star","Une nouvelle étoile","Ein neuer Stern","Una nueva estrella","Uma nova estrela","Una nuova stella");
        add("Friend code · 16 characters","Code de ton ami · 16 caractères","Freundescode · 16 Zeichen","Código de amigo · 16 caracteres","Código de amigo · 16 carateres","Codice amico · 16 caratteri");
        add("Send invitation","Envoyer une invitation","Einladung senden","Enviar invitación","Enviar convite","Invia invito");
        add("TODAY'S GIFTS","CADEAUX DU JOUR","GESCHENKE DES TAGES","REGALOS DE HOY","PRESENTES DO DIA","REGALI DI OGGI");
        add("%d / 3 sends left · 1 gift per friend per UTC day","%d / 3 envois restants · 1 cadeau par ami et par jour UTC","%d / 3 Sendungen übrig · 1 Geschenk pro Freund und UTC-Tag","%d / 3 envíos restantes · 1 regalo por amigo y día UTC","%d / 3 envios restantes · 1 presente por amigo e dia UTC","%d / 3 invii rimasti · 1 regalo per amico e giorno UTC");
        add("Each gift contains 1 earned hint, never LumaGold. Receive up to 5 per day, available for 7 days.","Chaque cadeau contient 1 indice gagné, jamais de LumaGold. 5 réceptions par jour maximum, disponibles 7 jours.","Jedes Geschenk enthält 1 verdienten Hinweis, niemals LumaGold. Bis zu 5 pro Tag abholen, 7 Tage verfügbar.","Cada regalo contiene 1 pista ganada, nunca LumaGold. Recoge hasta 5 al día; disponibles durante 7 días.","Cada presente contém 1 dica ganha, nunca LumaGold. Recolhe até 5 por dia; disponíveis durante 7 dias.","Ogni regalo contiene 1 indizio ottenuto giocando, mai LumaGold. Ritira fino a 5 al giorno; disponibili per 7 giorni.");
        add("1 hint","1 indice","1 Hinweis","1 pista","1 dica","1 indizio");
        add("Check delivery","Vérifier la réception","Zustellung prüfen","Comprobar recepción","Verificar a receção","Verifica ricezione");
        add("Receive hint","Recevoir l'indice","Hinweis erhalten","Recibir pista","Receber dica","Ricevi indizio");
        add("Your star mailbox is empty for now.","Ta boîte à étoiles est vide pour le moment.","Dein Sternenbriefkasten ist noch leer.","Tu buzón de estrellas está vacío por ahora.","A tua caixa de estrelas está vazia por agora.","La tua casella delle stelle è ancora vuota.");
        add("INVITATIONS RECEIVED · ","INVITATIONS REÇUES · ","ERHALTENE EINLADUNGEN · ","INVITACIONES RECIBIDAS · ","CONVITES RECEBIDOS · ","INVITI RICEVUTI · ");
        add("Accept","Accepter","Annehmen","Aceptar","Aceitar","Accetta");
        add("Decline","Refuser","Ablehnen","Rechazar","Recusar","Rifiuta");
        add("YOUR CIRCLE · ","TON CERCLE · ","DEIN KREIS · ","TU CÍRCULO · ","O TEU CÍRCULO · ","LA TUA CERCHIA · ");
        add("On the map: level ","Sur la carte : niveau ","Auf der Karte: Level ","En el mapa: nivel ","No mapa: nível ","Sulla mappa: livello ");
        add("Private position","Position privée","Privater Kartenstand","Posición privada","Posição privada","Posizione privata");
        add("Gift 1 hint","Offrir 1 indice","1 Hinweis schenken","Regalar 1 pista","Oferecer 1 dica","Regala 1 indizio");
        add("Remove friend","Retirer du cercle","Freund entfernen","Eliminar amigo","Remover amigo","Rimuovi amico");
        add("Remove this friend?","Retirer cet ami ?","Diesen Freund entfernen?","¿Eliminar este amigo?","Remover este amigo?","Rimuovere questo amico?");
        add("Remove","Retirer","Entfernen","Eliminar","Remover","Rimuovi");
        add("Cancel","Annuler","Abbrechen","Cancelar","Cancelar","Annulla");
        add("Share your code to see friends here and on the map when they share their position.","Partage ton code pour voir tes amis ici et sur la carte s'ils partagent leur position.","Teile deinen Code, um Freunde hier und bei freigegebenem Kartenstand auf der Karte zu sehen.","Comparte tu código para ver amigos aquí y en el mapa cuando compartan su posición.","Partilha o código para veres amigos aqui e no mapa quando partilharem a posição.","Condividi il codice per vedere gli amici qui e sulla mappa quando condividono la posizione.");
        add("INVITATIONS SENT · ","INVITATIONS ENVOYÉES · ","GESENDETE EINLADUNGEN · ","INVITACIONES ENVIADAS · ","CONVITES ENVIADOS · ","INVITI INVIATI · ");
        add("Cancel invitation","Annuler l'invitation","Einladung zurückziehen","Cancelar invitación","Cancelar convite","Annulla invito");
        add("Refresh circle","Actualiser le cercle","Kreis aktualisieren","Actualizar círculo","Atualizar círculo","Aggiorna cerchia");
        add("Sending…","Envoi…","Wird gesendet…","Enviando…","A enviar…","Invio…");
        add("Receiving…","Réception…","Wird empfangen…","Recibiendo…","A receber…","Ricezione…");
        add("+1 hint in your inventory","+1 indice dans ton inventaire","+1 Hinweis in deinem Inventar","+1 pista en tu inventario","+1 dica no teu inventário","+1 indizio nel tuo inventario");
        add("Already received, or inventory full. Retry after using a hint.","Déjà reçu, ou inventaire plein. Tu peux réessayer après avoir utilisé un indice.","Bereits erhalten oder Inventar voll. Nach Verwendung eines Hinweises erneut versuchen.","Ya recibido o inventario lleno. Reintenta después de usar una pista.","Já recebido ou inventário cheio. Tenta novamente depois de usares uma dica.","Già ricevuto o inventario pieno. Riprova dopo aver usato un indizio.");
        add("Delivery not finalized. Retry: the receipt is retained without duplicates.","Réception non finalisée. Réessaie : le reçu est conservé sans doublon.","Zustellung nicht abgeschlossen. Erneut versuchen: Der Beleg bleibt ohne doppelte Belohnung erhalten.","Recepción sin finalizar. Reintenta: se conserva el recibo sin duplicados.","Receção por finalizar. Tenta novamente: o recibo é mantido sem duplicados.","Ricezione non completata. Riprova: la ricevuta resta conservata senza duplicati.");
        add("Refreshing…","Actualisation…","Wird aktualisiert…","Actualizando…","A atualizar…","Aggiornamento…");
        add("Reconnect your Google account to find your circle.","Reconnecte ton compte Google pour retrouver ton cercle.","Verbinde dein Google-Konto erneut, um deinen Kreis zu finden.","Reconecta tu cuenta Google para recuperar tu círculo.","Volta a ligar a conta Google para encontrares o teu círculo.","Ricollega il tuo account Google per ritrovare la cerchia.");
        add("Online circle is not configured in this build. No sample friends are added.","Le cercle en ligne n'est pas configuré dans cette version. Aucun ami fictif n'est ajouté.","Der Online-Kreis ist in dieser Version nicht eingerichtet. Es werden keine Beispielfreunde hinzugefügt.","El círculo en línea no está configurado en esta versión. No se añaden amigos de ejemplo.","O círculo online não está configurado nesta versão. Não são adicionados amigos de exemplo.","La cerchia online non è configurata in questa versione. Non vengono aggiunti amici fittizi.");
        add("The friend service is not deployed yet. Your adventure remains available offline.","Le service amis n'est pas encore déployé. Ton aventure reste disponible hors ligne.","Der Freundesdienst ist noch nicht bereitgestellt. Dein Abenteuer bleibt offline verfügbar.","El servicio de amigos aún no está desplegado. Puedes seguir jugando sin conexión.","O serviço de amigos ainda não foi publicado. A tua aventura continua disponível offline.","Il servizio amici non è ancora pubblicato. Puoi continuare a giocare offline.");
        add("Check the 16-character friend code.","Vérifie le code ami de 16 caractères.","Prüfe den 16-stelligen Freundescode.","Comprueba el código de amigo de 16 caracteres.","Verifica o código de amigo de 16 carateres.","Controlla il codice amico di 16 caratteri.");
        add("That is your own code! Send it to a friend.","C'est ton propre code ! Envoie-le à un ami.","Das ist dein eigener Code! Schicke ihn einem Freund.","¡Es tu propio código! Envíalo a un amigo.","É o teu próprio código! Envia-o a um amigo.","È il tuo codice! Invialo a un amico.");
        add("Your 3 daily gifts are sent. Come back after midnight UTC.","Tes 3 cadeaux du jour sont envoyés. Reviens après minuit UTC.","Deine 3 Tagesgeschenke sind verschickt. Nach Mitternacht UTC geht es weiter.","Ya enviaste tus 3 regalos diarios. Vuelve después de medianoche UTC.","Já enviaste os 3 presentes do dia. Volta depois da meia-noite UTC.","Hai inviato i 3 regali giornalieri. Torna dopo mezzanotte UTC.");
        add("You received 5 gifts today. The others stay in your mailbox until expiry.","Tu as reçu 5 cadeaux aujourd’hui. Les autres restent dans ta boîte jusqu’à leur expiration.","Du hast heute 5 Geschenke erhalten. Die übrigen bleiben bis zum Ablauf im Briefkasten.","Hoy recibiste 5 regalos. Los demás quedan en el buzón hasta que caduquen.","Recebeste 5 presentes hoje. Os restantes ficam na caixa até expirarem.","Hai ricevuto 5 regali oggi. Gli altri restano nella casella fino alla scadenza.");
        add("This friend already received today's gift.","Cet ami a déjà reçu ton cadeau du jour.","Dieser Freund hat dein heutiges Geschenk bereits erhalten.","Este amigo ya recibió tu regalo de hoy.","Este amigo já recebeu o teu presente do dia.","Questo amico ha già ricevuto il tuo regalo di oggi.");
        add("Limit reached: 40 friends, 20 active invitations and 20 new ones per day.","Limite atteinte : 40 amis, 20 invitations actives et 20 nouvelles par jour.","Limit erreicht: 40 Freunde, 20 aktive Einladungen und 20 neue pro Tag.","Límite alcanzado: 40 amigos, 20 invitaciones activas y 20 nuevas al día.","Limite atingido: 40 amigos, 20 convites ativos e 20 novos por dia.","Limite raggiunto: 40 amici, 20 inviti attivi e 20 nuovi al giorno.");
        add("This gift expired after 7 days.","Ce cadeau a expiré après 7 jours.","Dieses Geschenk ist nach 7 Tagen abgelaufen.","Este regalo caducó tras 7 días.","Este presente expirou após 7 dias.","Questo regalo è scaduto dopo 7 giorni.");
        add("Choose a nickname of 1 to 32 characters, without control characters.","Choisis un surnom de 1 à 32 caractères, sans caractères de contrôle.","Wähle einen Spitznamen mit 1 bis 32 Zeichen ohne Steuerzeichen.","Elige un apodo de 1 a 32 caracteres, sin caracteres de control.","Escolhe uma alcunha de 1 a 32 carateres, sem carateres de controlo.","Scegli un soprannome da 1 a 32 caratteri, senza caratteri di controllo.");
        add("This friend already invited you: accept their request below.","Cet ami t'a déjà invité : accepte sa demande ci-dessous.","Dieser Freund hat dich bereits eingeladen: Nimm seine Anfrage unten an.","Este amigo ya te invitó: acepta su solicitud abajo.","Este amigo já te convidou: aceita o pedido abaixo.","Questo amico ti ha già invitato: accetta la richiesta qui sotto.");
        add("One moment: retry in a minute.","Un petit instant : réessaie dans une minute.","Einen Moment: In einer Minute erneut versuchen.","Un momento: vuelve a intentarlo en un minuto.","Um momento: tenta novamente dentro de um minuto.","Un momento: riprova tra un minuto.");
        add("The circle is unavailable. Check your connection and retry. No offline rewards are invented.","Le cercle est indisponible. Vérifie ta connexion et réessaie. Aucune récompense n'est inventée hors ligne.","Der Kreis ist nicht verfügbar. Prüfe die Verbindung und versuche es erneut. Es werden keine Offline-Belohnungen erfunden.","El círculo no está disponible. Comprueba la conexión y reintenta. No se inventan recompensas sin conexión.","O círculo está indisponível. Verifica a ligação e tenta novamente. Não são inventadas recompensas offline.","La cerchia non è disponibile. Controlla la connessione e riprova. Non vengono inventati premi offline.");
    }
    private static void add(String en,String fr,String de,String es,String pt,String it) {
        if(COPY.put(fr,new String[]{en,fr,de,es,pt,it})!=null)throw new IllegalStateException(fr);
    }
    public static String get(String language,String fr,String en,Object...args) {
        String[] values=COPY.get(fr);
        if(values==null||!values[0].equals(en))throw new IllegalArgumentException("Unknown social copy pair");
        String normalized=Strings.normalize(language);
        int index=Arrays.asList("en","fr","de","es","pt","it").indexOf(normalized);
        String value=values[Math.max(0,index)];
        return args.length==0?value:String.format(Locale.forLanguageTag(normalized),value,args);
    }
    public static Set<String> keys() { return Collections.unmodifiableSet(COPY.keySet()); }
    private SocialStrings() { }
}
