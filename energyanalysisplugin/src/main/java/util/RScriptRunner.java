package util;

import java.io.*;

public class RScriptRunner  {

    /**
     * Write R scripts with hardcoded values for all graphs
     * Dynamically write these bat files when user generates a graph
     * Execute the bat file
     *
     *
     */


    public static void createBatFile(String batFilePath, String rFilePath, String[] args) {
        StringBuilder script = new StringBuilder("Rscript" + " " + rFilePath + " ");
        for (String arg: args){
            script.append(arg).append(" ");
        }
        MyFilesUtils.writeFileData(batFilePath, script.toString(), true);
    }

    /**
     *
     * Executes the .bat file at @param batFilePath
     * Sends success/error logs to UI through @param eventListener
     * @return the execution success and failure.
     */
    public static RScriptResult executeBatFile(String batFilePath) {

        boolean isSuccess =true;
        String result = "";

        String error = "";
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
            int count = 0;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (count > 1) {
                    result += line.trim() + "\n";
                    if (result.contains("Error")){
                        error += line.trim() + "\n";
                        isSuccess = false;
                    }
                }
                count ++;
            }
            while ((line = bre.readLine()) != null) {
                System.out.println("Error: "+ line);
                if (count > 1) {
                    error += line.trim() + "\n";
                }
                isSuccess = false;
            }
            bre.close();
        } catch (IOException e) {
            e.printStackTrace();
            isSuccess =false;
        }
        RScriptResult scriptResult = new RScriptResult();
        scriptResult.result = result;
        scriptResult.error = error;
        scriptResult.successful = isSuccess;
        return scriptResult;
    }
}
