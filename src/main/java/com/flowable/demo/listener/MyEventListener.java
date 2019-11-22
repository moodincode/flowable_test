package com.flowable.demo.listener;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.springframework.stereotype.Component;

/**
 * @program: flowable_test
 * @description: 自定义任务监听器,监听任务的执行情况
 * @author: moodincode
 * @create: 2019/9/23
 **/
@Component
public class MyEventListener implements FlowableEventListener {

    @Override
    public void onEvent(FlowableEvent event) {
        switch ((FlowableEngineEventType)event.getType()) {

            case JOB_EXECUTION_SUCCESS:
                System.out.println("A job well done!");
                break;

            case JOB_EXECUTION_FAILURE:
                System.out.println("A job has failed...");
                break;

            default:
                System.out.println("Event received: " + event.getType());
        }
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        return false;
    }

    @Override
    public String getOnTransaction() {
        return null;
    }

    @Override
    public boolean isFailOnException() {
        // onEvent方法中的逻辑并不重要，可以忽略日志失败异常……

        return false;
    }
}