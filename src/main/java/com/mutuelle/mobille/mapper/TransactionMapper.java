package com.mutuelle.mobille.mapper;


import com.mutuelle.mobille.dto.transaction.TransactionResponseDTO;
import com.mutuelle.mobille.models.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionMapper {

    public static TransactionResponseDTO toResponseDTO(Transaction tx) {
        if (tx == null) {
            return null;
        }

        String memberFullName = null;
        if (tx.getAccountMember() != null && tx.getAccountMember().getMember() != null) {
            memberFullName = tx.getAccountMember().getMember().getFirstname() + " " +
                    tx.getAccountMember().getMember().getLastname();
        }

        List<TransactionResponseDTO> children = tx.getChildren().stream()
                .map(TransactionMapper::toResponseDTO)
                .toList();

        return new TransactionResponseDTO(
                tx.getId(),
                tx.getAmount(),
                tx.getTransactionType(),
                tx.getTransactionDirection(),
                tx.getAccountMember() != null ? tx.getAccountMember().getId() : null,
                memberFullName,
                tx.getSession() != null ? tx.getSession().getId() : null,
                tx.getCreatedAt(),
                tx.getUpdatedAt(),
                children
        );
    }
}