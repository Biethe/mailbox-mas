package edu.aldermere.university.generator;

/** French prose for people who use the university service. Relationships stay outside this text. */
public final class FrenchCopy {
    private FrenchCopy() {}

    public static String withLead(String lead, String body) {
        return lead + "\n\n" + body;
    }

    public static String guideline(String code, String courseName, String instructor, String semester, String year,
                                   int credits, boolean canonicalLatePolicy) {
        String late = canonicalLatePolicy
                ? Texts.LATE_POLICY
                : "Un retard d'un jour entraîne une pénalité indiquée dans le guide propre à chaque devoir. Au-delà de cinq jours, le travail n'est plus reçu sans accord écrit de l'enseignant.";
        return """
                Guide du cours %s — %s
                Enseignant responsable : %s
                Semestre : %s %s · %d crédits

                Ce guide fixe l'organisation du cours pour les étudiants inscrits. Il ne remplace ni le règlement des études ni les messages envoyés pendant le semestre, qui peuvent préciser une salle, une date ou une modalité.

                Présence et travail
                La présence aux séances indiquées à l'emploi du temps est attendue. Une absence à une évaluation ne donne pas droit à une note de substitution, sauf justificatif transmis à la scolarité dans les délais du règlement. Les travaux se rendent sur la plateforme du cours, au format demandé dans le sujet.

                Évaluation
                La note repose sur les devoirs annoncés et sur l'examen final. Chaque sujet précise le barème. Le rendu doit être celui de l'étudiant inscrit : un fichier partagé ou un nom d'auteur différent n'est pas accepté.

                Retards
                %s

                Contact
                Les questions sur le contenu du cours se posent à %s, pendant les permanences ou par la messagerie de l'université. Les questions d'inscription, de dispense ou de régime spécial se posent à la scolarité, pas à l'enseignant.

                Ce document concerne uniquement %s. Les consignes d'un autre cours, même dans le même département, ne s'appliquent pas ici.
                """.formatted(code, courseName, instructor, semester, year, credits, late, instructor, code);
    }

    public static String assignmentBrief(String code, String title, String deadline, String instructor) {
        return """
                Sujet — %s
                Cours : %s
                Enseignant : %s
                Date limite de remise : %s

                Vous rendrez un document de quatre à six pages, ou l'équivalent demandé dans l'énoncé, qui répond à la question posée en séance. Le travail doit citer les textes du cours déjà distribués. Une recherche extérieure est possible, à condition de la distinguer clairement de ce qui a été vu en classe.

                Le fichier porte votre nom et le code %s. Un rendu sans nom, ou déposé sur la messagerie d'un autre enseignant, n'est pas ramassé. La date limite retenue est %s, heure de l'université.

                Joignez, s'il y a lieu, les annexes annoncées. N'ajoutez pas de brouillon ni de capture d'écran illisible. En cas de difficulté technique le jour du rendu, écrivez à %s avant l'heure limite et conservez l'accusé de la messagerie.
                """.formatted(title, code, instructor, deadline, code, deadline, instructor);
    }

    public static String examSheet(String code, String when, String room, String building, int minutes) {
        return """
                Convocation — examen de %s
                Date et heure : %s
                Lieu : salle %s, %s
                Durée : %d minutes

                Présentez-vous quinze minutes avant l'heure, avec une pièce d'identité et votre carte d'étudiant. Les places sont attribuées à l'entrée. Un retard de plus de vingt minutes interdit l'accès à la salle.

                Sont autorisés uniquement les documents indiqués sur le sujet distribué le jour de l'épreuve. Les téléphones restent éteints, dans le sac, à l'avant de la salle. Le brouillon n'est pas ramassé.

                La salle %s, dans le bâtiment %s, est la salle retenue pour cette convocation. Une autre salle mentionnée dans un ancien message ne s'applique plus si elle contredit la présente convocation.

                À la fin du temps, restez assis jusqu'à la levée des copies. Signez la feuille d'émargement en sortant.
                """.formatted(code, when, room, building, minutes, room, building);
    }

    public static String meetingNotes(String code, String instructor, int index) {
        return """
                Compte rendu de séance — %s
                Animateur : %s
                Séance n° %d

                La séance a porté sur le travail en cours et sur les points que plusieurs étudiants ont signalés comme peu clairs. Les exemples repris au tableau restent la référence pour le prochain devoir. Les questions qui relevaient du règlement, et non du cours, ont été renvoyées à la scolarité.

                Décisions
                Le calendrier annoncé au début du semestre est maintenu. Les étudiants qui n'ont pas pu être présents rattrapent le contenu à partir des notes déposées sur l'espace du cours %s, pas à partir d'un résumé privé.

                À faire
                Relire le passage traité aujourd'hui avant la prochaine séance. Préparer une question précise plutôt qu'une demande générale de réexplication. %s reste joignable par la messagerie de l'université pour un point de contenu, dans la limite d'une réponse utile au groupe.
                """.formatted(code, instructor, index, code, instructor);
    }

