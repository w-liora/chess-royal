package application;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.Insets;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import models.Piece;
import models.Position;
import models.Square;

// Pièces concrètes (pour initialiser le plateau de départ)
import models.Pawn;
import models.Rook;
import models.Knight;
import models.Bishop;
import models.Queen;
import models.King;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import messages.CoupEchecs;
import messages.Message;
import messages.ProtocoleMessageType;
import reseau.Client;

/**
 * Contrôleur du plateau d'échecs JavaFX.
 *
 * Responsabilités :
 *  - Créer le modèle (Square[][] board) et y placer les pièces de départ.
 *  - Créer 64 SquareButton et les ajouter au GridPane.
 *  - Gérer le clic :
 *      1er clic  → sélectionne la pièce, affiche les coups possibles (cases vertes).
 *      2e clic   → déplace la pièce vers la case choisie et rafraîchit l'affichage.
 */
public class ChessBoardCtrl implements Initializable {

    // ──────────────── constantes ────────────────
    private static final int TAILLE = 8;          // 8×8
    private static final int TAILLE_CASE = 75;    // pixels par case

    // Répertoire des images (dans src/main/resources/images/)
    private static final String IMG_DIR = "/images/";

    // ──────────────── composants FXML ──────────────
    @FXML private GridPane gridPlateau;
    @FXML private Label    labelStatut;

    // ──────────────── état interne ────────────────
    /** Modèle : tableau 8×8 de cases (ligne 0 = haut = rangée des noirs) */
    private Square[][] board = new Square[TAILLE][TAILLE];

    /** Les 64 boutons de l'interface, indexés [row][col] */
    private Button[][] boutons = new Button[TAILLE][TAILLE];

    /** Case sélectionnée (null si aucune) */
    private int selectedRow = -1;
    private int selectedCol = -1;

    /** Coups possibles de la pièce sélectionnée */
    private Position[] coupsPossibles = new Position[0];

    /**
     * Cache des images : clé = nom du fichier (ex: "roi_blanc_sur_case_claire.png")
     * Chargé une seule fois au démarrage dans prechargerImages().
     */
    private final Map<String, ImageView> cacheImages = new HashMap<>();

    // ──────────────── réseau ────────────────

    /**
     * Connexion TCP vers le serveur. Null en mode local (sans réseau).
     * Injectée depuis ConnexionCtrl via {@link #initReseau(Client, int)}.
     */
    private Client connexion = null;

    /**
     * Numéro de couleur du joueur local : 0 = blanc, 1 = noir.
     * Seul le joueur dont c'est la couleur peut déplacer ses pièces.
     */
    private int couleurLocale = 0;

    /** Service JavaFX pour écouter les messages entrants en arrière-plan. */
    private Service<Message> serviceReception;
    
    private boolean monTour = true;

