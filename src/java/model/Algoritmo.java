/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import xml.XMLWriterDOM;

/**
 *
 * @author Norhan
 */
public class Algoritmo {
    public final static int TAMX = 6;
    public final static int TAMY = 5;
    public final static int CHILDSPERSECTION = 20;
    private List<Object> tabla;
    public Algoritmo(){
        tabla = new ArrayList<>();
        tabla.add(new int[TAMX][TAMY]);
    }
    public boolean esSolucion(boolean[] asignados){
        for(int i = 0; i < asignados.length;i++){
            if(!asignados[i])
                return false;
        }
        return true;
    }
    
    
    /**
     * Dada una lista de patrones en los que se puede impartir la clase y 
     * una cuadricula con el horario del profesor o del estudiante
     * @param l1
     * @param huecos
     * @return 
     */
    public ArrayList<ArrayList<Tupla>> opcionesCompatibles(ArrayList<ArrayList<Tupla>> l1, int huecos[][]){
        ArrayList<ArrayList<Tupla>> ret = new ArrayList<>();
        for(ArrayList<Tupla> lista:l1){
                boolean tvalida = true;
                for(Tupla t:lista){
                    if(huecos[(Integer)t.x][(Integer)t.y] != 0)
                        tvalida = false;
                }
                if(tvalida)
                    ret.add(lista);
        }
        return ret;
    }
    
    public void algo(){
        Consultas cs = new Consultas();
        int[] idsprueba = {739,688,796,733,676,837,718,702,717,846,690,
                            721,735,722,680,706,755,746,872,873,935,650};
        ArrayList<Course> rst = cs.getRestricciones(idsprueba);
        ArrayList<Teacher> trst = cs.teachersList();
        HashMap<String,ArrayList<Tupla>> seccionesExistentes = new HashMap<>();
        HashMap<Integer,ArrayList<Integer>> studentsCourse = new HashMap<>();
        HashMap<Integer,Student> students = new HashMap<>();
        
        int [] numst = new int[2];
        for(Course c : rst){
            ArrayList<Student> st = cs.restriccionesStudent(c.getIdCourse(), numst);
            ArrayList<Integer> stids= new ArrayList<>();
            for(Student s:st){
                students.put(s.getId(), s);
                stids.add(s.getId());
            }
            studentsCourse.put(c.getIdCourse(),stids);
        }
        
        boolean[] asignados = new boolean[idsprueba.length];
        int i = 0;
        for(Course course: rst){
            int minsections = 1 + studentsCourse.get(course.getIdCourse()).size()/CHILDSPERSECTION;
            course.setSections(minsections);
            for(int j = 0;j<minsections;j++){
                for(Teacher teacher:trst){
                    int id = course.getIdCourse();
                    if(teacher.asignaturaCursable(id)){
                        for(ArrayList<Tupla> ar: course.opciones()){
                            int numberStudents = 0;
                            for(Integer st2 :studentsCourse.get(course.getIdCourse()))
                                if(course.getTrestricctions().contains(teacher.idTeacher) 
                                        && teacher.patronCompatible(ar) && students.get(st2).patronCompatible(ar)){
                                        numberStudents++;
                                }
                            int size = studentsCourse.get(course.getIdCourse()).size();
                            if(numberStudents > size/minsections -1){
                                teacher.ocuparHueco(ar, id*100+j);
                                seccionesExistentes.put(""+course.getIdCourse()+j,ar);
                                asignados[i] = true;
                                break;
                            }
                        }
                    }
                    if(asignados[i])
                        break;
                }
                asignados[i] = false;
            }
            studentGroups(course,minsections,seccionesExistentes,studentsCourse.get(course.getIdCourse()),students);
            
            i++;
        }
        
        XMLWriterDOM.xmlCreate(trst, null);
        
        for(Teacher teacher:trst){
            teacher.mostrarHuecos();
            System.out.println("");
        }
        for(Map.Entry<Integer, Student> entry : students.entrySet()) {
            entry.getValue().mostrarHuecos();
            System.out.println("");
        }
                
    }
    
    private class Comp implements Comparator<ArrayList<Integer>>{
        @Override
        public int compare(ArrayList<Integer> e1, ArrayList<Integer> e2) {
            if(e1.size() < e2.size()){
                return 1;
            } else {
                return -1;
            }
        }
    }
    
    private void studentGroups(Course c,int minsections,HashMap<String, ArrayList<Tupla>> sec,
            ArrayList<Integer> studentsCourse,HashMap<Integer,Student> students){
        ArrayList<ArrayList<Integer>> stids = new ArrayList<>();
        for(int i = 0; i < minsections;i++){
            stids.add(new ArrayList<>());
            for(Integer j:studentsCourse){
                if(students.get(j).patronCompatible(sec.get(""+c.getIdCourse()+i))){
                    stids.get(i).add(j);
                }
            }
        }
        stids.sort(new Comp());
        if((stids.get(0).size()==studentsCourse.size() && stids.size()==1)
                ||(stids.get(0).size()==studentsCourse.size() && stids.get(1).size() == studentsCourse.size())){
            int i=0;
            for(Integer j:studentsCourse){
                if(stids.size()==1 || i< studentsCourse.size()/2)
                    students.get(j).ocuparHueco(sec.get(""+c.getIdCourse()+0), c.getIdCourse()*100);
                else
                    students.get(j).ocuparHueco(sec.get(""+c.getIdCourse()+1), c.getIdCourse()*100+1);
                i++;
            }
        }else{
            ArrayList<Integer> diferencia = stids.get(stids.size()-1);
            for(int j = stids.size()-1; j >= 0;j--){
                diferencia = Conjuntos.diferencia(diferencia, stids.get(j));
                int i=0;
                for(Integer k:diferencia){
                    if(i<(studentsCourse.size()/2+3))
                        students.get(k).ocuparHueco(sec.get(""+c.getIdCourse()+0), c.getIdCourse()*100);
                    i++;
                }
            }
        }
          
    }
}
