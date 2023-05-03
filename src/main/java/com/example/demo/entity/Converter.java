package com.example.demo.entity;

import jakarta.persistence.AttributeConverter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@jakarta.persistence.Converter(autoApply = true)
public class Converter implements AttributeConverter<List<Role>,String> {

    @Override
    public String convertToDatabaseColumn(List<Role> attribute) {

        return attribute.stream().map(r -> r.name()).collect(Collectors.joining(","));
    }

    @Override
    public List<Role> convertToEntityAttribute(String dbData) {
        return Arrays.stream(dbData.split(",")).map(r -> Role.valueOf(r)).toList();
    }
}
