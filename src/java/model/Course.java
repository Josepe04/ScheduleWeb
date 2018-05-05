/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import dataManage.Tupla;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Norhan
 */
public class Course {
    
    private String[][] huecos; // cuadricula
    private int idCourse; // id del curso
    private int blocksWeek; // bloques por semana
    private String maxSections; // maximo numero de grupos
    private String minGapBlocks; // espacio minimo entre bloques
    private int minSections;
    private int minGapDays; //cada cuantos dias entre bloques
    private int rank; // prioridad
    private boolean GR; //
    private ArrayList<Integer> excludeRows; // bloques que no se pueden usar
    private ArrayList<Integer> excludeCols;
    private ArrayList<Tupla<Integer,Integer>> excludeBlocks;
    private int maxBlocksPerDay;
    private int sections;
    private int sectionsNoEnrolled;
    private double percentEnrolled;
    private ArrayList<Integer> studentsNoAsignados; 
    private ArrayList<Integer> studentsAsignados;
    private ArrayList<ArrayList<Tupla>> patronesStudents; 
    private int maxChildPerSection;
    private ArrayList<Integer> rooms;
    private ArrayList<Integer> trestricctions;
    
    public Course(int idCourse) {
        this.idCourse = idCourse;
        this.rank = Integer.MAX_VALUE;
        huecos=new String[Algoritmo.TAMX][Algoritmo.TAMY];
        for(int i = 0;i<Algoritmo.TAMX;i++)
            for(int j = 0; j < Algoritmo.TAMY;j++)
                huecos[i][j] = "0";
        maxBlocksPerDay = 1;
        sections = 1;
        studentsNoAsignados = new ArrayList<>();
        patronesStudents = new ArrayList<>();
        trestricctions = new ArrayList();
        rooms = new ArrayList();
    }
    
    
    public void addRoom(int id){
        rooms.add(id);
    }
    
    /**
     * Actualiza el nnumero de alumnos que
     * no se han podido matricular.
     * @param sectionsEnrolled 
     */
    public void setSectionsNoEnrolled(int sectionsEnrolled) {
        this.sectionsNoEnrolled = sectionsEnrolled;
    }
    
    /**
     * Ocupa un hueco en una seccion
     * @param list 
     */
    public void ocuparHueco(ArrayList<Tupla> list){
        if(list!=null && !list.isEmpty()){
            if(huecos[(Integer)list.get(0).x][(Integer)list.get(0).y].equals("0"))
                for(Tupla<Integer,Integer> t:list)
                    huecos[t.x][t.y] = ""+sections;
            else
                for(Tupla<Integer,Integer> t:list)
                    huecos[t.x][t.y] += " and "+sections;
            sections++;
        }
    }
    
    /**
     * Devuelve los huecos disponibles en los estudiantes no asignados al curso
     * @return 
     */
    public int[][] huecosStudents(){
        int[][] ret = new int[Algoritmo.TAMX][Algoritmo.TAMY];
        for(ArrayList<Tupla> ar:this.patronesStudents){
            for(Tupla<Integer,Integer> t:ar){
                ret[t.x][t.y] = 1;
            }
        }
        return ret;
    }
    
