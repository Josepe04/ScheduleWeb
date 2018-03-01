/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controllers;

/**
 *
 * @author nmohamed
 */


import atg.taglib.json.util.JSONObject;
import com.google.gson.Gson;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.LoginVerification;
import model.User;
import javax.servlet.http.HttpSession;
import model.Algoritmo;
import model.DBConnect;

import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;


@Controller
public class Homepage extends MultiActionController  {
    public static Connection cn;
    public static Connection cn2;
    public static Statement st;
    public static Statement st2;
    
    private Object getBean(String nombrebean, ServletContext servlet){
        ApplicationContext contexto = WebApplicationContextUtils.getRequiredWebApplicationContext(servlet);
        Object beanobject = contexto.getBean(nombrebean);
        return beanobject;
    }
    
    @RequestMapping
    public ModelAndView inicio(HttpServletRequest hsr, HttpServletResponse hsr1) throws Exception {
        //connection to comunication
        DriverManagerDataSource dataSource = (DriverManagerDataSource) this.getBean("comunicacion", hsr.getServletContext());
        if(this.cn!=null)
            Homepage.cn.close();
        this.cn = dataSource.getConnection();
        st = this.cn.createStatement(); 
        //connection to datasourceAH
        DriverManagerDataSource dataSource2 = (DriverManagerDataSource)this.getBean("dataSourceAH",hsr.getServletContext());
        if(this.cn2!=null)
            Homepage.cn2.close();
        this.cn2 = dataSource2.getConnection();
        this.st2 = cn2.createStatement();
        DBConnect db = new DBConnect();
        ModelAndView mv = new ModelAndView("index");
        //(new Algoritmo()).algo();
        return mv;
        //return new ModelAndView("userform");
    }
    
    public static ModelAndView checklogin(HttpServletRequest hsr){
        if(hsr.getSession().getAttribute("user") == null)
            return new ModelAndView("redirect:/");
        return null;
    }
    
    @RequestMapping("/login.htm")
    public ModelAndView login(HttpServletRequest hsr, HttpServletResponse hsr1) throws Exception {
            HttpSession session = hsr.getSession();
            User user = new User();//cambiar
            int scgrpid = 0;
            boolean result = false;
            LoginVerification login = new LoginVerification();
            ModelAndView mv = new ModelAndView("redirect:/menu/start.htm?folder=null");
            
//            setTipo(user);//borrar
//            session.setAttribute("user", user); //borrar
            String txtusuario = hsr.getParameter("txtusuario");
            if(txtusuario==null){
               return new ModelAndView("userform");
            }else{
               user = login.consultUserDB(hsr.getParameter("txtusuario"), hsr.getParameter("txtpassword"));
               // if the username or password incorrect
               if(user.getId()==0){
                    mv = new ModelAndView("userform");
                    String message = "Username or password incorrect";
                    mv.addObject("message", message);
                    return mv;
                }
                //if the user is not part of the group
                else{
                    scgrpid=login.getSecurityGroupID("Communications APP");
                    result = login.fromGroup(scgrpid, user.getId());
                    String consulta = "select * from pwb.DesignConfig";
                    DriverManagerDataSource dataSource2 = (DriverManagerDataSource)this.getBean("dataSourceAH",hsr.getServletContext());
                    Connection co = dataSource2.getConnection();
                    Statement st3 = co.createStatement();
                    ResultSet rs = st3.executeQuery(consulta);
                    String LeftMenuHeaderColor = "";
                    String leftmenubackground = "";
                    String oddrowbackground = "";
                    while(rs.next()){
                        LeftMenuHeaderColor = rs.getString("LeftMenuHeaderColor");
                        leftmenubackground = rs.getString("leftmenubackground");
                        oddrowbackground = rs.getString("oddrowbackground");
                    }
                    String estilo = "#infousuario{background-image: url(https://ca-pan.client.renweb.com/pw/design/ca-pan/header%20color960.png);background-repeat: repeat-y; background-color:white;}"+
                        "#table_folders>tbody>tr:nth-child(odd)>td," +
                        "#table_folders>tbody>tr:nth-child(odd)>th {" +
                        "background-color: white;" +
                        "}" +
                        "#table_folders>tbody>tr:nth-child(even)>td," +
                        "#table_folders>tbody>tr:nth-child(even)>th {" +
                        "background-color: #"+oddrowbackground+";" +
                        "}" +
                        "#table_folders>thead>tr>th {" +
                        "background-color: #"+leftmenubackground+";" +
                        "}"+
                        "#table_id>tbody>tr:nth-child(odd)>td," +
                        "#table_id>tbody>tr:nth-child(odd)>th {" +
                        "background-color: white;" +
                        "}" +
                        "#table_id>tbody>tr:nth-child(even)>td," +
                        "#table_id>tbody>tr:nth-child(even)>th {" +
                        "background-color: #"+oddrowbackground+";" +
                        "}" +
                        "#table_id>thead>tr>th {" +
                        "background-color: #"+leftmenubackground+";" +
                        "}"+
                        ".form-control{background-color:#"+leftmenubackground+"}"+ 
                        "#tabla_carpetas{background-color:#"+leftmenubackground+"}";
                    session.setAttribute("estilo", estilo);
                    if (result == true){
//                       user.setId(10393);//padre
//                       user.setId(10332);//profe
                        setTipo(user);
                        session.setAttribute("user", user);
                        return mv;
                    }
                    else{
                        mv = new ModelAndView("userform");
                        String message = "Username or Password incorrect";
                        mv.addObject("message", message);
                        return mv;
                    }
                }
             }
        //return mv;
    }

        //user.setId(10333);
        //user.setId(10366);

    public void setTipo(User user) {
        boolean padre = false, profesor = false;
        try {
            String consulta = "SELECT count(*) AS cuenta FROM Staff where Faculty = 1 and StaffID =" + user.getId();
            ResultSet rs = st2.executeQuery(consulta);
            if (rs.next()) {
                profesor = rs.getInt("cuenta") > 0;
            }
            consulta = "SELECT count(*) AS cuenta FROM Parent_Student where ParentID =" + user.getId();
            ResultSet rs2 = st2.executeQuery(consulta);
            if (rs2.next()) {
                padre = rs2.getInt("cuenta") > 0;
            }
        } catch (SQLException ex) {
            System.out.println("error");
        }
        if (padre && profesor) {
            user.setType(0);
        } else if (padre) {
            user.setType(1);
        } else if(profesor){
            user.setType(2);
        } else {
            user.setType(3);
        }
    }

}



