package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.hunt.cmd.FundCommand
import grails.gorm.transactions.Transactional
import org.apache.poi.ss.usermodel.CellType
import org.springframework.web.multipart.MultipartFile
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row

import javax.servlet.http.HttpServletRequest
import java.text.DateFormat
import java.time.LocalDate

@Transactional
class FundService {
    static final List<String> fields = ['序号', '项目编号', '省（市）立项', '省（市）中期', '省（市）结项','校立项', '校中期', '校结项', '院立项', '院中期', '院结项']
    static final List<Map> colsName = [[:], [:],
                                       [level: Level.PROVINCE, reportType: 1],
                                       [level: Level.PROVINCE, reportType: 3],
                                       [level: Level.PROVINCE, reportType: 4],
                                       [level: Level.UNIVERSITY, reportType: 1],
                                       [level: Level.UNIVERSITY, reportType: 3],
                                       [level: Level.UNIVERSITY, reportType: 4],
                                       [level: Level.COLLEGE, reportType: 1],
                                       [level: Level.COLLEGE, reportType: 3],
                                       [level: Level.COLLEGE, reportType: 4],]

    def upload(FundType fundType, HttpServletRequest request) {
        MultipartFile uploadFile = request.getFile('file')

        if (!uploadFile.empty) {
            def workBook = WorkbookFactory.create(uploadFile.inputStream)
            def sheet = workBook.getSheetAt(0)
            if (!checkFirstRow(sheet)) {
                throw new BadRequestException('数据不符合规范')
            }
            def rowList = sheet.rowIterator().collect()
            rowList.remove(0.intValue())
            def table = []
            rowList.each {row ->
                def values = []
                def cellList = getCellListByRow(row as Row, fields.size() - 1)
                cellList.each {cell ->
                    values << getValueByCell(cell as Cell)
                }
                if (values[1] != "") {
                    Project project = Project.findByCode(values[1] as String)
                    if (!project) {
                        throw new BadRequestException("项目编号“${values[1]}”不存在")
                    }
                    (2..fields.size() - 1).each {i ->
                        if (values[i] !="" && values[i] != 0) {
                            Fund fund = new Fund(
                                    project: project,
                                    dateCreated: new Date(),
                                    level: colsName[i].level,
                                    reportType: colsName[i].reportType,
                                    type: fundType,
                                    amount: values[i] as BigDecimal
                            )
                            if (!fund.save()) {
                                fund.errors.each {
                                    println it
                                }
                            }
                        }
                    }
                    table << values
                }
            }
            return table
        }
        return null
    }

    /**
     * 检验表格合法性
     * @param sheet
     * @return
     */
    static def checkFirstRow(Sheet sheet) {
        def row0 = sheet.getRow(0)

        fields.eachWithIndex{ String entry, int i ->
            if (entry != row0.getCell(i)) return false
        }
        return true
    }

    static def getCellListByRow(Row row, Integer cellNum) {
        def cellList = []
        (0..cellNum).each {index->
            cellList << row.getCell(index)
        }
        return cellList
    }

    static def getValueByCell(Cell cell) {
        if (cell.cellType == CellType.BLANK) {
            return ""
        } else if (cell.cellType == CellType.BOOLEAN) {
            return cell.booleanCellValue as Boolean
        } else if (cell.cellType == CellType.ERROR) {
            return cell.errorCellValue
        } else if (cell.cellType == CellType.FORMULA) {
            return cell.cellFormula
        } else if (cell.cellType == CellType.NUMERIC) {
            return cell.numericCellValue
        } else if (cell.cellType == CellType.STRING) {
            return cell.stringCellValue
        } else {
            return cell.stringCellValue
        }
    }

    def getProjectFunds(Long id) {
        Fund.executeQuery'''
select new map (
f.dateCreated as dateCreated,
f.level as level,
f.reportType as reportType,
f.type as type,
f.amount as amount
) from Fund f
join f.project p
where p.id = :id
''', [id: id]
    }

    /**
     * 粘贴板式的导入
     */
    def create(FundCommand cmd) {
        // 报错列表
        def error = new ArrayList<String>()
        def success = 0
        cmd.table?.eachWithIndex { row, index ->
            Project project = Project.findByCode(row.data[1])
            if (!project) {
                error.add("第${index + 1}行，项目编号“${row.data[1]}”不存在")
            } else if (row.data.size() < 11) {
                error.add("第${index + 1}行，存在空值，请填充为‘0’")
            }else {
                def rowSuccess = false
                (2..fields.size() - 1).each { i ->
                    if (row.data[i] != '0' && !row.data[i].isEmpty()) {
                        if (Fund.findByProjectAndLevelAndReportTypeAndType(project, colsName[i].level, colsName[i].reportType, cmd.fundType)) {
                            error.add("第${index + 1}行，第${i + 1}列重复导入！")
                        } else {
                            Fund fund = new Fund(
                                    project: project,
                                    dateCreated: new Date(),
                                    level: colsName[i].level,
                                    reportType: colsName[i].reportType,
                                    type: cmd.fundType,
                                    amount: row.data[i] as BigDecimal
                            )
                            if (!fund.save()) {
                                fund.errors.each {
                                    println it
                                }
                            } else {
                                rowSuccess = true
                            }
                        }
                    }
                }
                if (rowSuccess) {
                    success ++
                }
            }
        }
        return [error: error, success: success]
    }

    def monthesCreated() {
        Fund.executeQuery'''select distinct to_char(f.dateCreated, 'YYYY-MM') from Fund f'''
    }

    def list(String month, String fundType) {
        def dateStart = LocalDate.parse("${month}-01")
        def dateEnd = dateStart.plusMonths(1)
        Fund.executeQuery'''
select distinct new map(
f.project.code as code,
max(case when f.level = 'PROVINCE' and f.reportType = 1 then f.amount end) as col1,
max(case when f.level = 'PROVINCE' and f.reportType = 3 then f.amount end) as col2,
max(case when f.level = 'PROVINCE' and f.reportType = 4 then f.amount end) as col3,
max(case when f.level = 'UNIVERSITY' and f.reportType = 1 then f.amount end) as col4,
max(case when f.level = 'UNIVERSITY' and f.reportType = 3 then f.amount end) as col5,
max(case when f.level = 'UNIVERSITY' and f.reportType = 4 then f.amount end) as col6,
max(case when f.level = 'COLLEGE' and f.reportType = 1 then f.amount end) as col7,
max(case when f.level = 'COLLEGE' and f.reportType = 3 then f.amount end) as col8,
max(case when f.level = 'COLLEGE' and f.reportType = 4 then f.amount end) as col9
)
from Fund f
where f.dateCreated between to_date(:dateStart, 'YYYY-MM-DD') and to_date(:dateEnd, 'YYYY-MM-DD') 
and f.type = :fundType
group by f.project.code
''', [fundType: fundType, dateStart: dateStart.toString(), dateEnd: dateEnd.toString()]
    }
}
