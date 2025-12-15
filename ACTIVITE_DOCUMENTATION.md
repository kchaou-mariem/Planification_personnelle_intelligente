# Documentation Complète - Classe Activite

## 📋 Vue d'ensemble

Ce document décrit l'implémentation complète de la gestion des activités dans le système de planification personnelle intelligente. Il couvre :
- L'entité `Activite`
- L'énumération `TypeActivite`
- L'interface et l'implémentation DAO
- Le service métier
- Les tests unitaires

---

## 📚 Structures de Données

### 1. **TypeActivite (Énumération)**
**Fichier:** `src/Entities/TypeActivite.java`

Types d'activités disponibles :
- `sport` - Activités de sport et fitness
- `Etude` - Activités d'étude et d'apprentissage
- `Loisirs` - Activités de loisirs et divertissement
- `Repos` - Activités de repos et relaxation
- `Travail` - Activités de travail professionnel

**Exemple d'utilisation:**
```java
TypeActivite type = TypeActivite.Travail;
String label = type.getLabel(); // "Travail"
TypeActivite t = TypeActivite.fromLabel("Sport"); // Retourne TypeActivite.sport
```

### 2. **Activite (Entité)**
**Fichier:** `src/Entities/Activite.java`

#### Attributs:
| Attribut | Type | Description |
|----------|------|-------------|
| `idActivite` | Long | Identifiant unique |
| `titre` | String | Titre de l'activité |
| `description` | String | Description détaillée |
| `type` | TypeActivite | Type d'activité |
| `duree` | int | Durée en minutes |
| `priorite` | int | Priorité (1-10) |
| `deadline` | LocalDateTime | Date limite de l'activité |
| `horaireDebut` | LocalDateTime | Heure de début |
| `horaireFin` | LocalDateTime | Heure de fin |
| `idUtilisateur` | Long | ID de l'utilisateur propriétaire |
| `completee` | boolean | Statut de complétude |

#### Constructeurs:
```java
// Constructeur avec ID (pour récupération BD)
new Activite(id, titre, description, type, duree, priorite, 
             deadline, horaireDebut, horaireFin, idUtilisateur, completee)

// Constructeur sans ID (pour création)
new Activite(titre, description, type, duree, priorite, 
             deadline, horaireDebut, horaireFin, idUtilisateur)
```

---

## 🗄️ Couche Accès Données (DAO)

### Interface ActiviteDAO
**Fichier:** `src/dao/interfaces/ActiviteDAO.java`

#### Opérations CRUD de Base:
```java
Long ajouter(Activite activite)                    // Ajouter une activité
boolean modifier(Activite activite)                // Modifier une activité
boolean supprimer(Long idActivite)                 // Supprimer une activité
Optional<Activite> getById(Long idActivite)        // Récupérer par ID
List<Activite> getAll()                            // Récupérer toutes les activités
```

#### Opérations de Recherche et Filtrage:
```java
List<Activite> getByUtilisateur(Long idUtilisateur)
List<Activite> getByType(TypeActivite type)
List<Activite> getByTypeAndUtilisateur(Long idUtilisateur, TypeActivite type)
List<Activite> getActivitesNonCompletees()
List<Activite> getActivitesCompletees()
List<Activite> getActivitesNonCompleteesByUtilisateur(Long idUtilisateur)
List<Activite> getByPeriode(LocalDateTime dateDebut, LocalDateTime dateFin)
List<Activite> getByUtilisateurAndPeriode(Long idUtilisateur, LocalDateTime dateDebut, LocalDateTime dateFin)
List<Activite> getActivitesAvecDeadlineProche(int joursAvance)
List<Activite> getByPriorite(int priorite)
List<Activite> rechercherParMotCle(String motCle)
List<Activite> rechercherParMotCleUtilisateur(Long idUtilisateur, String motCle)
```

