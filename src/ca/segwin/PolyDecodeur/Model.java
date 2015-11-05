package ca.segwin.PolyDecodeur;

import ca.segwin.PolyDecodeur.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe modèle selon le modèle MVC. Contient les données du programme.
 */
public class Model {
    /**
     * Nombre max. d'évaluations dans un cours
     */
    final public static int MAX_EVAL = 20;

    // Constructeur

    public Model() {
        cours = new Cours();
        etudiants = new ArrayList<Student>();
    }

    // Méthodes

    /**
     * Permet de calculer les données statistiques sur les résultats du cours tels que la moyenne,
     * l'écart type, et les valeurs min/max pour chaque évaluation.
     */
    public void calcStats() {
        // Initialisation
        min = new ArrayList<Float>();
        max = new ArrayList<Float>();
        moy = new ArrayList<Float>();
        sigma = new ArrayList<Float>();

        List<Float> sommes = new ArrayList<Float>();
        float sommeMoyGlobale = 0;
        int nEtudiants = 0;

        // Calcul des attributs
        for (Student e: etudiants) {        // boucle sur la liste d'étudiants
            nEtudiants++;

            List<Float> r = e.getResults();

            for (int i = 0; i < r.size(); i++) {
                // Ajouter r[i] à somme[i]
                try {
                    sommes.set(i, sommes.get(i) + r.get(i));
                } catch (IndexOutOfBoundsException err) {
                    sommes.add(r.get(i));
                }

                // Comparer r[i] avec min[i] et max[i]
                // Si min[i] ou max[i] indéfini, initialiser avec r[i]
                try {
                    if (r.get(i) < min.get(i)) min.set(i, r.get(i));
                } catch (IndexOutOfBoundsException err) {
                    min.add(r.get(i));
                }

                try {
                    if (r.get(i) > max.get(i)) max.set(i, r.get(i));
                } catch (IndexOutOfBoundsException err) {
                    max.add(r.get(i));
                }
            }

            // Calculer la moyenne globale de l'étudiant
            e.calcMoyenne(cours.getPoids(), cours.getPtsMax());
            sommeMoyGlobale += e.getMoyenne();
        }

        // Calcul des moyennes
        for (int i = 0; i < sommes.size(); i++) {   // Moyenne sur chaque évaluation
            float m = sommes.get(i) / nEtudiants;
            moy.add(m);
        }

        moyGlobale = sommeMoyGlobale / nEtudiants;  // Moyenne globale

        // Calcul des écarts types
        List<Float> v = new ArrayList<Float>();
        float vGlobal = 0;

        for (Student e: etudiants) {
            List<Float> r = e.getResults();

            // Ajouter au numérateur de la variance
            for (int i = 0; i < r.size(); i++) {
                float delta = (r.get(i) - moy.get(i));
                float vs;

                try {   // Tente d'ajouter delta^2 à v[i], sinon initialise v[i] avec delta^2
                    vs = v.get(i) + delta * delta;
                    v.set(i, vs);
                } catch (NullPointerException|IndexOutOfBoundsException exception) {
                    vs = delta * delta;
                    v.add(vs);
                }
            }

            // Ajouter au numérateur des résultats globaux
            vGlobal += e.getMoyenne();
        }

        // Calculer les écarts types de chaque évaluation
        for (int i = 0; i < v.size(); i++) {
            float va = v.get(i) / etudiants.size();
            sigma.add((float) Math.sqrt(va));
        }

        // Calculer l'écart type global
        sigmaGlobal = (float) Math.sqrt(vGlobal / etudiants.size());
    }

    // Accesseurs/mutateurs

    public Cours getCours() { return cours; }

    /**
     * @param c Cours à ajouter au modèle
     */
    public void setCours(Cours c) { cours = c; }

    /**
     * @param i Indice de l'étudiant sur la liste <code>etudiants</code> à retourner
     */
    public Student getEtudiant(int i) { return etudiants.get(i); }

    public List<Student> getEtudiants() { return etudiants; }

    /**
     * @param e Étudiant à ajouter au modèle
     */
    public void addEtudiant(Student e) { etudiants.add(e); }

    // Attributs

    /**
     * Le cours à l'étude
     */
    private Cours cours;

    /**
     * Liste des étudiants dans le cours
     */
    private List<Student> etudiants;

    /**
     * Résultats minimum sur chaque évaluation
     */
    protected List<Float> min;

    /**
     * Résultats maximum sur chaque évaluation
     */
    protected List<Float> max;

    /**
     * Résultats moyens pour chaque évaluation
     */
    protected List<Float> moy;

    /**
     * Écart type pour chaque évaluation
     */
    protected List<Float> sigma;

    /**
     * Moyenne des résultats finaux
     */
    protected float moyGlobale;

    /**
     * Écart type des résultats finaux
     */
    protected float sigmaGlobal;
}
