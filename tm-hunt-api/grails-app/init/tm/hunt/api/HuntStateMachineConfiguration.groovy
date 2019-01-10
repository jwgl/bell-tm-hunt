package tm.hunt.api

import cn.edu.bnuz.bell.workflow.Activities
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.actions.AbstractEntryAction
import cn.edu.bnuz.bell.workflow.actions.ManualEntryAction
import cn.edu.bnuz.bell.workflow.actions.SubmittedEntryAction
import cn.edu.bnuz.bell.workflow.config.StandardActionConfiguration
import cn.edu.bnuz.bell.workflow.events.EventData
import cn.edu.bnuz.bell.workflow.events.ManualEventData
import cn.edu.bnuz.bell.workflow.events.RejectEventData
import cn.edu.bnuz.bell.workflow.events.SubmitEventData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.statemachine.StateContext
import org.springframework.statemachine.action.Action
import org.springframework.statemachine.config.EnableStateMachine
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer

@Configuration
@EnableStateMachine(name='HuntStateMachine')
@Import(StandardActionConfiguration)
class HuntStateMachineConfiguration extends EnumStateMachineConfigurerAdapter<State, Event> {
    @Autowired
    StandardActionConfiguration actions

    @Override
    void configure(StateMachineStateConfigurer<State, Event> states) throws Exception {
        states
                .withStates()
                .initial(State.CREATED)
                .state(State.CREATED,   [actions.logEntryAction()], null)
                .state(State.SUBMITTED, [actions.logEntryAction(), approvedEntryAction()],  [actions.workitemProcessedAction()])
                .state(State.CLOSED,    [actions.logEntryAction(), actions.closedEntryAction()], null)
                .state(State.CHECKED,   [actions.logEntryAction(), actions.checkedEntryAction()],    [actions.workitemProcessedAction()])
                .state(State.REJECTED,  [actions.logEntryAction(), actions.rejectedEntryAction()],   [actions.workitemProcessedAction()])
                .state(State.FINISHED,  [actions.logEntryAction(), actions.notifySubmitterAction()], null)
    }

    @Override
    void configure(StateMachineTransitionConfigurer<State, Event> transitions) throws Exception {
        transitions
            .withInternal()
                .source(State.CREATED)
                .event(Event.UPDATE)
                .action(actions.logTransitionAction())
                .and()
            .withExternal()
                .source(State.CREATED)
                .event(Event.SUBMIT)
                .target(State.SUBMITTED)
                .and()
            .withExternal()
                .source(State.SUBMITTED)
                .event(Event.ACCEPT)
                .target(State.CHECKED)
                .and()
            .withExternal()
                .source(State.SUBMITTED)
                .event(Event.REJECT)
                .target(State.REJECTED)
                .and()
            .withExternal()
                .source(State.SUBMITTED)
                .event(Event.FINISH)
                .target(State.FINISHED)
                .and()
            .withExternal()
                .source(State.SUBMITTED)
                .event(Event.CLOSE)
                .target(State.CLOSED)
                .and()
            .withExternal()
                .source(State.CHECKED)
                .event(Event.FINISH)
                .target(State.FINISHED)
                .and()
            .withExternal()
                .source(State.CHECKED)
                .event(Event.REJECT)
                .target(State.SUBMITTED)
                .and()
            .withExternal()
                .source(State.CHECKED)
                .event(Event.CLOSE)
                .target(State.CLOSED)
                .and()
            .withInternal()
                .source(State.SUBMITTED)
                .event(Event.REVIEW)
                .action(actions.logTransitionAction())
                .action(reviewEntryAction())
                .and()
        .withInternal()
                .source(State.CHECKED)
                .event(Event.REVIEW)
                .action(actions.logTransitionAction())
                .action(actions.workitemProcessedAction())
                .action(reviewEntryAction())
                .and()
            .withInternal()
                .source(State.REJECTED)
                .event(Event.UPDATE)
                .action(actions.logTransitionAction())
                .and()
            .withExternal()
                .source(State.REJECTED)
                .event(Event.SUBMIT)
                .target(State.SUBMITTED)
                .and()
            .withExternal()
                .source(State.CHECKED)
                .event(Event.ROLLBACK)
                .target(State.SUBMITTED)
    }

    @Bean
    Action<State, Event> approvedEntryAction() {
        new AbstractEntryAction() {
            @Override
            void execute(StateContext<State, Event> context) {
                def data = context.getMessageHeader(EventData.KEY)
                if (data instanceof SubmitEventData ) {
                    def event = data as SubmitEventData
                    def workflowInstance = event.entity.workflowInstance

                    if (!workflowInstance) {
                        workflowInstance = workflowService.createInstance(event.entity.workflowId, event.title, event.entity.id)
                        event.entity.workflowInstance = workflowInstance
                    }

                    workflowService.createWorkitem(
                            workflowInstance,
                            event.fromUser,
                            context.event,
                            context.target.id,
                            event.comment,
                            event.ipAddress,
                            event.toUser,
                            Activities.CHECK,
                    )

                } else if (data instanceof ManualEventData) {
                    def event = data as ManualEventData
                    workflowService.createWorkitem(data.entity.workflowInstance,
                            event.fromUser,
                            context.event,
                            context.target.id,
                            event.comment,
                            event.ipAddress,
                            event.toUser,
                            Activities.CHECK,
                    )
                } else {
                    throw new Exception('Unsupported event type')
                }
            }
        }
    }

    @Bean
    Action<State, Event> reviewEntryAction() {
        new ManualEntryAction(Activities.REVIEW)
    }
}
