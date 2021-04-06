package util;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.*;

/**
 * @author Iffat Fatima
 * @created on 22/10/2020
 */
//Reference @link https://stackoverflow.com/questions/2097813/how-to-parse-the-androidmanifest-xml-file-inside-an-apk-package
public class APKReader {

    private static APKReader apkReader;
    private APKReader(){}
    public static APKReader getInstance() {
        if (apkReader == null) {
            apkReader = new APKReader();
        }
        return apkReader;
    }

    /**
     * @param apkPath Reads APK file from the apkPath,
     *               extracts AndroidManifest.xml from it and saves it as Manifest.xml.
     *               Uses xpath to find the package name from the nodes.
     * @return package name of the apk file as a string
     */
    public static String getPackageName(String apkPath) {
        try (ApkFile apkFile = new ApkFile(new File(apkPath))) {
            ApkMeta apkMeta = apkFile.getApkMeta();
            System.out.println("PAckage"+ apkMeta.getPackageName());
            return apkMeta.getPackageName();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Reads the manifest file Manifest.xml and extracts qualified name of the launcher activity using xPath
     * Prerequisite: Manifest file must be present in the directory
     * @return qualified name of launcher activity as String
     */
    public String getLauncherClass(String apkPath) {
        String launcherClass = "";
        try (ApkFile apkFile = new ApkFile(new File(apkPath))) {
            String manifestXml = apkFile.getManifestXml();
            File file = new File(System.getProperty("user.home") + File.separator +"Manifest.xml");
            if (file.exists()) file.delete();
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(manifestXml);
            fileWriter.close();
            if (file.exists()) {
                try {
                    InputStream is = new FileInputStream(file);
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(is);

                    Element element=doc.getDocumentElement();
                    element.normalize();
                    XPathFactory xPathfactory = XPathFactory.newInstance();
                    XPath xpath = xPathfactory.newXPath();
                    XPathExpression expr = xpath.compile("//manifest/application/activity/intent-filter/category[@name]");
                    NodeList nl = (NodeList) expr.evaluate(element, XPathConstants.NODESET);
                    System.out.println("Node list: "+ nl.getLength());
                    for (int i = 0; i < nl.getLength(); i++) {
                        Node currentItem = nl.item(i);
                        System.out.println("Extracted: "+ currentItem.getTextContent());
                        if (currentItem != null) {
                            if (currentItem.getAttributes().getNamedItem("android:name").getNodeValue().contains("LAUNCHER")) {
                                launcherClass = currentItem.getParentNode().getParentNode().getAttributes().getNamedItem("android:name").getNodeValue();
                                System.out.println("Activity: " + launcherClass);
                            }
                        }
                    }
                } catch (ParserConfigurationException | XPathExpressionException | SAXException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("No manifest extracted yet");
            }
            return launcherClass;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return launcherClass;
    }
}
