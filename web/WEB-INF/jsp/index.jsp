<%@page import="model.Consultas"%>
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
        <title>Welcome to Spring Web MVC project</title>
    </head>
    <style>
        .tcolores{
            background-color: #8080804f;
        }
    </style>
    <body>
        <h2>Teachers</h2>
        <%
            Consultas cs = new Consultas();
            Integer TAMX = (Integer)request.getAttribute("TAMX");
            Integer TAMY = (Integer)request.getAttribute("TAMY");
            List<Teacher> lista = (List)request.getAttribute("profesores");
            for(Teacher t:lista){
                out.println("<h3>"+t.getName()+"</h3>");
                out.println("<table id='table_id' class='table'>");
                out.println("<tr><th>S</th><th>M</th><th>T</th><th>W</th><th>TA</th><th>TB</th></tr>");
                boolean swapcolor = true;
                for(int i = 0; i < TAMY; i++){
                    if(swapcolor){
                        out.println("<tr class='tcolores'>");
                        swapcolor = false;
                    }else{
                        out.println("<tr>");
                        swapcolor = true;
                    }
                        
                    for(int j = 0; j < TAMX; j++){
                        out.println("<td>"+cs.nameCourse(t.getHuecos()[j][i])+"</td>");
                    }
                    out.println("</tr>");
                }
                out.println("</table>");
            }
         %>
         <h2>Students</h2>
         <%
            Consultas cs2 = new Consultas();
            List<Student> lista2 = (List)request.getAttribute("students");
            for(Student t:lista2){
                out.println("<h3>"+t.getName()+"</h3>");
                out.println("<table id='table_id' class='table'>");
                out.println("<tr><th>S</th><th>M</th><th>T</th><th>W</th><th>TA</th><th>TB</th></tr>");
                boolean swapcolor = true;
                for(int i = 0; i < TAMY; i++){
                    if(swapcolor){
                        out.println("<tr class='tcolores'>");
                        swapcolor = false;
                    }else{
                        out.println("<tr>");
                        swapcolor = true;
                    }
                        
                    for(int j = 0; j < TAMX; j++){
                        out.println("<td>"+cs2.nameCourse(t.getHuecos()[j][i])+"</td>");
                    }
                    out.println("</tr>");
                }
                out.println("</table>");
            }
         %>
         
    </body>
</html>
