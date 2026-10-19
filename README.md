# PRO3600-26-RAD-25 - Chess royal

Jeu d'échecs en réseau à deux joueurs (JavaFX + TCP)

## Contexte
 Ce projet implémente une variante des échecs, utilisant un système de cartes que chaque joueur peut utiliser pour impacter la partie de différentes manières.
Celui-ci implémente toutes les fonctionnalités d'un jeu d'échec  classique, à l'exception de la vérification de la mise en échec.

## Fonctionnalités
- Échecs complets : roque, prise en passant, promotion
- Parties en réseau (Blancs / Noirs) via un serveur TCP
- Cartes : pioche, main de 3 max, défausse
- Fin de partie à la capture du roi

## Cartes spéciales 

- BouncingBishop	: Fou, mais	s'il se déplace en diagonale, il peut rebondir une fois sur les bords
- PawnKamikaze	: Un pion	qui explose à son 2ᵉ déplacement et détruit les 8 cases adjacentes
- ExterminatorKnight	: Un cavalier	qui capture toutes les pièces adverses sur son trajet
- LavaSquare	: Toute pièce se déplaçant sur la case disparaît

À chaque tour : un coup, ou une pioche, ou une carte jouée.

### Lancement
- Le serveur sur sa machine (un des deux joueurs) : ./serveur.sh 4545 
- Les deux joueurs sur leur machine : ./client.sh sur chaque machine (deux terminaux différents pour chaque client si même machine)
- Dans la fenêtre qui apparaît : rentrer pseudo, hôte (IP de la machine du serveur, localhost si même machine), port 4545
- Un joueur clique sur "Commencer la partie"

## Les deux joueurs doivent être connectés au même Wi-Fi si les deux machines sont différentes. 

Remarque : Images de*.png générées par IA

<img width="364" height="206" alt="interface" src="https://github.com/user-attachments/assets/eefd15dc-cd75-46b2-a214-47a5e4f1927f" />







