package com.ebdf.controller;
import java.io.IOException;import java.util.*;import javax.servlet.annotation.WebServlet;import javax.servlet.http.*;import com.google.gson.Gson;
@WebServlet("/api/session") public class SessionServlet extends HttpServlet {
 private final Gson gson=new Gson();
 protected void doGet(HttpServletRequest q,HttpServletResponse p)throws IOException{p.setContentType("application/json");p.setCharacterEncoding("UTF-8");HttpSession s=q.getSession(false);if(s==null||s.getAttribute("userId")==null){p.setStatus(401);p.getWriter().write("{\"authenticated\":false}");return;}Map<String,Object>m=new LinkedHashMap<>();m.put("authenticated",true);m.put("userId",s.getAttribute("userId"));m.put("role",s.getAttribute("userRole"));m.put("fullName",s.getAttribute("fullName"));m.put("email",s.getAttribute("email"));p.getWriter().write(gson.toJson(m));}
}
