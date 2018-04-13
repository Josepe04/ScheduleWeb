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
    private int totalBlocks;
    
    public Consultas(){
        teachers = new ArrayList<>();
        tdefault = teacherDefault();
        stDefault = new Student(0);
        stDefault.setGenero("Male");
        stDefault.setName("default");
        courseName = new HashMap<>();
        totalBlocks = this.totalBlocks();
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
    
    public static HashMap<Integer,ArrayList<Integer>> getCoursesGroups(ArrayList<Student> st,ArrayList<Integer> listaCourses,
            String yearid, String tempid){
        String consulta="select * from ClassGroups where yearid ="+yearid +" and templateid="+tempid;
        ArrayList<Integer> groups = new ArrayList();
        HashMap<Integer,ArrayList<Integer>> classes = new HashMap();
        HashMap<Integer,ArrayList<Integer>> courses = new HashMap();
        try {
            ResultSet rs = DBConnect.st.executeQuery(consulta);
            while(rs.next()){
                groups.add(rs.getInt("GroupID"));               
                st.add(new Student(rs.getInt("GroupID"),"group"+rs.getInt("GroupID"),"group"));
            }
            for(Integer g:groups){
                classes.put(g, new ArrayList());
                consulta="select * from ClassGroupClasses where GroupID="+g;
                rs = DBConnect.st.executeQuery(consulta);
                while(rs.next()){
                    classes.get(g).add(rs.getInt("classid"));               
                }
                for(Integer c:classes.get(g)){
                    consulta="select * from classes where classid="+c;
                    rs = DBConnect.st.executeQuery(consulta);
                    while(rs.next()){
                        if(!courses.containsKey(rs.getInt("courseid"))){
                            courses.put(rs.getInt("courseid"), new ArrayList());
                            listaCourses.add(rs.getInt("courseid"));
                        }
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
    
    protected ArrayList<Course> getRestricciones(int[] ids,int[] tempinfo){
        ArrayList<Course> ret = new ArrayList<>();
        String consulta = "";
        try {
            ResultSet rs;
            boolean tempcorrect;
            for(int i = 0; i < ids.length;i++){
                tempcorrect = false;
                consulta = "select * from courses where courseid="+ids[i]
                        +" and Elementary="+tempinfo[0]
                        +" and HS="+tempinfo[1]
                        +" and MidleSchool="+tempinfo[2]
                        +" and PreSchool="+tempinfo[3];
                rs = DBConnect.st.executeQuery(consulta);
                if(rs.next()){
                    tempcorrect=true;
                }
                consulta = "select udd.data\n" +
                "                from uddata udd\n" +
                "                inner join udfield udf\n" +
                "                    on udd.fieldid = udf.fieldid\n" +
                "                inner join udgroup udg\n" +
                "                    on udg.groupid = udf.groupid\n" +
                "                    and udg.grouptype = 'course'\n" +
                "                    and udg.groupname = 'Schedule'\n" +
                "                    and udf.fieldName = 'Schedule'\n" +
                "                where udd.id ="+ids[i];
                rs = DBConnect.st.executeQuery(consulta);
                if(rs.next() && tempcorrect){
                    if(rs.getInt(1)==1){
                        Course r=new Course(ids[i]);
                        ret.add(r);
                        courseName.put(ids[i], fetchNameCourse(ids[i]));
                    }
                }
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
                    try{
                        ret.get(i).setMinGapDays(rs.getInt(1));
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
                    if(s2!=null){
                        int idt = convertString(s2);
                        ar.add(idt);
                        if(!teachers.contains(idt))
                            teachers.add(idt);
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
                "                    and udf.fieldName = 'Rooms'\n" +
                "                where udd.id ="+ret.get(i).getIdCourse();

                rs=DBConnect.st.executeQuery(consulta);
                String rooms = "";
                while(rs.next()){
                    rooms = rs.getString(1);
                }
                if(!rooms.equals(""))
                    for(String room : rooms.split(",")){
                        try{
                            ret.get(i).addRoom(Integer.parseInt(room));
                        }catch(Exception e){
                            System.err.println("no se puede leer bien el campo rooms en el curso" 
                                    + ret.get(i).getIdCourse());
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
                
                consulta="select MaxSize from courses where courseid="
                        +ret.get(i).getIdCourse();
                rs=DBConnect.st.executeQuery(consulta);
                while(rs.next()){
                    ret.get(i).setMaxChildPerSection(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    } 
    
    public HashMap<Integer,Room> getRooms(){
        HashMap<Integer,Room> rooms = new HashMap();
        String consulta = "select * from rooms";
        ResultSet rs;
        try{
            rs = DBConnect.st.executeQuery(consulta);
            while(rs.next()){
                int id = rs.getInt("roomid");
                Room r = new Room(rs.getInt("roomid"),rs.getString("room"),rs.getInt("size"));
                rooms.put(id, r);
            }
        }catch(SQLException ex){
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rooms;
    }
    
    private int totalBlocks(){
        String excludes = "";
        int ret = Algoritmo.TAMX*Algoritmo.TAMY;
        Course caux = new Course(1);
        String consulta="select udd.data\n" +
                "                from uddata udd\n" +
                "                inner join udfield udf\n" +
                "                    on udd.fieldid = udf.fieldid\n" +
                "                inner join udgroup udg\n" +
                "                    on udg.groupid = udf.groupid\n" +
                "                    and udg.grouptype = 'school'\n" +
                "                    and udg.groupname = 'Schedule'\n" +
                "                    and udf.fieldName = 'ExcludeBlocks'";
                
        ResultSet rs;
        try {
            rs = DBConnect.st.executeQuery(consulta);
        
                while(rs.next()){
                    if(!excludes.contains(rs.getString(1)))
                        excludes+=rs.getString(1);
                }
                caux.setExcludeBlocks(excludes);
                ret = caux.opciones().size();
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
    
    public int[] templateInfo(String tempid){
        int[] ret = new int[4];
        String consulta = "select * from ScheduleTemplate where templateid="+tempid;
        try {
            ResultSet rs = DBConnect.st.executeQuery(consulta);
            while(rs.next()){
                ret[0] = rs.getInt("Elementary");
                ret[1] = rs.getInt("HighSchool");
                ret[2] = rs.getInt("MiddleSchool");
                ret[3] = rs.getInt("Preschool");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
    
    /**
     * 
     * @param c
     * @param stCourse
     * @param yearid
     * @return 
     */
    public ArrayList<Student> restriccionesStudent(ArrayList<Integer> c,HashMap<Integer,ArrayList<Integer>> stCourse,String yearid){
        ArrayList<Student> ret= new ArrayList<>();
        String consulta = "    select sr.courseid, sr.studentid, p.gender\n" +
            "    from studentrequests sr, person p, person_student ps \n" +
            "     where sr.studentid = p.personid\n" +
            "    and ps.studentid = p.personid\n" +
            "    and sr.yearid = " +yearid+
            "    and ps.status = 'enrolled'\n" +
            "    and ps.nextstatus = 'enrolled'\n" +
            "    order by gender";
        ResultSet rs;
        try {
            rs = DBConnect.st.executeQuery(consulta);
            while(rs.next()){
                int courseid = rs.getInt("courseid");
                int studentid = rs.getInt("studentid");
                Student st = new Student(studentid);
                st.setGenero(rs.getString("gender"));
                if(!c.contains(courseid))
                    c.add(courseid);
                if(!ret.contains(st))
                    ret.add(st);
                if(!stCourse.containsKey(courseid))
                    stCourse.put(courseid, new ArrayList());
                stCourse.get(courseid).add(studentid);
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
    
    public static int[] convertIntegers(ArrayList<Integer> integers) {
        int[] ret = new int[integers.size()];
        for (int i=0; i < ret.length; i++){
            ret[i] = integers.get(i).intValue();
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
 
    public String fetchName(int id){
        String consulta = "select * from person where personid="+id;
        String ret = "";
        ResultSet rs;
        try {
            rs = DBConnect.st.executeQuery(consulta);
            while(rs.next()){
               ret= rs.getString("lastname")+" ";
               ret += rs.getString("firstname");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
    
    private int convertString(String s){
        int ret=0; 
        for(int i = 1; i <= s.length();i++){
            switch(s.substring(i-1, i)){
                case "0":
                case "1":
                case "2":
                case "3":
                case "4":
                case "5":
                case "6":
                case "7":
                case "8":
                case "9":
                    ret*=10;
                    ret+=Integer.parseInt(s.substring(i-1, i));
                    break;
                default:
                    break;
            }
        }
        return ret;
    }

    
    /*
    -----------------------
    --GETTERS AND SETTERS--
    -----------------------
    */
    
    public int getTotalBlocks() {
        return totalBlocks;
    }

    public void setTotalBlocks(int totalBlocks) {
        this.totalBlocks = totalBlocks;
    }
}
