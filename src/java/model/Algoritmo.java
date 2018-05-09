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

    public static int TAMX = 3;
    public static int TAMY = 11;
    public final static int CHILDSPERSECTION = 25;
    private ArrayList<String> Log;
    private Conjuntos<Integer> conjuntos;

    public Algoritmo() {
        Log = new ArrayList<>();
        conjuntos = new Conjuntos<>();
    }

    public Algoritmo(int x, int y) {
        TAMX = x;
        TAMY = y;
        Log = new ArrayList<>();
        conjuntos = new Conjuntos<>();
    }

    /**
     * algoritmo
     *
     * @param mv
     * @param r
     * @param roommode
     */
    public void algo(ModelAndView mv, Restrictions r, int roommode) {
        for (Course course : r.courses) {
            int minsections = 1; //falla al dividir entre 0
            try {
                int numAlumnos = course.getMaxChildPerSection();
                if (numAlumnos == 0) {
                    numAlumnos = CHILDSPERSECTION; // POR DEFECTO
                }
                //MINIMO DE SECCIONES NECESARIAS PARA TODOS LOS ESTUDIANTES (TENIENDO EN CUENTA EL NUMERO MAX  DE ALUMNOS POR AULA)
                //solo prueba
                if (r.studentsCourse.get(course.getIdCourse()).size() % numAlumnos == 0) {
                    minsections = (r.studentsCourse.get(course.getIdCourse()).size() / numAlumnos);
                } else {
                    minsections = 1 + (r.studentsCourse.get(course.getIdCourse()).size() / numAlumnos);
                }
                //pruebas 
                //if(minsections == 0) minsections = 4;
            } catch (ArithmeticException e) {
                //e.printStackTrace();
                System.out.println("id:" + course.getIdCourse() + " name: " + r.cs.nameCourse(course.getIdCourse()));
            }
            course.setMinSections(minsections);
            ArrayList<Integer> noAsign = (ArrayList<Integer>) r.studentsCourse.get(course.getIdCourse()).clone();
            HashMap<Integer, Integer> noAsignSection = new HashMap<Integer, Integer>(); // indicara seccion de cada alumno de la clase
            // esto rellena las secciones de manera equitativa tutilizando el random

            for (int i = 0; i < noAsign.size(); i++) { // secciones 0,1,2,3,...
                noAsignSection.put(noAsign.get(i), i % minsections);
            }

            if (course.opciones().size() > 0) {
                //Segun el modo de gestion de clases cambia como rellenas la parte de studentsSections
                switch (roommode) {
                    case 0:
                        noAsign = studentSections(r, r.teachers, course, minsections,
                                course.opciones(), noAsign, noAsignSection, r.students, null);
                        break;
                    case 1:
                        noAsign = studentSections(r, r.teachers, course, minsections,
                                course.opciones(), noAsign, noAsignSection, r.students, course.getRooms());
                        break;
                    case 2:
                        noAsign = studentSections(r, r.teachers, course, minsections,
                                course.opciones(), noAsign, noAsignSection, r.students, r.groupRooms);
                        break;
                    case 3:
                        if (course.getRooms().isEmpty()) {
                            noAsign = studentSections(r, r.teachers, course, minsections,
                                    course.opciones(), noAsign, noAsignSection, r.students, r.groupRooms);
                        } else {
                            noAsign = studentSections(r, r.teachers, course, minsections,
                                    course.opciones(), noAsign, noAsignSection, r.students, course.getRooms());
                        }
                        break;
                    default:
                        break;
                }

                //si no asign es distinto de null quiere decir que no se han podido 
                //asignar todos los estudiantes al curso
                if (noAsign != null) {
                    int numAlumnos = course.getMaxChildPerSection();
                    if (numAlumnos == 0) {
                        numAlumnos = CHILDSPERSECTION; // POR DEFECTO
                    }
                    int sectionsNoEnroled = noAsign.size() / numAlumnos;
                    if (sectionsNoEnroled == 0) {
                        sectionsNoEnroled = 1;
                    }
                    course.setStudentsNoAsignados(noAsign);
                    course.setSectionsNoEnrolled(sectionsNoEnroled);
                    double noasignsize = noAsign.size();
                    double studentscoursesize = r.studentsCourse.get(course.getIdCourse()).size();
                    double percent = 100 - (noasignsize / studentscoursesize) * 100;
                    course.setPercentEnrolled(percent);
                } else {
                    course.setSectionsNoEnrolled(0);
                    course.setPercentEnrolled(100);
                }
            } else {
                System.out.println("FAILURE: " + course.getIdCourse());
            }
        }
//        XMLWriterDOM.xmlCreate(trst, retst);
        mv.addObject("TAMX", TAMX);
        mv.addObject("TAMY", TAMY);
        mv.addObject("profesores", r.teachers);
        mv.addObject("students", r.students);
        mv.addObject("Courses", r.courses);
        mv.addObject("cs", r.cs);
        mv.addObject("rooms", r.rooms);
        mv.addObject("grouprooms", r.groupRooms);
        mv.addObject("log", Log);
    }

    private class CompConjuntos implements Comparator<Tupla<Integer, ArrayList<Integer>>> {

        @Override
        public int compare(Tupla<Integer, ArrayList<Integer>> e1, Tupla<Integer, ArrayList<Integer>> e2) {
            if (e1.y.size() < e2.y.size()) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    private void deleteInStids(ArrayList<Tupla<Integer, ArrayList<Integer>>> stids, int pos) {
        for (int i = 0; i < stids.size(); i++) {
            if (stids.get(i).getX() == pos) {
                stids.remove(i);
            }
        }
    }

    private void updateStidsWithUserDefined(ArrayList<Tupla<Integer, ArrayList<Integer>>> stids, ArrayList<ArrayList<Boolean>> totalBlocks) { // se encarga de descartar las filas que han sido bloqueadas desde confiuration school 
        int contador = 0;
        for (int i = 0; i < totalBlocks.size(); i++) {
            if (!totalBlocks.get(i).isEmpty()) {
                for (int j = 0; j < totalBlocks.get(i).size(); j++) {
                    if (!totalBlocks.get(i).get(j)) {
                        deleteInStids(stids, contador);
                    }
                    contador++;
                }
            }
        }
    }

    private int buscarPosBlock(int pos, ArrayList<Tupla<Integer, ArrayList<Integer>>> stids){
        for (int i = 0; i < stids.size(); i++){
            if(stids.get(i).x == pos) return i;
        }
        return -1;
    }
    private void sortStidsByPriority(ArrayList<Tupla<Integer, ArrayList<Integer>>> stids, Course c, Restrictions r) {
       /* if (c.getPreferedBlocks() != null && c.getPreferedBlocks().size() > 0) {
            for (int h = 0; h < c.getPreferedBlocks().get(c.getSections() - 1).size(); h++) {
                ArrayList<Tupla> auxTupla = new ArrayList();
                auxTupla.add(new Tupla(c.getPreferedBlocks().get(c.getSections() - 1).get(h).x - 1, c.getPreferedBlocks().get(c.getSections() - 1).get(h).y - 1));
                if (!idsAsignados.contains(j) && students.get(j).patronCompatible(auxTupla)) {
                    idsAsignados.add(j);
                    students.get(j).ocuparHueco(auxTupla, c.getIdCourse() * 100 + c.getSections());
                    k++;
                    lastStudent = i;
                }
            }
        }*/
       if (c.getPreferedBlocks() != null && c.getPreferedBlocks().size() > 0) {
           for (int i = 0; i < c.getPreferedBlocks().get(c.getSections()-1).size(); i++) {
               Tupla<Integer, ArrayList<Integer>> tuplaAux = stids.get(i);
               
               int posBlock = ((c.getPreferedBlocks().get(c.getSections()-1).get(i).x -1)) + ((c.getPreferedBlocks().get(c.getSections()-1).get(i).y -1 )*r.totalBlocks.get(0).size());
               int res = buscarPosBlock(posBlock,stids);
               if(res != -1){
                    stids.set(i,stids.get(res));
                    stids.set(posBlock,tuplaAux);
               }
           }
        }
    }

    private ArrayList<Integer> studentSections(Restrictions r, ArrayList<Teacher> teachers, Course c, int minsections, ArrayList<ArrayList<Tupla>> sec,
            ArrayList<Integer> studentsCourse, HashMap<Integer, Integer> studentsCourseSection, HashMap<Integer, Student> students, ArrayList<Integer> rooms) {

        ArrayList<Tupla<Integer, ArrayList<Integer>>> stids = new ArrayList<>();
        ArrayList<Integer> idsAsignados = new ArrayList<>();

        //Crea una lista con conjuntos de estudiantes compatibles con cada seccion
        //disponible del curso.
        // aqui es donde se tendra que modificar por donde se comenzara a buscar las posiciones del patron
        for (int i = 0; i < sec.size(); i++) {
            stids.add(new Tupla(i, new ArrayList<>()));
            for (Integer j : studentsCourse) {
                if (students.get(j).patronCompatible(sec.get(i))) {
                    stids.get(i).y.add(j);
                }
            }
        }


        /*
        for (int i = 0; i < sec.size(); i++) {
            stids.add(new Tupla(i, new ArrayList<>()));
            
            for (Integer j : studentsCourse) {            
                ArrayList<Tupla<Integer, Integer>> arrayAux = c.getPreferedBlocks().get(studentsCourseSection.get(j));
                
                for (int k = 0; k < arrayAux.size(); k++) {
                    if(students.get(j).patronCompatible2(arrayAux.get(k))){
                        stids.add(new Tupla(k, new ArrayList<>()));
                        
                        stids.get(i).y.add(j);
                    }
                } 
                
                if (students.get(j).patronCompatible(sec.get(i))) {
                    stids.get(i).y.add(j);
                }
            }
        }*/
        updateStidsWithUserDefined(stids, r.totalBlocks);

        //Ordena la lista de conjuntos por numero de estudiantes de mayor a menor.
        try {
            stids.sort(new CompConjuntos());
        } catch (Exception e) {
            return null;
        }
        sortStidsByPriority(stids, c,r);

        //inicializo el conjunto de estudiantes seleccionables
        ArrayList<Integer> diferencia;
        if (!stids.isEmpty()) {
            diferencia = stids.get(0).y;
        } else {
            diferencia = new ArrayList();
        }
        int lastTeacher = -1;
        int lastStudent = -1;
        int i = 0;
        int numSeccion = 0; // indicara numeros de seccion se iniciara en 0 hasta el n-1 seccion

        //recorro la lista de conjuntos y la de profesores
        while (i < stids.size()) { // recorrido a los bloques disponibles
            for (Teacher t : teachers) { // recorrido a los teachers  totales
                Room compatibleRoom = null;
                //compruebo que el profesor puede impartir esta clase
                if (c.getTrestricctions().contains(t.getIdTeacher()) //comprobar que el profesor puede dar ese curso
                        && t.asignaturaCursable(c.getIdCourse()) // comprueba que el profesor puede iniciar una nueva seccion
                        && t.patronCompatible(sec.get(stids.get(i).x))
                        && c.getSections() <= c.getMinSections()) {
                    //si el schedule por rooms esta activado comprueba si las rooms disponibles 
                    //tienen la seccion elegida disponible -- NO REVISADA
                    if (rooms != null) {
                        for (Integer room : rooms) {
                            if (r.rooms.get(room).patronCompatible(sec.get(stids.get(i).x))) {
                                compatibleRoom = r.rooms.get(room);
                                break;
                            }
                        }
                    }

                    //si hay una room compatible o no el schedule por rooms esta desactivado
                    //entonces ya procedemos a ocupar los huecos de los estudiantes con la seccion elegida
                    if (compatibleRoom != null || rooms == null) {
                        int k = 0;
                        lastTeacher = i;
                        int studentsBySection = studentsCourse.size() / c.getMinSections(); // alumnos por seccion
                        if (studentsCourse.size() % c.getMinSections() == 0) {
                            studentsBySection++; // esto comprueba si no es multiplo es cuando debe incrementarse 1
                        }

                        for (Integer j : diferencia) {
                            /*if (c.getPreferedBlocks() != null && c.getPreferedBlocks().size() > 0) {
                                    for (int h = 0; h < c.getPreferedBlocks().get(c.getSections() - 1).size(); h++) {
                                        ArrayList<Tupla> auxTupla = new ArrayList();
                                        auxTupla.add(new Tupla(c.getPreferedBlocks().get(c.getSections() - 1).get(h).x - 1, c.getPreferedBlocks().get(c.getSections() - 1).get(h).y - 1));
                                        if (!idsAsignados.contains(j) && students.get(j).patronCompatible(auxTupla)) {
                                            idsAsignados.add(j);
                                            students.get(j).ocuparHueco(auxTupla, c.getIdCourse() * 100 + c.getSections());
                                            k++;
                                            lastStudent = i;
                                        }
                                    }
                                }*/
                            //    if ((k < studentsCourse.size() / c.getMinSections() + 1 || studentsCourse.size() == 1) && !idsAsignados.contains(j)
                            if (((k < studentsBySection) || studentsCourse.size() == 1) && !idsAsignados.contains(j)
                                    && students.get(j).patronCompatible(sec.get(stids.get(i).x))) {

                                idsAsignados.add(j);
                                students.get(j).ocuparHueco(sec.get(stids.get(i).x), c.getIdCourse() * 100 + c.getSections());
                                k++;
                                lastStudent = i;
                            }
                        }
                        if (k < studentsBySection) { // si no  se llena la seccion
                            // se entra aqui para meter los alumnos que no cumplian las restricciones ?? -- no se si es necesario
                            for (Integer j : stids.get(i).y) {
                                //     if ((k < studentsCourse.size() / c.getMinSections() + 1 || studentsCourse.size() == 1) && !idsAsignados.contains(j)
                                if ((k < studentsBySection || studentsCourse.size() == 1) && !idsAsignados.contains(j)
                                        && students.get(j).patronCompatible(sec.get(stids.get(i).x))) {
                                    idsAsignados.add(j);
                                    students.get(j).ocuparHueco(sec.get(stids.get(i).x), c.getIdCourse() * 100 + c.getSections());
                                    k++;
                                    lastStudent = i;
                                }
                            }
                        }
                        //una vez que ya hay estudiantes asignados ha esta seccion ocupamos el hueco en el teacher
                        //y añadimos la seccion a la tabla del curso.
                        if (k > 0) { // se llena los huecos de ese profesor incluyendole la seccion
                            t.ocuparHueco(sec.get(stids.get(i).x), c.getIdCourse() * 100 + c.getSections());
                            c.ocuparHueco(sec.get(stids.get(i).x));
                            if (compatibleRoom != null) {
                                compatibleRoom.ocuparHueco(c.getIdCourse() * 100 + c.getSections(), sec.get(stids.get(i).x));
                            }
                        }
                        if (idsAsignados.size() == studentsCourse.size()) { // se pudo organizar un horario con todos los alumnos
                            // de la clase
                            for (Integer st : idsAsignados) {
                                students.get(st).addAsignado(c.getIdCourse()); // aqui se agrega este curso en una lista la cual
                                // indica los cursos donde fue "matriculado" este alumno
                            }
                            c.setStudentsAsignados(idsAsignados); // se actualiza la lista de alumnos de ese curso
                            return null;
                        }
                    } else {
                        Log.add("-No hay rooms compatibles con el curso:" + r.cs.nameCourse(c.getIdCourse()));
                    }
                }
            }
            if (i + 1 < stids.size()) {
                diferencia = conjuntos.diferencia(stids.get(i + 1).y, stids.get(i).y);
            }
            i++;
        }

        c.setStudentsAsignados(idsAsignados); // se actualiza la lista aunque no se usaron a todos los estudiantes
        for (Integer st : idsAsignados) {
            students.get(st).addAsignado(c.getIdCourse());
        }

        //Si los estudiantes asignados son menos que el numero de students request
        //creamos una entrada en el log y ponemos el porcentaje de acierto en el curso.
        if (idsAsignados.size() != studentsCourse.size()) {
            System.out.println("FAILURE");
            ArrayList<Integer> ret = conjuntos.diferencia(studentsCourse, idsAsignados);
            String tname = "";
            for (Integer teacher : c.getTrestricctions()) {
                tname += r.cs.fetchName(teacher) + " ,";
            }
            if (tname.length() > 2) {
                tname = tname.substring(0, tname.length() - 1);
            }
            if (c.getTrestricctions().isEmpty()) {
                Log.add("-No hay profesores asignados al curso:" + r.cs.nameCourse(c.getIdCourse()));
            } else if (lastTeacher <= lastStudent) {
                Log.add("-Los profesores " + tname + " asignados al curso:" + r.cs.nameCourse(c.getIdCourse()) + " no tienen disponible ningun hueco compatible");
            } else {
                Log.add("-Los siguientes estudiantes no tienen secciones disponibles para el curso " + r.cs.nameCourse(c.getIdCourse()) + ":");
                String anadir = "";
                ArrayList<ArrayList<Tupla>> aux = null;
                for (Integer i2 : ret) {
                    anadir += students.get(i2).getName() + ",";
                    if (aux == null) {
                        aux = students.get(i2).listPatronesCompatibles(c.opciones());
                    } else {
                        aux = students.get(i2).listPatronesCompatibles(aux);
                    }
                }
                c.setPatronesStudents(aux);

                //anadir = anadir.substring(0, anadir.length() - 2) + ".";
                Log.add(anadir);
            }
            for (Integer st : ret) {
                students.get(st).addNoAsignado(c.getIdCourse());
            }
            return ret;
        }
        return null;
    }
}
