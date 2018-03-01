/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.ArrayList;

/**
 *
 * @author Chema
 */
public class Conjuntos {

    public static ArrayList<Integer> union(ArrayList<Integer> primero, ArrayList<Integer> segundo) {
        ArrayList<Integer> retVal = new ArrayList<Integer>(primero);
        for (Integer worte : segundo) {
            if (!primero.contains(worte)) {
                retVal.add(worte);
            }
        }
        return retVal;
    }

    public static ArrayList<Integer> interseccion(ArrayList<Integer> a, ArrayList<Integer> b) {
        ArrayList<Integer> c = new ArrayList<Integer>();
        ArrayList<Integer> iter = a.size() > b.size() ? a : b;
        for (Integer elem : iter) {
            if (a.contains(elem) && b.contains(elem)) {
                c.add(elem);
            }
        }
        return c;
    }

    public static ArrayList<Integer> diferencia(ArrayList<Integer> a, ArrayList<Integer> b) {
        ArrayList<Integer> c = new ArrayList<Integer>();
        for (Integer elem : a) {
            if (!b.contains(elem)) {
                c.add(elem);
            }
        }
        return c;
    }
}
