package com.veor.lab1p.mapper;

import com.veor.lab1p.dto.TransactionDTO;
import com.veor.lab1p.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);
    TransactionDTO toDTO(Transaction transaction);
}