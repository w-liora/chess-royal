# PRO3600-26-RAD-25 - Chess royal
## Contexte
 Ce projet a pour but d'implémenter une variante des échecs, utilisant un système de cartes que chaque joueur peut utiliser pour impacter la partie de différentes manières.
 
Le projet est encore en développement, et comporte pour l'instant une version textuelle du jeu d'échec, passant par la console. Celui-ci implémente toutes les fonctionnalités d'un jeu d'échec  classique, à l'exception de la promotion et de la vérificaton de la mise en échec.

Le projet est voué à évoluer, pour implémenter une interface graphique, du jeu en réseau, et des cartes.

## Version textuelle
### Lancement
La version textuelle du jeu se trouve dans le package game, et est nommé TextualGame.java. Lancez ce fichier pour démarrer une partie, et observez la console : le plateau apparait. La notation usuelle des échecs est utilisée pour représenter les cases et les pièces, et le symbole 'p' est utilisé pour représenter les pions. Les pièces du joueur blanc sont situées en bas du plateau, et celles du joueur noir en haut.


La correspondance pièce-symbole est la suivante, si elle ne vous est pas familière:
* Pion <-> p
* Tour <-> R
* Fou <-> B
* Cavalier <-> N
* Reine <-> Q
* Roi <-> K

### Fonctionnement
Vous devriez voir que le tour est au joueur blanc, et qu'il lui est demandé de rentrer la case corresondant à la pièce qu'il souhaite déplacer. Rentrez simplement pour cela la colonne de la pièce, en minuscule, collée au numéro de la ligne. (par exemple "e2" pour déplacer le pion devant le roi). Attention à ne pas rajouter d'espace et ne pas inverser l'ordre, le texte serait considéré comme invalide. Si la case n'est pas valide (elle n'existe pas, elle ne contient pas de pièce, la pièce ne peut pas se déplacer...), il vous sera demandé à nouveau d'en choisir une.

Vous pourrez ensuite choisir la case vers laquelle déplacer cette pièce, avec la même syntaxe. Encore une fois, si la case est invalide, vous devrez en rentrer une nouvelle. Les règles de déplacement sont les mêmes qu'aux échecs, à l'exception que vous pouvez laisser votre roi en échec, et les pions ne peuvent pas être promus.

Après avoir choisi la seconde case, la pièce se déplace. Le nouveau plateau est affiché, et le joueur noir peut alors jouer son tour, de la même façon. La partie continue ainsi jusqu'à ce que l'un des rois soit capturé, marquant la défaite du joueur concerné.

## Version en ligne, avec interface graphique
à venir