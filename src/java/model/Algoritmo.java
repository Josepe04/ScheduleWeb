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
    protected static int TAMX = 6;
    protected static int TAMY = 12;
    public final static int CHILDSPERSECTION = 20;
    private ArrayList<String> Log;
    Conjuntos<Integer> conjuntos;
    Consultas cs;
    
    public Algoritmo(){
        Log = new ArrayList<>();
        cs = new Consultas();
        conjuntos = new Conjuntos<>();
    }
    
    public Algoritmo(int x, int y){
        TAMX = x;
        TAMY = y;
        Log = new ArrayList<>();
        cs = new Consultas();
        conjuntos = new Conjuntos<>();
    }
    
    public boolean esSolucion(boolean[] asignados){
        for(int i = 0; i < asignados.length;i++){
            if(!asignados[i])
                return false;
        }
        return true;
    }
    
    /**
     * algoritmo
     * @param mv 
     */
    public void algo(ModelAndView mv,String yearid,String tempid){
        cs = new Consultas();
        ArrayList<Integer> idCourses = new ArrayList(); 
        ArrayList<Student> st = new ArrayList();
        HashMap<Integer,ArrayList<Integer>> studentsCourse = Consultas.getCoursesGroups(st,idCourses,yearid,tempid);
        HashMap<Integer,Student> students = new HashMap<>();
        st = (new Conjuntos<Student>()).union(st,
                cs.restriccionesStudent(idCourses,studentsCourse,yearid));  
        for(Student s:st){
            students.put(s.getId(), s);
        }
        ArrayList<Course> rst = cs.getRestricciones(Consultas.convertIntegers(idCourses),cs.templateInfo(tempid));
        ArrayList<Teacher> trst = cs.teachersList();
      
        int numcursos = 0;
        for(Course course: rst){
            int minsections = 1 + studentsCourse.get(course.getIdCourse()).size()/CHILDSPERSECTION;
            course.setSections(minsections);
            
            if(course.opciones().size()>0){
                ArrayList<Integer> noAsign = studentSections(trst,course,minsections,
                        course.opciones(),studentsCourse.get(course.getIdCourse()),students);
                if(noAsign != null)
                    course.setStudentsNoAsignados(noAsign);
            }
            else
                System.out.println("FAILURE: " + course.getIdCourse());
            numcursos++;
        }
        
        for(Teacher teacher:trst){
            teacher.mostrarHuecos();
            System.out.println("");
        }
        
        for(Map.Entry<Integer, Student> entry : students.entrySet()) {
            entry.getValue().mostrarHuecos();
            System.out.println("");
        }
        
//        XMLWriterDOM.xmlCreate(trst, retst);
        
        mv.addObject("TAMX",TAMX);
        mv.addObject("TAMY",TAMY);
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
                diferencia = conjuntos.diferencia(stids.get(i+1).y, stids.get(i).y);
        }
        
        for(Integer st:idsAsignados){
            students.get(st).addAsignado(c.getIdCourse());
        }
        c.setStudentsAsignados(idsAsignados);
        double percent = ((double)idsAsignados.size())/((double)studentsCourse.size())*100;
        c.setPercentEnrolled(percent);
        if(idsAsignados.size()!= studentsCourse.size()){
            System.out.println("FAILURE");      
            ArrayList<Integer> ret = conjuntos.diferencia(studentsCourse, idsAsignados);
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