#### Opérations Métier:
```java
boolean marquerCommeCompletee(Long idActivite)
int marquerPlusieursCommeCompletees(List<Long> idsActivites)
boolean marquerCommeNonCompletee(Long idActivite)
boolean hasChevauchement(Long idActivite, LocalDateTime horaireDebut, LocalDateTime horaireFin)
List<Activite> getActivitesChevauchantes(LocalDateTime horaireDebut, LocalDateTime horaireFin)
List<Activite> getActivitesChevauchantesUtilisateur(Long idUtilisateur, LocalDateTime horaireDebut, LocalDateTime horaireFin)
```

#### Statistiques:
```java
int compterToutesLesActivites()
int compterActivitesUtilisateur(Long idUtilisateur)
int compterActivitesCompletees()
int compterActivitesNonCompletees()
int compterParType(TypeActivite type)
int calculerDureeTotalActivites()
int calculerDureeTotalActivitesUtilisateur(Long idUtilisateur)
List<Activite> getActivitesRecentes(int limite)
List<Activite> getActivitesHautePriorite()
```

### Implémentation ActiviteDAOImpl
**Fichier:** `src/dao/impl/ActiviteDAOImpl.java`

Implémente l'interface `ActiviteDAO` avec opérations sur la base de données MySQL.

**Exemple d'utilisation:**
```java
ActiviteDAO dao = new ActiviteDAOImpl();

// Créer une activité
Activite activite = new Activite("Réunion", "Réunion importante", 
                                  TypeActivite.Travail, 90, 8,
                                  LocalDateTime.now().plusDays(1),
                                  LocalDateTime.of(2024, 12, 20, 10, 0),
                                  LocalDateTime.of(2024, 12, 20, 11, 30),
                                  1L);
Long idCreated = dao.ajouter(activite);

// Récupérer une activité
Optional<Activite> activiteOpt = dao.getById(idCreated);

// Modifier
if (activiteOpt.isPresent()) {
    Activite a = activiteOpt.get();
    a.setPriorite(9);
    dao.modifier(a);
}

// Supprimer
dao.supprimer(idCreated);
```

---

## 🎯 Couche Service

### Interface ActiviteService
**Fichier:** `src/service/ActiviteService.java`

Service métier qui encapsule la logique applicative et la validation.

#### Opérations CRUD avec Validation:
```java
Long creerActivite(Activite activite)              // Créer avec validation
boolean mettreAJourActivite(Activite activite)    // Mettre à jour avec validation
boolean supprimerActivite(Long idActivite)
Optional<Activite> obtenirActivite(Long idActivite)
List<Activite> obtenirToutesLesActivites()
```

#### Recherche et Filtrage:
```java
List<Activite> obtenirActivitesUtilisateur(Long idUtilisateur)
List<Activite> obtenirActivitesParType(TypeActivite type)
List<Activite> obtenirActivitesNonCompletees(Long idUtilisateur)
List<Activite> obtenirActivitesCompletees(Long idUtilisateur)
List<Activite> obtenirActivitesDansLaPeriode(LocalDateTime dateDebut, LocalDateTime dateFin)
List<Activite> obtenirActivitesUtilisateurDansLaPeriode(Long idUtilisateur, LocalDateTime dateDebut, LocalDateTime dateFin)
List<Activite> rechercherActivites(String motCle)
List<Activite> rechercherActivitesUtilisateur(Long idUtilisateur, String motCle)
```

#### Opérations Métier:
```java
boolean completerActivite(Long idActivite)
boolean decompleterActivite(Long idActivite)
boolean verifierChevauchement(Long idUtilisateur, LocalDateTime horaireDebut, LocalDateTime horaireFin)
List<Activite> obtenirActivitesChevauchantes(Long idUtilisateur, LocalDateTime horaireDebut, LocalDateTime horaireFin)
```

