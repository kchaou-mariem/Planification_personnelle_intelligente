package test;

import service.impl.ConflitServiceImpl;
import service.ConflitService;
import entities.Conflit;

import java.util.List;

public class TestConflitService {
    public static void main(String[] args) {
        ConflitService service = new ConflitServiceImpl();

        Long idUtilisateur = 1L; // Adapter selon vos données

        System.out.println("=== Détection des chevauchements ===");
        List<Conflit> detects = service.detecterChevauchementsUtilisateur(idUtilisateur);
        System.out.println("Conflits détectés: " + detects.size());

        System.out.println("=== Résolution automatique des chevauchements ===");
        int resolus = service.resoudreChevauchementsUtilisateur(idUtilisateur);
        System.out.println("Conflits résolus automatiquement: " + resolus);

        if (!detects.isEmpty()) {
            System.out.println("=== Marquage manuel d'un conflit comme résolu ===");
            boolean ok = service.marquerConflitCommeResolu(detects.get(0).getidConflit());
            System.out.println("Marqué: " + ok);
        }
    }
}