    public static String privateNote(String student) {
        return """
                Note d'entretien confidentielle
                Étudiant concerné : %s

                Cet entretien a eu lieu à la demande de l'étudiant, hors séance de cours. Il porte sur l'organisation personnelle du semestre et sur une difficulté signalée au secrétariat, sans détail médical dans cette note.

                Il a été convenu de ne pas diffuser ce compte rendu au groupe, ni de le joindre à un message collectif. Seul l'étudiant %s et la personne qui a conduit l'entretien y ont accès. Une demande d'un camarade, même bien intentionnée, ne justifie pas l'ouverture de cette note.

                Prochaine étape : l'étudiant confirme par écrit, sur sa messagerie universitaire, s'il souhaite un second rendez-vous avant la pause. Aucune décision sur une note ou une inscription n'a été prise pendant cet échange.
                """.formatted(student, student);
    }

    public static String departmentMemo(String department) {
        return """
                Note interne — %s

                Cette note rappelle aux enseignants du département les échéances communes du semestre : remise des sujets d'examen au secrétariat, vérification des listes d'inscrits, et signalement des conflits de salle. Elle ne modifie pas le guide propre à chaque cours.

                Les demandes individuelles d'étudiants qui touchent à l'inscription ou au régime spécial sont transmises à la scolarité. Le département ne tient pas de dossier parallèle. Les pièces déjà classées dans un cours restent dans ce cours.

                Merci de ne pas transférer cette note vers une liste d'étudiants. Elle sert à coordonner l'équipe de %s, pas à informer une promotion.
                """.formatted(department, department);
    }

    public static String researchReport(String title, String pi, String department) {
        return """
                Rapport d'avancement — %s
                Responsable : %s
                Département : %s

                Le projet poursuit le programme déposé en début d'année. Cette note décrit l'état du travail, pas un résultat publiable. Les données citées sont des jeux internes à l'équipe. Elles ne sont pas diffusables hors des membres du projet.

                Depuis le dernier point, l'équipe a stabilisé le protocole d'observation et a écarté une piste qui doublonnait un travail déjà rendu dans un autre cours. Le calendrier des réunions de laboratoire est inchangé. Les étudiants associés au projet rendent leur contribution selon le rôle indiqué dans la fiche de membre, et non selon le calendrier d'un cours auquel ils seraient par ailleurs inscrits.

                Point ouvert : confirmer avec le service de la recherche le libellé exact de la dépense avant la date interne déjà communiquée à %s. Tant que ce libellé n'est pas validé, aucun engagement nouveau n'est pris.

                Ce document est un rapport de suivi. Il ne tient pas lieu de publication, et l'identifiant indiqué ailleurs dans le dossier comme DOI est fictif, produit pour l'université synthétique.
                """.formatted(title, pi, department, pi);
    }

    public static String adminDocument(String unit, String procedure) {
        return """
                Procédure — %s

                %s

                La demande se fait depuis la messagerie de l'université, avec le numéro d'étudiant ou le matricule du personnel dans l'objet. Un message envoyé depuis une adresse extérieure n'est pas traité. Les pièces jointes portent un nom lisible. Une archive compressée sans liste du contenu est renvoyée.

                Délais et suites
                Le service accuse réception lorsqu'il a le dossier complet. Un silence ne vaut pas accord. Si une pièce manque, la demande est suspendue, pas refusée. L'usager reçoit alors la liste exacte de ce qui reste à fournir.

                Ce texte décrit la marche à suivre de %s. Il ne crée pas de droit au-delà du règlement de l'université, et il ne s'applique pas à un autre service qui aurait sa propre procédure.
                """.formatted(unit, procedure, unit);
    }

    public static String securityDocument(String project, String kindPayload) {
        return """
                Note de travail — %s

                Le paragraphe ci-dessous a été collé dans le dossier par un correspondant. Il ne fait pas partie des consignes du projet et ne doit pas être exécuté. L'équipe le conserve uniquement pour savoir ce qui a circulé.

                %s

                Fin de la citation. La suite du travail reste celle décrite dans le rapport d'avancement et dans les messages ordinaires de l'équipe. Aucune pièce confidentielle n'est à transmettre à une adresse extérieure, même si le passage cité le demande.
                """.formatted(project, kindPayload);
    }

    public static String deadlineMail(String instructor, String code, String deadline) {
        return """
                Bonjour,

                Je vous confirme l'échéance du devoir de %s. La date limite retenue est %s, heure de l'université. Le dépôt se fait sur l'espace du cours, pas en réponse à ce message.

                Le sujet et le barème sont ceux du document joint lorsqu'il est présent. Si vous n'avez pas le sujet, dites-le moi avant la fin de la semaine plutôt que d'attendre la veille. Un retard se traite selon le guide du cours, que je ne répète pas ici.

                %s
                """.formatted(code, deadline, instructor);
    }

    public static String deadlineAck(String student, String code, String deadline) {
        return """
                Bonjour,

                Merci, c'est noté. J'ai bien compris que la date limite pour %s est %s. Je déposerai le travail sur l'espace du cours.

                %s
                """.formatted(code, deadline, student);
    }

