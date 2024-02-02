package com.xpeng.airplay.service;

import android.os.ShellCommand;
import java.io.PrintWriter;
/* loaded from: classes.dex */
public class XpAirplayShellCommand extends ShellCommand {
    private static final String TAG = "XpAirplayShellCommand";
    private XpAirplayServiceImpl mXpAirplayService;

    public XpAirplayShellCommand(XpAirplayServiceImpl service) {
        this.mXpAirplayService = service;
    }

    public int onCommand(String cmd) {
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        char c = 65535;
        int hashCode = cmd.hashCode();
        if (hashCode != -1376541547) {
            if (hashCode != -284749990) {
                if (hashCode == 1930173637 && cmd.equals("get-info")) {
                    c = 1;
                }
            } else if (cmd.equals("get-state")) {
                c = 0;
            }
        } else if (cmd.equals("get-data-usage")) {
            c = 2;
        }
        switch (c) {
            case 0:
                getOutPrintWriter().print(this.mXpAirplayService.getServerState());
                return 0;
            case 1:
                getOutPrintWriter().print(this.mXpAirplayService.getServerInfo());
                return 0;
            case 2:
                long dataUsge = this.mXpAirplayService.getTetheringStats();
                getOutPrintWriter().print(this.mXpAirplayService.formatTetheringDataUsage(dataUsge) + "\n");
                return 0;
            default:
                return handleDefaultCommands(cmd);
        }
    }

    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("Usage: cmd xpairplay SUBCMD args");
        pw.println("    help\n");
        pw.println("      show this message\n");
        pw.println("    get-state\n");
        pw.println("    get-info\n");
        pw.println("    get-data-usage\n");
    }
}
