# Getting Started
flowable工作流 测试文件

*[参考文件](https://blog.csdn.net/puhaiyang/article/details/79845248)

备注:
ExpenseProcess.bpmn20.xml文件可以通过bpm相关插件生成,idea中可使用actbmp插件绘制.bpmn文件
flowable支持.bpmn20.xml或.bpmn文件的工作流配置
flowable 会默认加载resource目录下的processes时就可以将此流程配置加载到数据库进行持久化
1.先启动好此项目，然后创建一个流程：

访问：http://localhost:4000/expense/add?userId=123&money=123321

返回：提交成功.流程Id为：2501

 

2.查询待办列表:

访问：http://localhost:4000/expense/list?userId=123

输出：Task[id=2507, name=出差报销]

 

3.同意：

访问：http://localhost:4000/expense/apply?taskId=2507

返回：processed ok!

 

4.生成流程图：

访问：http://localhost:4000/expense/processDiagram?processId=2501
5.查询历史记录

http://localhost:4000/expense/history?userId=123

http://localhost:4000/expense/add?userId=123&money=123
   提交成功.流程Id为：d12fe431-ddb0-11e9-af79-3ef011314025
http://localhost:4000/expense/list?userId=123
    [{"suspensionState":1,"name":"出差报销","originalPersistentState":{"owner":null,"processInstanceId":"d12fe431-ddb0-11e9-af79-3ef011314025","processDefinitionId":"Expense:2:9557ea1f-ddb0-11e9-af79-3ef011314025","suspensionState":1,"formKey":null,"priority":50,"executionId":"d1308074-ddb0-11e9-af79-3ef011314025","taskDefinitionKey":"fillTask","subTaskCount":0,"createTime":"2019-09-23T03:18:30.515+0000","name":"出差报销","isCountEnabled":true,"variableCount":0,"assignee":"123","identityLinkCount":0,"category":null},"id":"d1331888-ddb0-11e9-af79-3ef011314025","assignee":"123","revision":1}]

http://localhost:4000/expense/apply?taskId=d1331888-ddb0-11e9-af79-3ef011314025
processed ok!
http://localhost:4000/expense/processDiagram?processId=d12fe431-ddb0-11e9-af79-3ef011314025
