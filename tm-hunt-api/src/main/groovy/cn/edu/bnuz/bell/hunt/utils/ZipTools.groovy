package cn.edu.bnuz.bell.hunt.utils

import cn.edu.bnuz.bell.hunt.InfoChange
import cn.edu.bnuz.bell.hunt.Review
import cn.edu.bnuz.bell.hunt.ReviewTask
import cn.edu.bnuz.bell.organization.Teacher

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipTools {

    static byte[] zip(ReviewTask reviewTask, String baseDir) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        ZipOutputStream zipFile = new ZipOutputStream(baos)

        if (reviewTask.attach) {
            reviewTask.attach.each { name ->
                addEntry("${baseDir}/${name}", "${name}", zipFile)
            }
        }
        zipFile.finish()
        return baos.toByteArray()
    }

    static byte[] zip(Review review, String baseDir) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        ZipOutputStream zipFile = new ZipOutputStream(baos)

        if (review.mainInfoForm) {
            addEntry("${baseDir}/${review.mainInfoForm}", "${outputFileName('main', review, getExt(review.mainInfoForm))}", zipFile)
        }
        if (review.proofFile) {
            review.proofFile.eachWithIndex { name, index ->
                addEntry("${baseDir}/${name}", "${index + 1}_${outputFileName('proof', review, getExt(name))}", zipFile)
            }
        }
        if (review.summaryReport) {
            addEntry("${baseDir}/${review.summaryReport}", "${outputFileName('summary', review, getExt(review.summaryReport))}", zipFile)
        }

        zipFile.finish()

        return baos.toByteArray()
    }

    static byte[] zipAll(List<Review> reviews, String baseDir) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        ZipOutputStream zipFile = new ZipOutputStream(baos)
        reviews.each {review ->
            def fileDir = "${baseDir}/${review.project.principal.id}"
            if (review.mainInfoForm) {
                addEntry("${fileDir}/${review.mainInfoForm}", "${review.project.name}/${outputFileName('main', review, getExt(review.mainInfoForm))}", zipFile)
            }
            if (review.proofFile) {
                review.proofFile.eachWithIndex { name, index ->
                    addEntry("${baseDir}/${name}", "${index + 1}_${outputFileName('proof', review, getExt(name))}", zipFile)
                }
            }
            if (review.summaryReport) {
                addEntry("${fileDir}/${review.summaryReport}", "${review.project.name}/${outputFileName('summary', review, getExt(review.summaryReport))}", zipFile)
            }
        }
        zipFile.finish()

        return baos.toByteArray()
    }

    static byte[] zip(InfoChange infoChange, String baseDir) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        ZipOutputStream zipFile = new ZipOutputStream(baos)

        if (infoChange.mainInfoForm) {
            Teacher teacher =infoChange.principal ? infoChange.principal : infoChange.project.principal
            String outputName = "申报书-${infoChange.project.code}-${levelLabel(infoChange.project.level.name())}-${infoChange.project.subtype.name}-${teacher.name}"
            addEntry("${baseDir}/${infoChange.mainInfoForm}", "${outputName}.${getExt(infoChange.mainInfoForm)}", zipFile)
        }

        zipFile.finish()

        return baos.toByteArray()
    }

    private static addEntry(String baseName, String outputName, ZipOutputStream zipFile) {
        File file = new File(baseName)
        if (file?.exists() && file.isFile() && file.name.indexOf("bak_") == -1) {
            zipFile.putNextEntry(new ZipEntry(outputName))
            file.withInputStream { input -> zipFile << input }
            zipFile.closeEntry()
        }
    }

    private static preLabel(String pre, Integer reportType) {
        def labelMap = [
                main: ['申报书', '验收登记表'],
                proof: '主要佐证材料',
                summary: '总结报告',
                other: '其他']
        if (pre == 'main') {
            return labelMap.main[reportType ==1 ? 0 : 1]
        } else {
            return labelMap[pre]
        }
    }

    private static outputFileName(String pre, Review review, String ext) {
        if (review.reportType == 1) {
            return nameConvention(pre, review.project.name, review.project.level.name(), review.project.subtype.name, review.project.principal.name, ext)
        } else {
            return nameConvention(pre, review.reportType, review.project.code, review.project.level.name(), review.project.subtype.name, review.project.principal.name, ext)
        }
    }

    static levelLabel(String level) {
        def labelMap = [
                UNIVERSITY: '校级',
                CITY: '市级',
                PROVINCE: '省级',
                NATION: '国家'
        ]
        return labelMap[level]
    }

    private static getExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase()
    }

    static nameConvention(String pre, String prejectName, String levelName, String subTypeName, String teacherName, String ext) {
        return "${preLabel(pre, 1)}-${prejectName}-${levelLabel(levelName)}-${subTypeName}-${teacherName}.${ext}"
    }

    static nameConvention(String pre, Integer reportType, String projectCode, String levelName, String subTypeName, String teacherName, String ext) {
        return "${preLabel(pre, reportType)}-${levelLabel(levelName)}-${projectCode}-${subTypeName}-${teacherName}.${ext}"
    }
}
