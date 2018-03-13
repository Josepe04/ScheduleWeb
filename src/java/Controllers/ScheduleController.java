/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Algoritmo;
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
        String xs = hsr.getParameter("cols");
        String ys = hsr.getParameter("rows");
        int x = Integer.parseInt(xs);
        int y = Integer.parseInt(ys);
        (new Algoritmo(x,y)).algo(mv);
        return mv;
    }
}
