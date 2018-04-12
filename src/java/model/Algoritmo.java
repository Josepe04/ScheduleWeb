/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
    private Conjuntos<Integer> conjuntos;
    private Consultas cs;
    private ArrayList<Teacher> teachers;
    private ArrayList<Course> courses;
    private HashMap<Integer,Student> students;
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
        rst.sort(new CompCoursesRank());
        ArrayList<Teacher> trst = cs.teachersList();
      
        for(Course course: rst){
            int minsections = 1;
            try{
                minsections = 1 + studentsCourse.get(course.getIdCourse()).size()/course.getMaxChildPerSection();
            }catch(ArithmeticException e){
                //e.printStackTrace();
                System.out.println("id:"+course.getIdCourse()+" name: "+cs.nameCourse(course.getIdCourse()));
            }
            course.setMinSections(minsections);
            
            if(course.opciones().size()>0){
                ArrayList<Integer> noAsign = studentSections(trst,course,minsections,
                        course.opciones(),studentsCourse.get(course.getIdCourse()),students);
                if(noAsign != null)
                    course.setStudentsNoAsignados(noAsign);
            }
            else
                System.out.println("FAILURE: " + course.getIdCourse());
        }
//        XMLWriterDOM.xmlCreate(trst, retst);
        this.teachers = trst;
        this.students = students;
        this.courses = rst;
        mv.addObject("TAMX",TAMX);
        mv.addObject("TAMY",TAMY);
        mv.addObject("profesores", trst);
        mv.addObject("students",students);
        mv.addObject("Courses",rst);
        mv.addObject("cs",cs);
        mv.addObject("log",Log);
    }
    
    private class CompConjuntos implements Comparator<Tupla<Integer,ArrayList<Integer>>>{
        @Override
        public int compare(Tupla<Integer,ArrayList<Integer>> e1, Tupla<Integer,ArrayList<Integer>> e2) {
            if(e1.y.size() < e2.y.size()){
                return 1;
            } else {
                return -1;
            }
        }
    }
    
    private class CompCoursesRank implements Comparator<Course>{
        @Override
        public int compare(Course e1, Course e2) {
            if(e1.getRank() < e2.getRank())
                return -1;
            else
                return 1;
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
            stids.sort(new CompConjuntos());
        }catch(Exception e){
            return null;
        }
        ArrayList<Integer> diferencia;
        diferencia = stids.get(0).y;
        int lastTeacher = -1;
        int lastStudent = -1;
        for(int i = 0;i < stids.size();i++){
            for(Teacher t : teachers){
                boolean nextSection = false;
                if(c.getTrestricctions().contains(t.idTeacher) &&
                        t.asignaturaCursable(c.getIdCourse()) && 
                        t.patronCompatible(sec.get(stids.get(i).x))){
                    int k = 0;
                    lastTeacher = i;
                    for(Integer j:diferencia){
                        if((k<studentsCourse.size()/c.getMinSections()+1 || studentsCourse.size()==1) && !idsAsignados.contains(j) && 
                                students.get(j).patronCompatible(sec.get(stids.get(i).x))){
                            idsAsignados.add(j);
                            students.get(j).ocuparHueco(sec.get(stids.get(i).x), c.getIdCourse()*100+c.getSections());
                            k++;
                            lastStudent = i;
                            nextSection = true;
                        }
                    }
                    if(k<studentsCourse.size()/c.getMinSections()){
                        for(Integer j:stids.get(i).y){
                            if((k<studentsCourse.size()/c.getMinSections()+1 || studentsCourse.size()==1) && !idsAsignados.contains(j) && 
                                    students.get(j).patronCompatible(sec.get(stids.get(i).x))){
                                idsAsignados.add(j);
                                students.get(j).ocuparHueco(sec.get(stids.get(i).x), c.getIdCourse()*100+c.getSections());
                                k++;
                                lastStudent = i;
                                nextSection = true;
                            }
                        }
                    }
                    if(k>0){
                        t.ocuparHueco(sec.get(stids.get(i).x), c.getIdCourse()*100+c.getSections());
                        c.ocuparHueco(sec.get(stids.get(i).x));
                    }
                    c.updateSectionsNoEnrolled(c.getSections());
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
        c.updateSectionsNoEnrolled(c.getMinSections()-c.getSections());
        if(idsAsignados.size()!= studentsCourse.size()){
            System.out.println("FAILURE");      
            ArrayList<Integer> ret = conjuntos.diferencia(studentsCourse, idsAsignados);
            String tname = "";
            for(Integer teacher: c.getTrestricctions()){
                tname += cs.fetchName(teacher) + " ,";
            }
            if(tname.length()>2)
                tname = tname.substring(0, tname.length()-1);
            if(c.getTrestricctions().isEmpty())
                Log.add("-No hay profesores asignados al curso:"+cs.nameCourse(c.getIdCourse()));
            else if(lastTeacher <= lastStudent){
                Log.add("-Los profesores "+tname+" asignados al curso:"+cs.nameCourse(c.getIdCourse())+" no tienen disponible ningun hueco compatible");
            }else{
                Log.add("-Los siguientes estudiantes no tienen secciones disponibles para el curso "+cs.nameCourse(c.getIdCourse())+":");
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

    public String studentsJSON(){
        if(this.students == null)
            return "ejecuta el algoritmo";
        else{
            return (new Gson()).toJson(this.students);
        }
    }
    
    public String teachersJSON(){
        if(this.teachers == null)
            return "ejecuta el algoritmo";
        else{
            return (new Gson()).toJson(this.teachers);
        }
    }

    public String coursesJSON(){
        if(this.courses == null)
            return "ejecuta el algoritmo";
        else{
            return (new Gson()).toJson(this.courses);
        }
    }
}