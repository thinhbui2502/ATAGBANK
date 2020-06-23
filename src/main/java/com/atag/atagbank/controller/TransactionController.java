package com.atag.atagbank.controller;

import com.atag.atagbank.model.Account;
import com.atag.atagbank.model.MyUser;
import com.atag.atagbank.model.Transaction;
import com.atag.atagbank.service.account.IAccountService;
import com.atag.atagbank.service.transaction.ITransactionService;
import com.atag.atagbank.service.user.MyUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    ITransactionService iTransactionService;
    @Autowired
    IAccountService iAccountService;
    @Autowired
    MyUserService myUserService;

    @ModelAttribute("accounts")
    public Iterable<Account> accounts() {
        return iAccountService.findAll();
    }

    @GetMapping("/listing")
    ModelAndView getAllTransaction(@PageableDefault(sort = "time", direction = Sort.Direction.DESC) Pageable pageable, HttpSession session) {
        Page<Transaction> transactions;
        ModelAndView modelAndView = new ModelAndView("transaction/listing");
        String currentUserName = (String) session.getAttribute("currentUserName");
        MyUser currentUser = myUserService.findByName(currentUserName);
        transactions = iTransactionService.findAllByAccount(currentUser.getAccount(), pageable);
        modelAndView.addObject("transactions", transactions);
        return modelAndView;
    }

    @GetMapping("/create")
    ModelAndView showCreateTransactionForm() {
        ModelAndView modelAndView = new ModelAndView("transaction/create");
        modelAndView.addObject("transaction", new Transaction());
        return modelAndView;
    }

    @PostMapping("/create")
    ModelAndView createTransaction(@ModelAttribute("transaction") Transaction transaction, HttpSession session) {
        transaction.setType("CREDIT");
        transaction.setTime(new Timestamp(System.currentTimeMillis()));
        iTransactionService.save(transaction);

        String senderName = (String) session.getAttribute("currentUserName");
        MyUser senderAccount = myUserService.findByName(senderName);
        Transaction syncTransaction = new Transaction();
        syncTransaction.setTime(new Timestamp(System.currentTimeMillis()));
        syncTransaction.setAmount(transaction.getAmount());
        syncTransaction.setType("DEBIT");
        syncTransaction.setAccount(senderAccount.getAccount());
        iTransactionService.save(syncTransaction);

        iAccountService.addMoneyToAccount(transaction.getAmount(), transaction.getAccount().getId());
        iAccountService.minusMoneyFromAccount(transaction.getAmount(), senderAccount.getAccount().getId());

        ModelAndView modelAndView = new ModelAndView("transaction/create");
        modelAndView.addObject("transaction", new Transaction());
        modelAndView.addObject("message", "Send successfully");
        return modelAndView;
    }
}