    /**
     * Devuelve los huecos donde se puede colocar
     * una seccion
     * @return 
     */
    public ArrayList<ArrayList<Tupla>> opciones(){ // AQUI ES DONDE SE LIMUTAN LOS HUECOS DISPONIBLES PARA EL ALGORITMO
        ArrayList<ArrayList<Tupla>> ret = new ArrayList<>();
        try{
            if(maxSections == null && Integer.parseInt(maxSections)==0 
                    && Integer.parseInt(maxSections) <= sections)
                return ret;
        }catch(Exception e){}
        for(int j = 0;j<Algoritmo.TAMY;j++){ // AQUI ES EL FALLO COMPARA LA HORA CON EL DIA TENIENDO EN CUENTA QUE LAS Y SON LAS HORAS Y LAS X LOS DIAS
            if((excludeRows==null && excludeCols==null && excludeBlocks==null)  || !excludeRows.contains(j+1)){
                int k,bloqueados;
                int gd= this.minGapDays;
                if(gd == 0)
                    gd++;
                for(int i = 0; i < Algoritmo.TAMX;i++){ // cambiar!
                    ArrayList<Tupla> t = new ArrayList<>(); 
                    int sum=0;
                    bloqueados = 0;
                    k=this.blocksWeek;
                    while(k>0){
                        Tupla taux = new Tupla((i+sum)%Algoritmo.TAMX,j);  // cambiar!
                        if(!t.contains(taux) && !this.excludeBlocks.contains(taux) && 
                                !this.excludeCols.contains((i+sum)%Algoritmo.TAMX)){ 
                            t.add(taux);
                            k--;
                        } else {
                            bloqueados++;
                        }
                        if(bloqueados > Algoritmo.TAMY)
                            break;
                        sum+=gd;
                    }  
                    if(k<=0 && !ret.contains(t))
                        ret.add(t);
                }
            }
        }
    return ret;
    }
    
    
    //---------------------------------
    //-------GETTERS AND SETTERS-------
    //---------------------------------

    public ArrayList<Integer> getRooms() {
        return rooms;
    }

    public void setRooms(String rooms) {
        String rparse1 = rooms.substring(1,rooms.length()-1);
        String[] rparse2 = rparse1.split(",");
        for(String s:rparse2){
            try{
                this.rooms.add(Integer.parseInt(s));
            }catch(Exception e){}
        }
    }

    public int getMinSections() {
        return minSections;
    }

    public void setMinSections(int minSections) {
        this.minSections = minSections;
    }
    
    public ArrayList<ArrayList<Tupla>> getPatronesStudents() {
        return patronesStudents;
    }

    public void setPatronesStudents(ArrayList<ArrayList<Tupla>> patronesStudents) {
        if(patronesStudents != null)
            this.patronesStudents = patronesStudents;
    }
    
    public int getSectionsNoEnrolled() {
        return sectionsNoEnrolled;
    }

    public int getMaxChildPerSection() {
        return maxChildPerSection;
    }

    public void setMaxChildPerSection(int maxChildPerSection) {
        this.maxChildPerSection = maxChildPerSection;
    }
    
    public double getPercentEnrolled() {
        return percentEnrolled;
    }

    public void setPercentEnrolled(double percentEnrolled) {
        this.percentEnrolled = percentEnrolled;
    }
    
    
    public ArrayList<Integer> getStudentsAsignados() {
        return studentsAsignados;
    }

    public void setStudentsAsignados(ArrayList<Integer> studentsAsignados) {
        this.studentsAsignados = studentsAsignados;
    }
    
    public ArrayList<Integer> getStudentsNoAsignados() {
        return studentsNoAsignados;
    }

    public void setStudentsNoAsignados(ArrayList<Integer> studentsNoAsignados) {
        this.studentsNoAsignados = studentsNoAsignados;
    }

    

    public int getSections() {
        return sections;
    }
    
    
    public int getIdCourse() {
        return idCourse;
    }

    public void setIdCourse(int idCourse) {
        this.idCourse = idCourse;
    }

    public int getBlocksWeek() {
        return blocksWeek;
    }

    public void setBlocksWeek(int blocksWeek) {
        this.blocksWeek = blocksWeek;
    }

    public int getMaxSections() {
        return Integer.parseInt(maxSections);
    }

    public void setMaxSections(String maxSections) {
        this.maxSections = maxSections;
    }

    public String getMinGapBlocks() {
        return minGapBlocks;
    }

    public void setMinGapBlocks(String minGapBlocks) {
        this.minGapBlocks = minGapBlocks;
    }

    public int getMinGapDays() {
        return minGapDays;
    }

