package com.cg.model.dto;

import com.cg.model.Customer;
import com.cg.model.Deposit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositDTO implements Validator {

    private long customerId;
    private String fullName;
    private BigDecimal balance;

    @NotNull(message = "The transaction amount is required")
    @DecimalMin(value = "50", message = "Transaction Amount must be greater than or equal to 50")
    @DecimalMax(value = "10000000000", message = "Transaction Amount must be less than or equal to 10.000.000.000")
    private BigDecimal transactionAmount;

    public DepositDTO(long customerId, String fullName, BigDecimal balance) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.balance = balance;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return DepositDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        DepositDTO depositDTO = (DepositDTO) target;

        BigDecimal transactionAmount = depositDTO.getTransactionAmount();

//        ValidationUtils.rejectIfEmpty(errors, "transactionAmount", "transactionAmount.empty");

        if (transactionAmount != null) {
            if (transactionAmount.toString().length() > 12){
                errors.rejectValue("transactionAmount", "transactionAmount.length");
            }

            if (!transactionAmount.toString().matches("(^$|[0-9]*$)")){
                errors.rejectValue("transactionAmount", "transactionAmount.matches");
            }
        } else {
            errors.rejectValue("transactionAmount", "transactionAmount.null");
        }
    }

    public Deposit toDeposit(Optional<Customer> customer) {
        return new Deposit()
            .setCustomer(customer.get())
            .setTransactionAmount(transactionAmount);
    }

}
