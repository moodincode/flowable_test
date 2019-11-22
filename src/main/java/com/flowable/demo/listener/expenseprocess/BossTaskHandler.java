package com.flowable.demo.listener.expenseprocess;

import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

/**
 * @program: flowable_test
 * @description: 任务监听器,用于审批流程通知到相关流程的审核人
 * @author: moodincode
 * @create: 2019/9/23
 **/
public class BossTaskHandler implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        delegateTask.setAssignee("老板");
        System.out.println("老板审批通知");
    }
}
