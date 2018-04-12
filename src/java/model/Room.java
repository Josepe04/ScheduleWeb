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
public class Room {
    private int roomid;
    private int[][] huecos;
    private String name;
    private int size;

    public Room(int roomid, String name, int size) {
        this.roomid = roomid;
        this.name = name;
        this.size = size;
        this.huecos = new int[Algoritmo.TAMX][Algoritmo.TAMY]; 
    }
    
    public boolean ocuparHueco(int valor ,ArrayList<Tupla<Integer,Integer>> ar){
        for(Tupla<Integer,Integer> t:ar){
            if(huecos[t.x][t.y] == 0){
               huecos[t.x][t.y] = valor; 
            }else{
                return false;
            }
        }
        return true;
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
