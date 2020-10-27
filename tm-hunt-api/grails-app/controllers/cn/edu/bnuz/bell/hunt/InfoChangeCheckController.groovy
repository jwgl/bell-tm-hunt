package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.workflow.Activities
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.ListCommand
import cn.edu.bnuz.bell.workflow.ListType
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import org.springframework.beans.factory.annotation.Value


class InfoChangeCheckController {
	InfoChangeCheckService infoChangeCheckService
    FileTransferService fileTransferService
    @Value('${bell.teacher.filesPath}')
    String filesPath

    def index(String checkerId, ListCommand cmd) {
        renderJson infoChangeCheckService.list(checkerId, cmd)
    }

    def show(String checkerId, Long infoChangeCheckId, String id, String type) {
        ListType listType = ListType.valueOf(type)
        if (id == 'undefined') {
            renderJson infoChangeCheckService.getFormForCheck(checkerId, infoChangeCheckId, listType, Activities.CHECK)
        } else {
            renderJson infoChangeCheckService.getFormForCheck(checkerId, infoChangeCheckId, listType, UUID.fromString(id))
        }
    }

    def patch(String checkerId, Long infoChangeCheckId, String id, String op) {
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.ACCEPT:
                def cmd = new AcceptCommand()
                bindData(cmd, request.JSON)
                cmd.id = infoChangeCheckId
                infoChangeCheckService.accept(checkerId, cmd, UUID.fromString(id))
                break
            case Event.REJECT:
                def cmd = new RejectCommand()
                bindData(cmd, request.JSON)
                cmd.id = infoChangeCheckId
                infoChangeCheckService.reject(checkerId, cmd, UUID.fromString(id))
                break
            default:
                throw new BadRequestException()
        }

        show(checkerId, infoChangeCheckId, id, 'todo')
    }
}
