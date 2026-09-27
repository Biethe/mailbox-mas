package edu.aldermere.university.generator;

/** Fixed catalogs. Profile counts select a prefix of these lists; they are not dataset sizes. */
public final class Catalog {
    private Catalog() {}

    public record FacultySpec(String name, int slot) {}

    public record DeptSpec(String name, String prefix, int facultySlot) {}

    public record UnitSpec(String name, String description, String procedure) {}

    public static final FacultySpec[] FACULTIES = {
            new FacultySpec("Faculté des sciences", 0),
            new FacultySpec("Faculté d'ingénierie", 1),
            new FacultySpec("Faculté des humanités", 2),
            new FacultySpec("Faculté des sciences sociales", 3),
            new FacultySpec("Faculté de santé", 4)
    };

    public static final DeptSpec[] DEPARTMENTS = {
            new DeptSpec("Informatique", "CS", 0),
            new DeptSpec("Mathématiques", "MA", 0),
            new DeptSpec("Physique", "PH", 0),
            new DeptSpec("Biologie", "BI", 0),
            new DeptSpec("Chimie", "CH", 0),
            new DeptSpec("Génie électrique", "EE", 1),
            new DeptSpec("Génie mécanique", "ME", 1),
            new DeptSpec("Génie civil", "CE", 1),
            new DeptSpec("Histoire", "HI", 2),
            new DeptSpec("Linguistique", "LI", 2),
            new DeptSpec("Philosophie", "PL", 2),
            new DeptSpec("Économie", "EC", 3),
            new DeptSpec("Sociologie", "SO", 3),
            new DeptSpec("Études politiques", "PS", 3),
            new DeptSpec("Sciences infirmières", "NU", 4),
            new DeptSpec("Santé publique", "PU", 4)
    };

    public static final String[] BUILDINGS = {
            "Bâtiment des sciences", "Bloc d'ingénierie", "Cour des humanités", "Annexe de la bibliothèque", "Pavillon de recherche"
    };

    public static final UnitSpec[] UNITS = {
            new UnitSpec("Scolarité",
                    "La scolarité tient les inscriptions, les dossiers étudiants et les changements de régime.",
                    "Un changement d'inscription se dépose avant la fin de la deuxième semaine d'enseignement, depuis la messagerie universitaire."),
            new UnitSpec("Service informatique",
                    "Le service informatique ouvre les comptes et les messageries de l'université.",
                    "Un compte universitaire se demande au service informatique et est activé sous deux jours ouvrés."),
            new UnitSpec("Service de la recherche",
                    "Le service de la recherche enregistre les projets et la procédure de financement.",
                    "Une demande de financement se dépose au service de la recherche avant la date de début du projet."),
            new UnitSpec("Service financier",
                    "Le service financier publie les modalités de droits d'inscription et de paiement.",
                    "Les appels de droits sont émis par le service financier au début de chaque semestre."),
            new UnitSpec("Service des relations internationales",
                    "Le service des relations internationales accueille les étudiants arrivés de l'extérieur.",
                    "Les étudiants internationaux confirment leur adresse locale auprès du service pendant la première semaine."),
            new UnitSpec("Ressources humaines",
                    "Les ressources humaines tiennent les nominations et les congés du personnel.",
                    "Une demande de congé se dépose aux ressources humaines au moins cinq jours ouvrés à l'avance.")
    };

    public static final String[] COURSE_TOPICS = {
            "Fondements", "Méthodes de recherche", "Travaux de laboratoire", "Séminaire", "Étude intermédiaire",
            "Étude avancée", "Données et preuves", "Conception de systèmes", "Méthodes de terrain", "Projet de fin d'études"
    };

    public static final String[] PROJECT_STEMS = {
            "signaux", "archives", "terrain", "modèles", "matériaux", "langage", "systèmes de santé", "calcul"
    };

    public static final String[] FIRST_NAMES = {
            "Mara", "Jonas", "Priya", "Elena", "Hugo", "Nora", "Felix", "Amina", "Leo", "Ines",
            "Omar", "Clara", "Nils", "Sofia", "Idris", "Helena", "Mateo", "Yara", "Anton", "Leila",
            "Soren", "Adaeze", "Ruben", "Maya", "Tomas", "Freya", "Karim", "Lina", "Evan", "Noor",
            "Paolo", "Hana", "Viktor", "Aisha", "Nico", "Selma"
    };

    public static final String[] LAST_NAMES = {
            "Ellison", "Moreau", "Rahman", "Vogel", "Ibarra", "Novak", "Okeke", "Berg", "Duval", "Sato",
            "Keller", "Haddad", "Pereira", "Lind", "Cho", "Marin", "Petrov", "Diallo", "Kruger", "Amini",
            "Costa", "Walsh", "Nguyen", "Horvat", "Adeyemi", "Becker", "Silva", "Farouk", "Olsen", "Rossi",
            "Khan", "Dupuis", "Abebe", "Holm", "Navarro", "Weiss"
    };
}
