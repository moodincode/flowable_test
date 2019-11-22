package com.flowable.demo.listener.holidayRequest;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * @program: flowable_test
 * @description:
 * @author: moodincode
 * @create: 2019/9/23
 **/
public class SendRejectionMail implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        System.out.println("Calling the SendRejectionMail "
                + execution.getVariable("employee"));
    }
}