    // ──────────────── initialisation ────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initBoard();          // créer le modèle
        prechargerImages();   // charger toutes les images en mémoire
    }

    // ──────────────── réseau : initialisation ────────────────

    /**
     * Injecte la connexion réseau et la couleur du joueur local,
     * puis démarre l'écoute des messages entrants.
     * À appeler depuis ConnexionCtrl juste après le chargement du FXML,
     * avant que l'utilisateur puisse interagir avec le plateau.
     *
     * @param connexion     client TCP connecté au serveur.
     * @param monNumero     numéro attribué par le serveur (0 = blanc, 1 = noir).
     */
    public void initReseau(Client connexion, int monNumero) {
        this.connexion    = connexion;
        this.couleurLocale = monNumero; // 0 = blanc, 1 = noir
        this.monTour = (monNumero == 0); // seuls les blancs peuvent jouer en premier
        initBoutons();        // créer les boutons JavaFX
        rafraichirVue();      // afficher les images
        demarrerServiceReception();
    }

    /**
     * Démarre (ou relance) le service JavaFX chargé de lire les messages réseau
     * en arrière-plan et de les appliquer sur le plateau en fil JavaFX.
     */
    private void demarrerServiceReception() {
        serviceReception = new Service<Message>() {
            @Override
            protected Task<Message> createTask() {
                return new Task<Message>() {
                    @Override
                    protected Message call() throws Exception {
                        // Appel bloquant — s'exécute hors du fil JavaFX.
                        return connexion.lireMessage();
                    }
                };
            }
        };

        EventHandler<WorkerStateEvent> onSucceed = event -> {
            Message msg = serviceReception.getValue();
            if (msg != null && msg.getProtocoleMessage() == ProtocoleMessageType.COUP_ECHECS) {
                CoupEchecs coup = (CoupEchecs) msg.getData();
                appliquerCoupAdverse(coup);
            }
            // Relancer pour écouter le prochain message.
            serviceReception.restart();
        };

        EventHandler<WorkerStateEvent> onFailed = event -> {
            labelStatut.setText("Connexion perdue avec le serveur.");
        };

        serviceReception.setOnSucceeded(onSucceed);
        serviceReception.setOnFailed(onFailed);
        serviceReception.start();
    }

    /**
     * Applique sur le plateau local un coup envoyé par l'adversaire.
     * Appelé depuis le fil JavaFX via le service de réception.
     *
     * @param coup le coup à appliquer.
     */
    private void appliquerCoupAdverse(CoupEchecs coup) {
        Piece piece = board[coup.getFromRow()][coup.getFromCol()].getPiece();
        if (piece == null) return;

        piece.moveTo(board, coup.getToRow(), coup.getToCol());

        // Promotion éventuelle demandée par l'adversaire.
        if (coup.isPromotion()) {
            Position pos = new Position(coup.getToRow(), coup.getToCol());
            Piece promue = switch (coup.getPromotionSymbol()) {
                case 'Q' -> new Queen (pos, piece.getColor());
                case 'R' -> new Rook  (pos, piece.getColor());
                case 'B' -> new Bishop(pos, piece.getColor());
                case 'N' -> new Knight(pos, piece.getColor());
                default  -> new Queen (pos, piece.getColor());
            };
            board[coup.getToRow()][coup.getToCol()].setPiece(promue);
        }

        // Mettre à jour le tour : après un coup adverse, c'est notre tour.
        rafraichirVue();
        
        if (trouverRoi(couleurLocale) == null) {
        	rafraichirVue();
        	terminerpartie(1 - couleurLocale);
        }
        else {
	        monTour = (board[coup.getToRow()][coup.getToCol()].getPiece() != null
	                           && board[coup.getToRow()][coup.getToCol()].getPiece().getColor() != couleurLocale);
	        
        }
    }


    private void prechargerImages() {
        // Toutes les combinaisons possibles de noms d'image
        String[] fonds    = {"claire", "sombre"};
        String[] suffixes = {"", "_survolée"};

        // Cases vides
        for (String fond : fonds) {
            for (String suf : suffixes) {
                chargerEtMettrEnCache("case_" + fond + suf + ".png");
            }
        }

        // Pièces : on liste tous les préfixes possibles
        String[] prefixes = {
            "roi_blanc", "roi_noir",
            "dame_blanche", "dame_noire",
            "tour_blanche", "tour_noire",
            "fou_blanc", "fou_noir",
            "cavalier_blanc", "cavalier_noir",
            "pion_blanc", "pion_noir"
        };
        for (String prefixe : prefixes) {
            for (String fond : fonds) {
                for (String suf : suffixes) {
                    chargerEtMettrEnCache(prefixe + "_sur_case_" + fond + suf + ".png");
                }
            }
        }
    }

    /**
     * Charge une image et la stocke dans le cache sous son nom de fichier.
     * Si le fichier est introuvable, on ne plante pas : on ignore silencieusement.
     */
    private void chargerEtMettrEnCache(String nomFichier) {
        URL url = getClass().getResource(IMG_DIR + nomFichier);
        if (url != null) {
            Image img = new Image(url.toString(),
                    TAILLE_CASE, TAILLE_CASE, true, true);
            ImageView iv = new ImageView(img);
            iv.setFitWidth(TAILLE_CASE);
            iv.setFitHeight(TAILLE_CASE);
            cacheImages.put(nomFichier, iv);
        }
    }

    /**
     * Crée le modèle : toutes les cases, puis place les pièces en position initiale.
     * Convention : row 0 = haut = rangée noire (color=1), row 7 = bas = rangée blanche (color=0)
     */
    private void initBoard() {
        // Remplir toutes les cases avec des Square vides
        for (int r = 0; r < TAILLE; r++) {
            for (int c = 0; c < TAILLE; c++) {
                board[r][c] = new Square();
            }
        }

        // ── Rangée 0 : pièces noires (color = 1) ──
        board[0][0].setPiece(new Rook  (new Position(0,0), 1));
        board[0][1].setPiece(new Knight(new Position(0,1), 1));
        board[0][2].setPiece(new Bishop(new Position(0,2), 1));
        board[0][3].setPiece(new Queen (new Position(0,3), 1));
        board[0][4].setPiece(new King  (new Position(0,4), 1));
        board[0][5].setPiece(new Bishop(new Position(0,5), 1));
        board[0][6].setPiece(new Knight(new Position(0,6), 1));
        board[0][7].setPiece(new Rook  (new Position(0,7), 1));

        // ── Rangée 1 : pions noirs ──
        for (int c = 0; c < TAILLE; c++) {
            board[1][c].setPiece(new Pawn(new Position(1,c), 1));
        }

        // ── Rangée 6 : pions blancs ──
        for (int c = 0; c < TAILLE; c++) {
            board[6][c].setPiece(new Pawn(new Position(6,c), 0));
        }

        // ── Rangée 7 : pièces blanches (color = 0) ──
        board[7][0].setPiece(new Rook  (new Position(7,0), 0));
        board[7][1].setPiece(new Knight(new Position(7,1), 0));
        board[7][2].setPiece(new Bishop(new Position(7,2), 0));
        board[7][3].setPiece(new Queen (new Position(7,3), 0));
        board[7][4].setPiece(new King  (new Position(7,4), 0));
        board[7][5].setPiece(new Bishop(new Position(7,5), 0));
        board[7][6].setPiece(new Knight(new Position(7,6), 0));
        board[7][7].setPiece(new Rook  (new Position(7,7), 0));
    }

    /**
     * Crée les 64 boutons et les ajoute au GridPane.
     * Chaque bouton connaît sa position (row, col) via la lambda.
     */
    private void initBoutons() {
        if (couleurLocale == 0) {
	        for (int r = 0; r < TAILLE; r++) {
	            for (int c = 0; c < TAILLE; c++) {
	                Button btn = new Button();
	                btn.setPrefSize(TAILLE_CASE, TAILLE_CASE);
	                btn.setAlignment(Pos.CENTER);
	                // Pas de padding ni d'espacement interne autour de l'image
	                btn.setStyle("-fx-padding: 0;");
	
	                // Capture des indices pour la lambda
	                final int row = r;
	                final int col = c;
	                btn.setOnAction(e -> onCaseCliquee(row, col));
	
	                boutons[r][c] = btn;
	                GridPane.setRowIndex(btn, r);
	                GridPane.setColumnIndex(btn, c);
	                gridPlateau.getChildren().add(btn);
	            }
	        }
        }
        else{
	        for (int r = 0; r < TAILLE; r++) {
	            for (int c = 0; c < TAILLE; c++) {
	                Button btn = new Button();
	                btn.setPrefSize(TAILLE_CASE, TAILLE_CASE);
	                btn.setAlignment(Pos.CENTER);
	                // Pas de padding ni d'espacement interne autour de l'image
	                btn.setStyle("-fx-padding: 0;");
	
	                // Capture des indices pour la lambda
	                final int row = TAILLE - 1 - r;
	                final int col = TAILLE - 1 - c;
	                btn.setOnAction(e -> onCaseCliquee(row,col));
	
	                boutons[r][c] = btn;
	                GridPane.setRowIndex(btn, r);
	                GridPane.setColumnIndex(btn, c);
	                gridPlateau.getChildren().add(btn);
	            }
	        }
        }
    }

    // ──────────────── affichage ────────────────

    /**
     * Met à jour l'image de chaque bouton selon l'état du modèle.
     * Réinitialise aussi tous les surlignages (aucun coup sélectionné).
     */
    private void rafraichirVue() {
    	if (couleurLocale == 0) {
    		for (int r = 0; r < TAILLE; r++) {
                for (int c = 0; c < TAILLE; c++) {
                    String nomImage = getNomImage(r, c, false, false);
                    setImageBouton(boutons[r][c], nomImage);
                }
            }
    	}
    	else {
    		for (int r = 0; r < TAILLE; r++) {
    			for (int c = 0; c < TAILLE; c++) {
                    String nomImage = getNomImage(r, c, false, false);
                    setImageBouton(boutons[TAILLE - 1 - r][TAILLE - 1 - c], nomImage);
                }
            }
    	}
    }

    /**
     * Rafraîchit l'affichage en tenant compte de la sélection courante :
     *  - case sélectionnée → image "survolée" (appuyée)
     *  - cases disponibles → image "survolée" de la case vide
     *  - reste → image normale
     */
    private void rafraichirVueAvecSelection() {
        if (couleurLocale == 0 ) {
	    	for (int r = 0; r < TAILLE; r++) {
	            for (int c = 0; c < TAILLE; c++) {
	                boolean estSelectionnee = (r == selectedRow && c == selectedCol);
	                boolean estDisponible   = estParmisCoups(r, c);
	                String nomImage = getNomImage(r, c, estSelectionnee, estDisponible);
	                setImageBouton(boutons[r][c], nomImage);
	            }
	        }
        }
        else {
        	for (int r = 0; r < TAILLE; r++) {
	            for (int c = 0; c < TAILLE; c++) {
	                boolean estSelectionnee = (r == selectedRow && c == selectedCol);
	                boolean estDisponible   = estParmisCoups(r, c);
	                String nomImage = getNomImage(r, c, estSelectionnee, estDisponible);
	                setImageBouton(boutons[TAILLE - 1 - r][TAILLE - 1 - c], nomImage);
	            }
        	}
        }
    }

    /**
     * Met à jour le graphic du bouton depuis le cache — aucun accès disque.
     * On crée un nouveau ImageView à partir de l'Image déjà en mémoire
     * (un ImageView ne peut être utilisé qu'une fois dans le scene graph).
     */
    private void setImageBouton(Button btn, String nomImage) {
        ImageView cached = cacheImages.get(nomImage);
        if (cached != null) {
            // Réutiliser l'Image (déjà chargée), mais créer un nouvel ImageView
            ImageView iv = new ImageView(cached.getImage());
            iv.setFitWidth(TAILLE_CASE);
            iv.setFitHeight(TAILLE_CASE);
            btn.setGraphic(iv);
            btn.setText("");
            btn.setStyle("-fx-padding: 0; -fx-background-color: transparent; -fx-border-width: 0;");
        } else {
            // fallback : couleur unie si l'image manque
            btn.setGraphic(null);
            boolean caseSombre = (r(btn) + c(btn)) % 2 == 1;
            btn.setStyle("-fx-background-color: " + (caseSombre ? "#769656" : "#eeeed2") + "; -fx-padding: 0;");
        }
    }

    // ──────────────── logique de nommage des images ────────────────

    /**
     * Construit le nom du fichier image à utiliser pour la case (row, col).
     *
     * Convention des noms de fichiers dans resources/images/ :
     *
     *   Case vide claire :          case_claire.png
     *   Case vide sombre :          case_sombre.png
     *   Case vide claire survolée : case_claire_survolée.png
     *   Case vide sombre survolée : case_sombre_survolée.png
     *
     *   Pièce sur fond clair :      {piece}_{couleur}_sur_case_claire.png
     *   Pièce sur fond sombre :     {piece}_{couleur}_sur_case_sombre.png
     *   Pièce survolée clair :      {piece}_{couleur}_sur_case_claire_survolée.png
     *   Pièce survolée sombre :     {piece}_{couleur}_sur_case_sombre_survolée.png
     *
     *   Exemples :
     *     roi_blanc_sur_case_claire.png
     *     cavalier_noir_sur_case_sombre_survolée.png
     *     pion_blanc_sur_case_claire_survolée.png
     *
     * @param estSurvole  true si case sélectionnée OU case disponible
     */
    private String getNomImage(int row, int col, boolean estSelectionnee, boolean estDisponible) {
        // Case claire ou sombre selon la parité (0,0) = claire
        boolean caseSombre = (row + col) % 2 == 1;
        String fond = caseSombre ? "sombre" : "claire";

        // Surlignage : la case sélectionnée ET les cases disponibles sont "survolées"
        boolean survole = estSelectionnee || estDisponible;
        String suffixe = survole ? "_survolée" : "";

        Piece piece = board[row][col].getPiece();

        if (piece == null) {
            // Case vide
            return "case_" + fond + suffixe + ".png";
        }

        // Case avec pièce
        String couleur = (piece.getColor() == 0) ? "blanc" : "noir";
        String nomPiece = getNomPiece(piece, couleur);

        return nomPiece + "_sur_case_" + fond + suffixe + ".png";
    }

    /**
     * Renvoie le préfixe du nom de fichier pour une pièce donnée.
     * S'appuie sur le symbole de la pièce (défini dans chaque sous-classe).
     */
    private String getNomPiece(Piece piece, String couleur) {
        return switch (piece.symbol) {
            case 'K' -> "roi_"      + couleur;
            case 'Q' -> "dame_"     + (couleur.equals("blanc") ? "blanche" : "noire");
            case 'R' -> "tour_"     + (couleur.equals("blanc") ? "blanche" : "noire");
            case 'B' -> "fou_"      + couleur;
            case 'N' -> "cavalier_" + couleur;
            case 'p' -> "pion_"     + couleur;
            default  -> "pion_"     + couleur; // fallback
        };
    }

    // ──────────────── gestion des clics ────────────────

    /**
     * Appelé quand le joueur clique sur la case (row, col).
     *
     * Cas 1 : aucune pièce sélectionnée → sélectionner la pièce cliquée.
     * Cas 2 : clic sur une case disponible → déplacer la pièce.
     * Cas 3 : clic sur la case déjà sélectionnée → désélectionner.
     * Cas 4 : clic sur une autre pièce → changer la sélection.
     */
    private void onCaseCliquee(int row, int col) {
        
    	if (monTour) {
	    	// ── Cas 2 : on a une pièce sélectionnée et on clique sur un coup valide ──
	        if (selectedRow != -1 && estParmisCoups(row, col)) {
	            int fromRow = selectedRow;
	            int fromCol = selectedCol;
	            deplacerPiece(fromRow, fromCol, row, col);
	            // Après le déplacement (et l'éventuelle promotion), on envoie le coup résultant.
	            envoyerCoupReseau(fromRow, fromCol, row, col);
	            monTour = false;
	            deselectionner();
	            if (connexion != null) {
	                labelStatut.setText("Coup envoyé — en attente de l'adversaire…");
	            }
	            return;
	        }
	
	        // ── Cas 3 : clic sur la case déjà sélectionnée ──
	        if (row == selectedRow && col == selectedCol) {
	            deselectionner();
	            return;
	        }
	
	        // ── Cas 1 & 4 : sélectionner la pièce sur la case cliquée ──
	        Piece piece = board[row][col].getPiece();
	        if (piece != null) {
	            // En mode réseau : bloquer les pièces de l'adversaire.
	            if (connexion != null && piece.getColor() != couleurLocale) {
	                labelStatut.setText("Ce ne sont pas vos pièces !");
	                return;
	            }
	            selectedRow = row;
	            selectedCol = col;
	            coupsPossibles = piece.getPossibleMoves(board);
	            labelStatut.setText(getNomComplet(piece) + " sélectionné(e) — "
	                    + coupsPossibles.length + " coup(s) possible(s)");
	            rafraichirVueAvecSelection();
	        } 
    	}
	      else {
            // Clic sur une case vide sans sélection → rien
            deselectionner();
        }
    }

    /**
     * Envoie le coup joué au serveur (qui le relayera à l'adversaire).
     * En mode local (sans réseau), ne fait rien.
     *
     * La promotion éventuelle est lue directement depuis le board après
     * que {@link #deplacerPiece} l'a déjà appliquée.
     */
    private void envoyerCoupReseau(int fromRow, int fromCol, int toRow, int toCol) {
        if (connexion == null) return;
        try {
            Piece arrivee = board[toRow][toCol].getPiece();
            char promo = '\0';
            if (arrivee != null && !(arrivee instanceof Pawn)) {
                // Si ce n'est plus un pion sur la case d'arrivée, il y a eu promotion.
                // On récupère le symbole de la pièce promue.
                promo = arrivee.symbol;
                // Mais on n'envoie promotion que si le coup venait réellement d'un pion.
                // On vérfie via la rangée : pion blanc promeut en row 0, noir en row 7.
                boolean promoblanc = (couleurLocale == 0 && toRow == 0);
                boolean promonoir  = (couleurLocale == 1 && toRow == 7);
                if (!promoblanc && !promonoir) promo = '\0';
            }
            connexion.envoyerCoup(new CoupEchecs(fromRow, fromCol, toRow, toCol, promo));
        } catch (IOException e) {
            labelStatut.setText("Erreur réseau lors de l'envoi du coup.");
        }
    }


    /**
     * Déplace la pièce de (fromRow, fromCol) vers (toRow, toCol)
     * en utilisant la méthode moveTo() du modèle.
     */
    private void deplacerPiece(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = board[fromRow][fromCol].getPiece();
        if (piece != null) {
            piece.moveTo(board, toRow, toCol);
        // Si c'est un pion qui vient de promouvoir → popup de choix
        if (trouverRoi(1 - couleurLocale) == null) {
        	rafraichirVue();
        	terminerpartie(couleurLocale);
        }
        if (piece instanceof Pawn && ((Pawn) piece).isPromotion()) {
        	rafraichirVue(); // afficher le pion arrivé avant la popup
        	PopupPromotion(toRow, toCol, piece.getColor());
        	}
            rafraichirVue();
        }
    }
    
    
    /**
     * Popup avec 4 boutons  (dame, tour, fou, cavalier)
     * Bloquetant que le joueur n'a pas choisi
     */
    private void PopupPromotion(int row, int col, int color) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL); //Bloque si choix pas fait 
        popup.initStyle(StageStyle.UNDECORATED); //Pour ne pas pouvoir fermer la fenêtre
 
        String couleur  = (color == 0) ? "blanc" : "noir";
        boolean sombre  = (row + col) % 2 == 1;
        String  fond    = sombre ? "sombre" : "claire";
 
        String[] prefixes = {
            "dame_"     + (color == 0 ? "blanche" : "noire"),
            "tour_"     + (color == 0 ? "blanche" : "noire"),
            "fou_"      + couleur,
            "cavalier_" + couleur
        };

 
        HBox hbox = new HBox(8); //Boite horizontal, 8 = espace entre boutons
        hbox.setPadding(new Insets(12));
        hbox.setStyle("-fx-background-color: #2b2b2b; -fx-border-color: #888; -fx-border-width: 2;");
 
        for (int i = 0; i < 4; i++) {
            int idx = i; //obligé de faire ça pour la fonction lambda
            String nomImg = prefixes[i] + "_sur_case_" + fond + ".png";
            ImageView cached = cacheImages.get(nomImg);
 
            Button btn = new Button();
            btn.setPrefSize(TAILLE_CASE, TAILLE_CASE);
            btn.setStyle("-fx-padding: 0; -fx-background-color: transparent; -fx-cursor: hand;");
 
            ImageView iv = new ImageView(cached.getImage());
            iv.setFitWidth(TAILLE_CASE);
            iv.setFitHeight(TAILLE_CASE);
            btn.setGraphic(iv); //Afficher l'image dans le boutton
            
            btn.setOnAction(e -> { //Pour chaque bouton
                Position pos = new Position(row, col);
                Piece nouvellePiece = switch (idx) {
                    case 0 -> new Queen (pos, color);//Si on est à l'étpae 0, on attribut au boutton 0 la promotion en dame
                    case 1 -> new Rook  (pos, color); //etc.
                    case 2 -> new Bishop(pos, color);
                    case 3 -> new Knight(pos, color);
                    default -> new Queen(pos, color);
                };
                board[row][col].setPiece(nouvellePiece);
                
                rafraichirVue();
                popup.close(); 
            });
 
            hbox.getChildren().add(btn);
        }
 
        popup.setScene(new Scene(hbox));
        popup.showAndWait();
 
        // Positionner la popup près de la case de promotion
        Stage parent = (Stage) gridPlateau.getScene().getWindow(); //On récupère le gridpane du fxml
        double baseX = parent.getX() + gridPlateau.localToScene(0, 0).getX() + col * TAILLE_CASE; //On récupère la position de la case de promotion
        double baseY = parent.getY() + gridPlateau.localToScene(0, 0).getY() + row * TAILLE_CASE;
        // Si Blanc, promeut en row 0 en haut → popup en dessous ; Si Noir promeut en row 7 en bas → popup au-dessus
        double offsetY = (color == 0) ? TAILLE_CASE + 4 : -(TAILLE_CASE + 16);
        popup.setX(baseX);
        popup.setY(baseY + offsetY);
    }
    
    
    

    /** Réinitialise la sélection et rafraîchit l'affichage. */
    private void deselectionner() {
        selectedRow = -1;
        selectedCol = -1;
        coupsPossibles = new Position[0];
        labelStatut.setText("Cliquez sur une pièce pour voir ses déplacements");
        rafraichirVue();
    }

    // ──────────────── utilitaires ────────────────
    private void terminerpartie(int gagnant) {
    	monTour = false;
    	Stage popup =  new Stage();
    	HBox hbox = new HBox();
    	popup.setScene(new Scene(hbox));
    	popup.show();
    }
    
    private Piece trouverRoi(int color) {
        for (int r = 0; r < TAILLE; r++) {
            for (int c = 0; c < TAILLE; c++) {
                Piece p = board[r][c].getPiece();
                if (p != null && p.symbol == 'K' && p.getColor() == color) {
                    return p;
                }
            }
        }
        return null; // roi introuvable
    }
    
    /** Vérifie si la position (row, col) fait partie des coups possibles. */
    private boolean estParmisCoups(int row, int col) {
        for (Position p : coupsPossibles) {
            if (p.getRow() == row && p.getCol() == col) return true;
        }
        return false;
    }

    /** Récupère le row d'un bouton depuis le GridPane (pour le fallback couleur). */
    private int r(Button btn) {
        Integer v = GridPane.getRowIndex(btn);
        return v == null ? 0 : v;
    }

    /** Récupère le col d'un bouton depuis le GridPane (pour le fallback couleur). */
    private int c(Button btn) {
        Integer v = GridPane.getColumnIndex(btn);
        return v == null ? 0 : v;
    }



    /** Renvoie le nom complet lisible de la pièce. */
    private String getNomComplet(Piece piece) {
        String couleur = (piece.getColor() == 0) ? "blanc" : "noir";
        return switch (piece.symbol) {
            case 'K' -> "Roi "     + couleur;
            case 'Q' -> "Dame "    + couleur;
            case 'R' -> "Tour "    + couleur;
            case 'B' -> "Fou "     + couleur;
            case 'N' -> "Cavalier "+ couleur;
            case 'p' -> "Pion "    + couleur;
            default  -> "Pièce "   + couleur;
        };
    }
}