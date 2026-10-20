package com.abos.server.module.workorder.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.abos.server.common.result.Result;
import com.abos.server.module.workorder.entity.WorkOrder;
import com.abos.server.module.workorder.service.WorkOrderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/work-order")
@RequiredArgsConstructor
public class WorkOrderAdminController {

    private final WorkOrderService workOrderService;

    @GetMapping("/list")
    public Result<List<WorkOrder>> list(@RequestParam(required = false) String status) {
        return Result.ok(workOrderService.list(status));
    }

    @GetMapping("/{id}")
    public Result<WorkOrder> detail(@PathVariable Long id) {
        return Result.ok(workOrderService.detail(id));
    }

    @PostMapping
    @SaCheckPermission("workorder:manage")
    public Result<WorkOrder> create(@RequestBody WorkOrder workOrder) {
        return Result.ok(workOrderService.create(workOrder));
    }

    @PutMapping
    @SaCheckPermission("workorder:manage")
    public Result<WorkOrder> update(@RequestBody WorkOrder workOrder) {
        return Result.ok(workOrderService.update(workOrder));
    }

    @PostMapping("/{id}/start")
    @SaCheckPermission("workorder:manage")
    public Result<WorkOrder> start(@PathVariable Long id) {
        return Result.ok(workOrderService.start(id));
    }

    @PostMapping("/{id}/finish")
    @SaCheckPermission("workorder:manage")
    public Result<WorkOrder> finish(@PathVariable Long id, @RequestBody NoteBody body) {
        return Result.ok(workOrderService.finish(id, body.getContent()));
    }

    @PostMapping("/{id}/cancel")
    @SaCheckPermission("workorder:manage")
    public Result<WorkOrder> cancel(@PathVariable Long id, @RequestBody NoteBody body) {
        return Result.ok(workOrderService.cancel(id, body.getContent()));
    }

    @PostMapping("/{id}/note")
    @SaCheckPermission("workorder:manage")
    public Result<WorkOrder> note(@PathVariable Long id, @RequestBody NoteBody body) {
        return Result.ok(workOrderService.note(id, body.getContent()));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("workorder:manage")
    public Result<Void> delete(@PathVariable Long id) {
        workOrderService.delete(id);
        return Result.ok();
    }

    @Data
    public static class NoteBody {
        private String content;
    }
}
