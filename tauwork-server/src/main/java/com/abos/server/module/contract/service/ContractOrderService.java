package com.abos.server.module.contract.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.contract.entity.ContractOrder;
import com.abos.server.module.contract.mapper.ContractOrderMapper;
import com.abos.server.module.customer.entity.Customer;
import com.abos.server.module.customer.mapper.CustomerMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContractOrderService {

    private final ContractOrderMapper contractMapper;
    private final CustomerMapper customerMapper;

    public List<ContractOrder> list() {
        List<ContractOrder> rows = contractMapper.selectList(Wrappers.<ContractOrder>lambdaQuery()
                .orderByDesc(ContractOrder::getCreateTime));
        fillCustomer(rows);
        return rows;
    }

    @Transactional
    public ContractOrder create(ContractOrder dto) {
        ContractOrder row = new ContractOrder();
        copy(row, dto);
        contractMapper.insert(row);
        fillCustomer(List.of(row));
        return row;
    }

    @Transactional
    public ContractOrder update(ContractOrder dto) {
        ContractOrder row = must(dto.getId());
        copy(row, dto);
        contractMapper.updateById(row);
        fillCustomer(List.of(row));
        return row;
    }

    @Transactional
    public void delete(Long id) {
        must(id);
        contractMapper.deleteById(id);
    }

    private ContractOrder must(Long id) {
        ContractOrder row = id == null ? null : contractMapper.selectById(id);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "合同不存在");
        }
        return row;
    }

    private void copy(ContractOrder row, ContractOrder dto) {
        if (dto.getCustomerId() == null || customerMapper.selectById(dto.getCustomerId()) == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请选择客户");
        }
        if (dto.getAmount() == null || dto.getAmount().signum() < 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请填写合同金额");
        }
        row.setCustomerId(dto.getCustomerId());
        row.setAmount(dto.getAmount().setScale(2, RoundingMode.HALF_UP));
        row.setFileUrl(trim(dto.getFileUrl()));
        row.setFileName(trim(dto.getFileName()));
        row.setRemark(trim(dto.getRemark()));
    }

    private void fillCustomer(List<ContractOrder> rows) {
        if (rows.isEmpty()) {
            return;
        }
        List<Long> ids = rows.stream().map(ContractOrder::getCustomerId).distinct().toList();
        Map<Long, Customer> customers = new HashMap<>();
        customerMapper.selectBatchIds(ids).forEach(customer -> customers.put(customer.getId(), customer));
        rows.forEach(row -> {
            Customer customer = customers.get(row.getCustomerId());
            if (customer != null) {
                row.setCustomerName(StringUtils.hasText(customer.getName()) ? customer.getName() : "未命名");
                row.setCustomerPhone(customer.getPhone());
            }
        });
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
