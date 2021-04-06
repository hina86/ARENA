package util;

import cleanup.CleanupEventListener;
import cleanup.AnnotationStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import settings.ExperimentEventListener;
import visualization.models.GRAPH_TYPE;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Iffat Fatima
 * @created on 27/10/2020
 */
public class MyFilesUtils {

    /**
     *  Deletes any file on @param filePath and creates a new one
     *  with @param data  written in it
     */
    public static boolean writeFileData(String filePath, String data, boolean rewrite){
        System.out.println("Script File path: "+ filePath);
        try{
            File file = new File(filePath);
            if (rewrite) {
                if (file.exists()) {
                    file.delete();
                }
                if (file.createNewFile()) {
                    FileWriter fw = new FileWriter(file.getPath());
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(data);
                    bw.close();
                    return true;
                }
            } else {
                if (!file.exists()){
                    if(file.createNewFile()) {
                        FileWriter fw = new FileWriter(file.getPath());
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write(data);
                        bw.close();
                        return true;
                    }
                }
            }


        }
        catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     *
     * Executes the .bat file at @param batFilePath
     * Sends success/error logs to UI through @param eventListener
     * @return the execution success and failure.
     */
    public static boolean executeBatFile(String batFilePath, ExperimentEventListener eventListener) {
        boolean isSuccess =false;
        int exitStatus = 0;
        File dmbatchFile = new File(batFilePath);
        ProcessBuilder processBuilder = new ProcessBuilder(dmbatchFile.getAbsolutePath());
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            BufferedReader bre = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;

            while ((line = br.readLine()) != null) {
                System.out.println(line);
//                eventListener.printMessage("Message: "+ line);
            }
            while ((line = bre.readLine()) != null) {
                eventListener.printMessage("Error: "+line);
            }
            bre.close();
            exitStatus = process.waitFor();
            isSuccess = true;
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            eventListener.printError(e);
            isSuccess =false;
        }
        System.out.println("Processed finished with status: " + exitStatus);
        return isSuccess;
    }
    /**
     *
     * Executes the .bat file at @param batFilePath
     * Sends success/error logs to UI through @param eventListener
     * @return the execution success and failure.
     */
    public static String executeAPIBatFile(String batFilePath, ExperimentEventListener eventListener) {
        String apiVersion = "";
        boolean isSuccess =false;
        int exitStatus = 0;
        File dmbatchFile = new File(batFilePath);
        ProcessBuilder processBuilder = new ProcessBuilder(dmbatchFile.getAbsolutePath());
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            int i=0;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (i==2) {
                    apiVersion = line;
                    System.out.println("API Version " + apiVersion);
                    break;
                }
                i++;
            }
            exitStatus = process.waitFor();
            isSuccess = true;
//            if (exitStatus == 0 ) {
//                isSuccess =true;
//            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            eventListener.printError(e);
            isSuccess =false;
        }
        System.out.println("Processed finished with status: " + exitStatus);
        return apiVersion;
    }
    /**
     * 
     * Executes python file with path @param filePath,
     * arguments as a single @param arguments String,
     *  @param eventListener that is used to display success/error messages in terminal
     */
    public static boolean runPythonScript(String filePath, String arguments, ExperimentEventListener eventListener){
        String command = "python "+ filePath + " " + arguments;
        int exitStatus = -1;
        try {
//            eventListener.printMessage("Executing command: "+ command);
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader bri = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader bre = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = bri.readLine()) != null) {
//                eventListener.printMessage(line);
            }
            bri.close();
            while ((line = bre.readLine()) != null) {
               // eventListener.printMessage("Error: "+line);
            }
            bre.close();
            exitStatus = process.waitFor();
            System.out.println("Power Monitor Exit Status: "+ exitStatus);
            process.destroy();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            eventListener.printError(e);
        }
        if (exitStatus == 0) {
//            eventListener.printMessage("Started Power Monitor");
            return true;
        } else {
            eventListener.printMessage("Error running Power Monitor");
            return false;
        }
    }

    public static boolean isDeviceConnected(String filePath, ExperimentEventListener eventListener) {
        boolean isSuccess =false;
        int exitStatus = 0;
        File dmbatchFile = new File(filePath);
        ProcessBuilder processBuilder = new ProcessBuilder(dmbatchFile.getAbsolutePath());
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            BufferedReader bre = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            while ((line = bre.readLine()) != null) {
                System.out.println(line);
            }
            bre.close();
            exitStatus = process.waitFor();
            System.out.println("exit status: "+ exitStatus);
            if (exitStatus == 0) {
                isSuccess = true;
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            eventListener.printError(e);
            isSuccess = false;
        }
        System.out.println("Processed finished with status: " + exitStatus);
        return isSuccess;
    }

    public static String getCurrentLocalDateTimeStamp() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    }

    public static void writeObjToCsvFile(Object bean, File file, CleanupEventListener eventListener) {
        try {
            Writer writer = new FileWriter(file);
            StatefulBeanToCsv<Object> beanToCsv = new StatefulBeanToCsvBuilder<>(writer)
                    .withMappingStrategy(new AnnotationStrategy(bean.getClass()))
                    .build();
            beanToCsv.write(bean);
            writer.close();
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e){
            e.printStackTrace();
            eventListener.printCleanupMessage("Writing clean file failed. Make sure all files are closed in Results Folder");
        }
    }

    public static String getSelectedValue(String[] strings, int selectedIndex) {
        return "";
    }

    public static GRAPH_TYPE getGraphType(String[] strings, int selectedIndex) {
        return GRAPH_TYPE.BOXPLOT;
    }

    public static List<Object> asList(final DefaultListModel model) {
        return new AbstractList<Object>() {
            @Override
            public int size() {
                return model.size();
            }

            @Override public Object get(int index) {
                return  model.getElementAt(index);
            }
        };
    }

    public static Color getRandomColor() {
        return new Color((int)(Math.random() * 0x1000000));
    }

    public static List<String> convertAwtColorToHexColorCode(List<Object> list) {
        List<String> hexColors = new ArrayList<String>();
        for(Object obj: list){
            Color color = (Color) obj;
            hexColors.add(String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()));
        }
        return hexColors;
    }

    public static String listToString(ArrayList<String> list) {
        String converted = "";
        for (String value: list){
            converted = converted + "'" +value + "', ";
        }
        if (!converted.isEmpty()){
           converted = converted.substring(0, converted.length()-2);
        }
        return converted;
    }
}
