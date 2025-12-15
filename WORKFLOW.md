# Guide de Workflow Git - Planification Personnelle Intelligente

## Contexte

Ce projet utilise la branche `main` comme branche principale. Historiquement, des commits ont été faits sur `master`, ce qui a créé une divergence entre les deux branches.

## Solution Appliquée

Le code de la branche `master` a été fusionné dans `main` pour synchroniser tout le code :
- **10 commits** de `master` ont été intégrés
- Les conflits ont été résolus (noms de packages normalisés à `entities` en minuscule)
- Les erreurs de compilation ont été corrigées

## Bonnes Pratiques pour l'Avenir

### 1. Utiliser une seule branche principale
- **Recommandé** : Utiliser `main` comme branche principale
- Tous les nouveaux développements doivent être basés sur `main`

### 2. Pour les nouveaux développements
```bash
# Créer une nouvelle branche de feature
git checkout main
git pull origin main
git checkout -b feature/nom-de-la-fonctionnalite

# Faire vos modifications et commits
git add .
git commit -m "Description des changements"

# Pousser vers GitHub
git push origin feature/nom-de-la-fonctionnalite
```

### 3. Si du code est poussé par erreur sur master
```bash
# Depuis votre branche de travail
git fetch origin master
git merge origin/master --allow-unrelated-histories

# Résoudre les conflits si nécessaire
# Puis commit et push
```

### 4. Pour éviter la confusion
- Considérer la suppression ou l'archivage de la branche `master`
- Configurer `main` comme branche par défaut dans les paramètres GitHub

## État Actuel du Projet

### Fichiers Principaux
- **Entités Java** : `Activite`, `Contrainte`, `Conflit`, `Utilisateur`
- **Enums** : `TypeActivite`, `TypeContrainte`, `TypeConflit`
- **Base de données** : `personal_planner.sql` (schéma MySQL/MariaDB)
- **Configuration Eclipse** : `.classpath`, `.project`, `.settings/`

### Structure des Packages
Tous les fichiers Java utilisent le package `entities` (en minuscule) pour la cohérence.

### Compilation
Le projet peut être compilé avec :
```bash
javac -d bin -sourcepath src src/entities/*.java
```

## Résumé des Changements (master → main)

- ✅ Classes `Utilisateur.java` ajoutée avec gestion sécurisée des mots de passe
- ✅ Schéma de base de données SQL ajouté
- ✅ Configuration Eclipse ajoutée
- ✅ `.gitignore` amélioré pour exclure les fichiers de build
- ✅ Normalisation des noms de packages à `entities`
- ✅ Correction des erreurs de compilation
