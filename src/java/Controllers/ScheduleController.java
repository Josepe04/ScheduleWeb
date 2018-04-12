/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Algoritmo;
import model.Consultas;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Chema
 */
@Controller
public class ScheduleController {
    
    @RequestMapping("/schedule/start.htm")
    public ModelAndView scheduleStart(HttpServletRequest hsr, HttpServletResponse hsr1){
        ModelAndView mv = new ModelAndView("index");
        String tempid = hsr.getParameter("tempid");
        String xs = hsr.getParameter("cols");
        String ys = hsr.getParameter("rows");
        int id = Integer.parseInt(hsr.getParameter("id"));
        String yearid = hsr.getParameter("yearid");
        int x = Integer.parseInt(xs);
        int y = Integer.parseInt(ys);
        mv.addObject("hFilas",Consultas.getRowHeader(id, y));
        mv.addObject("hcols",Consultas.getColHeader(id, x));
        Algoritmo algo = new Algoritmo(x,y);
        algo.algo(mv,yearid,tempid);
        String json = algo.teachersJSON();
        return mv;
    }
    
    @RequestMapping("/schedule/teacherMasterSchedule.htm")
    public String masterSchedule(HttpServletRequest hsr, HttpServletResponse hsr1){
        
        return "";
    }
}