#### Validations:
```java
boolean validerHoraires(LocalDateTime horaireDebut, LocalDateTime horaireFin)
boolean validerDuree(int duree, LocalDateTime horaireDebut, LocalDateTime horaireFin)
boolean validerPriorite(int priorite)
```

#### Statistiques:
```java
int obtenirNombreTotalActivites()
int obtenirNombreActivitesUtilisateur(Long idUtilisateur)
int obtenirNombreActivitesCompletees()
int obtenirNombreActivitesNonCompletees()
double obtenirTauxCompletion()
double obtenirTauxCompletionUtilisateur(Long idUtilisateur)
int obtenirDureeTotalActivites()
int obtenirDureeTotalActivitesUtilisateur(Long idUtilisateur)
List<Activite> obtenirActivitesDeadlineProche(int joursAvance)
List<Activite> obtenirActivitesHautePriorite()
List<Activite> obtenirActivitesRecentes(int nombre)
```

### Implémentation ActiviteServiceImpl
**Fichier:** `src/service/impl/ActiviteServiceImpl.java`

Implémentation du service avec:
- Validation complète des données
- Gestion des chevauchements
- Calcul de statistiques
- Gestion des erreurs

**Exemple d'utilisation:**
```java
ActiviteService service = new ActiviteServiceImpl();

// Créer une activité (avec validation)
Activite activite = new Activite("Développement", "Coder le module X",
                                  TypeActivite.Travail, 240, 9,
                                  LocalDateTime.of(2024, 12, 25, 17, 0),
                                  LocalDateTime.of(2024, 12, 20, 9, 0),
                                  LocalDateTime.of(2024, 12, 20, 13, 0),
                                  1L);

Long id = service.creerActivite(activite);
if (id > 0) {
    System.out.println("Activité créée: " + id);
} else {
    System.out.println("Erreur de création");
}

// Vérifier chevauchement
boolean chevauchement = service.verifierChevauchement(1L, 
    LocalDateTime.of(2024, 12, 20, 10, 0),
    LocalDateTime.of(2024, 12, 20, 11, 0));

// Obtenir statistiques
double taux = service.obtenirTauxCompletion();
int total = service.obtenirNombreTotalActivites();
```

---

## 🧪 Tests

### TestActiviteDAO
**Fichier:** `src/test/TestActiviteDAO.java`

Tests complets des opérations DAO:
- Ajout d'activités
- Récupération par ID
- Modification
- Suppression
- Recherche par type, utilisateur, période
- Détection des chevauchements
- Marquage comme complété
- Recherche par mot-clé
- Statistiques

**Exécution:**
```bash
javac -cp ".:mysql-connector-java.jar" src/test/TestActiviteDAO.java
java -cp ".:mysql-connector-java.jar" test.TestActiviteDAO
```

### TestActiviteService
**Fichier:** `src/test/TestActiviteService.java`

Tests de validation et cas limites:
- Validation des horaires
- Validation de la durée
- Validation de la priorité
- Validation d'activités complètes
- Cas limites (valeurs null, invalides)
- Statistiques par utilisateur
- Activités deadline proche
- Activités haute priorité

**Exécution:**
```bash
javac -cp ".:mysql-connector-java.jar" src/test/TestActiviteService.java
java -cp ".:mysql-connector-java.jar" test.TestActiviteService
```

---

## 💾 Schéma Base de Données

La table `activite` doit avoir la structure suivante :

```sql
CREATE TABLE activite (
    id_activite BIGINT PRIMARY KEY AUTO_INCREMENT,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    type_activite VARCHAR(50) NOT NULL,
    duree INT NOT NULL,
    priorite INT NOT NULL CHECK (priorite BETWEEN 1 AND 10),
    deadline DATETIME NOT NULL,
    horaire_debut DATETIME NOT NULL,
    horaire_fin DATETIME NOT NULL,
    id_utilisateur BIGINT NOT NULL,
    completee TINYINT(1) DEFAULT 0,
    FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id_utilisateur) ON DELETE CASCADE,
    INDEX idx_utilisateur (id_utilisateur),
    INDEX idx_type (type_activite),
    INDEX idx_deadline (deadline),
    INDEX idx_horaires (horaire_debut, horaire_fin)
);
```

