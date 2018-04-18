/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import dataManage.Tupla;
import java.util.ArrayList;

/**
 *
 * @author Chema
 */
public class Room {
    private int roomid;
    private int[][] huecos;
    private String name;
    private int size;
    private int disponibilidad;
    private int ocupacion;
    
    
    public Room(int roomid, String name, int size) {
        this.roomid = roomid;
        this.name = name;
        this.size = size;
        this.disponibilidad = Algoritmo.TAMX * Algoritmo.TAMY;
        this.huecos = new int[Algoritmo.TAMX][Algoritmo.TAMY]; 
    }
    
    public ArrayList<ArrayList<Tupla>> patronescompatibles(ArrayList<ArrayList<Tupla>> sec){
        ArrayList<ArrayList<Tupla>> ret = new ArrayList();
        for(ArrayList<Tupla> ar:sec){
            if(this.patronCompatible(ar))
                ret.add(ar);
        }
        return ret;
    }
    
    
    
    public boolean ocuparHueco(int valor ,ArrayList<Tupla> ar){
        for(Tupla<Integer,Integer> t:ar){
            if(huecos[t.x][t.y] == 0){
               huecos[t.x][t.y] = valor; 
            }else{
                return false;
            }
        }
        this.ocupacion++;
        this.disponibilidad--;
        return true;
    }
    
    public boolean patronCompatible(ArrayList<Tupla> ar){
        return ar.stream().noneMatch((t) -> (huecos[(Integer)t.x][(Integer)t.y] != 0));
    }

    
    
    
    public int getDisponibilidad() {
        return disponibilidad;
    }

    /*
    /////////////////////
    //GETTER AND SETTER//
    /////////////////////
     */
    
    public double getPercentOcupation(){
        double o = this.ocupacion;
        double d = this.disponibilidad;
        return o/(o+d)*100;
    }
    
    public int getOcupacion() {
        return ocupacion;
    }

    public int getRoomid() {
        return roomid;
    }

    public void setRoomid(int roomid) {
        this.roomid = roomid;
    }

    public int[][] getHuecos() {
        return huecos;
    }

    public void setHuecos(int[][] huecos) {
        this.huecos = huecos;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
    
}
