# Memento Mori

Widget d'écran d'accueil Android : la vie entière en semaines, une case par semaine,
52 par ligne. Les semaines révolues sont pleines, la semaine en cours est rouge, celles
qui restent sont éteintes. Sous la grille, le nombre de semaines restantes.

Aucune permission, aucune dépendance, aucun réseau. Le widget se redessine une fois par jour.

## Installation

Via [Obtainium](https://github.com/ImranR98/Obtainium) : ajouter une application avec
l'URL `https://github.com/kvngch/memento-mori`, les mises à jour suivent les releases.
Sinon, télécharger l'APK de la dernière release et l'installer.

Poser le widget sur l'écran d'accueil, l'étirer à toute la grille, régler la date de
naissance dans le sélecteur qui s'ouvre. Un appui sur le widget rouvre ce réglage.

## Réglages

L'espérance de vie est fixée à 80 ans dans `EXPECTANCY_YEARS` (`app/src/main/java/fr/kvngch/memento/Life.kt`).

## Publier une version

1. Incrémenter `versionCode` et `versionName` dans `app/build.gradle.kts`.
2. Commiter, poser un tag `vX.Y.Z` et le pousser : la CI construit l'APK signé et le publie.

Le keystore de signature vit dans le vault (`PERSO/projects/memento-mori/_secrets/`),
ses copies sont chargées en secrets Actions. Le perdre casse les mises à jour.
