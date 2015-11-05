package ca.segwin.PolyDecodeur;

import ca.segwin.PolyDecodeur.model.*;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.DoubleStream;

/**
 * Classe controlleur selon le modèle MVC. Gère les commandes de l'utilisateur et met à jour la vue du programme.
 */
public class Controller {
    /**
     * Position de la première évaluation relative à la première colonne du tableau
     */
    final public static int NOTES_OFFSET = 5;

    /**
     * Titre du résultat dans la légende d'un étudiant dont on a fait une recherche par matricule
     */
    final public static String STR_LEGENDE_MOI = "Résultat de l'étudiant sélectionné";

    /**
     * Génère un objet de la classe BufferedReader permettant de lire le fichier <code>fin</code> ligne par ligne.
     *
     * @param fin Fichier à lire
     * @throws IOException Exception lancée s'il y a une erreur lors de la lecture du fichier
     */
    public static BufferedReader getBR(File fin) throws IOException {
        FileInputStream fis = new FileInputStream(fin);
        InputStreamReader isr = new InputStreamReader(fis);
        return new BufferedReader(isr);
    }

    /**
     * Permet de déterminer si une chaîne de caractères peut être interprété comme un nombre réel
     *
     * @param num Chaîne de caractères à interpréter
     */
    public static boolean isNumeric(String num) {
        try {                                   // Essaie d'interpréter num comme un nombre réel
            double d = Double.parseDouble(num);
        } catch (NumberFormatException e) {     // Si c'est pas un nombre
            return false;                       // Retourne faux
        }

        return true;                            // Si c'est un nombre, retoure vrai
    }

