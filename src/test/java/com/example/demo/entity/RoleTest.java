package com.example.demo.entity;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void hello() {
        Set<Role> set = new HashSet<>();
        set.add(Role.ADMIN);
        set.add(Role.USER);
        set.add(Role.INSTAGRAM);
        String collect = set.stream().map(Enum::name).collect(Collectors.joining(","));
        System.out.println("collect = " + collect);

        String collect2 = set.stream().map(i -> "ROLE_" + i.name()).collect(Collectors.joining(","));
        Set<Role> collect1 = Arrays.stream(collect2.split(",")).map(r -> Role.valueOf(r.substring(5))).collect(Collectors.toSet());
        for (Role role : collect1) {
            System.out.println(role);
        }

    }
}