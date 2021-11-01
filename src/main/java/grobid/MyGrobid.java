package grobid;

import org.grobid.core.data.BiblioItem;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.main.GrobidHomeFinder;
import org.grobid.core.utilities.GrobidProperties;
import shadedwipo.org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/*Author
 * rainbow0220
 */

public class MyGrobid {
    private static Engine engine=null;
    private static GrobidAnalysisConfig config = GrobidAnalysisConfig.defaultInstance();
    public MyGrobid(){
        try{
            Properties prop =new Properties();
            prop.load(new FileInputStream("grobid.properties"));
            //String pGrobidHome = prop.getProperty("grobid.pGrobidHome");
            String pGrobidHome = "/home/ljg/grobid-0.7.0/grobid-home";
            GrobidHomeFinder grobidHomeFinder = new GrobidHomeFinder(Arrays.asList(pGrobidHome));

            GrobidProperties.getInstance(grobidHomeFinder);

            System.out.println(">>>>>>>>GROBID_HOME="+ GrobidProperties.getGrobidHome());

            engine = GrobidFactory.getInstance().createEngine();
            config = new GrobidAnalysisConfig.GrobidAnalysisConfigBuilder().build();

        } catch (Exception e) {
            // If an exception is generated, print a stack trace
            e.printStackTrace();
        }
    }

    public static String runGrobid(File pdfFile, String process) {
        String tei = null;
        try {
            if(process.equals("fulltext")) {
                // Biblio object for the result
                BiblioItem resFullText = new BiblioItem();
                tei = engine.fullTextToTEI(pdfFile, config);
            }else {
                System.err.println("Unknown selected process: "+ process);
                System.err.println("Usage: command process[header] path_to_pdf");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tei;
    }

    public void close(){
    }
    public static void main(String[] args) {

        String pdfPath = "/home/ljg/oriPDFs";
        String telPath = "/home/ljg/outputs";

        System.out.print("header" + " " + pdfPath + " " + telPath);

        File pdfFile = new File(pdfPath);
        File telFile = new File(telPath);

        if (!pdfFile.exists()) {
            System.err.println("Path does not exist: " + pdfPath);
            System.exit(0);
        }

        List<File> filesToProcess = new ArrayList<File>();
        if (pdfFile.isFile()) {
            filesToProcess.add(pdfFile);
        } else if (pdfFile.isDirectory()) {
            if (!telFile.exists()) {
                System.err.println("Path does not exist: " + telPath);
                System.exit(0);
            }

            if (!telFile.isDirectory()) {
                System.err.println("BibTex path is not a directory: " + telPath);
                System.exit(0);
            }

            File[] refFiles = pdfFile.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".pdf") || name.endsWith(".PDF");
                }
            });

            if (refFiles == null) {
                System.err.println("No PDF file to be processed under directory: " + pdfPath);
                System.exit(0);
            }

            for (int i = 0; i < refFiles.length; i++) {
                filesToProcess.add(refFiles[i]);
            }
        }

        MyGrobid mygrobid = new MyGrobid();
        try {
            for (File fileToProcess : filesToProcess) {
                String result = mygrobid.runGrobid(fileToProcess, "fulltext");
                if (!telFile.exists() || telFile.isFile())
                    FileUtils.writeStringToFile(telFile, result, "UTF-8");
                else {
                    File theTelFile = new File(telFile.getPath() + "/" +
                            fileToProcess.getName().replace(".pdf", ".xml").replace(".PDF", ".xml"));
                    FileUtils.writeStringToFile(theTelFile, result, "UTF-8");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
