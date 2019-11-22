package com.flowable.demo.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowable.demo.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * @program: flowable_test
 * @description:
 * @author: moodincode
 * @create: 2019/9/23
 **/

@RestController
@RequestMapping(value = "expense")
@Slf4j
public class ExpenseController {
    private static ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RepositoryService repositoryService;
    @Resource
    private ProcessEngine processEngine;
    @Autowired
    private HistoryService historyService;

/***************此处为业务代码******************/

    /**
     * 添加报销
     *
     * @param userId    用户Id
     * @param money     报销金额
     * @param descption 描述
     */
    @RequestMapping(value = "add")
    public String addExpense(String userId, Integer money, String descption) {
        //启动流程
        /**'
         * map中的key为该流程的中的条件名称,value为条件值,
         */
        HashMap<String, Object> map = new HashMap<>();
        map.put("taskUser", userId);
        map.put("money", money);
        //参数1,为工作流.bpmn20.xml文件中process的Id值, <process id="Expense" name="ExpenseProcess" isExecutable="true">,map对象的值为流程中用到的变量
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Expense", map);
        return "提交成功.流程Id为：" + processInstance.getId();
    }

    /**
     * 获取审批管理列表
     */
    @RequestMapping(value = "/list")
    public Object list(String userId) throws JsonProcessingException {
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(userId).orderByTaskCreateTime().desc().list();
        List<Map<String, Object>> listMap = new ArrayList<>();

        // 循环
        String[] ps = {"id", "name", "revision", "key", "originalPersistentState", "resourceName", "deploymentId", "assignee",
                "suspensionState"};
        for (Task pd : tasks) {
            Map<String, Object> map = JsonUtil.obj2map(pd, ps);
            listMap.add(map);
        }
        return listMap;

    }

    /**
     * 批准
     *
     * @param taskId 任务ID
     */
    @RequestMapping(value = "apply")
    public String apply(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new RuntimeException("流程不存在");
        }
        //通过审核
        HashMap<String, Object> map = new HashMap<>();
        map.put("outcome", "通过");
        taskService.complete(taskId, map);
        return "processed ok!";

    }

    /**
     * 拒绝
     */
    @RequestMapping(value = "reject")
    public String reject(String taskId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("outcome", "驳回");
        taskService.complete(taskId, map);
        return "reject";
    }

    /**
     * 生成流程图
     *
     * @param processId 任务ID
     */
    @RequestMapping(value = "processDiagram")
    public void genProcessDiagram(HttpServletResponse httpServletResponse, String processId) throws Exception {
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();

        //流程走完的不显示图
        if (pi == null) {
            return;
        }
        Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        //使用流程实例ID，查询正在执行的执行对象表，返回流程实例对象
        String InstanceId = task.getProcessInstanceId();
        List<Execution> executions = runtimeService
                .createExecutionQuery()
                .processInstanceId(InstanceId)
                .list();

        //得到正在执行的Activity的Id
        List<String> activityIds = new ArrayList<>();
        List<String> flows = new ArrayList<>();
        for (Execution exe : executions) {
            List<String> ids = runtimeService.getActiveActivityIds(exe.getId());
            activityIds.addAll(ids);
        }

        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
        ProcessEngineConfiguration engconf = processEngine.getProcessEngineConfiguration();
        ProcessDiagramGenerator diagramGenerator = engconf.getProcessDiagramGenerator();
        //InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png", activityIds, flows, engconf.getActivityFontName(), engconf.getLabelFontName(), engconf.getAnnotationFontName(), engconf.getClassLoader(), 1.0);
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png", activityIds, flows, engconf.getActivityFontName(), engconf.getLabelFontName(), engconf.getAnnotationFontName(), engconf.getClassLoader(), 1.0, true);
        OutputStream out = null;
        byte[] buf = new byte[1024];
        int legth = 0;
        try {
            out = httpServletResponse.getOutputStream();
            while ((legth = in.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * 查询历史数据
     */
    @RequestMapping(value = "/history")
    public Object queryHistoryData(String userId) {
        List<HistoricActivityInstance> activities =
                historyService.createHistoricActivityInstanceQuery().taskAssignee(userId)
                        .orderByHistoricActivityInstanceEndTime().asc()
                        .list();

        return activities;

    }

    @RequestMapping("/create")
    public Map<String,Object> createFlow(){
        Map<String,Object> res =new HashMap<>();
        Map<String,Object> data = new HashMap<>();

        String flowPath ="E:\\flowablestudy\\flowablech5\\src\\main\\resources\\flows\\测试BPMN模型2.bpmn20.xml";

        Map<String,Object> createRes = generateFlow(flowPath);

        if (null == createRes){
            res.put("msg","创建流程失败");
            res.put("res","0");
            res.put("data",data);
            return res;
        }
        List<Process> processes =(List<Process>)createRes.get("processes");

        ArrayList<String> ids = new ArrayList<>();
        for (Process process :processes){
            ids.add(process.getId());
        }
        data.put("processKeys",ids);
        data.put("deployId",((Deployment)createRes.get("deployment")).getId());
        res.put("data",data);
        res.put("msg","创建流程成功");
        res.put("res","1");
        return res;
    }

    private Map<String, Object> generateFlow(String filePath) {
        Map<String, Object> res = new HashMap<>();
        //解析BPMN模型看是否成功
        XMLStreamReader reader = null;
        InputStream inputStream = null;
        try {
            BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
            XMLInputFactory factory = XMLInputFactory.newInstance();
            inputStream = new FileInputStream(new File(filePath));
            reader = factory.createXMLStreamReader(inputStream);
            BpmnModel model = bpmnXMLConverter.convertToBpmnModel(reader);
            List<Process> processes = model.getProcesses();
            Process curProcess = null;
            if (CollectionUtils.isEmpty(processes)) {
                log.error("BPMN模型没有配置流程");
                return null;
            }
            res.put("processes", processes);
            curProcess = processes.get(0);

            inputStream = new FileInputStream(new File(filePath));
            DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().name("TEST_FLOW")
                    .addInputStream(filePath, inputStream);

            Deployment deployment = deploymentBuilder.deploy();
            res.put("deployment", deployment);
            log.warn("部署流程 name:" + curProcess.getName() + " key " + deployment.getKey() + " deploy " + deployment);
            return res;
        } catch (Exception e) {
            log.error("BPMN模型创建流程异常", e);
            return null;
        } finally {
            try {
                reader.close();
            } catch (XMLStreamException e) {
                log.error("关闭异常", e);
            }
        }
    }


}
