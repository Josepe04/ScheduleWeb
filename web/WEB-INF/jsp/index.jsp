<%@page import="dataManage.Tupla"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="model.Course"%>
<%@page import="dataManage.Consultas"%>
<%@page import="model.Student"%>
<%@page import="model.Teacher"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        
        <%@ include file="infouser.jsp" %>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Menu</title>
        <style>
            .tcolores{
                background-color: #8080804f;
            }
            #table_id{
                table-layout: fixed;
            }
            #table_id td{
                height: 33%;
            }
            .espacioLibre{
                background-color: green;
            }
            
        </style>
        <script>
            $(document).ready(function () {
                $("#showSTC").click(function () {
                    $("#STC").toggleClass('in');
                });
                
                $("#showTC").click(function () {
                    $("#TC").toggleClass('in');
                });
                
                $("#showLogComplete").click(function () {
                    $("#LogComplete").toggleClass('in');
                });
                
                
                $("#showCourses").click(function () {
                    $("#Coursestable").toggleClass('in');
                });
                
                $("#showCoursesenrol").click(function () {
                    $("#Coursesenrol").toggleClass('in');
                });
                
                $("#showTeachers").click(function () {
                    $("#Teacherstable").toggleClass('in');
                });
                
                $("#showTeachers2").click(function () {
                    $("#Teacherstable2").toggleClass('in');
                });
                
                $("#showTeachersdisp").click(function () {
                    $("#Teachersdisp").toggleClass('in');
                });
                
                $("#showStudents").click(function () {
                    $("#Studentstable").toggleClass('in');
                });
                
                $("#showStudentsEnrolled").click(function () {
                    $("#StudentsEnrolled").toggleClass('in');
                });
            });
        </script>
    </head>
    <body>
        <%
            Consultas cs = (Consultas)request.getAttribute("cs");
            Integer TAMX = (Integer)request.getAttribute("TAMX");
            Integer TAMY = (Integer)request.getAttribute("TAMY");
            ArrayList<Tupla<String,String>> headRow = (ArrayList<Tupla<String,String>>)request.getAttribute("hFilas");
            ArrayList<String> headCol = (ArrayList<String>)request.getAttribute("hcols");
            List<Course> courses = (List)request.getAttribute("Courses");
            List<Teacher> lista = (List)request.getAttribute("profesores");
            HashMap<Integer,Student> lista2 = (HashMap)request.getAttribute("students");
            ArrayList<String> log = (ArrayList<String>)request.getAttribute("log");
            boolean swapcolor = true;
            String headCols = "<tr><th>Period</th>";
            for(String s : headCol){
                headCols+="<th>"+s;
                headCols+="</th>";
            }
            headCols+="</tr>";
        %>
        <div class="col-xs-12 text-center" id="myTab">
            <ul class="nav nav-tabs">
                <li class="active"><a id="Courses" data-toggle="tab" href="#courses" role="tab" >Courses</a></li>
                <li><a id="Teachers" data-toggle="tab" href="#teachers" role="tab">Teachers</a></li>
                <li><a id="Students" data-toggle="tab" href="#students" role="tab">Students</a></li>
                <!--<li><a id="Log" data-toggle="tab" href="#log" role="tab">Log</a></li>-->                
            </ul>
        </div>
        
        <div class="tab-content">

            <div role="tabpanel" class="col-xs-12 tab-pane in active" id="courses">
                <legend id="showCourses">
                        Schedule
                        <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                        </span>
                </legend>
                <div class="form-group collapse" id="Coursestable">
                <%    
                    for(Course t: courses){
                        out.println("<h3>"+cs.nameCourse(t.getIdCourse())+"</h3>");
                        out.println("<table id='table_id' class='table'>");
                        out.println(headCols);
                        swapcolor = true;
                        for(int i = 0; i < TAMY; i++){
                            if(swapcolor){
                                out.println("<tr class='tcolores'>");
                                swapcolor = false;
                            }else{
                                out.println("<tr>");
                                swapcolor = true;
                            }
                            if(i<headRow.size())
                                out.println("<td>"+headRow.get(i).text()+"</td>");
                            else
                                out.println("<td></td>");
                            for(int j = 0; j < TAMX; j++){
                                if(!t.getHuecos()[j][i].equals("0"))
                                    out.println("<td> section "+t.getHuecos()[j][i]+"</td>");
                                else
                                    out.println("<td> </td>");
                            }
                            out.println("</tr>");
                        }
                        out.println("</table>");
                    }
                %>
                </div>
                <legend id="showCoursesenrol">
                        Missing Enrolled
                        <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                        </span>
                </legend>
                <div class="form-group collapse" id="Coursesenrol">
                    <%
                    for(Course c: courses){
                        if(c.getPercentEnrolled()!=100){
                            out.println("<h3>"+cs.nameCourse(c.getIdCourse())+"</h3>");
                            out.println("<table id='table_id' class='table'>");
                            out.println("<tr><th>Field</th><th>Content</th></tr>");

                            out.println("<tr>");
                            out.println("<td>Enrolled students percent</td>");
                            out.println("<td>"+c.getPercentEnrolled()+"</td>");
                            out.println("</tr>");

                            out.println("<tr>");
                            out.println("<td>Number of sections no enrolled</td>");
                            out.println("<td>"+c.getSectionsNoEnrolled()+"</td>");
                            out.println("</tr>");
                            out.println("</table>");
                            
                            String studentNames = "";
                            out.println("<tr>");
                            out.println("<td>Students no enrolled</td>");
                            out.println("<td>");
                            if(!c.getStudentsNoAsignados().isEmpty())
                                studentNames += lista2.get(c.getStudentsNoAsignados().get(0)).getName();
                            for(int i = 1; i < c.getStudentsNoAsignados().size();i++){
                                studentNames += " ,"+lista2.get(c.getStudentsNoAsignados().get(i)).getName();
                            }
                            out.println(studentNames + ".");
                            out.println("</td>");
                            out.println("</tr>");
                            out.println("</table>");
                                    
                            out.println("<table id='table_id' class='table'>");
                            out.println(headCols);
                            swapcolor = true;
                            int[][] huecosStudents = c.huecosStudents();
                            for(int i = 0; i < TAMY; i++){
                                if(swapcolor){
                                    out.println("<tr class='tcolores'>");
                                    swapcolor = false;
                                }else{
                                    out.println("<tr>");
                                    swapcolor = true;
                                }
                                if(i<headRow.size())
                                    out.println("<td>"+headRow.get(i).text()+"</td>");
                                else
                                    out.println("<td></td>");
                                for(int j = 0; j < TAMX; j++){
                                    if(huecosStudents[j][i] != 0)
                                        out.println("<td class='espacioLibre'> free space </td>");
                                    else
                                        out.println("<td> </td>");
                                }
                                out.println("</tr>");
                            }
                            out.println("</table>");
                        }
                    }
                    %>
                </div>
            </div>
            
            <div role="tabpanel" class="col-xs-12 tab-pane" id="teachers">
                <legend id="showTeachers">
                        Schedule
                        <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                        </span>
                </legend>
                <div class="form-group collapse" id="Teacherstable">
                <%
                    out.println("<h2>Teachers</h2>");
                    for(Teacher t:lista){
                        out.println("<h3>"+t.getName()+"</h3>");
                        out.println("<table id='table_id' class='table'>");
                        out.println(headCols);
                        swapcolor = true;
                        for(int i = 0; i < TAMY; i++){
                            if(swapcolor){
                                out.println("<tr class='tcolores'>");
                                swapcolor = false;
                            }else{
                                out.println("<tr>");
                                swapcolor = true;
                            }
                            if(i<headRow.size())
                                out.println("<td>"+headRow.get(i).text()+"</td>");
                            else
                                out.println("<td></td>");
                            for(int j = 0; j < TAMX; j++){
                                if(t.getHuecos()[j][i]!=0)
                                    out.println("<td>"+cs.nameCourseAndSection(t.getHuecos()[j][i])+"</td>");
                                else
                                    out.println("<td></td>");
                            }
                            out.println("</tr>");
                        }
                        out.println("</table>");
                    }
                %>
                </div>
                
                <legend id="showTeachers2">
                        Teachers Master Schedule
                        <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                        </span>
                </legend>
                <div class="form-group collapse" id="Teacherstable2">
                <%
                    int countDays = 0;
                    for(String s:headCol){
                        out.println("<h3>"+s+"</h3>");
                        out.println("<table class='table'>");
                        swapcolor = true;
                        out.println("<tr>");
                        out.println("<th>Teachers | Hours</th>");
                        for(int i = 0; i < headRow.size(); i++){
                            out.println("<th>"+headRow.get(i).text()+"</th>");
                        }
                        out.println("</tr>");
                        for(int i = 0; i < TAMX;i++){
                            for(Teacher t : lista){
                                if(swapcolor){
                                    out.println("<tr class='tcolores'>");
                                    swapcolor = false;
                                }else{
                                    out.println("<tr>");
                                    swapcolor = true;
                                }
                                out.println("<td>"+t.getName()+"</td>");
                                for(int j = 0; j < TAMY;j++){
                                    if(t.getHuecos()[countDays][j]!=0)
                                        out.println("<td>"+cs.nameCourseAndSection(t.getHuecos()[countDays][j])+"</td>");
                                    else
                                        out.println("<td></td>");
                                }
                                out.println("</tr>");
                            }
                        }
                        countDays++;
                    }
                    out.println("</table>");
                %>
                </div>
                
                <legend id="showTeachersdisp">
                        Availability
                        <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                        </span>
                </legend>
                <div class="form-group collapse" id="Teachersdisp">
                    <%
                        for(Teacher t : lista){
                            out.println("<h3>"+t.getName()+"</h3>");
                            out.println("<table id='table_id' class='table'>");
                            out.println("<tr><th>Field</th><th>Content</th></tr>");
                            
                            out.println("<tr>");
                            out.println("<td>Courses teaching</td>");
                            out.println("<td>");
                            for(Integer i:t.getPrepsComplete()){
                                out.println(", "+cs.nameCourse(i));
                            }
                            out.println("</td>");
                            out.println("</tr>");
                            
                            out.println("<tr>");
                            out.println("<td>Section availability</td>");
                            out.println("<td>"+t.seccionesDisponibles(cs.getTotalBlocks())+"</td>");
                            out.println("</tr>");
                            
                            out.println("<tr>");
                            out.println("<td>Prep availability</td>");
                            out.println("<td>"+t.prepsDisponibles(cs.getTotalBlocks())+"</td>");
                            out.println("</tr>");
                            
                            out.println("</table>");
                        }
                    
                    %>
                </div>
                
            </div>
            
            <div role="tabpanel" class="col-xs-12 tab-pane" id="students">
                <legend id="showStudents">
                        Students schedule
                        <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                        </span>
                </legend>
                <div class="form-group collapse" id="Studentstable">
                <%    
                    out.println("<h2>Students</h2>");
                    for(Map.Entry<Integer, Student> entry : lista2.entrySet()){
                        out.println("<h3>"+entry.getValue().getName()+"</h3>");
                        out.println("<table id='table_id' class='table'>");
                        out.println(headCols);
                        swapcolor = true;
                        for(int i = 0; i < TAMY; i++){
                            if(swapcolor){
                                out.println("<tr class='tcolores'>");
                                swapcolor = false;
                            }else{
                                out.println("<tr>");
                                swapcolor = true;
                            }
                            if(i<headRow.size())
                                out.println("<td>"+headRow.get(i).text()+"</td>");
                            else
                                out.println("<td></td>");
                            for(int j = 0; j < TAMX; j++){
                                if(entry.getValue().getHuecos()[j][i] != 0)
                                    out.println("<td>"+cs.nameCourseAndSection(entry.getValue().getHuecos()[j][i])+"</td>");
                                else
                                    out.println("<td></td>");
                            }
                            out.println("</tr>");
                        }
                        out.println("</table>");
                    }
                 %>
                 </div>
                 
                 <legend id="showStudentsEnrolled">
                        Students enrolled
                        <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                        </span>
                </legend>
                <div class="form-group collapse" id="StudentsEnrolled">
                 <%
                    for(Map.Entry<Integer, Student> entry : lista2.entrySet()){
                        out.println("<table id='table_id' class='table'>");
                        out.println("<h3>"+entry.getValue().getName()+"</h3>");
                        out.println("<tr><th>Course Name</th><th>Enrolled</th></tr>");
                        for(Integer i: entry.getValue().getCursosAsignados()){
                            out.println("<tr>");
                            out.println("<td>"+cs.nameCourse(i)+"</td><td>yes</td>");
                            out.println("</tr>");
                        }
                        for(Integer i: entry.getValue().getCursosNoAsignados()){
                            out.println("<tr>");
                            out.println("<td>"+cs.nameCourse(i)+"</td><td>no</td>");
                            out.println("</tr>");
                        }
                        out.println("</table>");
                        out.println("<table id='table_id' class='table'>");
                        out.println("<tr><th>Total enrolled</th><th>Total no enrolled</th></tr>");
                        out.println("<tr>");
                        out.println("<td>"+entry.getValue().getCursosAsignados().size()+"</td><td>"+entry.getValue().getCursosNoAsignados().size()+"</td>");
                        out.println("</tr>");
                        out.println("</table>");
                    }
                 %>
                </div>
            </div>
            <!--<div role="tabpanel" class="col-xs-12 tab-pane" id="log">
                <legend id="showSTC">
                        Student Course
                        <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                        </span>
                </legend>
                <div class="form-group collapse" id="STC">
                <%    
                    out.println("<table id='table_id' class='table'>");
                    out.println("<tr>");
                    out.println("<th> Students|courses </th>");
                    for(Course c: courses){
                        out.println("<th>"+cs.nameCourse(c.getIdCourse())+"</th>");
                    }
                    out.println("</tr>");
                    for(Map.Entry<Integer, Student> entry : lista2.entrySet()){
                        if(swapcolor){
                            out.println("<tr class='tcolores'>");
                            swapcolor = false;
                        }else{
                            out.println("<tr>");
                            swapcolor = true;
                        }
                        out.println("<td>"+entry.getValue().getName()+"</td>");
                        ArrayList<Integer> sta;
                        for(Course c: courses){
                            sta = c.getStudentsAsignados();
                            if(sta!=null && sta.contains(entry.getValue().getId())){
                                out.println("<td>"+1+"</td>");
                            }else if(sta!=null && sta.contains(entry.getValue().getId())){
                                out.println("<td>"+2+"</td>");
                            }else{
                                out.println("<td>"+0+"</td>");
                            }
                        }
                        out.println("</tr>");
                    }
                    out.println("</table>");
                 %>
                </div> 
                 
                <legend id="showTC">
                        Teacher Course
                        <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                        </span>
                </legend>
                <div class="form-group collapse" id="TC">
                 <%    
                    out.println("<h2>Teacher Course</h2>");
                    out.println("<table id='table_id' class='table'>");
                    out.println("<tr>");
                    out.println("<th> Teachers|courses </th>");
                    for(Course c: courses){
                        out.println("<th>"+cs.nameCourse(c.getIdCourse())+"</th>");
                    }
                    out.println("<th>setions/maxsections</th>");
                    out.println("</tr>");
                    swapcolor = true;
                    for(Teacher t:lista){
                        if(swapcolor){
                            out.println("<tr class='tcolores'>");
                            swapcolor = false;
                        }else{
                            out.println("<tr>");
                            swapcolor = true;
                        }
                        out.println("<td>"+t.getName()+"</td>");
                        for(Course c: courses){
                            if(t.getSecciones().containsKey(c.getIdCourse())){
                                out.println("<td>"+t.getSecciones().get(c.getIdCourse())+"</td>");
                            }else{
                                out.println("<td>"+0+"</td>");
                            }
                        }
                        out.println("<td>"+t.getSecsComplete()+"/"+t.getMaxSections()+"</td>");
                        out.println("</tr>");
                    }
                    out.println("</table>");
                 %>
                 </div> -->
                 <legend id="showLogComplete">
                        Log complete
                        <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                        </span>
                </legend>
                <div class="form-group collapse" id="LogComplete">
                    <%
                        for(String s:log){
                            out.print("<p>");
                            out.print(s);
                            out.print("</p>");
                        }
                    %>
                </div>
            <!--</div>-->
        </div>
    </body>
</html>
