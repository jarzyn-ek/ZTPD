
import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.*;

import java.io.IOException;

public class Main {
    public static EPDeployment compileAndDeploy(EPRuntime epRuntime, String epl) {
        EPDeploymentService deploymentService = epRuntime.getDeploymentService();
        CompilerArguments args = new CompilerArguments(epRuntime.getConfigurationDeepCopy());
        EPDeployment deployment;
        try {
            EPCompiled epCompiled = EPCompilerProvider.getCompiler().compile(epl, args);
            deployment = deploymentService.deploy(epCompiled);
        } catch (EPCompileException e) {
            throw new RuntimeException(e);
        } catch (EPDeployException e) {
            throw new RuntimeException(e);
        }
        return deployment;
    }

    public static void main(String[] args) throws IOException {

        Configuration configuration = new Configuration();
        configuration.getCommon().addEventType(KursAkcji.class);
        EPRuntime epRuntime = EPRuntimeProvider.getDefaultRuntime(configuration);
        EPDeployment deployment = compileAndDeploy(epRuntime,
                "select ISTREAM data, spolka, kursOtwarcia - min(kursOtwarcia) as roznica " +
                        "from KursAkcji(spolka='Oracle').win:length(2) " +
                        "having kursOtwarcia > MIN(kursOtwarcia)"
        );

        ProstyListener prostyListener = new ProstyListener();

        for (EPStatement statement : deployment.getStatements()) {
            statement.addListener(prostyListener);
        }

        InputStream inputStream = new InputStream();
        inputStream.generuj(epRuntime.getEventService());
    }
}
/*
24.
                "select IRSTREAM spolka as X, kursOtwarcia as Y " +
                        "from KursAkcji.win:length(3) " +
                        "where spolka = 'Oracle'"
Klauzula WHERE jest wykorzystywana dopiero przy filtrowaniu zdarzeń wynikowych

25.
                "select IRSTREAM data, kursOtwarcia, spolka " +
                        "from KursAkcji.win:length(3) " +
                        "where spolka = 'Oracle'"
26.
                "select IRSTREAM data, kursOtwarcia, spolka " +
                        "from KursAkcji(spolka='Oracle').win:length(3) "
27.
                "select ISTREAM data, kursOtwarcia, spolka " +
                        "from KursAkcji(spolka='Oracle').win:length(3) "
28.
                "select ISTREAM data, max(kursOtwarcia), spolka " +
                        "from KursAkcji(spolka='Oracle').win:length(5) "
29.
                "select ISTREAM data, spolka, kursOtwarcia - max(kursOtwarcia) as roznica " +
                        "from KursAkcji(spolka='Oracle').win:length(5) "
Funkcja max oblicza maksimum z aktualnie przetwarzanego okna, natomiast w SQL jest to maksimum z grupy lub tabeli

30.
                "select ISTREAM data, spolka, kursOtwarcia - min(kursOtwarcia) as roznica " +
                        "from KursAkcji(spolka='Oracle').win:length(2) " +
                        "having kursOtwarcia > MIN(kursOtwarcia)"
Tak, wynik zawiera tylko wzrosty
*/