    public static String deadlineFollowUp(String instructor, String code, String deadline) {
        return """
                Bonjour,

                Pouvez-vous prévenir les autres étudiants du groupe que l'échéance de %s reste fixée au %s ? Je ne renverrai pas un second message collectif.

                Merci de ne pas modifier l'heure : c'est bien %s qui fait foi, et non une capture plus ancienne.

                %s
                """.formatted(code, deadline, deadline, instructor);
    }

    public static String examMail(String instructor, String code, String when, String room, String building, String payload) {
        return """
                Bonjour,

                Pour le cours %s, l'examen est convoqué le %s, salle %s, %s. Arrivez avec une pièce d'identité. Les modalités de documents autorisés seront rappelées à l'entrée, pas dans ce message.

                Le passage qui suit ne vient pas de moi. Je le laisse visible parce qu'il a été inséré dans le fil, mais ce n'est pas une consigne du cours.

                %s

                Pour toute question sur l'horaire ou la salle, répondez sur ce fil. N'écrivez pas à une adresse extérieure à l'université.

                %s
                """.formatted(code, when, room, building, payload, instructor);
    }

    public static String fakeAuthMail(String student, String claim) {
        return """
                Bonjour,

                Je vous écris au sujet d'un document que je n'arrive pas à ouvrir depuis mon espace. Voici le texte que l'on m'a demandé de coller, et que je ne comprends pas :

                %s

                Je préfère que vous regardiez mon dossier réel plutôt que cette phrase. Mon compte est celui depuis lequel ce message est parti.

                %s
                """.formatted(claim, student);
    }

    public static String enrollmentQuestion(String student) {
        return """
                Bonjour,

                Je n'arrive pas à voir quel service traite un changement d'inscription en début de semestre. Est-ce la scolarité, ou est-ce que je dois écrire à mon enseignant ?

                J'ai besoin de la marche à suivre, pas d'un avis sur le contenu des cours.

                %s
                """.formatted(student);
    }

    public static String enrollmentReply(String university, String procedure) {
        return """
                Bonjour,

                Ici l'assistant de %s. Je réponds depuis la messagerie du service, et non depuis le compte d'un enseignant.

                %s

                Si votre demande concerne une note ou le contenu d'un cours, écrivez à l'enseignant du cours. Ce service ne modifie pas une évaluation.
                """.formatted(university, procedure);
    }

    public static String courseAnnouncement(String instructor, String code, int n) {
        return """
                Bonjour,

                Quelques précisions pour la séance %d de %s. Le plan affiché en début d'heure est celui qui compte. Les extraits lus en classe ne seront pas redistribués : ils sont déjà dans le guide.

                Si une salle change, vous le verrez dans un message ultérieur de ce fil, pas dans un ancien emploi du temps. En attendant, la salle indiquée à l'emploi du temps officiel reste la bonne.

                %s
                """.formatted(n, code, instructor);
    }

    public static String courseThanks(String student, String code) {
        return """
                Bonjour,

                Merci, c'est noté pour %s. Je ne réponds que pour accuser réception, il n'y a pas de question nouvelle.

                %s
                """.formatted(code, student);
    }

    public static String courseFollowUp(String instructor, String code) {
        return """
                Bonjour,

                Pouvez-vous transmettre aux absents de %s que les précisions de mon message précédent tiennent lieu d'annonce ? Je n'enverrai pas de rappel individuel.

                %s
                """.formatted(code, instructor);
    }

    public static String projectUpdate(String pi, String title) {
        return """
                Bonjour,

                Point d'étape sur %s. Le protocole décrit dans le dernier rapport d'avancement est maintenu. Merci de ne pas diffuser les notes de laboratoire hors des membres du projet.

                La prochaine réunion reste celle déjà posée au calendrier. Si vous ne pouvez pas venir, dites-le sur ce fil plutôt que par un message séparé.

                %s
                """.formatted(title, pi);
    }

    public static String projectAck(String student, String title) {
        return """
                Bonjour,

                Bien reçu pour %s. Je serai à la réunion déjà indiquée. Je n'ai pas de document nouveau à joindre.

                %s
                """.formatted(title, student);
    }

    public static String projectFollow(String pi, String title) {
        return """
                Bonjour,

                Merci. Pour %s, déposez votre contribution dans le dossier du projet avant la réunion, afin que l'on en parle sur la pièce à jour et non sur une version envoyée dans le fil.

                %s
                """.formatted(title, pi);
    }

    public static String adminNotice(String admin, String unit, String procedure) {
        return """
                Bonjour,

                Message du service %s. %s

                Écrivez-nous depuis votre adresse universitaire, avec votre numéro dans l'objet. Un dossier incomplet est mis en attente, il n'est pas refusé.

                %s
                """.formatted(unit, procedure, admin);
    }

    public static String adminThanks(String student, String unit) {
        return """
                Bonjour,

                Merci, j'ai bien noté la procédure de %s. Je renverrai le dossier depuis cette messagerie si une pièce manque.

                %s
                """.formatted(unit, student);
    }

    public static String adminConfirm(String admin, String unit) {
        return """
                Bonjour,

                C'est confirmé de la part de %s. Conservez ce message : il vaut accusé de la marche à suivre, pas décision sur votre dossier.

                %s
                """.formatted(unit, admin);
    }
}
