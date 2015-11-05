package ca.segwin.PolyDecodeur.model;

import java.util.List;
import java.util.ArrayList;

/**
 * Représente un cours donné à Poly. Comprend les informations du cours (nom, sigle, ...) ainsi que les résultats des élèves.
 */
public class Cours {
    // Constructeur
    public Cours() {
        nEvals = 0;
        evals = new ArrayList<String>();
        poids = new ArrayList<Float>();
        ptsMax = new ArrayList<Float>();
    }

    // Méthodes
    public String getSigle() { return sigle; }
    public void setSigle(String s) { sigle = s; }

    public String getTitre() { return titre; }
    public void setTitre(String t) { titre = t; }

    public List<String> getEvals() { return evals; }
    public String getEvals(int i) { return evals.get(i); }
    public void addEval(String e) { evals.add(e); }
    public void setEvals(List<String> e) { evals = e; }

    public List<Float> getPoids() { return poids; }
    public float getPoids(int i) { return poids.get(i); }
    public void addPoids(float p) { poids.add(p); }
    public void setPoids(int i, float p) { poids.set(i, p); }

    public List<Float> getPtsMax() { return ptsMax; }
    public float getPtsMax(int i) { return ptsMax.get(i); }
    public void addPtsMax(float p) { ptsMax.add(p); }
    public void setPtsMax(int i, float p) { ptsMax.set(i, p); }

    public String getEval(int i) { return evals.get(i); }
    public void setnEvals(int n) { nEvals = n; }

    // Attributs
    private String sigle;
    private String titre;

    private List<String> evals;
    private List<Float> poids;
    private List<Float> ptsMax;
    public int nEvals;
}