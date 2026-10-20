package com.abos.server.module.workorder.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.customer.entity.Customer;
import com.abos.server.module.customer.mapper.CustomerMapper;
import com.abos.server.module.sysuser.mapper.SysUserMapper;
import com.abos.server.module.sysuser.service.SysUserService;
import com.abos.server.module.workorder.entity.WorkOrder;
import com.abos.server.module.workorder.entity.WorkOrderLog;
import com.abos.server.module.workorder.mapper.WorkOrderLogMapper;
import com.abos.server.module.workorder.mapper.WorkOrderMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class WorkOrderService {

    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderLogMapper logMapper;
    private final CustomerMapper customerMapper;
    private final SysUserMapper sysUserMapper;
    private final SysUserService staff;

    public List<WorkOrder> list(String status) {
        List<WorkOrder> rows = workOrderMapper.selectList(Wrappers.<WorkOrder>lambdaQuery()
                .eq(StringUtils.hasText(status), WorkOrder::getStatus, status)
                .orderByDesc(WorkOrder::getCreateTime));
        fill(rows);
        return rows;
    }

    public WorkOrder detail(Long id) {
        WorkOrder row = must(id);
        fill(List.of(row));
        List<WorkOrderLog> logs = logMapper.selectList(Wrappers.<WorkOrderLog>lambdaQuery()
                .eq(WorkOrderLog::getWorkOrderId, id)
                .orderByAsc(WorkOrderLog::getCreateTime));
        var names = staff.names(logs.stream().map(WorkOrderLog::getCreateBy).filter(Objects::nonNull).toList());
        logs.forEach(log -> log.setOperatorName(names.get(log.getCreateBy())));
        row.setLogs(logs);
        return row;
    }

    @Transactional
    public WorkOrder create(WorkOrder dto) {
        WorkOrder row = new WorkOrder();
        copy(row, dto);
        row.setStatus(WorkOrder.OPEN);
        workOrderMapper.insert(row);
        log(row.getId(), WorkOrderLog.CREATE, trim(dto.getContent()));
        return detail(row.getId());
    }

    @Transactional
    public WorkOrder update(WorkOrder dto) {
        WorkOrder row = must(dto.getId());
        requireOpen(row);
        copy(row, dto);
        workOrderMapper.update(null, Wrappers.<WorkOrder>lambdaUpdate()
                .set(WorkOrder::getTitle, row.getTitle())
                .set(WorkOrder::getCustomerId, row.getCustomerId())
                .set(WorkOrder::getAssigneeId, row.getAssigneeId())
                .set(WorkOrder::getContent, row.getContent())
                .eq(WorkOrder::getId, row.getId()));
        return detail(row.getId());
    }

    @Transactional
    public WorkOrder start(Long id) {
        WorkOrder row = must(id);
        if (!WorkOrder.OPEN.equals(row.getStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "只有待处理工单可以开始处理");
        }
        row.setStatus(WorkOrder.PROCESSING);
        workOrderMapper.updateById(row);
        log(id, WorkOrderLog.START, null);
        return detail(id);
    }

    @Transactional
    public WorkOrder finish(Long id, String result) {
        WorkOrder row = must(id);
        if (!WorkOrder.PROCESSING.equals(row.getStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请先开始处理，再完成工单");
        }
        if (!StringUtils.hasText(result)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请填写处理结果");
        }
        row.setStatus(WorkOrder.DONE);
        row.setResult(result.trim());
        workOrderMapper.updateById(row);
        log(id, WorkOrderLog.FINISH, result.trim());
        return detail(id);
    }

    @Transactional
    public WorkOrder cancel(Long id, String reason) {
        WorkOrder row = must(id);
        if (WorkOrder.DONE.equals(row.getStatus()) || WorkOrder.CANCELED.equals(row.getStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "这份工单已经结束");
        }
        row.setStatus(WorkOrder.CANCELED);
        workOrderMapper.updateById(row);
        log(id, WorkOrderLog.CANCEL, trim(reason));
        return detail(id);
    }

    @Transactional
    public WorkOrder note(Long id, String content) {
        must(id);
        if (!StringUtils.hasText(content)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请填写处理说明");
        }
        log(id, WorkOrderLog.NOTE, content.trim());
        return detail(id);
    }

    @Transactional
    public void delete(Long id) {
        WorkOrder row = must(id);
        if (WorkOrder.PROCESSING.equals(row.getStatus()) || WorkOrder.DONE.equals(row.getStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "处理中或已完成的工单请取消，不要直接删除");
        }
        workOrderMapper.deleteById(id);
    }

    private void copy(WorkOrder row, WorkOrder dto) {
        if (!StringUtils.hasText(dto.getTitle())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请填写工单标题");
        }
        if (dto.getCustomerId() != null && customerMapper.selectById(dto.getCustomerId()) == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "客户不存在");
        }
        if (dto.getAssigneeId() != null && sysUserMapper.selectById(dto.getAssigneeId()) == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "处理人不存在");
        }
        row.setTitle(dto.getTitle().trim());
        row.setCustomerId(dto.getCustomerId());
        row.setAssigneeId(dto.getAssigneeId());
        row.setContent(trim(dto.getContent()));
    }

    private void requireOpen(WorkOrder row) {
        if (WorkOrder.DONE.equals(row.getStatus()) || WorkOrder.CANCELED.equals(row.getStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "已结束的工单不能再改");
        }
    }

    private void log(Long workOrderId, String action, String content) {
        WorkOrderLog row = new WorkOrderLog();
        row.setWorkOrderId(workOrderId);
        row.setAction(action);
        row.setContent(content);
        logMapper.insert(row);
    }

    private WorkOrder must(Long id) {
        WorkOrder row = id == null ? null : workOrderMapper.selectById(id);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "工单不存在");
        }
        return row;
    }

    private void fill(List<WorkOrder> rows) {
        if (rows.isEmpty()) {
            return;
        }
        List<Long> customerIds = rows.stream().map(WorkOrder::getCustomerId).filter(Objects::nonNull).distinct().toList();
        Map<Long, Customer> customers = new HashMap<>();
        if (!customerIds.isEmpty()) {
            customerMapper.selectBatchIds(customerIds).forEach(customer -> customers.put(customer.getId(), customer));
        }
        var names = staff.names(rows.stream().map(WorkOrder::getAssigneeId).filter(Objects::nonNull).distinct().toList());
        rows.forEach(row -> {
            Customer customer = customers.get(row.getCustomerId());
            if (customer != null) {
                row.setCustomerName(StringUtils.hasText(customer.getName()) ? customer.getName() : "未命名");
                row.setCustomerPhone(customer.getPhone());
            }
            row.setAssigneeName(names.get(row.getAssigneeId()));
        });
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
