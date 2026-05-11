package course;

import static common.Log.COMM;
import static common.Log.GEN;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;

import common.Log;
import messages.CoupEchecs;
import messages.ListeJoueursConnectes;
import messages.Message;
import messages.MessageSigne;
import messages.NouveauJoueur;
import messages.NumeroCoureur;
import messages.ProtocoleMessageType;
import reseau.Serveur;

/**
 * Serveur dédié au jeu d'échecs en réseau.
 *
 * Protocole simplifié :
 *   (0) Attente des connexions TCP des deux joueurs.
 *   (1) Réception INFO_NOUVEAU_JOUEUR de chaque client.
 *   (2) Envoi LISTE_JOUEURS_CONNECTES à tous après chaque connexion.
 *   (3) Réception DEBUT_PARTIE d'un client → clôture des connexions entrantes.
 *   (4) Envoi NUMERO_COUREUR à chaque client (0 = Blanc, 1 = Noir).
 *   (5) Boucle infinie : réception COUP_ECHECS → rediffusion à tous les clients.
 */
public class ServeurEchecs {

    private static final int NB_JOUEURS = 2;

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: ServeurEchecs <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);

        Log.setLevel(COMM, Level.TRACE);
        Log.setLevel(GEN,  Level.TRACE);

        Serveur serveur = new Serveur(port);
        GEN.info("ServeurEchecs démarré sur le port " + port);

        // ── Phase 1 : connexion des joueurs ──────────────────────────────────
        List<String> pseudos     = new ArrayList<>();
        List<SelectionKey> keys  = new ArrayList<>();

        while (pseudos.size() < NB_JOUEURS) {
            MessageSigne ms = serveur.lireMessage();

            if (ms.getProtocoleMessageType() == ProtocoleMessageType.DECONNEXION) {
                GEN.warn("Un joueur s'est déconnecté pendant la phase de connexion.");
                continue;
            }

            if (ms.getProtocoleMessageType() == ProtocoleMessageType.INFO_NOUVEAU_JOUEUR) {
                NouveauJoueur nj = (NouveauJoueur) ms.getData();
                pseudos.add(nj.getPseudo());
                keys.add(ms.getIdReseau());
                GEN.info("Joueur connecté : " + nj.getPseudo()
                         + " (" + pseudos.size() + "/" + NB_JOUEURS + ")");

                // Envoyer la liste mise à jour à tous.
                List<Coureur> coureurs = new ArrayList<>();
                for (String p : pseudos) coureurs.add(new Coureur(p));
                ListeJoueursConnectes liste = new ListeJoueursConnectes(coureurs);
                serveur.envoyerATous(new Message(liste, ProtocoleMessageType.LISTE_JOUEURS_CONNECTES));
            }
        }

        // ── Phase 2 : attente du signal DEBUT_PARTIE ─────────────────────────
        GEN.info("Attente du signal DEBUT_PARTIE…");
        boolean debutRecu = false;
        while (!debutRecu) {
            MessageSigne ms = serveur.lireMessage();
            if (ms.getProtocoleMessageType() == ProtocoleMessageType.DEBUT_PARTIE) {
                debutRecu = true;
            }
        }

        // Clore le canal d'acceptation (plus aucune connexion entrante).
        serveur.cloreServerSocketChannel();

        // Envoyer le numéro de couleur à chaque joueur.
        for (int i = 0; i < NB_JOUEURS; i++) {
            NumeroCoureur nc = new NumeroCoureur(i);
            serveur.envoyerMessage(keys.get(i), new Message(nc, ProtocoleMessageType.NUMERO_COUREUR));
            GEN.info("Joueur " + pseudos.get(i) + " reçoit le numéro " + i
                     + (i == 0 ? " (Blancs)" : " (Noirs)"));
        }

        // ── Phase 3 : boucle de jeu — relay des coups d'échecs ───────────────
        GEN.info("Partie démarrée ! Relais des coups en cours…");
        while (true) {
            MessageSigne ms;
            try {
                ms = serveur.lireMessage();
            } catch (IOException e) {
                GEN.warn("Erreur réseau : " + e.getMessage());
                break;
            }

            if (ms.getProtocoleMessageType() == ProtocoleMessageType.DECONNEXION) {
                GEN.info("Un joueur s'est déconnecté. Fin de partie.");
                break;
            }

            if (ms.getProtocoleMessageType() == ProtocoleMessageType.COUP_ECHECS) {
                CoupEchecs coup = (CoupEchecs) ms.getData();
                GEN.info("Coup reçu : " + coup + " — rediffusion à tous.");
                serveur.envoyerATous(new Message(coup, ProtocoleMessageType.COUP_ECHECS));
            }
        }

        serveur.close();
        GEN.info("ServeurEchecs arrêté.");
    }
}