    /**
     * Permet de lire un fichier de résultats de Poly et en extraire les informations sur le cours,
     * la liste des étudiants inscrits et les résultats de chacun.
     *
     * @param fin Fichier de résultats sous le format standard de Poly
     * @throws IOException Relance l'exception si elle a lieu au niveau de la méthode {@link #getBR}
     */
    public static void lireFichier(File fin) throws IOException {
        BufferedReader br = getBR(fin);

        // On remet le modèle à neuf
        model = new Model();

        // Cours
        int lastEval = 20;
        Cours cours = new Cours();

        // Étudiant
        int matricule;
        List<Float> results;
        Student etudiant;

        // Lecture du fichier ligne par ligne
        String ligne;
        String[] l;
        int rangee = 0;

        while ((ligne = br.readLine()) != null) {

            // Lire uniquement les lignes commençant par un diviseur vertical
            if (ligne.charAt(0) == '|') {
                rangee++;

                // Séparer la ligne en ses cellules composantes (séparés par l'opérateur "|")
                l = ligne.split(Pattern.quote("|"));

                // Enlever les cellules vides
                l = Arrays.stream(l)
                        .filter(s -> (s != null && s.length() > 0))
                        .toArray(String[]::new);

                for (int i = 0; i < l.length; i++) {
                    l[i] = l[i].trim();
                }

                // Rangée 1: Sigle du cours, noms des évaluations et position de la dernière colonne
                if (rangee == 1) {
                    // Sigle du cours
                    Pattern pattern = Pattern.compile("([A-Z][A-Z][A-Z][0-9][0-9][0-9][0-9][A-Z]?)");
                    Matcher matcher = pattern.matcher(l[0]);
                    if (matcher.find()) {
                        String m = matcher.group(0);
                        cours.setSigle(m);
                    } else {
                        System.out.println("Incapable de déchiffrer le sigle du cours");
                        cours.setSigle("");
                    }

                    // Position de la dernière colonne d'évaluation
                    // On enlève d'abord les cellules vides à la fin (puisqu'on a fait trim() sur l[i] tantôt)
                    String[] c = Arrays.stream(l)
                            .filter(s -> (s != null && s.length() > 0))
                            .toArray(String[]::new);

                    lastEval = c.length;

                    // Noms des évaluations
                    for (int i = NOTES_OFFSET; i < l.length && i < lastEval; i++) {
                        cours.addEval(l[i].trim());
                    }
                }

                // Rangée 2: Titre du cours, poids (I) d'évaluation
                else if (rangee == 2) {
                    // Titre du cours
                    String[] m = l[0].split(Pattern.quote(":"));
                    cours.setTitre(m[1].trim());

                    // Poids (I) d'évaluation
                    for (int i = NOTES_OFFSET; i < l.length && i < lastEval; i++) {
                        String num = l[i].trim();
                        if (isNumeric(num)) {
                            cours.addPoids(Float.parseFloat(num) / 100);
                        } else {
                            cours.addPoids((float) 1);
                        }
                    }
                }

                // Rangée 4: Poids (II) d'évaluation
                else if (rangee == 4) {
                    for (int i = NOTES_OFFSET; i < l.length && i < lastEval; i++) {
                        String num = l[i].trim();
                        if (isNumeric(num)) {
                            float produit = cours.getPoids(i - NOTES_OFFSET) * Float.parseFloat(num) / 100;
                            cours.setPoids(i - NOTES_OFFSET, produit);
                        } else {
                            cours.setPoids(i - NOTES_OFFSET, (float) 0);
                        }
                    }
                }

                // Rangée 5: Notes maximales (e.g. 20/20) des évaluations
                else if (rangee == 5) {
                    for (int i = NOTES_OFFSET; i < l.length && i < lastEval; i++) {
                        String num = l[i].trim();
                        if (isNumeric(num)) {
                            cours.addPtsMax(Float.parseFloat(num));
                        } else {
                            cours.addPtsMax((float) 0);
                        }
                    }
                }

                // Rangées > 5: Résultats des étudiants
                else if (isNumeric(l[0])) {
                    matricule = Integer.parseInt(l[0]);
                    results = new ArrayList<Float>();

                    for (int i = NOTES_OFFSET + 2; i < l.length && i < lastEval + 2; i++) {
                        try {                                   // tente de lire le résultat
                            results.add(Float.parseFloat(l[i]));
                        } catch (NumberFormatException e) {     // si résultat non-numérique (e.g. case vide)
                            results.add((float) 0);             // substituer par 0
                        }
                    }

                    etudiant = new Student(matricule);
                    etudiant.setResults(results);
                    model.addEtudiant(etudiant);
                }
            }
        }

        model.setCours(cours);
        model.calcStats();
    }

    /**
     * Vérifie si tous les étudiants ont un résultat nul sur une évaluation donnée
     *
     * @param e La liste d'étudiants
     * @param i L'indice de l'évaluation
     * @return Retourne vrai si tous les élèves dans la liste <code>e</code> ont un résultat nul. Retourne faux sinon.
     */
    public static boolean isAllZero(List<Student> e, int i) {
        for (Student s: e) if (s.getResult(i) != 0) return false;
        return true;
    }

