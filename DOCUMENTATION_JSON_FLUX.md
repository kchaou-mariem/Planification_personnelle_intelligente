/**
 * EXPLICATION DÉTAILLÉE : Stockage et récupération des données avec JSON
 * 
 * =========================================================================
 * SCÉNARIO 1 : INSERTION D'UNE NOUVELLE CONTRAINTE
 * =========================================================================
 * 
 * Supposons que vous créez une contrainte avec les données suivantes en Java :
 * 
 *   Contrainte c = new Contrainte(
 *     "Pause déjeuner",                          // titre
 *     TypeContrainte.REPOS,                      // type
 *     LocalTime.of(12, 0),                       // dateHeureDeb (heure de début)
 *     LocalTime.of(13, 0),                       // dateHeureFin (heure de fin)
 *     true,                                      // repetitif (oui, c'est répétitif)
 *     Arrays.asList(LocalDate.of(2025, 12, 25)), // datesSpecifiques (noël)
 *     Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY) // joursSemaine (lundi et mercredi)
 *   );
 *   c.setUtilisateurId(1L);
 * 
 * ---
 * ÉTAPE 1 : Avant l'insertion (état Java)
 * ---
 * 
 * Objet Contrainte en mémoire :
 * {
 *   id: null (sera généré par auto-increment),
 *   titre: "Pause déjeuner",
 *   type: REPOS,
 *   dateHeureDeb: 12:00:00,
 *   dateHeureFin: 13:00:00,
 *   repetitif: true,
 *   datesSpecifiques: [2025-12-25],
 *   joursSemaine: [MONDAY, WEDNESDAY],
 *   utilisateurId: 1
 * }
 * 
 * ---
 * ÉTAPE 2 : Sérialisation JSON (conversion Java -> JSON textuel)
 * ---
 * 
 * Le code DAO appelle ContrainteDAOImpl.ajouter(c).
 * 
 * À l'intérieur, les listes sont converties en JSON avec les méthodes :
 * 
 * • datesToJson(List<LocalDate> dates) :
 *   Input:  [2025-12-25]
 *   Output: "[\"2025-12-25\"]"
 * 
 * • joursToJson(List<DayOfWeek> jours) :
 *   Input:  [MONDAY, WEDNESDAY]
 *   Output: "[\"MONDAY\",\"WEDNESDAY\"]"
 * 
 * Les heures (LocalTime) sont converties en TIME SQL :
 * • LocalTime.of(12, 0) -> "12:00:00" (type TIME)
 * • LocalTime.of(13, 0) -> "13:00:00" (type TIME)
 * 
 * ---
 * ÉTAPE 3 : Insertion en base de données
 * ---
 * 
 * La requête SQL préparée ressemble à :
 * 
 *   INSERT INTO contrainte 
 *     (type_contrainte, heure_debut, heure_fin, repetitif, id_activite, dates_specifiques, jours)
 *   VALUES
 *     (?, ?, ?, ?, ?, ?, ?)
 * 
 * Les paramètres sont liés comme suit :
 * 
 *   ps.setString(1, "Repos");                              // type_contrainte (enum convertie en string)
 *   ps.setTime(2, Time.valueOf("12:00:00"));              // heure_debut
 *   ps.setTime(3, Time.valueOf("13:00:00"));              // heure_fin
 *   ps.setInt(4, 1);                                       // repetitif (true -> 1)
 *   ps.setLong(5, 1L);                                     // id_activite (utilisateurId)
 *   ps.setString(6, "[\"2025-12-25\"]");                  // dates_specifiques (JSON text)
 *   ps.setString(7, "[\"MONDAY\",\"WEDNESDAY\"]");        // jours (JSON text)
 * 
 * ---
 * ÉTAPE 4 : Résultat dans la base de données
 * ---
 * 
 * La ligne insérée dans la table `contrainte` :
 * 
 *   ┌─────────────────┬──────────────────┬──────────┬───────────┬──────────┬────────────┬──────────────────────────────────┬──────────────────────────────┐
 *   │ id_contrainte   │ type_contrainte  │ heure_   │ heure_fin │ repetitif│ id_activite│ dates_specifiques                │ jours                        │
 *   │                 │                  │ debut    │           │          │            │                                  │                              │
 *   ├─────────────────┼──────────────────┼──────────┼───────────┼──────────┼────────────┼──────────────────────────────────┼──────────────────────────────┤
 *   │ 1 (auto-incr)   │ Repos            │ 12:00:00 │ 13:00:00  │ 1        │ 1          │ ["2025-12-25"]                   │ ["MONDAY","WEDNESDAY"]       │
 *   └─────────────────┴──────────────────┴──────────┴───────────┴──────────┴────────────┴──────────────────────────────────┴──────────────────────────────┘
 * 
 * Les colonnes JSON (dates_specifiques et jours) stockent du texte JSON.
 * Ce ne sont PAS des colonnes JSON natives MySQL (type JSON), juste du TEXT/VARCHAR.
 * 
 * 
 * =========================================================================
 * SCÉNARIO 2 : RÉCUPÉRATION / RECHERCHE D'UNE CONTRAINTE
 * =========================================================================
 * 
 * Vous faites une recherche :
 * 
 *   ContrainteDAOImpl dao = new ContrainteDAOImpl();
 *   Optional<Contrainte> result = dao.getById(1L);
 * 
 * ---
 * ÉTAPE 1 : Requête SQL SELECT
 * ---
 * 
 *   SELECT id_contrainte, type_contrainte, heure_debut, heure_fin, repetitif, 
 *          id_activite, dates_specifiques, jours
 *   FROM contrainte
 *   WHERE id_contrainte = 1
 * 
 * ---
 * ÉTAPE 2 : Lecture depuis ResultSet
 * ---
 * 
 * Le ResultSet contient une ligne :
 * 
 *   id_contrainte: 1
 *   type_contrainte: "Repos"
 *   heure_debut: 12:00:00
 *   heure_fin: 13:00:00
 *   repetitif: 1
 *   id_activite: 1
 *   dates_specifiques: "[\"2025-12-25\"]"  (STRING, pas parsed encore)
 *   jours: "[\"MONDAY\",\"WEDNESDAY\"]"    (STRING, pas parsed encore)
 * 
 * ---
 * ÉTAPE 3 : Désérialisation JSON (conversion JSON textuel -> objets Java)
 * ---
 * 
 * La méthode mapRow(ResultSet rs) est appelée pour transformer les données :
 * 
 * 1) Conversion des heures (TIME -> LocalTime) :
 *    rs.getTime("heure_debut") -> Time.valueOf("12:00:00") -> LocalTime.of(12, 0)
 *    rs.getTime("heure_fin")   -> Time.valueOf("13:00:00") -> LocalTime.of(13, 0)
 * 
 * 2) Conversion du type enum (String -> TypeContrainte) :
 *    rs.getString("type_contrainte") -> "Repos" -> TypeContrainte.REPOS
 * 
 * 3) Conversion des dates JSON (String JSON -> List<LocalDate>) :
 *    
 *    parseDatesFromJson("[\"2025-12-25\"]") fait :
 *    
 *    a) Utilise une expression régulière pour extraire les valeurs entre guillemets :
 *       Pattern: "\"([^\"]*)\"" 
 *       Trouvé: "2025-12-25"
 *    
 *    b) Parse chaque chaîne trouvée en LocalDate :
 *       LocalDate.parse("2025-12-25") -> LocalDate.of(2025, 12, 25)
 *    
 *    c) Retourne la liste :
 *       [LocalDate.of(2025, 12, 25)]
 * 
 * 4) Conversion des jours JSON (String JSON -> List<DayOfWeek>) :
 *    
 *    parseJoursFromJson("[\"MONDAY\",\"WEDNESDAY\"]") fait :
 *    
 *    a) Extrait les valeurs entre guillemets :
 *       Trouvé: "MONDAY", "WEDNESDAY"
 *    
 *    b) Parse chaque valeur en DayOfWeek enum :
 *       DayOfWeek.valueOf("MONDAY")    -> DayOfWeek.MONDAY
 *       DayOfWeek.valueOf("WEDNESDAY") -> DayOfWeek.WEDNESDAY
 *    
 *    c) Retourne la liste :
 *       [DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY]
 * 
 * ---
 * ÉTAPE 4 : Objet Java reconstitué
 * ---
 * 
 * Après mapRow(), l'objet Contrainte en mémoire est :
 * 
 *   Contrainte {
 *     id: 1,
 *     titre: "Pause déjeuner",
 *     type: REPOS,
 *     dateHeureDeb: 12:00:00,
 *     dateHeureFin: 13:00:00,
 *     repetitif: true,
 *     datesSpecifiques: [2025-12-25],
 *     joursSemaine: [MONDAY, WEDNESDAY],
 *     utilisateurId: 1
 *   }
 * 
 * C'est l'objet que votre application utilise normalement !
 * 
 * ---
 * ÉTAPE 5 : Utilisation en application
 * ---
 * 
 *   result.ifPresentOrElse(
 *     contrainte -> {
 *       System.out.println("Contrainte trouvée: " + contrainte.getTitre());
 *       System.out.println("Jours: " + contrainte.getJoursSemaine()); // [MONDAY, WEDNESDAY]
 *       System.out.println("Dates: " + contrainte.getDatesSpecifiques()); // [2025-12-25]
 *     },
 *     () -> System.out.println("Contrainte non trouvée")
 *   );
 * 
 * 
 * =========================================================================
 * SCÉNARIO 3 : MODIFICATION D'UNE CONTRAINTE
 * =========================================================================
 * 
 * Vous modifiez la contrainte (ex: ajouter le vendredi aux jours) :
 * 
 *   contrainte.getJoursSemaine().add(DayOfWeek.FRIDAY);
 *   dao.modifier(contrainte);
 * 
 * ---
 * ÉTAPE 1 : Avant modification
 * ---
 * 
 *   joursSemaine: [MONDAY, WEDNESDAY]
 * 
 * ---
 * ÉTAPE 2 : Après ajout
 * ---
 * 
 *   joursSemaine: [MONDAY, WEDNESDAY, FRIDAY]
 * 
 * ---
 * ÉTAPE 3 : Sérialisation JSON pour UPDATE
 * ---
 * 
 *   joursToJson([MONDAY, WEDNESDAY, FRIDAY]) 
 *   -> "[\"MONDAY\",\"WEDNESDAY\",\"FRIDAY\"]"
 * 
 * ---
 * ÉTAPE 4 : Requête SQL UPDATE
 * ---
 * 
 *   UPDATE contrainte 
 *   SET jours = "[\"MONDAY\",\"WEDNESDAY\",\"FRIDAY\"]"
 *   WHERE id_contrainte = 1
 * 
 * ---
 * ÉTAPE 5 : Nouvelle ligne en base
 * ---
 * 
 *   ┌─────────────────┬──────────────────┬───────────┬───────────┬──────────┬────────────┬──────────────────────────────────┬─────────────────────────────────────────┐
 *   │ id_contrainte   │ type_contrainte  │ heure_    │ heure_fin │ repetitif│ id_activite│ dates_specifiques                │ jours                                   │
 *   │                 │                  │ debut     │           │          │            │                                  │                                         │
 *   ├─────────────────┼──────────────────┼───────────┼───────────┼──────────┼────────────┼──────────────────────────────────┼─────────────────────────────────────────┤
 *   │ 1               │ Repos            │ 12:00:00  │ 13:00:00  │ 1        │ 1          │ ["2025-12-25"]                   │ ["MONDAY","WEDNESDAY","FRIDAY"]         │
 *   └─────────────────┴──────────────────┴───────────┴───────────┴──────────┴────────────┴──────────────────────────────────┴─────────────────────────────────────────┘
 * 
 * 
 * =========================================================================
 * AVANTAGES DU JSON VS AUTRES APPROCHES
 * =========================================================================
 * 
 * 1. JSON vs CSV simple (ex: "MONDAY,WEDNESDAY") :
 *    • JSON : structure claire, facile à parser, pas d'ambiguïté sur délimiteurs
 *    • CSV  : plus simple à lire manuellement en DB, mais fragile si données contiennent ','
 * 
 * 2. JSON vs Tables auxiliaires (contrainte_date, contrainte_jour) :
 *    • JSON : une seule colonne, pas de jointures, plus rapide pour petites listes
 *    • Tables : permet des requêtes SQL complexes (WHERE jour = 'MONDAY'), normalisé
 * 
 * 3. JSON vs Stockage uniquement en mémoire/fichier :
 *    • JSON en DB : persiste les données, interrogeable depuis n'importe quel client
 *    • Mémoire : rapide mais volatile, perte si crash
 * 
 * 
 * =========================================================================
 * CAS D'USAGE : RECHERCHE INTELLIGENTE (futur)
 * =========================================================================
 * 
 * Actuellement, le parsing JSON se fait côté Java.
 * 
 * Si votre MySQL/MariaDB supporte JSON (v5.7+) :
 * 
 *   -- Chercher toutes les contraintes qui incluent MONDAY
 *   SELECT * FROM contrainte 
 *   WHERE JSON_CONTAINS(jours, '\"MONDAY\"');
 * 
 *   -- Chercher toutes les contraintes avec la date 2025-12-25
 *   SELECT * FROM contrainte 
 *   WHERE JSON_CONTAINS(dates_specifiques, '\"2025-12-25\"');
 * 
 * Cela éviterait de charger toutes les contraintes en Java et de filtrer en mémoire.
 * 
 * 
 * =========================================================================
 * RÉSUMÉ VISUEL DU FLUX
 * =========================================================================
 * 
 * INSERTION :
 *   Java Contrainte object
 *        ↓
 *   datesToJson() / joursToJson() [serialization]
 *        ↓
 *   String JSON: "[\"2025-12-25\"]" / "[\"MONDAY\",\"WEDNESDAY\"]"
 *        ↓
 *   INSERT SQL -> Base de données
 *        ↓
 *   Colonne TEXT/VARCHAR contient le JSON textuel
 * 
 * RÉCUPÉRATION :
 *   SELECT query -> ResultSet
 *        ↓
 *   String JSON: "[\"2025-12-25\"]" / "[\"MONDAY\",\"WEDNESDAY\"]"
 *        ↓
 *   parseDatesFromJson() / parseJoursFromJson() [deserialization]
 *        ↓
 *   Java List<LocalDate> / List<DayOfWeek>
 *        ↓
 *   Contrainte object (complet, prêt à utiliser)
 */
