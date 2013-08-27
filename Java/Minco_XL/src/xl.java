import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Ethan Petuchowski
 * 8/26/13
 *
 * Read in Minco Weekly Summaries
 * Summarize them
 * Insert the data into the appropriate place in the Fall_13.xls file
 *
 * Notes:
 * The first Row ("1") is number 0
 * The first Col ("A") is number 0
 */

/** TODO
 * Summarize Minco Weekly Summary
 *   Make it find the two most time-intensive activities PER subject
 *     and throw truncated versions into the activity slots in the XL
 *
 * Parse Args for Date Specs
 */
public class xl
{
    public static void main(String[] args)
            throws IOException, InvalidFormatException, ParseException {

        Semester s = new Semester();

        /* read and calculate Minco Weekly Summary */

        skipheaders(s);
        String line;
        while ((line = s.csv.readLine()) != null) {
            String[] splitLine = line.replaceAll("\"", "").replaceAll("\u0000", "").split(",");
            Date res = makeDate(s, splitLine);

            if (DateUtils.isSameDay(res, s.dateObjICareAbout)) {
                String[] taskLine = splitLine[s.mincoLine.get("Title")].split("\\s+");

                String lineSubject = taskLine[0];
                String lineTask    = getLineTask(taskLine);
                int    lineTime    = getLineTime(s, splitLine);

                if (s.subjectTaskTotals.containsKey(lineSubject)) {
                    putSubjectTaskTotals(s.subjectTaskTotals.get(lineSubject),
                                         lineTime,
                                         lineTask);
                }
                else System.out.println("UNKNOWN Subject: "+lineSubject);
            }
        }

        calcSubjectTotals(s);
        calcTopTwoSubjectTasks(s);

        /* READ AND UPDATE XL FILE */
        setDayAndTotalRowNums(s);
        debugSetRowNums(s);
        locateSubjectColumns(s);
        fillInData(s);
        s.writeOut();
    }

    /*
     * on the excel sheet, for each subject, write the total time spent, and top two tasks
     */
    private static void fillInData(Semester s) {
        for (String subject : s.subjects) {
            int col = s.subjectColumns.get(subject);
            Row row = s.sheet.getRow(s.theDayRowNum);
            List<String> tasks = s.subjectTasks.get(subject);
            System.out.println("\nWriting Subject: " + subject);

            // total time
            double timeInHours = s.subjectTotals.get(subject) / 60.0;
            String timeString = String.format("%3.2f", timeInHours);
            row.getCell(col).setCellValue(timeInHours);
            printNewCellContent(s.theDayRowNum, col, timeString);

            // top task
            row.getCell(++col).setCellValue(tasks.get(0));
            printNewCellContent(s.theDayRowNum, col, tasks.get(0));

            // second task
            row.getCell(++col).setCellValue(tasks.get(1));
            printNewCellContent(s.theDayRowNum, col, tasks.get(1));
        }
    }



    private static void printNewCellContent(int row, int col, String val) {
        if (!val.equals("")) {
            String colString = CellReference.convertNumToColString(col);
            System.out.printf("Putting %s in (%d, %s)\n", val, row, colString);
        }
    }

    private static int getLineTime(Semester s, String[] splitLine) {
        String lineTimeString = splitLine[s.mincoLine.get("Minutes")];
        return Integer.parseInt(lineTimeString);
    }

    private static String getLineTask(String[] taskLine) {
        StringBuilder lineTaskB = new StringBuilder();
        for (int i = 1; i < taskLine.length; i++)
            lineTaskB.append(taskLine[i] + " ");
        return lineTaskB.toString().trim();
    }

    private static Date makeDate(Semester s, String[] splitLine) throws ParseException {
        String lineDateString = splitLine[s.mincoLine.get("Date")];
        // TODO this is no longer what these will look like (works for tests though)
        DateFormat currentMincoFormat = new SimpleDateFormat("M/dd/yy");
        return currentMincoFormat.parse(lineDateString);
    }

    private static void printDateAndTitle(Semester s, String[] splitLine) {
        System.out.println(
            splitLine[s.mincoLine.get("Date")] + " " +
            splitLine[s.mincoLine.get("Title")]);
    }

    private static void skipheaders(Semester s) throws IOException {
        if (s.csv.readLine() == null) {
            System.out.println("This Minco File is empty or missing or something");
            System.exit(1);
        }
    }

    /*
     * add the data from one line of the Minco log to the task total
     * make a new task if necessary
     */
    private static void putSubjectTaskTotals(Map<String, Integer> tasks, int newTime, String task) {
        if (tasks.containsKey(task)) {
            int oldTime = tasks.get(task);
            tasks.put(task, oldTime + newTime);
        }
        else tasks.put(task, newTime);
    }

    /*
     * for each subject, find the column index of the place to put the Total Time
     */
    private static void locateSubjectColumns(Semester s) {
        for (String subject : s.subjectsArray) {
            // find column that contains this subject
            for (Cell header : s.headers)
                if (header.getStringCellValue().equals("c"+subject))
                    s.subjectColumns.put(subject, header.getColumnIndex() - 1);
        }
    }

    /*
     * for each subject, find the two tasks with most time spent on them
     */
    private static void calcTopTwoSubjectTasks(Semester s) {
        for (String subject : s.subjectTaskTotals.keySet()) {
            Map <String, Integer> subjectTaskMap = s.subjectTaskTotals.get(subject);
            int    max  = 0,  max2 = 0;
            String top  = "", top2 = "";
            for (String task : subjectTaskMap.keySet()) {
                int taskTotal = subjectTaskMap.get(task);
                if (max < taskTotal) {
                    max2 = max;
                    max = taskTotal;
                    top2 = top;
                    top = task;
                }
                else if (max2 < taskTotal) {
                    max2 = taskTotal;
                    top2 = task;
                }
            }
            List<String> tasksList = new ArrayList<>();
            tasksList.add(top);
            tasksList.add(top2);
            s.subjectTasks.put(subject, tasksList);
        }
    }

    /*
     * for each subject, find the total time spent in this day
     */
    private static void calcSubjectTotals(Semester s) {
        for (String subject : s.subjectTaskTotals.keySet()) {
            Map <String, Integer> subjectTaskMap = s.subjectTaskTotals.get(subject);
            int total = 0;
            for (String task : subjectTaskMap.keySet()) {
                total += subjectTaskMap.get(task);
            }
            s.subjectTotals.put(subject, total);
        }
    }

    private static void debugSetRowNums(Semester s) {
        if (!s.todayFound) {
            System.out.println("Given Date was not found in Excel Sheet");
            System.exit(3);
        }
        else System.out.println("Given Date was on row " + s.theDayRowNum);
    }

    /*
     * Set the index of today's row in this sheet, and
     * Set the index of the last row in this sheet
     */
    private static void setDayAndTotalRowNums(Semester s) {
        for (Row row : s.sheet) {
            // skip header row
            if (row.getRowNum() == 0) continue;
            Cell dateCell = row.getCell(0);
            if (dateCell != null) {
                dateCell.setCellType(Cell.CELL_TYPE_NUMERIC); // necessary for getDate
                Date thisDate = dateCell.getDateCellValue();
                String dateString = s.newMincoDateFormat.format(thisDate);
                s.lastRowNum++;
                if (dateString.equalsIgnoreCase(s.dateICareAbout)) {
                    s.todayFound = true;
                }
                if (!s.todayFound)
                    s.theDayRowNum++;
            }
        }
    }
}
