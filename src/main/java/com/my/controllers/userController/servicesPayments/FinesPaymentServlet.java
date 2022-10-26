package com.my.controllers.userController.servicesPayments;

import com.my.dto.UserDTO;
import com.my.service.AccountService;
import com.my.service.CardService;
import com.my.service.ReceiptService;
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

@WebServlet(USER_FINES_PAYMENT_PATH)
public class FinesPaymentServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        Validation validation = (Validation) session.getAttribute("valid");
        session.removeAttribute("valid");
        req.setAttribute("valid", validation);
        if (Objects.nonNull(session.getAttribute("notEnoughMoney"))) {
            session.removeAttribute("notEnoughMoney");
            req.setAttribute("notEnoughMoney", "Недостатньо грошей для операції");
        }
        session.setAttribute("purposeId", 5);
        req.getRequestDispatcher("/views/jsp/options/finesPayment.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        Validation validation = new Validation();
        UserDTO user = (UserDTO) session.getAttribute("user");
        String firstName = req.getParameter("firstName");
        String lastName = req.getParameter("lastName");
        String patronymic = req.getParameter("patronymic");
        String fineNumber = req.getParameter("number");
        String amountStr = req.getParameter("amount");
        boolean isValid = validation.finesPaymentValidation(firstName, patronymic, lastName,fineNumber,amountStr);
        if (!isValid) {
            session.setAttribute("valid", validation);
            resp.sendRedirect(MAIN_SERVLET_PATH + USER_FINES_PAYMENT_PATH);
            return;
        }
        int purposeId = Integer.parseInt(session.getAttribute("purposeId").toString());
        double amount = Double.parseDouble(amountStr);
        int accountId = Integer.parseInt(req.getParameter("accountId"));
        int cardId = AccountService.getCardId(accountId);
        double oldAmount = CardService.getAmount(cardId);
        double newAmount = oldAmount - amount;
        if (newAmount < 0) {
            session.setAttribute("notEnoughMoney", "msg");
            resp.sendRedirect(MAIN_SERVLET_PATH + USER_FINES_PAYMENT_PATH);
            return;
        }
        int serviceId = ReceiptService.createNewEntryInFinesService(firstName, lastName, patronymic, fineNumber);
        ReceiptService.createEntryInReceipt(accountId, purposeId, amount, serviceId, user.getId());
        CardService.updateAmount(newAmount, cardId);
        user.setPaymentsCount(user.getPaymentsCount() + 1);
        resp.sendRedirect(MAIN_SERVLET_PATH + USER_RECEIPTS_PATH);
    }
}
