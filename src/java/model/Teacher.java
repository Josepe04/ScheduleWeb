/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Norhan
 */
public class Teacher{
    private int[][] huecos;
    private int[] blocksPerDay;
    protected int idTeacher;
    protected int MaxSections; // maxima secciones
    private int secsComplete;
    protected int Preps;//maximo asignaturas
    private ArrayList<Integer> prepsComplete;
    protected int MaxBxD;//max blocksperday
    private String ExcludeBlocks;
    protected boolean ocupado;
    private String name;
    private HashMap<Integer,Integer> secciones;
    
    public Teacher(){
        huecos = new int[Algoritmo.TAMX][Algoritmo.TAMY];
        blocksPerDay = new int[Algoritmo.TAMX];
        for(int i = 0; i<blocksPerDay.length;i++){
            blocksPerDay[i] = 0;
        }
        secsComplete = 0;
        prepsComplete = new ArrayList<>();
        secciones = new HashMap<>();
    }
    
    public int seccionesDisponibles(){
        return MaxSections - secsComplete;
    }
    
    public int prepsDisponibles(){
        return Preps - prepsComplete.size();
    }
    
    public HashMap<Integer,Integer> getSecciones() {
        return secciones;
    }

    public void setSecciones(HashMap<Integer,Integer> secciones) {
        this.secciones = secciones;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public int getSecsComplete() {
        return secsComplete;
    }

    public int getMaxSections() {
        return MaxSections;
    }

    public int getIdTeacher() {
        return idTeacher;
    }

    public String getExcludeBlocks() {
        return ExcludeBlocks;
    }

    public void setExcludeBlocks(String ExcludeBlocks) {
        int[] tupla = new int[2];
        int i;
        for(String s : ExcludeBlocks.split(";")){
            i = 0;
            for(String s2:s.split(",")){
                if(s2.equals("*") && i<2)
                    tupla[i] = -1;
                else if(!s2.equals("") && i<2)
                    tupla[i] = Integer.parseInt(s2);
                i++;
            }
        }
        this.ExcludeBlocks = ExcludeBlocks;
    }
    
    public String toString(){
        return idTeacher +" sections: "+ MaxSections+" preps: "+ Preps+" maxbxd: "+ MaxBxD +" exclude: "+ExcludeBlocks;
    }
    
    public ArrayList<Tupla> getPosiciones(int id){
        ArrayList<Tupla> ret= new ArrayList<>();
        for(int i = 0; i < Algoritmo.TAMX;i++)
            for(int j = 0; j < Algoritmo.TAMY;j++)
                    if(huecos[i][j] == id)
                        ret.add(new Tupla(i,j));
        return ret;
    }
    
    public int[][] getHuecos(){
        return huecos;
    }
    
    public void mostrarHuecos(){
        for(int i = 0; i < Algoritmo.TAMY;i++){
            for(int j = 0; j < Algoritmo.TAMX;j++){
                System.out.print(" "+huecos[j][i]+" ");
            }
            System.out.println("");
        }
    }
    
    public void ocuparHueco(ArrayList<Tupla> ar,int id){
        for(Tupla t:ar)
            huecos[(Integer)t.x][(Integer)t.y]=id; 
        if(!prepsComplete.contains(id/100))
            prepsComplete.add(id/100);
        secsComplete++;
        if(secsComplete >= MaxSections)
            ocupado = true;
        if(secciones.containsKey(id/100)){
            int aux = secciones.get(id/100)+1;
            secciones.replace(id/100, aux);
        }else{
            secciones.put(id/100,1);
        }
    }
    
    public boolean asignaturaCursable(int id){
        if(ocupado || (!prepsComplete.contains(id) && prepsComplete.size()>=Preps))
            return false;
        else
            return true;
    }
    
    public boolean patronCompatible(ArrayList<Tupla> ar){
        for(Tupla t:ar)
            if(huecos[(Integer)t.x][(Integer)t.y]!=0 || blocksPerDay[(Integer)t.x] >= MaxBxD)
                return false;
        return true;
    }
    
    public ArrayList<Integer> getPrepsComplete() {
        return prepsComplete;
    }
}
