# 教学项目管理
```
/approvers  校管理员
    /approverId
        /tasks 任务
            /{taskId}
        /applications?taskId={taskId} 项目申请单
            /{applicationId}
                    
/checkers  学院审核员
    /checkerId
        /tasks 任务
            /{taskId}
        /applications?taskId={taskId} 项目申请单
            /{applicationId}

/teachers
    /{teacherId}
        /tasks 老师可申请或已申请过的任务
            /{taskId}
        /applications?taskId={taskId} 项目申请单
            /{applicationId}

/experts 专家
    /{expertId}
        /reviews   专家评审

/settings  设置
    /types 大类
    /subtypes 子类
    /experts 专家
    /teams 专家组
    /checkers 学院审核员
    /tasks 任务
        /${taskId}
            /projects 选择可检查项目


```