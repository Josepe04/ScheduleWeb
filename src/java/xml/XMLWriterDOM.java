  /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xml;

/**
 *
 * @author Chema
 */
import java.io.File;
import java.util.ArrayList;
 
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import model.Student;
import model.Teacher;
import model.Tupla;
 
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
 
 
public class XMLWriterDOM {
 
    public static void xmlCreate(ArrayList<Teacher> teachers ,ArrayList<Student> st) {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();
            //add elements to Document
            Element rootElement =
                doc.createElement("Schedule");
            //append root element to document
            doc.appendChild(rootElement);
            
            Element teachersxml =
                doc.createElement("Teachers");
            //append root element to document
            rootElement.appendChild(teachersxml);
            
            //append first child element to root element
            for(Teacher t:teachers)
                for(Integer i:t.getPrepsComplete())
                    teachersxml.appendChild(getTeacher(doc,t.getIdTeacher()+"",i+"1"));
 
            Element timetablesxml =
                doc.createElement("Timetables");
            rootElement.appendChild(timetablesxml);
            
            for(Teacher t:teachers)
                for(Integer i:t.getPrepsComplete())
                    for(Tupla t2: t.getPosiciones(i))
                        timetablesxml.appendChild(getTimetableLine(doc,t.getIdTeacher()+"",i+"1",t2.x.toString(),t2.y.toString()));
            
            
            //for output to file, console
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            //for pretty print
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
 
            //write to console or file
            StreamResult console = new StreamResult(System.out);
            StreamResult file = new StreamResult(new File("/Users/Norhan/Documents/emps.xml"));
 
            //write data
            transformer.transform(source, console);
            transformer.transform(source, file);
            System.out.println("DONE");
 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
 
    private static Node getTeacher(Document doc, String id, String clase) {
        Element employee = doc.createElement("Teacher");
 
        //set id attribute
        employee.setAttribute("id", id);
 
        //create name element
        employee.appendChild(getEmployeeElements(doc, employee, "clase", clase));
 
        return employee;
    }
    
    private static Node getTimetableLine(Document doc, String id, String clase,
            String day, String begin) {
        Element employee = doc.createElement("timeLine");
 
        //set id attribute
        employee.setAttribute("id", id);
 
        //create name element
        employee.appendChild(getEmployeeElements(doc, employee, "clase", clase));
        employee.appendChild(getEmployeeElements(doc, employee, "day", day));
        employee.appendChild(getEmployeeElements(doc, employee, "begin", begin));
 
        return employee;
    }
 
 
    //utility method to create text node
    private static Node getEmployeeElements(Document doc, Element element, String name, String value) {
        Element node = doc.createElement(name);
        node.appendChild(doc.createTextNode(value));
        return node;
    }
 
}
