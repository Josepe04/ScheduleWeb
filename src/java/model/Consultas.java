/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Norhan
 */
public class Consultas {
    private ArrayList<Integer> teachers;
    private Teacher tdefault;
    private Student stDefault;
    private HashMap<Integer,String> courseName;
    
    public Consultas(){
        teachers = new ArrayList<>();
        tdefault = teacherDefault();
        stDefault = new Student(0);
        stDefault.setGenero("Male");
        stDefault.setName("default");
        courseName = new HashMap<>();
    }
    
    public static ArrayList<Tupla<Integer,String>> getYears(){
        ArrayList<Tupla<Integer,String>> ret = new ArrayList<>();
        String consulta="select * from SchoolYear";
        try {
            ResultSet rs = DBConnect.st.executeQuery(consulta);
            while(rs.next()){
                int yearid = rs.getInt("yearid");
                String yearName = rs.getString("SchoolYear");
                ret.add(new Tupla<>(yearid,yearName));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    } 
    
    //public static getCoursesStudentRequest()
    
    public static HashMap<Integer,ArrayList<Integer>> getCoursesGroups(String yearid){
        String consulta="select * from ClassGroups where yearid ="+yearid;
        ArrayList<Integer> groups = new ArrayList();
        HashMap<Integer,ArrayList<Integer>> classes = new HashMap();
        HashMap<Integer,ArrayList<Integer>> courses = new HashMap();
        try {
            ResultSet rs = DBConnect.st.executeQuery(consulta);
            while(rs.next()){
                groups.add(rs.getInt("GroupID"));               
            }
            for(Integer g:groups){
                classes.put(g, new ArrayList());
                consulta="select * from ClassGroupClasses where GroupID="+g;
                rs = DBConnect.st.executeQuery(consulta);
                while(rs.next()){
                    classes.get(g).add(rs.getInt("classid"));               
                }
                for(Integer c:classes.get(g)){
                    if(!courses.containsKey(g))
                        courses.put(g, new ArrayList());
                    consulta="select * from classes where classid="+c;
                    rs = DBConnect.st.executeQuery(consulta);
                    while(rs.next()){
                        if(!courses.containsKey(rs.getInt("courseid")))
                            courses.put(rs.getInt("courseid"), new ArrayList());
                        courses.get(rs.getInt("courseid")).add(g);               
                    }
                }
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return courses;
    }
    
    public static ArrayList<Template> getTemplates(String yearid){
        ArrayList<Template> ret = new ArrayList();
        String consulta="select * from ScheduleTemplate where yearid="+yearid;
        try {
            ResultSet rs = DBConnect.st.executeQuery(consulta);
            while(rs.next()){
                String name = rs.getString("TemplateName");
                int cols = rs.getInt("cols");
                int rows = rs.getInt("rows");
                int id = rs.getInt("templateid");
                ret.add(new Template(id,cols,rows,name));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
    
    public static ArrayList<Tupla<String,String>> getRowHeader(int id,int rows){
        String consulta = "";
        ResultSet rs;
        ArrayList<Tupla<String,String>> ret = new ArrayList();
        for(int i = 1; i <= rows; i++){
            consulta = "select * from ScheduleTemplateTimeTable "
                    + "where templateid="+id+" and Row="+i+" and Col=0";
            try {
                rs = DBConnect.st.executeQuery(consulta);
                while(rs.next()){ 
                    ret.add(new Tupla(rs.getString("TemplateTime"),
                            rs.getString("TemplateText")));
                }
            } catch (SQLException ex) {
                Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return ret;
    }
    
    public static ArrayList<String> getColHeader(int id,int cols){
        String consulta = "";
        ResultSet rs;
        ArrayList<String> ret = new ArrayList();
        for(int i = 1; i <= cols; i++){
            consulta = "select * from ScheduleTemplateTimeTable "
                    + "where templateid="+id+" and Col="+i+" and Row=0";
            try {
                rs = DBConnect.st.executeQuery(consulta);
                while(rs.next()){ 
                    ret.add(rs.getString("TemplateText"));
                }
            } catch (SQLException ex) {
                Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return ret;
    }
    
    protected ArrayList<Course> getRestricciones(int[] ids){
        teachers = new ArrayList<>();
        ArrayList<Course> ret = new ArrayList<>();
        String consulta = "";
        try {
            ResultSet rs;
            for(int i = 0; i < ids.length;i++){
                Course r=new Course(ids[i]);
                ret.add(r);
                courseName.put(ids[i], fetchNameCourse(ids[i]));
            }
            for(int i = 0; i < ret.size();i++){
                consulta = "select udd.data\n" +
                "                from uddata udd\n" +
                "                inner join udfield udf\n" +
                "                    on udd.fieldid = udf.fieldid\n" +
                "                inner join udgroup udg\n" +
                "                    on udg.groupid = udf.groupid\n" +
                "                    and udg.grouptype = 'course'\n" +
                "                    and udg.groupname = 'Schedule'\n" +
                "                    and udf.fieldName = 'BlocksPerWeek'\n" +
                "                where udd.id ="+ret.get(i).getIdCourse();

                rs=DBConnect.st.executeQuery(consulta);
                while(rs.next()){
                    ret.get(i).setBlocksWeek(rs.getInt(1));
                }

                consulta="select udd.data\n" +
                "                from uddata udd\n" +
                "                inner join udfield udf\n" +
                "                    on udd.fieldid = udf.fieldid\n" +
                "                inner join udgroup udg\n" +
                "                    on udg.groupid = udf.groupid\n" +
                "                    and udg.grouptype = 'course'\n" +
                "                    and udg.groupname = 'Schedule'\n" +
                "                    and udf.fieldName = 'GR'\n" +
                "                where udd.id="+ret.get(i).getIdCourse();

                rs=DBConnect.st.executeQuery(consulta);
                while(rs.next()){
                    ret.get(i).setGR(rs.getBoolean(1));
                }

                consulta = "select udd.data\n" +
                "                from uddata udd\n" +
                "                inner join udfield udf\n" +
                "                    on udd.fieldid = udf.fieldid\n" +
                "                inner join udgroup udg\n" +
                "                    on udg.groupid = udf.groupid\n" +
                "                    and udg.grouptype = 'course'\n" +
                "                    and udg.groupname = 'Schedule'\n" +
                "                    and udf.fieldName = 'MaxSections'\n" +
                "                where udd.id ="+ret.get(i).getIdCourse();

                rs=DBConnect.st.executeQuery(consulta);
                while(rs.next()){
                    ret.get(i).setMaxSections(rs.getString(1));

                }

                consulta="select udd.data\n" +
                "                from uddata udd\n" +
                "                inner join udfield udf\n" +
                "                    on udd.fieldid = udf.fieldid\n" +
                "                inner join udgroup udg\n" +
                "                    on udg.groupid = udf.groupid\n" +
                "                    and udg.grouptype = 'course'\n" +
                "                    and udg.groupname = 'Schedule'\n" +
                "                    and udf.fieldName = 'MinGapBlocks'\n" +
                "                where udd.id ="+ret.get(i).getIdCourse();

                rs=DBConnect.st.executeQuery(consulta);
                while(rs.next()){
                    ret.get(i).setMinGapBlocks(rs.getString(1));
                }

                consulta="select udd.data\n" +
                "                from uddata udd\n" +
                "                inner join udfield udf\n" +
                "                    on udd.fieldid = udf.fieldid\n" +
                "                inner join udgroup udg\n" +
                "                    on udg.groupid = udf.groupid\n" +
                "                    and udg.grouptype = 'course'\n" +
                "                    and udg.groupname = 'Schedule'\n" +
                "                    and udf.fieldName = 'MinGapDays'\n" +
                "                where udd.id ="+ret.get(i).getIdCourse();

                rs=DBConnect.st.executeQuery(consulta);
                while(rs.next()){
                    ret.get(i).setMinGapDays(rs.getInt(1));
                }

                consulta="select udd.data\n" +
                "                from uddata udd\n" +
                "                inner join udfield udf\n" +
                "                    on udd.fieldid = udf.fieldid\n" +
                "                inner join udgroup udg\n" +
                "                    on udg.groupid = udf.groupid\n" +
                "                    and udg.grouptype = 'course'\n" +
                "                    and udg.groupname = 'Schedule'\n" +
                "                    and udf.fieldName = 'Rank'\n" +
                "                where udd.id ="+ret.get(i).getIdCourse();

                rs=DBConnect.st.executeQuery(consulta);
                while(rs.next()){
                    try{
                    ret.get(i).setRank(rs.getInt(1));
                    }catch(Exception e){
                    }
                }
                consulta="select udd.data\n" +
                "                from uddata udd\n" +
                "                inner join udfield udf\n" +
                "                    on udd.fieldid = udf.fieldid\n" +
                "                inner join udgroup udg\n" +
                "                    on udg.groupid = udf.groupid\n" +
                "                    and udg.grouptype = 'course'\n" +
                "                    and udg.groupname = 'Schedule'\n" +
                "                    and udf.fieldName = 'Teachers'\n" +
                "                where udd.id ="+ret.get(i).getIdCourse();

                rs=DBConnect.st.executeQuery(consulta);
                String[] s=new String[2];
                while(rs.next()){
                    s=rs.getString(1).split(",");
                }
                ArrayList<Integer> ar = new ArrayList<>();
                for(String s2:s){
                    if(!s2.equals("")){
                        ar.add(Integer.parseInt(s2));
                        if(!teachers.contains(Integer.parseInt(s2)))
                            teachers.add(Integer.parseInt(s2));
                    }
                }
                ret.get(i).setTrestricctions(ar);
                
                consulta="select udd.data\n" +
                "                from uddata udd\n" +
                "                inner join udfield udf\n" +
                "                    on udd.fieldid = udf.fieldid\n" +
                "                inner join udgroup udg\n" +
                "                    on udg.groupid = udf.groupid\n" +
                "                    and udg.grouptype = 'course'\n" +
                "                    and udg.groupname = 'Schedule'\n" +
                "                    and udf.fieldName = 'ExcludeBlocks'\n" +
                "                where udd.id ="+ret.get(i).getIdCourse();

                rs=DBConnect.st.executeQuery(consulta);
                String excludes = "";
                while(rs.next()){
                    excludes+=rs.getString(1);
                }
                
                consulta="select udd.data\n" +
                "                from uddata udd\n" +
                "                inner join udfield udf\n" +
                "                    on udd.fieldid = udf.fieldid\n" +
                "                inner join udgroup udg\n" +
                "                    on udg.groupid = udf.groupid\n" +
                "                    and udg.grouptype = 'school'\n" +
                "                    and udg.groupname = 'Schedule'\n" +
                "                    and udf.fieldName = 'ExcludeBlocks'";
                
                rs=DBConnect.st.executeQuery(consulta);
                while(rs.next()){
                    if(!excludes.contains(rs.getString(1)))
                        excludes+=rs.getString(1);
                }
                ret.get(i).setExcludeBlocks(excludes);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    } 
    
    private Teacher teacherDefault(){
        Teacher ret = new Teacher();
        String consulta;
        ResultSet rs;
        try {
            consulta="select udd.data\n" +
"        from uddata udd\n" +
"        inner join udfield udf\n" +
"            on udd.fieldid = udf.fieldid\n" +
"        inner join udgroup udg\n" +
"            on udg.groupid = udf.groupid\n" +
"            and udg.grouptype = 'school'\n" +
"            and udg.groupname = 'Schedule'\n" +
"            and udf.fieldName = 'MaxSections'";
            rs = DBConnect.st.executeQuery(consulta);
            while(rs.next()){
                ret.MaxSections=rs.getInt(1);
            }
            
            consulta="select udd.data\n" +
"        from uddata udd\n" +
"        inner join udfield udf\n" +
"            on udd.fieldid = udf.fieldid\n" +
"        inner join udgroup udg\n" +
"            on udg.groupid = udf.groupid\n" +
"            and udg.grouptype = 'school'\n" +
"            and udg.groupname = 'Schedule'\n" +
"            and udf.fieldName = 'MaxPreps'";
            rs = DBConnect.st.executeQuery(consulta);
            while(rs.next()){
                ret.Preps=rs.getInt(1);
            }
            
            consulta="select udd.data\n" +
"        from uddata udd\n" +
"        inner join udfield udf\n" +
"            on udd.fieldid = udf.fieldid\n" +
"        inner join udgroup udg\n" +
"            on udg.groupid = udf.groupid\n" +
"            and udg.grouptype = 'school'\n" +
"            and udg.groupname = 'Schedule'\n" +
"            and udf.fieldName = 'MaxBxD'\n";
            rs = DBConnect.st.executeQuery(consulta);
            while(rs.next()){
                ret.MaxBxD=rs.getInt(1);
            }
        } catch (Exception ex ) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
    
    public ArrayList<Teacher> teachersList(){
        ArrayList<Teacher> ret = new ArrayList<>();
        for(Integer s:teachers){
            if(!s.equals(""))
                ret.add(restriccionesTeacher(s));
        }
        return ret;
    }
    
    public Teacher restriccionesTeacher(int id){
        Teacher ret = new Teacher();
        String consulta = "";

        ResultSet rs;
        if(id!=0)
            try {
                consulta="select udd.data\n" +
    "                from uddata udd\n" +
    "                inner join udfield udf\n" +
    "                    on udd.fieldid = udf.fieldid\n" +
    "                inner join udgroup udg\n" +
    "                    on udg.groupid = udf.groupid\n" +
    "                    and udg.grouptype = 'Staff'\n" +
    "                    and udg.groupname = 'Schedule'\n" +
    "                    and udf.fieldName = 'MaxSections'\n" +
    "                where udd.id ="+id;
                rs = DBConnect.st.executeQuery(consulta);
                while(rs.next()){
                    ret.MaxSections=rs.getInt(1);
                }
                if(ret.MaxSections==0)
                    ret.MaxSections = tdefault.MaxSections;


                consulta="select udd.data\n" +
    "                from uddata udd\n" +
    "                inner join udfield udf\n" +
    "                    on udd.fieldid = udf.fieldid\n" +
    "                inner join udgroup udg\n" +
    "                    on udg.groupid = udf.groupid\n" +
    "                    and udg.grouptype = 'Staff'\n" +
    "                    and udg.groupname = 'Schedule'\n" +
    "                    and udf.fieldName = 'Preps'\n" +
    "                where udd.id ="+id;
                rs = DBConnect.st.executeQuery(consulta);
                while(rs.next()){
                    ret.Preps=rs.getInt(1);
                }
                if(ret.Preps == 0)
                    ret.Preps = tdefault.Preps;

                consulta="select udd.data\n" +
    "                from uddata udd\n" +
    "                inner join udfield udf\n" +
    "                    on udd.fieldid = udf.fieldid\n" +
    "                inner join udgroup udg\n" +
    "                    on udg.groupid = udf.groupid\n" +
    "                    and udg.grouptype = 'Staff'\n" +
    "                    and udg.groupname = 'Schedule'\n" +
    "                    and udf.fieldName = 'MaxBxD'\n" +
    "                where udd.id ="+id;
                rs = DBConnect.st.executeQuery(consulta);
                while(rs.next()){
                    String s = rs.getString(1);
                    try{
                        ret.MaxBxD = Integer.parseInt(s);
                    }catch(Exception e){
                        ret.MaxBxD = 1;
                    }
                }
                if(ret.MaxBxD == 0)
                    ret.MaxBxD = tdefault.MaxBxD;

                consulta="select udd.data\n" +
    "                from uddata udd\n" +
    "                inner join udfield udf\n" +
    "                    on udd.fieldid = udf.fieldid\n" +
    "                inner join udgroup udg\n" +
    "                    on udg.groupid = udf.groupid\n" +
    "                    and udg.grouptype = 'Staff'\n" +
    "                    and udg.groupname = 'Schedule'\n" +
    "                    and udf.fieldName = 'ExcludedBlocks'\n" +
    "                where udd.id="+id;
                rs = DBConnect.st.executeQuery(consulta);
                while(rs.next()){
                    ret.setExcludeBlocks(rs.getString(1));
                }
                ret.idTeacher = id;
            } catch (Exception ex ) {
                Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
            }
        ret.setName(fetchName(id));
        return ret;
    }
    
    public ArrayList<Student> restriccionesStudent(int id){
        ArrayList<Student> ret= new ArrayList<>();
        String consulta = "    select sr.studentid, p.gender\n" +
            "    from studentrequests sr, person p, person_student ps \n" +
            "     where sr.studentid = p.personid\n" +
            "    and ps.studentid = p.personid\n" +
            "    and sr.yearid = " +264+
            "    and sr.courseid = " +id+
            "    and ps.status = 'enrolled'\n" +
            "    and ps.nextstatus = 'enrolled'\n" +
            "    order by gender";
        ResultSet rs;
        try {
            rs = DBConnect.st.executeQuery(consulta);
            while(rs.next()){
                Student st = new Student(rs.getInt("studentid"));
                st.setGenero(rs.getString("gender"));
                ret.add(st);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(ret.isEmpty()){
            ret.add(stDefault);
        }else{
            for(Student st:ret){
                st.setName(fetchName(st.getId()));
            }
        }
        return ret;
    }
    
    private String fetchNameCourse(int id){
        String ret = "";
        try {
            String consulta = "select * from courses where courseid = "+id;
            ResultSet rs = DBConnect.st.executeQuery(consulta);
            while(rs.next()){
                ret = rs.getString("title");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
    
    public String nameCourse(int id){
        return courseName.get(id);
    }
    
    public String nameCourseAndSection(int id){
        if(id==0)
            return "0";
        int idc = id/100;
        id=id-(idc*100);
        
        return nameCourse(idc) + " Section: "+id;
    }
 
    private String fetchName(int id){
        String consulta = "select * from person where personid="+id;
        String ret = "";
        ResultSet rs;
        try {
            rs = DBConnect.st.executeQuery(consulta);
            while(rs.next()){
               ret= rs.getString("lastname")+" , ";
               ret += rs.getString("firstname");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
    
    
}
