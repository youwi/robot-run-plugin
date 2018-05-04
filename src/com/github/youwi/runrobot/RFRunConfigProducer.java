package com.github.youwi.runrobot;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.run.PythonConfigurationType;
import com.jetbrains.python.run.PythonRunConfiguration;

import java.util.HashMap;
import java.util.Map;

public class RFRunConfigProducer extends RunConfigurationProducer<PythonRunConfiguration> {
    public RFRunConfigProducer() {
        super(PythonConfigurationType.getInstance());
    }

    /**
     * 调试器协议
     * <p>
     * https://github.com/fabioz/PyDev.Debugger
     * <p>
     * python run 协议
     *
     * @param runConfig
     * @param context
     * @param sourceElement
     * @return
     */

    @Override
    protected boolean setupConfigurationFromContext(PythonRunConfiguration runConfig, ConfigurationContext context, Ref<PsiElement> sourceElement) {
        Location location = context.getLocation();
        if (location == null) {
            return false;
        }
        runConfig.setUseModuleSdk(false);
        runConfig.setModuleMode(true);
        runConfig.setScriptName(Plugin.default_module);
        runConfig.setWorkingDirectory(context.getProject().getBasePath());
        VirtualFile file = location.getVirtualFile();
        if (file == null) {
            return false;
        }
        String pathFile = file.getPath().replace(context.getProject().getBasePath() + "/", "");
        String testCaseName = getTestCaseName(context);
        runConfig.setScriptParameters(buildParameters(testCaseName, pathFile));

        Sdk sdk = ProjectRootManager.getInstance(context.getProject()).getProjectSdk();
        if (sdk != null) {
            runConfig.setSdkHome(sdk.getHomePath());//runConfig.setSdk(sdk);
        }
        // robot file
        if (testCaseName.startsWith("***")) {
            return false;
        }

        runConfig.setName(testCaseName);

        if (file.getName().endsWith(".robot"))
            return true;

     /*   if (file.getName().endsWith(".txt") || file.getName().endsWith(".md") || file.getName().endsWith(".side")) {
            runConfig.setScriptName(Plugin.default_md_module);
        }
        if (sourceElement != null) {
            if (sourceElement.toString().startsWith("|")) {
                return true;
            }
        }

        if (file.getName().endsWith(".robot"))
            return true;
        if (file.getName().endsWith(".txt")) {
            if (CACHE_MAP.get(file.getName()) != null) {
                return true;
            }
            String allText = location.getPsiElement().getText().trim();
            String[] allLines = allText.split("\n");
            int tableCount = 0;
            for (String line : allLines) {
                if (line.trim().startsWith("|") && line.trim().endsWith("|")) {
                    tableCount++;
                    if (tableCount > 3) break;
                } else {
                    tableCount = 0;
                }
            }
            CACHE_MAP.put(file.getName(), true);
        }
        String lineText = location.getPsiElement().getText().trim();
        if (lineText.startsWith("|") && lineText.endsWith("|")) {
            return true;
        }
        String parentLineText = location.getPsiElement().getParent().getParent().getText();
        if (parentLineText.startsWith("|") && parentLineText.endsWith("|")) {
            return true;
        }
*/
        return false;
    }

    public String buildParameters(String testCaseName, String scriptFileName) {
        return " -t \"" + testCaseName + "\" " + scriptFileName;
    }


    /**
     * caching .....
     */
    static Map CACHE_MAP = new HashMap();

    @Override
    public boolean isConfigurationFromContext(PythonRunConfiguration runConfig, ConfigurationContext context) {
        Location location = context.getLocation();
        if (location == null) {
            return false;
        }

        VirtualFile file = location.getVirtualFile();
        if (file == null) {
            return false;
        }

        String pathFile = file.getPath().replace(context.getProject().getBasePath() + "/", "");
        if (runConfig.getScriptParameters().trim().equals(buildParameters(getTestCaseName(context), pathFile).trim())) {
            return true;
        }

        return false;
    }

    public String getTestCaseName(ConfigurationContext context) {
        Location location = context.getLocation();
        VirtualFile file = location.getVirtualFile();
        if (file.getName().endsWith(".robot")) {
            PsiElement e = location.getPsiElement();
            String text = e.getText();
            String pText1 = e.getParent().getText();
            String pText2 = e.getParent().getParent().getText();
            String pText3 = e.getParent().getParent().getParent().getText();
            if (pText1.contains("\n")) {
                return pText1.split("\n")[0];
            }
            if (pText2.contains("\n")) {
                return pText2.split("\n")[0];
            }
            if (pText3.contains("\n")) {
                return pText3.split("\n")[0];
            }
            while (text.startsWith(" ") || text.startsWith("\t") || text.startsWith("\n") || text.equals("")) {
                e = e.getPrevSibling();
                text = e.getText();
            }
            return text;
        }
        if (file.getName().endsWith(".md")) {

        }


        return "";
    }


}


