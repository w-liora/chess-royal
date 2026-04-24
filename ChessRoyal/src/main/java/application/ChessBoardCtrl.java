package application;

import javafx.fxml.FXML;
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

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur du plateau d'échecs .
 *
 * Objectifs:
 *  - Créer le modèle (Square[][] board) et y placer les pièces de départ.
 *  - Créer 64 SquareButton et les ajouter au GridPane.
 *  - Gérer le clic :
 *      1er clic  → sélectionne la pièce, affiche les coups possibles (cases vertes).
 *      2e clic   → déplace la pièce vers la case choisie et rafraîchit l'affichage.
 */
public class ChessBoardCtrl implements Initializable {

    // ──────────────── constantes ────────────────
    private static final int TAILLE = 9;          // 8×8
    private static final int TAILLE_CASE = 75;    // pixels par case

    // Répertoire des images (dans src/main/resources/images/)
    private static final String IMG_DIR = "/images/";

    // ──────────────── composants FXML ────────────────
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

    // ──────────────── initialisation ────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initBoard();          // 1) créer le modèle
        initBoutons();        // 2) créer les boutons JavaFX
        rafraichirVue();      // 3) afficher les images
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
     * Chaque bouton connaît sa position (row, col) via fct lambda.
     */
    private void initBoutons() {
        for (int r = 0; r < TAILLE; r++) {
            for (int c = 0; c < TAILLE; c++) {
                Button btn = new Button();
                btn.setPrefSize(TAILLE_CASE, TAILLE_CASE);
                btn.setAlignment(Pos.CENTER);
                // Pas d'espace entre les images
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

    // ──────────────── affichage ────────────────

    /**
     * Met à jour l'image de chaque bouton selon l'état du modèle
     * Réinitialise aussi tous les surlignages (aucun coup sélectionné)
     */
    private void rafraichirVue() {
        for (int r = 0; r < TAILLE; r++) {
            for (int c = 0; c < TAILLE; c++) {
                String nomImage = getNomImage(r, c, false, false);
                setImageBouton(boutons[r][c], nomImage);
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
        for (int r = 0; r < TAILLE; r++) {
            for (int c = 0; c < TAILLE; c++) {
                boolean estSelectionnee = (r == selectedRow && c == selectedCol);
                boolean estDisponible   = estParmisCoups(r, c);
                String nomImage = getNomImage(r, c, estSelectionnee, estDisponible);
                setImageBouton(boutons[r][c], nomImage);
            }
        }
    }

    /**
     * Charge une image depuis /resources/images/ et la place dans le bouton.
     * Si l'image n'existe pas, le bouton reste vide (pas d'exception).
     */
    private void setImageBouton(Button btn, String nomImage) {
        URL url = getClass().getResource(IMG_DIR + nomImage);
        if (url != null) {
            Image img = new Image(url.toString(),
                    TAILLE_CASE, TAILLE_CASE,  // largeur, hauteur
                    true,                       // preserve ratio
                    true);                      // smooth
            ImageView iv = new ImageView(img);
            iv.setFitWidth(TAILLE_CASE);
            iv.setFitHeight(TAILLE_CASE);
            btn.setGraphic(iv);
            
            
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

        // ── Cas 2 : on a une pièce sélectionnée et on clique sur un coup valide ──
        if (selectedRow != -1 && estParmisCoups(row, col)) {
            deplacerPiece(selectedRow, selectedCol, row, col);
            deselectionner();
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
            selectedRow = row;
            selectedCol = col;
            coupsPossibles = piece.getPossibleMoves(board);
            labelStatut.setText(getNomComplet(piece) + " sélectionné(e) — "
                    + coupsPossibles.length + " coup(s) possible(s)");
            rafraichirVueAvecSelection();
        } else {
            // Clic sur une case vide sans sélection → rien
            deselectionner();
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
            labelStatut.setText(getNomComplet(piece) + " déplacé(e) en "
                    + colonneLettre(toCol) + (TAILLE - toRow));
            rafraichirVue();
        }
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

    /** Convertit un indice de colonne (0-7) en lettre échecs (a-h). */
    private char colonneLettre(int col) {
        return (char) ('a' + col);
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