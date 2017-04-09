package com.lims.utils;

import com.jfinal.kit.PathKit;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by qulongjun on 2017/4/8.
 */
public class ExcelRead {
    public static final String OFFICE_EXCEL_2003_POSTFIX = "xls";
    public static final String OFFICE_EXCEL_2010_POSTFIX = "xlsx";
    public static final String NOT_EXCEL_FILE = " : Not the Excel file!";
    public static final String EMPTY = "";
    public static final String POINT = ".";
    public static final String PROCESSING = "Processing...";

    /**
     * read the Excel file
     *
     * @param path the path of the Excel file
     * @return
     * @throws IOException
     */
    public List<Map> readExcel(String path, String[] titles) throws IOException {
        if (path == null || EMPTY.equals(path)) {
            return null;
        } else {
            String postfix = getPostfix(path);
            if (!EMPTY.equals(postfix)) {
                if (OFFICE_EXCEL_2003_POSTFIX.equals(postfix)) {
                    return readXls(path, titles);
                } else if (OFFICE_EXCEL_2010_POSTFIX.equals(postfix)) {
                    return readXlsx(path, titles);
                }
            } else {
                System.out.println(path + NOT_EXCEL_FILE);
            }
        }
        return null;
    }

    /**
     * Read the Excel 2010
     *
     * @param path the path of the excel file
     * @return
     * @throws IOException
     */
    public List<Map> readXlsx(String path, String[] titles) throws IOException {
        System.out.println(PROCESSING + path);
        InputStream is = new FileInputStream(PathKit.getWebRootPath() + path.replace("\\", "/").trim());
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(is);
        List<Map> list = new ArrayList<>();
        Map result = null;
        //List<Object> list = new ArrayList<Object>();
        // Read the Sheet
        for (int numSheet = 0; numSheet < xssfWorkbook.getNumberOfSheets(); numSheet++) {
            XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(numSheet);
            if (xssfSheet == null) {
                continue;
            }
            // Read the Row
            for (int rowNum = 1; rowNum <= xssfSheet.getLastRowNum(); rowNum++) {
                XSSFRow xssfRow = xssfSheet.getRow(rowNum);
                if (xssfRow != null) {
                    result = new HashMap<>();
                    for (int i = xssfRow.getFirstCellNum(); i < xssfRow.getLastCellNum(); i++) {
                        XSSFCell value = xssfRow.getCell(i);
                        result.put(titles[i], value);
                    }
                    System.out.println(result);
                    list.add(result);
                }
            }
        }
        return list;
    }

    /**
     * Read the Excel 2003-2007
     *
     * @param path the path of the Excel
     * @return
     * @throws IOException
     */
    public List<Map> readXls(String path, String[] titles) throws IOException {
        System.out.println(PROCESSING + path);
        InputStream is = new FileInputStream(PathKit.getWebRootPath() + path.replace("\\", "/").trim());
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook(is);
        List<Map> list = new ArrayList<>();
        Map result = null;
        //List<Student> list = new ArrayList<Student>();
        // Read the Sheet
        for (int numSheet = 0; numSheet < hssfWorkbook.getNumberOfSheets(); numSheet++) {
            HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(numSheet);
            if (hssfSheet == null) {
                continue;
            }
            // Read the Row
            for (int rowNum = 1; rowNum <= hssfSheet.getLastRowNum(); rowNum++) {
                HSSFRow hssfRow = hssfSheet.getRow(rowNum);
                if (hssfRow != null) {
                    result = new HashMap<>();
                    for (int i = hssfRow.getFirstCellNum(); i < hssfRow.getLastCellNum(); i++) {
                        HSSFCell value = hssfRow.getCell(i);
                        result.put(titles[i], value);
                    }
                    list.add(result);
                }
            }
        }
        return list;
    }

    @SuppressWarnings("static-access")
    private String getValue(XSSFCell xssfRow) {
        if (xssfRow.getCellType() == xssfRow.CELL_TYPE_BOOLEAN) {
            return String.valueOf(xssfRow.getBooleanCellValue());
        } else if (xssfRow.getCellType() == xssfRow.CELL_TYPE_NUMERIC) {
            return String.valueOf(xssfRow.getNumericCellValue());
        } else {
            return String.valueOf(xssfRow.getStringCellValue());
        }
    }

    @SuppressWarnings("static-access")
    private String getValue(HSSFCell hssfCell) {
        if (hssfCell.getCellType() == hssfCell.CELL_TYPE_BOOLEAN) {
            return String.valueOf(hssfCell.getBooleanCellValue());
        } else if (hssfCell.getCellType() == hssfCell.CELL_TYPE_NUMERIC) {
            return String.valueOf(hssfCell.getNumericCellValue());
        } else {
            return String.valueOf(hssfCell.getStringCellValue());
        }
    }


    /**
     * get postfix of the path
     *
     * @param path
     * @return
     */
    public static String getPostfix(String path) {
        if (path == null || EMPTY.equals(path.trim())) {
            return EMPTY;
        }
        if (path.contains(POINT)) {
            return path.substring(path.lastIndexOf(POINT) + 1, path.length());
        }
        return EMPTY;
    }
}
