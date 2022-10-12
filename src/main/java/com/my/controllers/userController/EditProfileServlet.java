package com.my.controllers.userController;

import com.my.dao.UserDAO;
import com.my.entities.User;
import com.my.utils.Validation;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/user/editProfile")
public class EditProfileServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("EditProfileServlet#doGet");
        HttpSession session = req.getSession();
        Validation validation = (Validation) session.getAttribute("valid");
        session.removeAttribute("valid");
        req.setAttribute("valid", validation);
        req.getRequestDispatcher("/views/jsp/editProfile.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("EditProfileServlet#doPost");
        HttpSession session = req.getSession();
        User user = (User)session.getAttribute("user");
        int userId = user.getId();
        Validation validation = new Validation();
        boolean isValid = validation.updateProfileValidation(
                req.getParameter("firstName"),
                req.getParameter("lastName"),
                req.getParameter("email"),
                req.getParameter("phoneNumber"));
        if(!isValid){
            session.setAttribute("valid", validation);
            resp.sendRedirect("/epamProject/user/editProfile");
            return;
        }
        String firstName = req.getParameter("firstName");
        String lastName = req.getParameter("lastName");
        String email = req.getParameter("email");
        String phoneNumber = req.getParameter("phoneNumber");
        UserDAO.updateUserData(firstName,lastName,email,phoneNumber,userId);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        session.removeAttribute("user");
        session.setAttribute("user", user);
        resp.sendRedirect("/epamProject/user/profile");
    }
}