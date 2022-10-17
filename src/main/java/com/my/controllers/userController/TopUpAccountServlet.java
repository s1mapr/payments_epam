package com.my.controllers.userController;

import com.my.dao.AccountDAO;
import com.my.dao.CardDAO;
import com.my.utils.Validation;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Objects;

import static com.my.utils.HttpConstants.*;

@WebServlet(USER_TOP_UP_PATH)
public class TopUpAccountServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("TopUpAccountServlet#doGet");
        HttpSession session = req.getSession();
        Validation validation = (Validation) session.getAttribute("valid");
        session.removeAttribute("valid");
        req.setAttribute("valid", validation);
        if (Objects.isNull(session.getAttribute("accountId"))) {
            String id = req.getParameter("id");
            session.setAttribute("accountId", id);
        }
        req.getRequestDispatcher("/views/jsp/topUpAccount.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("TopUpAccountServlet#doPost");
        HttpSession session = req.getSession();
        String id = session.getAttribute("accountId").toString();
        Validation validation = new Validation();
        boolean isValid = validation.topUpValidation(
                req.getParameter("cardNumber"),
                req.getParameter("expiryDate"),
                req.getParameter("cvv"),
                req.getParameter("amount"));
        if (!isValid) {
            session.setAttribute("valid", validation);
            resp.sendRedirect(MAIN_SERVLET_PATH + USER_TOP_UP_PATH);
            return;
        }
        int cardId = AccountDAO.getCardId(Integer.parseInt(id));
        double amount = Double.parseDouble(req.getParameter("amount"));
        double currentAmount = CardDAO.getAmount(cardId);
        double newAmount = currentAmount + amount;
        CardDAO.updateAmount(newAmount, cardId);
        session.removeAttribute("accountId");
        resp.sendRedirect(MAIN_SERVLET_PATH + USER_PROFILE_PATH);
    }
}