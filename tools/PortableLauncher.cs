using System;
using System.Diagnostics;
using System.IO;
using System.Text;
using System.Windows.Forms;

// Starts the packaged runtime, never a Java installation from PATH.
internal static class PortableLauncher {
    [STAThread]
    private static int Main() {
        string root = AppDomain.CurrentDomain.BaseDirectory;
        string logDir = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "midtermwork", "logs");
        string log = Path.Combine(logDir, DateTime.Now.ToString("yyyyMMdd-HHmmss-fff") + ".log");
        try {
            Directory.CreateDirectory(logDir);
            string java = Path.Combine(root, "runtime", "bin", "java.exe");
            string jar = Path.Combine(root, "app", "midtermwork.jar");
            if (!File.Exists(java) || !File.Exists(jar))
                throw new FileNotFoundException("배포 파일이 없습니다. ZIP 전체를 압축 해제하고 폴더 안의 포켓몬_불꽃의_여정.exe를 실행하세요.");
            using (StreamWriter writer = new StreamWriter(log, false, new UTF8Encoding(false))) {
                writer.AutoFlush = true;
                writer.WriteLine("포켓몬 불꽃의 여정 portable | " + DateTime.Now.ToString("O"));
                writer.WriteLine("Application directory: " + root);
                ProcessStartInfo info = new ProcessStartInfo(java,
                    "--enable-native-access=ALL-UNNAMED --add-exports=java.base/jdk.internal.misc=ALL-UNNAMED -Dfile.encoding=UTF-8 -jar \"" + jar + "\"");
                info.WorkingDirectory = root;
                info.UseShellExecute = false;
                info.CreateNoWindow = true;
                info.RedirectStandardOutput = true;
                info.RedirectStandardError = true;
                info.StandardOutputEncoding = Encoding.UTF8;
                info.StandardErrorEncoding = Encoding.UTF8;
                foreach (string name in new string[]{"JAVA_HOME", "CLASSPATH", "JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS"})
                    info.EnvironmentVariables.Remove(name);
                using (Process game = new Process()) {
                    game.StartInfo = info;
                    DataReceivedEventHandler output = delegate(object sender, DataReceivedEventArgs e) {
                        if (e.Data != null) lock(writer) writer.WriteLine(e.Data);
                    };
                    game.OutputDataReceived += output;
                    game.ErrorDataReceived += output;
                    game.Start();
                    game.BeginOutputReadLine();
                    game.BeginErrorReadLine();
                    game.WaitForExit();
                    writer.WriteLine("Exit code: " + game.ExitCode);
                    if (game.ExitCode != 0) {
                        MessageBox.Show("게임을 실행하지 못했습니다.\n\n로그 파일:\n" + log,
                            "포켓몬 불꽃의 여정 실행 오류", MessageBoxButtons.OK, MessageBoxIcon.Error);
                    }
                    return game.ExitCode;
                }
            }
        } catch (Exception error) {
            try { Directory.CreateDirectory(logDir); File.AppendAllText(log, error.ToString(), Encoding.UTF8); } catch { }
            MessageBox.Show(error.Message + "\n\nZIP 전체를 압축 해제했는지 확인하세요.\n로그: " + log,
                "포켓몬 불꽃의 여정 실행 오류", MessageBoxButtons.OK, MessageBoxIcon.Error);
            return 1;
        }
    }
}