---

## 📊 Exemples Complets

### Créer et gérer une activité

```java
ActiviteService service = new ActiviteServiceImpl();

// 1. Créer une activité
LocalDateTime debut = LocalDateTime.of(2024, 12, 20, 14, 0);
LocalDateTime fin = LocalDateTime.of(2024, 12, 20, 16, 0);
LocalDateTime deadline = LocalDateTime.of(2024, 12, 21, 17, 0);

Activite activite = new Activite(
    "Révisions Examen",
    "Revoir les chapitres 5 et 6",
    TypeActivite.Etude,
    120,  // 2 heures
    9,    // priorité haute
    deadline,
    debut,
    fin,
    1L    // ID utilisateur
);

Long idActivite = service.creerActivite(activite);
System.out.println("Activité créée: " + idActivite);

// 2. Vérifier chevauchement avant d'ajouter une autre
boolean chevauchement = service.verifierChevauchement(1L, 
    LocalDateTime.of(2024, 12, 20, 15, 0),
    LocalDateTime.of(2024, 12, 20, 16, 30));
    
if (chevauchement) {
    System.out.println("Conflit détecté!");
    List<Activite> conflits = service.obtenirActivitesChevauchantes(1L,
        LocalDateTime.of(2024, 12, 20, 15, 0),
        LocalDateTime.of(2024, 12, 20, 16, 30));
    for (Activite c : conflits) {
        System.out.println("  - " + c.getTitre());
    }
}

// 3. Marquer comme complétée
service.completerActivite(idActivite);

// 4. Obtenir les statistiques
System.out.println("Activités complétées: " + service.obtenirNombreActivitesCompletees());
System.out.printf("Taux de complétude: %.2f%%\n", service.obtenirTauxCompletion());

// 5. Obtenir les activités urgentes
List<Activite> urgentes = service.obtenirActivitesHautePriorite();
System.out.println("Activités haute priorité: " + urgentes.size());
```

---

## ✅ Règles de Validation

### Horaires
- La date/heure de début doit être **avant** la date/heure de fin
- Les deux ne peuvent pas être identiques
- Aucun chevauchement avec d'autres activités du même utilisateur

### Durée
- Doit être > 0
- Doit correspondre au calcul: `fin - début`

### Priorité
- Doit être entre 1 et 10 inclus

### Champs Obligatoires
- `titre` : non vide
- `type` : doit être spécifié
- `deadline` : doit être définie
- `idUtilisateur` : doit être > 0

---

## 🔄 Flux Typique d'Utilisation

```
Utilisateur
    ↓
Service (ActiviteService)
  ├─ Validation des données
  ├─ Vérification logique (chevauchements, etc.)
  └─ Gestion des erreurs
    ↓
DAO (ActiviteDAOImpl)
  ├─ Opérations SQL
  ├─ Conversion objet ↔ BD
  └─ Gestion des transactions
    ↓
Base de Données MySQL
```

---

## 📝 Notes Importantes

1. **Conversions d'énumération**: Les valeurs TypeActivite sont converties entre la base de données (français avec accents) et Java (noms constants)

2. **Gestion des transactions**: Les modifications critiques utilisent `conn.setAutoCommit(false)` pour assurer l'intégrité

3. **Validation en cascade**: Le Service valide avant le DAO, chaque DAO valide avant d'exécuter SQL

4. **Gestion des erreurs**: Tous les SQL sont enveloppés dans try-catch avec messages d'erreur détaillés

5. **Performance**: Les requêtes utilisent des INDEX sur `id_utilisateur`, `type_activite`, `deadline`, et les horaires

---

**Dernière mise à jour:** Décembre 2024
**Version:** 1.0
