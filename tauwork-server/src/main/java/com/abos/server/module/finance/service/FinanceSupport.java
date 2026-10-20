package com.abos.server.module.finance.service;

import com.abos.server.module.customer.entity.Customer;
import com.abos.server.module.customer.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
class FinanceSupport {

    private final CustomerMapper customerMapper;

    <T> void fillCustomer(List<T> rows, Function<T, Long> customerId,
                          BiConsumer<T, String> name, BiConsumer<T, String> phone) {
        if (rows.isEmpty()) {
            return;
        }
        List<Long> ids = rows.stream().map(customerId).filter(Objects::nonNull).distinct().toList();
        Map<Long, Customer> customers = new HashMap<>();
        if (!ids.isEmpty()) {
            customerMapper.selectBatchIds(ids).forEach(customer -> customers.put(customer.getId(), customer));
        }
        rows.forEach(row -> {
            Customer customer = customers.get(customerId.apply(row));
            if (customer != null) {
                name.accept(row, StringUtils.hasText(customer.getName()) ? customer.getName() : "未命名");
                phone.accept(row, customer.getPhone());
            }
        });
    }
}
