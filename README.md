# Planification Personnelle Intelligente

Application Java de planification personnelle avec gestion d'activités, contraintes et détection de conflits.

## 📋 Description

Ce projet implémente un système de planification personnelle intelligent permettant de :
- Gérer des activités avec priorités et deadlines
- Définir des contraintes temporelles
- Détecter automatiquement les conflits de planning
- Gérer des utilisateurs avec authentification sécurisée

## 🏗️ Structure du Projet

### Entités Principales
- **Utilisateur** : Gestion des utilisateurs avec hashage sécurisé des mots de passe (SHA-256 + Salt)
- **Activite** : Représente une activité planifiée (Sport, Étude, Loisirs, Repos, Travail)
- **Contrainte** : Définit les contraintes temporelles (Sommeil, Travail, RDV, Repos, Cours)
- **Conflit** : Détecte et gère les conflits entre activités/contraintes

### Technologies
- **Langage** : Java
- **Base de données** : MySQL/MariaDB
- **IDE** : Eclipse (configuration incluse)

## 🚀 Démarrage Rapide

### Prérequis
- Java JDK 8 ou supérieur
- MySQL/MariaDB (optionnel, pour la persistance)
- Eclipse (recommandé) ou tout autre IDE Java

### Compilation
```bash
javac -d bin -sourcepath src src/entities/*.java
```

### Base de Données
Importer le schéma depuis `personal_planner.sql` :
```bash
mysql -u [utilisateur] -p [nom_base] < personal_planner.sql
```

## 📚 Documentation

- [Guide de Workflow Git](WORKFLOW.md) - Bonnes pratiques pour contribuer au projet

## 🤝 Contribution

Consultez le fichier [WORKFLOW.md](WORKFLOW.md) pour les bonnes pratiques de développement et de gestion des branches.

## 📝 Notes

Ce projet est développé dans le cadre d'un mini-projet académique.
