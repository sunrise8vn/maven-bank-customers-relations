package com.cg.controller;

import com.cg.model.dto.*;
import com.cg.model.Customer;
import com.cg.service.customer.ICustomerService;
import com.cg.service.transfer.ITransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private ICustomerService customerService;

    @Autowired
    private ITransferService transferService;


    @GetMapping
    public ModelAndView listCustomers() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("/customer/list");
        Iterable<Customer> customers = customerService.findAllByDeletedIsFalse();
        modelAndView.addObject("customers", customers);
        return modelAndView;
    }

    @GetMapping("/create")
    public ModelAndView showCreateForm() {
        ModelAndView modelAndView = new ModelAndView("/customer/create");
        modelAndView.addObject("customer", new Customer());
        modelAndView.addObject("success", null);
        return modelAndView;
    }

    @GetMapping("/edit/{id}")
    public ModelAndView showUpdateForm(@PathVariable Long id) {
        Optional<Customer> customer = customerService.findById(id);

        if (customer.isPresent()) {
            ModelAndView modelAndView = new ModelAndView("/customer/edit");
            modelAndView.addObject("customer", customer.get());
            modelAndView.addObject("success", null);
            return modelAndView;
        } else {
            return new ModelAndView("/error.404");
        }
    }

    @GetMapping("/deposit/{id}")
    public ModelAndView showDepositsForm(@PathVariable Long id) {
        ModelAndView modelAndView = new ModelAndView("/customer/deposit");

        Optional<DepositDTO> depositDTO = customerService.findByIdWithDepositDTO(id);

        if (depositDTO.isPresent()) {
            modelAndView.addObject("depositDTO", depositDTO.get());
            modelAndView.addObject("success", null);
            modelAndView.addObject("error", null);
            modelAndView.addObject("script", null);
            return modelAndView;
        } else {
            return new ModelAndView("/error.404");
        }
    }

    @GetMapping("/withdraw/{id}")
    public ModelAndView showWithdrawForm(@PathVariable Long id) {
        ModelAndView modelAndView = new ModelAndView("/customer/withdraw");

        Optional<WithdrawDTO> withdrawDTO = customerService.findByIdWithWithdrawDTO(id);

        if (withdrawDTO.isPresent()) {
            modelAndView.addObject("withdrawDTO", withdrawDTO.get());
            modelAndView.addObject("success", null);
            modelAndView.addObject("error", null);
            modelAndView.addObject("script", null);
            return modelAndView;
        } else {
            return new ModelAndView("/error.404");
        }
    }


    @GetMapping("/transfer/{id}")
    public ModelAndView showTransferForm(@PathVariable Long id) {
        ModelAndView modelAndView = new ModelAndView("/customer/transfer");

        Optional<TransferDTO> transferDTO = transferService.findByIdWithTransferDTO(id);

//        Iterable<RecipientDTO> recipientDTOS = customerService.findAllRecipientDTOByIdWithOutSender(id);
        Iterable<RecipientDTO> recipientDTOS = customerService.findAllRecipientDTOByIdWithOutSenderAndDeletedIsFalse(id);

        if (transferDTO.isPresent()) {
            modelAndView.addObject("transferDTO", transferDTO.get());
            modelAndView.addObject("recipientDTOS", recipientDTOS);
            modelAndView.addObject("success", null);
            return modelAndView;
        } else {
            return new ModelAndView("/error.404");
        }
    }

    @GetMapping("/suspended/{id}")
    public ModelAndView showSuspensionForm(@PathVariable Long id) {
        ModelAndView modelAndView = new ModelAndView("/customer/suspended");

        Optional<Customer> customer = customerService.findById(id);

        if (customer.isPresent()) {
            modelAndView.addObject("customer", customer.get());
            modelAndView.addObject("success", null);
            return modelAndView;
        } else {
            return new ModelAndView("/error.404");
        }
    }

    @PostMapping(value = "/create", produces = "application/json;charset=UTF-8")
    public ModelAndView save(@Validated @ModelAttribute("customer") Customer customer, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("/customer/create");

        if (bindingResult.hasFieldErrors()) {
            modelAndView.addObject("script", true);
        }
        else {
            if (customerService.existsByEmail(customer.getEmail())) {
                modelAndView.addObject("error", "Email already exists");
            }
            else {
                try {
                    customer.setBalance(BigDecimal.valueOf(0));
                    customerService.save(customer);

                    modelAndView.addObject("customer", new Customer());
                    modelAndView.addObject("success", "Successfully added new customer");
                } catch (Exception e) {
                    e.printStackTrace();
                    modelAndView.addObject("error", "Invalid data, please contact system administrator");
                }
            }
        }

        return modelAndView;
    }

    @PutMapping("/edit/{id}")
    public ModelAndView updateCustomer(@Validated @ModelAttribute("customer") Customer customer, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView("/customer/edit");

        if (bindingResult.hasFieldErrors()){
            modelAndView.addObject("script", true);
        }
        else {
            try {
                customerService.save(customer);

                modelAndView.addObject("customer", customer);
                modelAndView.addObject("success", "Customer updated successfully");
            } catch (Exception e) {
                e.printStackTrace();
                modelAndView.addObject("error", "Invalid data, please contact system administrator");
            }
        }

        return modelAndView;
    }

    @PostMapping("/deposit/{customerId}")
    public ModelAndView doDeposits(@PathVariable Long customerId, @Validated @ModelAttribute DepositDTO depositDTO, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("/customer/deposit");

        new DepositDTO().validate(depositDTO, bindingResult);

        if (bindingResult.hasFieldErrors()){
            modelAndView.addObject("script", true);
        }
        else {
            try {
                customerService.doDeposit(customerId, depositDTO);

                modelAndView.addObject("depositDTO", customerService.findByIdWithDepositDTO(customerId).get());
                modelAndView.addObject("success", "Successful deposit transaction");
            } catch (DataIntegrityViolationException e) {
                e.printStackTrace();
                modelAndView.addObject("error", "Invalid data, please contact system administrator");
            }
        }

        return modelAndView;
    }

    @PostMapping("/withdraw/{customerId}")
    public ModelAndView doWithdraw(@PathVariable Long customerId, @Validated @ModelAttribute WithdrawDTO withdrawDTO, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("/customer/withdraw");

        new WithdrawDTO().validate(withdrawDTO, bindingResult);

        if (bindingResult.hasFieldErrors()){
            modelAndView.addObject("script", true);
        }
        else {
            Optional<Customer> customer = customerService.findById(customerId);
            BigDecimal current_balance = customer.get().getBalance();
            BigDecimal transactionAmount = withdrawDTO.getTransactionAmount();

            if (current_balance.compareTo(transactionAmount) >= 0) {
                try {
                    customerService.doWithdraw(customerId, withdrawDTO);

                    modelAndView.addObject("withdrawDTO", customerService.findByIdWithWithdrawDTO(customerId).get());
                    modelAndView.addObject("success", "Successful withdrawal transaction");
                } catch (Exception e) {
                    e.printStackTrace();
                    modelAndView.addObject("error", "Invalid data, please contact system administrator");
                }
            } else {
                modelAndView.addObject("error", "Customer's balance is not enough to make a withdrawal");
            }
        }

        return modelAndView;
    }

    @PostMapping("/transfer/{customerId}")
    public ModelAndView doTransfer(@PathVariable Long customerId, @Validated @ModelAttribute TransferDTO transferDTO, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("/customer/transfer");
        modelAndView.addObject("recipientDTOS", customerService.findAllRecipientDTOByIdWithOutSenderAndDeletedIsFalse(customerId));

        new TransferDTO().validate(transferDTO, bindingResult);

        if (bindingResult.hasFieldErrors()){
            modelAndView.addObject("script", true);
        }
        else {
            Optional<Customer> sender = customerService.findById(customerId);

            BigDecimal sender_balance = sender.get().getBalance();
            BigDecimal transferAmount = transferDTO.getTransferAmount();
            int fees = 10;
            BigDecimal feeAmount = transferAmount.divide(BigDecimal.valueOf(fees));
            BigDecimal transactionAmount = transferAmount.add(feeAmount);

            Optional<Customer> recipient = customerService.findById(transferDTO.getRecipientId());

            if (recipient.isPresent()) {
                if (sender_balance.compareTo(transactionAmount) >= 0) {
                    try {
                        transferDTO.setFees(fees);
                        transferDTO.setFeesAmount(feeAmount);
                        transferDTO.setTransactionAmount(transactionAmount);

                        customerService.doTransfer(transferDTO, sender, recipient);

                        modelAndView.addObject("transferDTO", transferService.findByIdWithTransferDTO(customerId).get());
                        modelAndView.addObject("success", "Successful transfer transaction");
                    } catch (Exception e) {
                        e.printStackTrace();
                        modelAndView.addObject("error", "Invalid data, please contact system administrator");
                    }
                }
                else {
                    modelAndView.addObject("error", "The sender's balance is not enough to make the transfer");
                }
            }
            else {
                modelAndView.addObject("error", "Invalid recipient information");
            }
        }

        return modelAndView;
    }

    @PostMapping("/suspended/{customerId}")
    public ModelAndView suspendedCustomer(@PathVariable Long customerId) {
        ModelAndView modelAndView = new ModelAndView("/customer/suspended");

        Optional<Customer> customer = customerService.findById(customerId);

        if (customer.isPresent()) {
            customer.get().setDeleted(true);
            customerService.save(customer.get());
            modelAndView.addObject("customer", customer.get());
            modelAndView.addObject("success", "Customer suspended successfully");
            return modelAndView;
        } else {
            return new ModelAndView("/error.404");
        }
    }


}
