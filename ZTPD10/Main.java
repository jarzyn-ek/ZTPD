import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.*;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Configuration configuration = new Configuration();
        configuration.getCommon().addEventType(KursAkcji.class);
        EPRuntime epRuntime = EPRuntimeProvider.getDefaultRuntime(configuration);

        EPDeployment deployment = compileAndDeploy(epRuntime,
                "SELECT data, spolka, obrot FROM KursAkcji(market='NYSE').win:ext_timed_batch(data.getTime(), 7 days) order by obrot desc limit 1 offset 2");

        ProstyListener prostyListener = new ProstyListener();
        for (EPStatement statement : deployment.getStatements()) {
            statement.addListener(prostyListener);
        }

        InputStream inputStream = new InputStream();
        inputStream.generuj(epRuntime.getEventService());
    }

    public static EPDeployment compileAndDeploy(EPRuntime epRuntime, String epl) {
        EPDeploymentService deploymentService = epRuntime.getDeploymentService();
        CompilerArguments args = new CompilerArguments(epRuntime.getConfigurationDeepCopy());
        EPDeployment deployment;
        try {
            EPCompiled epCompiled = EPCompilerProvider.getCompiler().compile(epl, args);
            deployment = deploymentService.deploy(epCompiled);
        } catch (EPCompileException | EPDeployException e) {
            throw new RuntimeException(e);
        }
        return deployment;
    }
}

/*
5.
SELECT ISTREAM data, kursZamkniecia, spolka, max(kursZamkniecia) - kursZamkniecia as roznica FROM KursAkcji.win:ext_timed_batch(data.getTime(), 1 day)

6.
SELECT ISTREAM data, kursZamkniecia, spolka, max(kursZamkniecia) - kursZamkniecia as roznica FROM KursAkcji(spolka in ('IBM', 'Honda', 'Microsoft')).win:ext_timed_batch(data.getTime(), 1 day)

7.
A)
SELECT ISTREAM data, kursZamkniecia, kursOtwarcia, spolka FROM KursAkcji(kursZamkniecia > kursOtwarcia).win:length(1)

B)
SELECT ISTREAM data, kursZamkniecia, kursOtwarcia, spolka FROM KursAkcji(KursAkcji.roznicaKursow(kursOtwarcia, kursZamkniecia) > 0).win:length(1)

8.
SELECT ISTREAM data, kursZamkniecia, spolka, max(kursZamkniecia) - kursZamkniecia as roznica FROM KursAkcji(spolka in ('CocaCola', 'PepsiCo')).win:ext_timed(data.getTime(), 7 days)

9.
SELECT ISTREAM data, kursZamkniecia, spolka, max(kursZamkniecia) - kursZamkniecia as roznica FROM KursAkcji(spolka in ('CocaCola', 'PepsiCo')).win:ext_timed_batch(data.getTime(), 1 day) HAVING max(kursZamkniecia) = kursZamkniecia

10.
SELECT ISTREAM max(kursZamkniecia) as maksimum FROM KursAkcji.win:ext_timed_batch(data.getTime(), 7 days)

11.
SELECT k.kursZamkniecia as kursCoc, p.data, p.kursZamkniecia as kursPep FROM KursAkcji(spolka='PepsiCo').win:length(1) as p full outer join KursAkcji(spolka='CocaCola').win:length(1) as k on p.data = k.data WHERE p.kursZamkniecia > k.kursZamkniecia

12.
SELECT k.data, k.kursZamkniecia as kursBiezacy, k.spolka, k.kursZamkniecia - a.kursZamkniecia as roznica FROM KursAkcji(spolka in ('PepsiCo', 'CocaCola')).win:length(1) as k join KursAkcji(spolka in ('PepsiCo', 'CocaCola')).std:firstunique(spolka) as a on k.spolka = a.spolka

13.
SELECT k.data, k.kursZamkniecia as kursBiezacy, k.spolka, k.kursZamkniecia - a.kursZamkniecia as roznica FROM KursAkcji.win:length(1) as k join KursAkcji.std:firstunique(spolka) as a on k.spolka = a.spolka WHERE k.kursZamkniecia > a.kursZamkniecia

14.
SELECT k.data as dataB, a.data as dataA, k.spolka, a.kursOtwarcia as kursA, k.kursOtwarcia as kursB  FROM KursAkcji.win:ext_timed(data.getTime(), 7 days) as k join KursAkcji.win:ext_timed(data.getTime(), 7 days) as a on k.spolka = a.spolka WHERE k.kursOtwarcia - a.kursOtwarcia > 3

15.
SELECT data, spolka, obrot FROM KursAkcji(market='NYSE').win:ext_timed_batch(data.getTime(), 7 days) order by obrot desc limit 3

16.
SELECT data, spolka, obrot FROM KursAkcji(market='NYSE').win:ext_timed_batch(data.getTime(), 7 days) order by obrot desc limit 1 offset 2
 */