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
import org.springframework.web.servlet.ModelAndView;
import xml.XMLWriterDOM;

/**
 *
 * @author Norhan
 */
public class Algoritmo {
    public static int TAMX = 6;
    public static int TAMY = 5;
    public final static int CHILDSPERSECTION = 20;
    private List<Object> tabla;
    private ArrayList<String> Log;
    Consultas cs;
    public Algoritmo(){
        Log = new ArrayList<>();
        tabla = new ArrayList<>();
        tabla.add(new int[TAMX][TAMY]);
         cs = new Consultas();
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
    
    public void algo(ModelAndView mv){
        
        int[] idsprueba = {739,688,796,733,676,837,718,702,717,846,690,
                            721,735,722,680,706,755,746,872,873,935,650};
        ArrayList<Course> rst = cs.getRestricciones(idsprueba);
        ArrayList<Teacher> trst = cs.teachersList();
        ArrayList<ArrayList<Tupla>> seccionesDisponibles = new ArrayList<>();
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
        
        for(Course course: rst){
            int minsections = 1 + studentsCourse.get(course.getIdCourse()).size()/CHILDSPERSECTION;
            course.setSections(minsections);
            for(ArrayList<Tupla> ar: course.opciones()){
                seccionesDisponibles.add(ar);
            }
            
            if(seccionesDisponibles.size()>0){
                ArrayList<Integer> noAsign = studentSections(trst,course,minsections,
                        seccionesDisponibles,studentsCourse.get(course.getIdCourse()),students);
                if(noAsign != null)
                    course.setStudentsNoAsignados(noAsign);
            }
            else
                System.out.println("FAILURE: " + course.getIdCourse());
            seccionesDisponibles = new ArrayList<>();
        }
        
        XMLWriterDOM.xmlCreate(trst, null);
        
        for(Teacher teacher:trst){
            teacher.mostrarHuecos();
            System.out.println("");
        }
        ArrayList<Student> retst = new ArrayList<>();
        for(Map.Entry<Integer, Student> entry : students.entrySet()) {
            entry.getValue().mostrarHuecos();
            retst.add(entry.getValue());
            System.out.println("");
        }
        mv.addObject("profesores", trst);
        mv.addObject("students",students);
        mv.addObject("Courses",rst);
        mv.addObject("cs",cs);
        mv.addObject("log",Log);
    }
    
    private class Comp implements Comparator<Tupla<Integer,ArrayList<Integer>>>{
        @Override
        public int compare(Tupla<Integer,ArrayList<Integer>> e1, Tupla<Integer,ArrayList<Integer>> e2) {
            if(e1.y.size() < e2.y.size()){
                return 1;
            } else {
                return -1;
            }
        }
    }
    
    private ArrayList<Integer> studentSections(ArrayList<Teacher> teachers,Course c,int minsections,ArrayList<ArrayList<Tupla>> sec,
        ArrayList<Integer> studentsCourse,HashMap<Integer,Student> students){
        ArrayList<Tupla<Integer,ArrayList<Integer>>> stids = new ArrayList<>();
        ArrayList<Integer> idsAsignados = new ArrayList<>();
        for(int i = 0; i < sec.size();i++){
            stids.add(new Tupla<Integer,ArrayList<Integer>>(i,new ArrayList<>()));
            for(Integer j:studentsCourse){
                if(students.get(j).patronCompatible(sec.get(i))){
                    stids.get(i).y.add(j);
                }
            }
        }
        try{
            stids.sort(new Comp());
        }catch(Exception e){
            return null;
        }
        ArrayList<Integer> diferencia;
        diferencia = stids.get(0).y;
        int lastTeacher = -1;
        int lastStudent = -1;
        for(int i = 0;i < stids.size();i++){
            for(Teacher t : teachers){
                if(c.getTrestricctions().contains(t.idTeacher) &&
                        t.asignaturaCursable(c.getIdCourse()) && 
                        t.patronCompatible(sec.get(stids.get(i).x))){
                    int k = 0;
                    lastTeacher = i;
                    for(Integer j:diferencia){
                        if((k<studentsCourse.size()/c.getSections()+1 || studentsCourse.size()==1) && !idsAsignados.contains(j) && 
                                students.get(j).patronCompatible(sec.get(stids.get(i).x))){
                            idsAsignados.add(j);
                            students.get(j).ocuparHueco(sec.get(stids.get(i).x), c.getIdCourse()*100+i+1);
                            k++;
                            lastStudent = i;
                        }
                    }
                    if(k<studentsCourse.size()/c.getSections()){
                        for(Integer j:stids.get(i).y){
                            if((k<studentsCourse.size()/c.getSections()+1 || studentsCourse.size()==1) && !idsAsignados.contains(j) && 
                                    students.get(j).patronCompatible(sec.get(stids.get(i).x))){
                                idsAsignados.add(j);
                                students.get(j).ocuparHueco(sec.get(stids.get(i).x), c.getIdCourse()*100+i+1);
                                k++;
                                lastStudent = i;
                            }
                        }
                    }
                    if(k>0){
                        t.ocuparHueco(sec.get(stids.get(i).x), c.getIdCourse()*100+i);
                        c.ocuparHueco(i+1, sec.get(stids.get(i).x));
                    }
                    c.updateSectionsNoEnrolled(k);
                    if(idsAsignados.size() == studentsCourse.size()){
                        c.setStudentsAsignados(idsAsignados);
                        c.setPercentEnrolled(100);
                        for(Integer st:idsAsignados){
                            students.get(st).addAsignado(c.getIdCourse());
                        }
                        return null;
                    }
                }
            }
            if(i+1<stids.size())
                diferencia = Conjuntos.diferencia(stids.get(i+1).y, stids.get(i).y);
        }
        
        for(Integer st:idsAsignados){
            students.get(st).addAsignado(c.getIdCourse());
        }
        c.setStudentsAsignados(idsAsignados);
        double percent = ((double)idsAsignados.size())/((double)studentsCourse.size())*100;
        c.setPercentEnrolled(percent);
        if(idsAsignados.size()!= studentsCourse.size()){
            System.out.println("FAILURE");      
            ArrayList<Integer> ret = Conjuntos.diferencia(studentsCourse, idsAsignados);
            if(lastTeacher <= lastStudent){
                Log.add("Los profesores asignados al curso:"+cs.nameCourse(c.getIdCourse())+" no tienen disponible ningun hueco compatible");
            }else{
                Log.add("Los siguientes estudiantes no tienen secciones disponibles para el curso "+cs.nameCourse(c.getIdCourse())+":");
                String anadir = "",anadir2="";
                ArrayList<ArrayList<Tupla>> aux = null;
                for(Integer i: ret){
                    anadir+=students.get(i).getName()+",";
                    if(aux == null) 
                        aux = students.get(i).listPatronesCompatibles(c.opciones());
                    else
                        aux = students.get(i).listPatronesCompatibles(aux);
                }
                c.setPatronesStudents(aux);
                anadir = anadir.substring(0,anadir.length()-2)+".";
                Log.add(anadir);
                //Log.add("Para este conjunto de estudiantes tenemos disponibles los siguientes patrones en común:");
                
            }
            for(Integer st:ret){
                students.get(st).addNoAsignado(c.getIdCourse());
            }
            return ret;
        }
        return null;
    }
}
