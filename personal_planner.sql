-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1:3306
-- Généré le : ven. 05 déc. 2025 à 08:59
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `personal_planner`
--

-- --------------------------------------------------------

--
-- Structure de la table `activite`
--

CREATE TABLE `activite` (
  `id_activite` int(11) NOT NULL,
  `titre` varchar(200) NOT NULL,
  `type_activite` enum('Sport','Etude','Loisirs','Repos','Travail') NOT NULL,
  `description` text DEFAULT NULL,
  `priorite` int(11) DEFAULT NULL,
  `deadline` datetime DEFAULT NULL,
  `horaire_debut` datetime DEFAULT NULL,
  `horaire_fin` datetime DEFAULT NULL,
  `id_utilisateur` int(11) NOT NULL,
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `conflit`
--

CREATE TABLE `conflit` (
  `id_conflit` int(11) NOT NULL,
  `horaire_detection` datetime NOT NULL,
  `type_conflit` enum('Chevauchement des activités','Violation de contrainte','Fatigue Excessive','Deadline','Équilibre Faible','Repos Insuffisant') NOT NULL,
  `resolu` tinyint(1) DEFAULT 0,
  `id_statistique` int(11) DEFAULT NULL,
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `contrainte`
--

CREATE TABLE `contrainte` (
  `id_contrainte` int(11) NOT NULL,
  `type_contrainte` enum('Sommeil','Travail','RDV','Repos','Cours') NOT NULL,
  `heure_debut` time NOT NULL,
  `heure_fin` time NOT NULL,
  `repetitif` tinyint(1) DEFAULT 0,
  `jour` varchar(20) DEFAULT NULL,
  `id_activite` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `statistique`
--

CREATE TABLE `statistique` (
  `id_statistique` int(11) NOT NULL,
  `type_statistique` enum('Hebdomadaire','Quotidien') NOT NULL,
  `date_debut` date NOT NULL,
  `date_fin` date NOT NULL,
  `id_activite` int(11) NOT NULL,
  `id_utilisateur` int(11) NOT NULL,
  `ratio_travail_repos` decimal(5,2) DEFAULT NULL,
  `moyenne_activites_par_jour` decimal(5,2) DEFAULT NULL,
  `score_fatigue` decimal(5,2) DEFAULT NULL,
  `niveau_equilibre` enum('Excellent','Bon','Moyen','Faible') DEFAULT NULL,
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `utilisateur`
--

CREATE TABLE `utilisateur` (
  `id_utilisateur` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `age` int(11) DEFAULT NULL,
  `genre` varchar(20) DEFAULT NULL,
  `poste` varchar(100) DEFAULT NULL,
  `motdepassehash` varchar(255) DEFAULT NULL,
  `salt` varchar(255) DEFAULT NULL,
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `activite`
--
ALTER TABLE `activite`
  ADD PRIMARY KEY (`id_activite`),
  ADD KEY `id_utilisateur` (`id_utilisateur`);

--
-- Index pour la table `conflit`
--
ALTER TABLE `conflit`
  ADD PRIMARY KEY (`id_conflit`),
  ADD KEY `id_statistique` (`id_statistique`);

--
-- Index pour la table `contrainte`
--
ALTER TABLE `contrainte`
  ADD PRIMARY KEY (`id_contrainte`),
  ADD KEY `id_activite` (`id_activite`);

--
-- Index pour la table `statistique`
--
ALTER TABLE `statistique`
  ADD PRIMARY KEY (`id_statistique`),
  ADD KEY `id_activite` (`id_activite`),
  ADD KEY `id_utilisateur` (`id_utilisateur`);

--
-- Index pour la table `utilisateur`
--
ALTER TABLE `utilisateur`
  ADD PRIMARY KEY (`id_utilisateur`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `activite`
--
ALTER TABLE `activite`
  MODIFY `id_activite` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `conflit`
--
ALTER TABLE `conflit`
  MODIFY `id_conflit` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `contrainte`
--
ALTER TABLE `contrainte`
  MODIFY `id_contrainte` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `statistique`
--
ALTER TABLE `statistique`
  MODIFY `id_statistique` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `utilisateur`
--
ALTER TABLE `utilisateur`
  MODIFY `id_utilisateur` int(11) NOT NULL AUTO_INCREMENT;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `activite`
--
ALTER TABLE `activite`
  ADD CONSTRAINT `activite_ibfk_1` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateur` (`id_utilisateur`) ON DELETE CASCADE;

--
-- Contraintes pour la table `conflit`
--
ALTER TABLE `conflit`
  ADD CONSTRAINT `conflit_ibfk_1` FOREIGN KEY (`id_statistique`) REFERENCES `statistique` (`id_statistique`) ON DELETE SET NULL;

--
-- Contraintes pour la table `contrainte`
--
ALTER TABLE `contrainte`
  ADD CONSTRAINT `contrainte_ibfk_1` FOREIGN KEY (`id_activite`) REFERENCES `activite` (`id_activite`) ON DELETE CASCADE;

--
-- Contraintes pour la table `statistique`
--
ALTER TABLE `statistique`
  ADD CONSTRAINT `statistique_ibfk_1` FOREIGN KEY (`id_activite`) REFERENCES `activite` (`id_activite`) ON DELETE CASCADE,
  ADD CONSTRAINT `statistique_ibfk_2` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateur` (`id_utilisateur`) ON DELETE CASCADE;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

ALTER TABLE `utilisateur`
ADD COLUMN `motdepassehash` varchar(255) DEFAULT NULL AFTER `poste`,
ADD COLUMN `salt` varchar(255) DEFAULT NULL AFTER `motdepassehash`;
COMMIT;