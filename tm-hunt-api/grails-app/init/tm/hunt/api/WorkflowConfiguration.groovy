package tm.hunt.api

import cn.edu.bnuz.bell.hunt.InfoChangeReviewerService
import cn.edu.bnuz.bell.hunt.ProjectReviewerService
import cn.edu.bnuz.bell.workflow.DomainStateMachineHandler
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.StateObject
import cn.edu.bnuz.bell.workflow.config.DefaultStateMachineConfiguration
import cn.edu.bnuz.bell.workflow.config.DefaultStateMachinePersistConfiguration
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.statemachine.StateMachine
import org.springframework.statemachine.persist.StateMachinePersister

@Configuration
@Import([DefaultStateMachineConfiguration, DefaultStateMachinePersistConfiguration])
class WorkflowConfiguration {
    @Bean('projectReviewStateMachine')
    DomainStateMachineHandler ProjectReviewStateMachine(
            @Qualifier('HuntStateMachine')
            StateMachine<State, Event> stateMachine,
            StateMachinePersister<State, Event, StateObject> persister,
            ProjectReviewerService projectReviewerService) {
        new DomainStateMachineHandler(stateMachine, persister, projectReviewerService)
    }

    @Bean('infoChangeReviewStateMachine')
    DomainStateMachineHandler InfoChangeReviewStateMachine(
            @Qualifier('HuntStateMachine')
                    StateMachine<State, Event> stateMachine,
            StateMachinePersister<State, Event, StateObject> persister,
            InfoChangeReviewerService infoChangeReviewerService) {
        new DomainStateMachineHandler(stateMachine, persister, infoChangeReviewerService)
    }
}