    public void setMinGapDays(int minGapDays) {
        this.minGapDays = minGapDays;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public boolean isGR() {
        return GR;
    }

    public void setGR(boolean GR) {
        this.GR = GR;
    }
    
    public int getMaxBlocksPerDay() {
        return maxBlocksPerDay;
    }

    public void setMaxBlocksPerDay(int maxBlocksPerDay) {
        this.maxBlocksPerDay = maxBlocksPerDay;
    }

    public void setExcludeBlocks(String excludeBlocks) {
        String [] s = excludeBlocks.split(";");
        String[] elem;
        this.excludeBlocks = new ArrayList();
        this.excludeCols = new ArrayList();
        this.excludeRows = new ArrayList();
        if(!s[0].equals("")){
            for(String s2 : s){
                elem = s2.split(",");
                int row = -1;
                int col = -1;
                try{
                    row = Integer.parseInt(elem[0]);
                }catch(Exception e){}
                try{
                    col = Integer.parseInt(elem[1]);
                }catch(Exception e){}
                if(row == -1){
                    this.excludeCols.add(col);
                }else if(col == -1){
                    this.excludeRows.add(row);
                }else{
                    this.excludeBlocks.add(new Tupla(row,col));
                }
            }
        }
    }
    
    public ArrayList<Integer> getTrestricctions() {
        return trestricctions;
    }

    public void setTrestricctions(ArrayList<Integer> trestricctions) {
        this.trestricctions = trestricctions;
    }
    
    public String[][] getHuecos() {
        return huecos;
    }
    
    private String excludeBlocksToString(){
        String ret = "";
        for(Tupla t : this.excludeBlocks){
            ret+=t.x.toString()+","+t.y.toString()+";";
        }
        return ret;
    }
    
    public void setExcludeBlocksOwnDB(String excludeblocks){
        String [] s = excludeblocks.split(";");
        String[] elem;
        this.excludeBlocks = new ArrayList();
        if(!s[0].equals("")){
            for(String s2 : s){
                elem = s2.split(",");
                int row = -1;
                int col = -1;
                try{
                    row = Integer.parseInt(elem[0]);
                }catch(Exception e){}
                try{
                    col = Integer.parseInt(elem[1]);
                }catch(Exception e){}
                if(row != -1 && col!=-1){
                    this.excludeBlocks.add(new Tupla(row,col));
                }
            }
        }
    }
    
    public void setExcludeCols(String cols){
        String [] s = cols.split(",");
        String[] elem;
        this.excludeCols = new ArrayList();
        if(!s[0].equals("")){
            for(String s2 : s){
                int col = -1;
                try{
                    col = Integer.parseInt(s2);
                }catch(Exception e){}
                if(col!=-1){
                    this.excludeCols.add(col);
                }
            }
        }
    }
    
    public void setExcludeRows(String rows){
        String [] s = rows.split(",");
        String[] elem;
        this.excludeRows = new ArrayList();
        if(!s[0].equals("")){
            for(String s2 : s){
                int row = -1;
                try{
                    row = Integer.parseInt(s2);
                }catch(Exception e){}
                if(row!=-1){
                    this.excludeRows.add(row);
                }
            }
        }
    }
    
    /**
     * inserta o actualiza si ya existe ,el curso en nuestra base de datos.
     */
    public void insertarOActualizarCurso(){
        String consulta = "select * from courses where id="+this.idCourse;
        boolean actualizar = false;
        try {
            ResultSet rs = DBConnect.own.executeQuery(consulta);
            while(rs.next())
                actualizar = true;
            if(!actualizar){
                int maxsec = 0,mingapblocks=0;
                try{
                    maxsec = Integer.parseInt(this.maxSections);
                }catch(Exception e){}
                
                try{
                    mingapblocks = Integer.parseInt(this.minGapBlocks);
                }catch(Exception e){}
                
                consulta = "insert into courses values("+this.idCourse+","+this.blocksWeek+","
                    +maxsec+","+mingapblocks+","
                    +this.minGapDays+","+this.rank+","+this.GR+",'"
                    +excludeBlocksToString()+"',"+this.maxBlocksPerDay+",'"
                    +this.rooms.toString()+"','"+this.excludeCols.toString()
                    +"','"+this.excludeRows.toString()+"','"+this.trestricctions.toString()+"')";
                DBConnect.own.executeUpdate(consulta);
            }else{
                //to do: UPDATE
            }
        } catch (SQLException ex) {
            Logger.getLogger(Course.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
    @Override
    public boolean equals(Object c){
        return this.idCourse == ((Course)c).idCourse;
    }
}
