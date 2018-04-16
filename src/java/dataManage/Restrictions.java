/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataManage;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import model.Algoritmo;
import model.Course;
import model.Room;
import model.Student;
import model.Teacher;

/**
 *
 * @author Chema
 */
public class Restrictions {
    public Consultas cs;
    public ArrayList<Integer> idCourses; 
    public HashMap<Integer,ArrayList<Integer>> studentsCourse;
    public HashMap<Integer,Student> students;
    public HashMap<Integer,Room> rooms;
    public ArrayList<Course> courses;
    public ArrayList<Teacher> teachers;
    public ArrayList<Integer> groupRooms; 
    
    public Restrictions(String yearid,String tempid,String groupofrooms){
        cs = new Consultas();
        this.idCourses = new ArrayList(); 
        this.groupRooms = cs.roomsGroup(groupofrooms);
        ArrayList<Student> st = new ArrayList();
        this.studentsCourse = Consultas.getCoursesGroups(st,idCourses,yearid,tempid);
        this.students = new HashMap<>();
        st = (new Conjuntos<Student>()).union(st,
                cs.restriccionesStudent(idCourses,studentsCourse,yearid));  
        for(Student s:st){
            this.students.put(s.getId(), s);
        }
        this.rooms = cs.getRooms();
        this.courses = cs.getRestricciones(Consultas.convertIntegers(idCourses),cs.templateInfo(tempid));
        this.courses.sort(new Restrictions.CompCoursesRank());
        this.teachers = cs.teachersList();
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
