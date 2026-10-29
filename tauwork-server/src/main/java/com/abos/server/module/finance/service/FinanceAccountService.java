package com.abos.server.module.finance.service;

import com.abos.server.module.finance.entity.FinanceAccount;
import com.abos.server.module.finance.mapper.FinanceAccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FinanceAccountService {

    private final FinanceAccountMapper financeAccountMapper;

    public FinanceAccount get() {
        FinanceAccount row = financeAccountMapper.selectById(FinanceAccount.SINGLE_ID);
        if (row != null) {
            return row;
        }
        FinanceAccount created = new FinanceAccount();
        created.setId(FinanceAccount.SINGLE_ID);
        created.setAccountName("九千善");
        created.setBankName("");
        created.setAccountNo("");
        created.setBranch("");
        financeAccountMapper.insert(created);
        return financeAccountMapper.selectById(FinanceAccount.SINGLE_ID);
    }

    @Transactional
    public FinanceAccount save(FinanceAccount input) {
        FinanceAccount row = get();
        row.setAccountName(text(input.getAccountName()));
        row.setBankName(text(input.getBankName()));
        row.setAccountNo(text(input.getAccountNo()));
        row.setBranch(text(input.getBranch()));
        financeAccountMapper.updateById(row);
        return get();
    }

    public Map<String, String> view() {
        FinanceAccount row = get();
        Map<String, String> account = new LinkedHashMap<>();
        account.put("accountName", text(row.getAccountName()));
        account.put("bankName", text(row.getBankName()));
        account.put("accountNo", text(row.getAccountNo()));
        account.put("branch", text(row.getBranch()));
        return account;
    }

    private String text(String value) {
        return value == null ? "" : value.trim();
    }
}