    /**
     * Retourne le graphe des résultats avec distribution normale (Gaussienne) comme référence
     *
     * @param eval L'indice de l'évaluation à utiliser. Pour la moyenne, on utilise: <code>i = -1</code>.
     * @return Retourne le graphe complet incluant les résultats et la distribution normale
     */
    private AreaChart<Number, Number> drawGraphe(int eval, String tabLabel) throws IllegalArgumentException {
        final int nEtudiants = model.getEtudiants().size();

        // Obtenir les statistiques du cours
        double mu;
        double sigma;
        double ptsMax;

        if (eval < 0) {
            mu = model.moyGlobale;
            sigma = model.sigmaGlobal;
            ptsMax = 100;
        } else {
            mu = model.moy.get(eval);
            sigma = model.sigma.get(eval);
            ptsMax = model.getCours().getPtsMax(eval);

            // Vérifier que l'évaluation a eu lieu
            if (isAllZero(model.getEtudiants(), eval)) throw new IllegalArgumentException("Aucun résultat non-nul sur cette évaluation: " + tabLabel);
        }

        // Générer les séries (résultats indiv. et distrib. normale)
        final double SAMPLE_SIZE = ptsMax / 100;
        double[] x = DoubleStream.iterate(0, s -> s + SAMPLE_SIZE).limit((long) (1 + (ptsMax / SAMPLE_SIZE))).toArray();

        AreaChart.Series<Number, Number> normSerie = new AreaChart.Series<>();
        normSerie.setName("Distribution normale (μ = " + Math.floor(mu * (100/ptsMax) * 100) / 100 + " %, σ = " + Math.floor(sigma * (100/ptsMax) * 100) / 100 + " %)");

        AreaChart.Series<Number, Number> resultatsSerie = new AreaChart.Series<>();
        resultatsSerie.setName("Résultats individuels");

        AreaChart.Series<Number, Number> moiSerie = new AreaChart.Series<>();
        moiSerie.setName(STR_LEGENDE_MOI);

        for (int i = 0; i < x.length; i++) {
            double t = (x[i] - mu) / sigma;
            double N = nEtudiants * Math.exp(-0.5 * t*t) / (sigma * Math.sqrt(2 * Math.PI));
            normSerie.getData().add(new XYChart.Data<>(i, N));
        }

        for (int i = 0; i < nEtudiants; i++) {
            double r;

            try {
                r = model.getEtudiant(i).getResult(eval);
            } catch (IndexOutOfBoundsException err) {
                r = model.getEtudiant(i).getMoyenne();
            }

            double t = (r - mu) / sigma;
            double N = nEtudiants * Math.exp(-0.5 * t*t) / (sigma * Math.sqrt(2 * Math.PI));

            resultatsSerie.getData().add(new XYChart.Data<>(r * 100/ptsMax, N));
        }

        // Créer le graphe
        final NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Résultats (%) - " + tabLabel);
        xAxis.setUpperBound(100);
        xAxis.setAutoRanging(false);
        xAxis.setTickUnit(10);

        final NumberAxis yAxis = new NumberAxis();
        yAxis.setLowerBound(0);

        final AreaChart<Number, Number> chart = new AreaChart<Number, Number>(xAxis, yAxis);
        chart.setTitle(model.getCours().getSigle() + ": " + model.getCours().getTitre());
        chart.getData().add(resultatsSerie);
        chart.getData().add(normSerie);
        chart.getData().add(moiSerie);

        chart.getStylesheets().addAll(getClass().getResource("css/nosymbols.css").toExternalForm());

        return chart;
    }

    public static Model model;
    public static Stage stage;

    @FXML private Text nomFichier;
    @FXML private TextField matricule;
    @FXML private TabPane tabs;

    @FXML private void handleFileSelection() {
        final FileChooser fc = new FileChooser();
        fc.setTitle("Choisir le fichier de résultats");
        File fin = fc.showOpenDialog(stage);

        if (fin != null) {
            try {
                // Lire le fichier
                lireFichier(fin);
                nomFichier.setText(fin.getPath());

                // Générer les graphes
                Cours cours = model.getCours();

                tabs.getTabs().remove(0, tabs.getTabs().size());

                for (int i = -1; i < cours.getEvals().size(); i++) {
                    // Définir le label sur l'axe des abscisses
                    String tabLabel;
                    if (i < 0)  tabLabel = "Moy.";
                    else        tabLabel = model.getCours().getEval(i);

                    // Ajouter le graphe à tab
                    Tab tab = new Tab();
                    tab.setText(tabLabel);

                    AreaChart<Number, Number> graphe;

                    try {
                        graphe = drawGraphe(i, tabLabel);
                        tab.setContent(graphe);
                    } catch (IllegalArgumentException err) {
                        tab.setDisable(true);
                        tab.setTooltip(new Tooltip("Aucun résultat n'est disponible pour cette évaluation"));
                        System.out.println(err.getMessage());
                    }

                    tabs.getTabs().add(tab);
                }
            } catch (IOException err) {
                System.out.println("Erreur lors de la lecture du fichier: " + err.getMessage());
            }
        }
    }

