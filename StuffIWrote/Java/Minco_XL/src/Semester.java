import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.*;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Ethan Petuchowski
 * 8/26/13
 *
 * NOTES:
 * - week# can be easily obtained from the Date object
 * - Minco files can be found at: /Users/Ethan/Library/Application
 *      Support/Minco/CSV_Files/Documents/Minco/2013/Minco Week 5.csv
 */
public class Semester
{
    int            lastRowNum, theDayRowNum, osColNum;
    Row            headers;
    Date           dateObjICareAbout;
    Sheet          sheet;
    String         dateICareAbout;
    String         rightNow;
    String         backupFile;
    String         csvFile;
    Workbook       workbook;
    BufferedReader csv;

    Boolean              debug         = false;
    String[]             subjectsArray = {"Alg", "NN", "LrngThry", "Geo"};
    Map<String, Subject> subjects      = new HashMap<>();
    Map<String, Integer> mincoLine     = new HashMap<>(3);

    String     excelName          = "Fall '13";
    String     xlDir              = "/Users/Ethan/Dropbox/School Help Files/";
    String     backupDir          = xlDir + "Fall '13 Backups/";
    String     excelFile          = xlDir + excelName + ".xlsm";
    DateFormat newMincoDateFormat = new SimpleDateFormat("yyyy-MM-dd");


    Semester(String[] args) throws Exception {

        /* create subjects */
        for (String name : subjectsArray)
            subjects.put(name, new Subject(name));

        /* argument parsing */
        Options options = new Options();
        options.addOption("y", "yesterday", false, "fill in yesterday's data");
        options.addOption("d", "date", true, "fill in data for a given date");
        options.addOption("n", "dry-run", false, "don't fill in data");
        options.addOption("t", "debug", false, "don't say where in sheet data is going");
        CommandLineParser cli = new GnuParser();
        CommandLine cl = cli.parse(options, args, true);

        /* everything else */
        mincoLine.put("Date", 0); // TODO this should be an ENUM
        mincoLine.put("Minutes", 3);
        mincoLine.put("Title", 4);
        lastRowNum = theDayRowNum = osColNum = 0;
        Calendar cal = Calendar.getInstance();
        if (cl.hasOption("y"))
            cal.add(Calendar.DATE, -1);
        else if (cl.hasOption("d")) {    // use given date (defaults to current year)
            String enteredDate = cl.getOptionValue("d").replace('-','/');
            if (StringUtils.countMatches(enteredDate,"/") == 1)
                enteredDate += "/" + cal.get(Calendar.YEAR);
            Date d = new Date(enteredDate);
            cal.setTime(d);
        }
        if (cl.hasOption("n"))
            System.exit(3);
        if (cl.hasOption("t"))
            debug = true;
        dateObjICareAbout = cal.getTime();
        dateICareAbout = newMincoDateFormat.format(dateObjICareAbout);
        rightNow = new SimpleDateFormat("_MM-dd-HH-mm-ss").format(new Date());
        backupFile = backupDir + excelName + rightNow + ".xlsm";
        System.out.println("Backing Up...");
        System.out.println("IT'S TOO LATE TO CANCEL UNTIL THE WHOLE THING FINISHES!");
        File start = new File(excelFile);
        File end = new File(backupFile);
        copyFile(start, end);
        System.out.println("Looking for "+dateICareAbout);
        sheet = this.getSheet(excelFile);
        headers = sheet.getRow(0);
        int week = cal.get(Calendar.WEEK_OF_YEAR);
        csvFile = "/Users/Ethan/Library/Application Support/Minco/" +
                  "CSV_Files/Documents/Minco/2013/Minco Week "+week+".csv";
        csv = new BufferedReader(new FileReader(csvFile));
    }

    private Sheet getSheet(String excelFile) {
        InputStream inputStream = null;
        try { inputStream = new FileInputStream(excelFile); }
        catch (FileNotFoundException e) { e.printStackTrace(); }

        try { workbook = WorkbookFactory.create(inputStream); }
        catch (IOException | InvalidFormatException e) { e.printStackTrace(); }
        assert workbook != null;

        return workbook.getSheet("MainSheet");
    }

    public void writeOut() {
        FileOutputStream fileOut = null;
        try { fileOut = new FileOutputStream(excelFile); }
        catch (FileNotFoundException e) { e.printStackTrace(); }

        try { workbook.write(fileOut); }
        catch (IOException e) { e.printStackTrace(); }

        try { if (fileOut != null)  fileOut.close(); }
        catch (IOException e) { e.printStackTrace(); }
    }

    /** https://gist.github.com/mrenouf/889747 */
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists())
            destFile.createNewFile();
        FileInputStream fIn = null;
        FileOutputStream fOut = null;
        FileChannel source = null;
        FileChannel destination = null;
        try {
            fIn  = new FileInputStream(sourceFile);
            fOut = new FileOutputStream(destFile);
            source = fIn.getChannel();
            destination = fOut.getChannel();
            long transferred = 0;
            long bytes = source.size();
            while (transferred < bytes) {
                transferred += destination.transferFrom(source, 0, source.size());
                destination.position(transferred);
            }
        } finally {
            if (source != null)      source.close();
            else if (fIn != null)    fIn.close();

            if (destination != null) destination.close();
            else if (fOut != null)   fOut.close();
        }
    }
}