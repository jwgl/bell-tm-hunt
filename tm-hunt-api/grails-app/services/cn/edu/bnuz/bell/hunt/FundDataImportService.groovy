package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import grails.gorm.transactions.Transactional
import org.apache.poi.ss.usermodel.CellType
import org.springframework.web.multipart.MultipartFile
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row

import javax.servlet.http.HttpServletRequest

@Transactional
class FundDataImportService {
    static final List<String> fields = ['序号', '项目编号', '省（市）立项', '省（市）中期', '省（市）结项','校立项', '校中期', '校结项', '院立项', '院中期', '院结项']
    static final List<Map> colsName = [[:], [:],
                                       [level: Level.PROVINCE, reportType: 1],
                                       [level: Level.PROVINCE, reportType: 2],
                                       [level: Level.PROVINCE, reportType: 3],
                                       [level: Level.UNIVERSITY, reportType: 1],
                                       [level: Level.UNIVERSITY, reportType: 2],
                                       [level: Level.UNIVERSITY, reportType: 3],
                                       [level: Level.COLLEGE, reportType: 1],
                                       [level: Level.COLLEGE, reportType: 2],
                                       [level: Level.COLLEGE, reportType: 3],]

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
}
