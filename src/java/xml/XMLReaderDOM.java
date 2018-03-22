/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import model.Student;
import model.Teacher;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

/**
 *
 * @author Chema
 */
public class XMLReaderDOM {
    public static void xmlRead(String filepath) {
        try {
            File fXmlFile = new File(filepath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

            ArrayList<Teacher> listaTeachers = new ArrayList();
            //LECTURA TEACHERS
            NodeList nList = doc.getElementsByTagName("Teacher");
            System.out.println("----------------------------");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Teacher t = new Teacher();
                Node nNode = nList.item(temp);

                System.out.println("\nCurrent Element :" + nNode.getNodeName());

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    t.setIdTeacher(Integer.parseInt(eElement.getAttribute("id")));
                    t.setMaxBxD(Integer.parseInt(eElement.getElementsByTagName("maxbxd").item(0).getTextContent()));
                    t.setMaxSections(Integer.parseInt(eElement.getElementsByTagName("secciones").item(0).getTextContent()));
                    t.setPreps(Integer.parseInt(eElement.getElementsByTagName("maxpreps").item(0).getTextContent()));
                    String course = eElement.getElementsByTagName("course").item(0).getTextContent();
                    //String[] courses = course.substring(1,course.length()-1).split(",");
                    System.out.println("Cursos : " + course);
                    listaTeachers.add(t);
                }
                
            }
            
            //LECTURA STUDENTS
            HashMap<Integer,Student> listaStudents = new HashMap();
            nList = doc.getElementsByTagName("Student");
            System.out.println("----------------------------");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                
                System.out.println("\nCurrent Element :" + nNode.getNodeName());

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    int id = Integer.parseInt(eElement.getAttribute("id"));
                    String name = eElement.getAttribute("name");
                    String genero = eElement.getAttribute("genero");
                    System.out.println("Staff id : " + eElement.getAttribute("id"));
                    System.out.println("Cursos : " + eElement.getElementsByTagName("course").item(0).getTextContent());
                    listaStudents.put(id, new Student(id,name,genero));
                    //SET CURSOS ASIGNADOS
                    String cursos = eElement.getElementsByTagName("course").item(0).getTextContent();
                    cursos = cursos.substring(1, cursos.length()-1);
                    String[] courses = cursos.split(",");
                    ArrayList<Integer> idasignado = new ArrayList();
                    for(String s : courses)
                        listaStudents.get(id).addAsignado(Integer.parseInt(s));
                    //SET CURSOS NO ASIGNADO
                    cursos = eElement.getElementsByTagName("courseno").item(0).getTextContent();
                    cursos = cursos.substring(1, cursos.length()-1);
                    courses = cursos.split(",");
                    for(String s : courses)
                        listaStudents.get(id).addNoAsignado(Integer.parseInt(s));
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 
}