    @FXML private void handleMatriculeEnter(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) handleMatriculeSelection();
    }

    @FXML private void handleMatriculeSelection() {
        double nEtudiants;

        try {
            nEtudiants = model.getEtudiants().size();
        } catch (NullPointerException err) {
            nEtudiants = 0;
        }

        // Vérifier si l'étudiant est inscrit au cours
        boolean introuvable = true;
        Student moi = new Student(-1);

        for (int i = 0; i < nEtudiants && introuvable; i++) {
            try {
                if (model.getEtudiant(i).getMatricule() == Integer.parseInt(matricule.getText())) {
                    introuvable = false;
                    moi = model.getEtudiant(i);
                }
            } catch (NumberFormatException err) {
                introuvable = true;
            }
        }

        if (introuvable) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Étudiant introuvable");
            alert.setHeaderText("Étudiant introuvable");
            alert.setContentText("Le matricule entré ne correspond à aucun élève dans le cours. Veuillez vérifier que vous avez bien saisi le matricule désiré.");
            alert.show();
            return;
        }

        int nTabs = tabs.getTabs().size();

        // Ajouter le résultat de l'étudiant cherché
        for (int i = -1; i < nTabs - 1; i++) {
            AreaChart<Number, Number> chart;

            try {
                chart = (AreaChart<Number, Number>) tabs.getTabs().get(i + 1).getContent();
            } catch (ClassCastException err) {
                throw new ClassCastException("Erreur lors de la lecture des contenus d'un onglet");
            }

            // Si cet onglet ne contient pas d'informations, le sauter
            try {
                chart.getData().get(0);
            } catch (NullPointerException err) {
                continue;
            }

            double mu;
            double sigma;
            double ptsMax;
            double r;

            if (i < 0) {
                mu = model.moyGlobale;
                sigma = model.sigmaGlobal;
                ptsMax = 100;
                r = moi.getMoyenne();
            } else {
                mu = model.moy.get(i);
                sigma = model.sigma.get(i);
                ptsMax = model.getCours().getPtsMax(i);
                r = moi.getResult(i);
            }

            try {
                chart.getData().remove(2);
            } catch (Exception err) {
                // Rien à faire
            }

            AreaChart.Series<Number, Number> moiSerie = new AreaChart.Series<>();
            moiSerie.setName(STR_LEGENDE_MOI + " (" + Math.floor(r * 100/ptsMax * 100) / 100 + " %)");

            double t = (r - mu) / sigma;
            double N = nEtudiants * Math.exp(-0.5 * t*t) / (sigma * Math.sqrt(2 * Math.PI));

            // Ajouter une impulsion sur l'étudiant désiré
            moiSerie.getData().add(new XYChart.Data<>(r * 100/ptsMax, 0));
            moiSerie.getData().add(new XYChart.Data<>(r * 100/ptsMax + ptsMax/1000, N));
            moiSerie.getData().add(new XYChart.Data<>(r * 100/ptsMax + ptsMax/1000, 0));

            chart.getData().add(moiSerie);
        }
    }

    @FXML private void handleClose() {
        stage.close();
    }

    @FXML private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("À propos du programme");
        alert.setHeaderText("Qu'est-ce que PolyDécodeur?");
        alert.setContentText("C'est un petit programme Java/JavaFX qui permet de décoder les fichiers de résultats si communs à Polytechnique. Tu peux voir la courbe des résultats d'un cours et regarder où toi et tes amis se placent. Je sais, c'est trop hot! \n" +
                "\n" +
                "Créé par Eric Seguin, 2015. Licence libre (GPL 3.0). \n" +
                "\n" +
                "https://github.com/segwin/PolyDecodeur");
        alert.show();
    }
}