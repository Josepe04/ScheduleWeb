/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import dataManage.Conjuntos;
import dataManage.Tupla;
import dataManage.Consultas;
import com.google.gson.Gson;
import dataManage.Restrictions;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Norhan
 */
public class Algoritmo {
    public static int TAMX = 6;
    public static int TAMY = 12;
    public final static int CHILDSPERSECTION = 20;
    private ArrayList<String> Log;
    private Conjuntos<Integer> conjuntos;
    public Algoritmo(){
        Log = new ArrayList<>();
        conjuntos = new Conjuntos<>();
    }
    
    public Algoritmo(int x, int y){
        TAMX = x;
        TAMY = y;
        Log = new ArrayList<>();
        conjuntos = new Conjuntos<>();
    }
    
    /**
     * algoritmo
     * @param mv 
     * @param r 
     * @param roommode 
     */
    public void algo(ModelAndView mv,Restrictions r,int roommode){
        for(Course course: r.courses){
            int minsections = 1;
            try{
                minsections = 1 + r.studentsCourse.get(course.getIdCourse()).size()/course.getMaxChildPerSection();
            }catch(ArithmeticException e){
                //e.printStackTrace();
                System.out.println("id:"+course.getIdCourse()+" name: "+r.cs.nameCourse(course.getIdCourse()));
            }
            course.setMinSections(minsections);
            ArrayList<Integer> noAsign = (ArrayList<Integer>) r.studentsCourse.get(course.getIdCourse()).clone();
            if(course.opciones().size()>0){
                //Segun el modo de gestion de clases cambia como rellenas la parte de studentsSections
                switch (roommode) {
                    case 0:
                        noAsign = studentSections(r,r.teachers,course,minsections,
                                    course.opciones(),noAsign,r.students,null);
                        break;
                    case 1:
                        noAsign = studentSections(r,r.teachers,course,minsections,
                                    course.opciones(),noAsign,r.students,course.getRooms());
                        break;
                    case 2:
                        noAsign = studentSections(r,r.teachers,course,minsections,
                                    course.opciones(),noAsign,r.students,r.groupRooms);
                        break;
                    case 3:
                        if(course.getRooms().isEmpty())
                            noAsign = studentSections(r,r.teachers,course,minsections,
                                    course.opciones(),noAsign,r.students,r.groupRooms);
                        else
                            noAsign = studentSections(r,r.teachers,course,minsections,
                                    course.opciones(),noAsign,r.students,course.getRooms());
                        break;
                    default:
                        break;
                }
                
                //si no asign es distinto de null quiere decir que no se han podido 
                //asignar todos los estudiantes al curso
                if(noAsign != null){
                    int sectionsNoEnroled = noAsign.size()/course.getMaxChildPerSection();
                    if(sectionsNoEnroled == 0) 
                        sectionsNoEnroled=1;
                    course.setStudentsNoAsignados(noAsign);
                    course.setSectionsNoEnrolled(sectionsNoEnroled);
                    double noasignsize = noAsign.size();
                    double studentscoursesize = r.studentsCourse.get(course.getIdCourse()).size();
                    double percent = 100 - (noasignsize/studentscoursesize)*100;
                    course.setPercentEnrolled(percent);
                }
                else{
                    course.setSectionsNoEnrolled(0);
                    course.setPercentEnrolled(100);
                }
            }
            else
                System.out.println("FAILURE: " + course.getIdCourse());
        }
//        XMLWriterDOM.xmlCreate(trst, retst);
        mv.addObject("TAMX",TAMX);
        mv.addObject("TAMY",TAMY);
        mv.addObject("profesores", r.teachers);
        mv.addObject("students",r.students);
        mv.addObject("Courses",r.courses);
        mv.addObject("cs",r.cs);
        mv.addObject("rooms",r.rooms);
        mv.addObject("grouprooms",r.groupRooms);
        
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
    
    
    private ArrayList<Integer> studentSections(Restrictions r,ArrayList<Teacher> teachers,Course c,int minsections,ArrayList<ArrayList<Tupla>> sec,
        ArrayList<Integer> studentsCourse,HashMap<Integer,Student> students,ArrayList<Integer> rooms){
        ArrayList<Tupla<Integer,ArrayList<Integer>>> stids = new ArrayList<>();
        ArrayList<Integer> idsAsignados = new ArrayList<>();
        //Crea una lista con conjuntos de estudiantes compatibles con cada seccion
        //disponible del curso.
        for(int i = 0; i < sec.size();i++){
            stids.add(new Tupla(i,new ArrayList<>()));
            for(Integer j:studentsCourse){
                if(students.get(j).patronCompatible(sec.get(i))){
                    stids.get(i).y.add(j);
                }
            }
        }
        //Ordena la lista de conjuntos por numero de estudiantes de mayor a menor.
        try{
            stids.sort(new CompConjuntos());
        }catch(Exception e){
            return null;
        }
        //inicializo el conjunto de estudiantes seleccionables
        ArrayList<Integer> diferencia;
        if(!stids.isEmpty())
            diferencia = stids.get(0).y;
        else
            diferencia = new ArrayList();
        int lastTeacher = -1;
        int lastStudent = -1;
        //recorro la lista de conjuntos y la de profesores
        for(int i = 0;i < stids.size();i++){
            for(Teacher t : teachers){
                Room compatibleRoom = null;
                //compruebo que el profesor tiene esta seccion disponible
                if(c.getTrestricctions().contains(t.getIdTeacher()) &&
                        t.asignaturaCursable(c.getIdCourse()) && 
                        t.patronCompatible(sec.get(stids.get(i).x))){
                    //si el schedule por rooms esta activado comprueba si las rooms disponibles 
                    //tienen la seccion elegida disponible
                    if(rooms != null)
                        for(Integer room : rooms){
                            if(r.rooms.get(room).patronCompatible(sec.get(stids.get(i).x))){
                                compatibleRoom = r.rooms.get(room);
                                break;
                            }
                        }
                    //si hay una room compatible o no el schedule por rooms esta desactivado
                    //entonces ya procedemos a ocupar los huecos de los estudiantes con la seccion elegida
                    if(compatibleRoom != null ||rooms == null){
                        int k = 0;
                        lastTeacher = i;
                        for(Integer j:diferencia){
                            if((k<studentsCourse.size()/c.getMinSections()+1 || studentsCourse.size()==1) && !idsAsignados.contains(j) && 
                                    students.get(j).patronCompatible(sec.get(stids.get(i).x))){
                                idsAsignados.add(j);
                                students.get(j).ocuparHueco(sec.get(stids.get(i).x), c.getIdCourse()*100+c.getSections());
                                k++;
                                lastStudent = i;
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
                                }
                            }
                        }
                        //una vez que ya hay estudiantes asignados ha esta seccion ocupamos el hueco en el teacher
                        //y añadimos la seccion a la tabla del curso.
                        if(k>0){
                            t.ocuparHueco(sec.get(stids.get(i).x), c.getIdCourse()*100+c.getSections());
                            c.ocuparHueco(sec.get(stids.get(i).x));
                            if(compatibleRoom!=null)
                                compatibleRoom.ocuparHueco(c.getIdCourse()*100+c.getSections(), sec.get(stids.get(i).x));
                        }
                        if(idsAsignados.size() == studentsCourse.size()){
                            for(Integer st:idsAsignados){
                                students.get(st).addAsignado(c.getIdCourse());
                            }
                            c.setStudentsAsignados(idsAsignados);
                            return null;
                        }
                    } else{
                        Log.add("-No hay rooms compatibles con el curso:"+r.cs.nameCourse(c.getIdCourse()));
                    }
                }
            }
            if(i+1<stids.size())
                diferencia = conjuntos.diferencia(stids.get(i+1).y, stids.get(i).y);
        }
        
        c.setStudentsAsignados(idsAsignados);
        for(Integer st:idsAsignados){
            students.get(st).addAsignado(c.getIdCourse());
        }
        //Si los estudiantes asignados son menos que el numero de students request
        //creamos una entrada en el log y ponemos el porcentaje de acierto en el curso.
        if(idsAsignados.size()!= studentsCourse.size()){
            System.out.println("FAILURE");      
            ArrayList<Integer> ret = conjuntos.diferencia(studentsCourse, idsAsignados);
            String tname = "";
            for(Integer teacher: c.getTrestricctions()){
                tname += r.cs.fetchName(teacher) + " ,";
            }
            if(tname.length()>2)
                tname = tname.substring(0, tname.length()-1);
            if(c.getTrestricctions().isEmpty())
                Log.add("-No hay profesores asignados al curso:"+r.cs.nameCourse(c.getIdCourse()));
            else if(lastTeacher <= lastStudent){
                Log.add("-Los profesores "+tname+" asignados al curso:"+r.cs.nameCourse(c.getIdCourse())+" no tienen disponible ningun hueco compatible");
            }else{
                Log.add("-Los siguientes estudiantes no tienen secciones disponibles para el curso "+r.cs.nameCourse(c.getIdCourse())+":");
                String anadir = "";
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
            }
            for(Integer st:ret){
                students.get(st).addNoAsignado(c.getIdCourse());
            }
            return ret;
        }
        return null;
    }
}