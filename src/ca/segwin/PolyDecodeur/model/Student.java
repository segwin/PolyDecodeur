package ca.segwin.PolyDecodeur.model;

import java.util.List;
import java.util.ArrayList;

/**
 * Représente un étudiant de Poly. Peut être inscrit à plusieurs cours.
 */
public class Student {
    // Constructeurs
    public Student(int m) {
        matricule = m;
    }

    // Méthodes
    public int getMatricule() { return matricule; }

    public List<Float> getResults() { return results; }
    public float getResult(int i) { return results.get(i); }
    public void setResults(List<Float> r) { results = r; }

    public float getMoyenne() { return moy; }

    public void calcMoyenne(List<Float> poids, List<Float> ptsMax) throws IndexOutOfBoundsException {
        // Vérifier si la liste de poids est de la bonne grandeur
        if (poids.size() != results.size()) throw new IndexOutOfBoundsException("Le nombre d'évaluations doit être égal au nombre de résultats de l'élève");

        // Calculer la moyenne de l'élève
        float m = 0;

        for (int i = 0; i < results.size(); i++) {
            m += (results.get(i) / ptsMax.get(i)) * poids.get(i) * 100;
        }

        moy = m;
    }

    // Attributs
    private int matricule;
    private List<Float> results;
    private float moy;
}
