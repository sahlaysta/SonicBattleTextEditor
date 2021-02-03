using System;
using System.IO;
using System.Drawing;
using System.Globalization;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace SonicBattleTextEditor
{
    public static class Globals
    {
        public static string dir = @Application.StartupPath;
        public static string sysLang = CultureInfo.InstalledUICulture.Name;
        public static string proLang = "";
        public static string langExt = ".json";
        public static string[] strings = new string[] { "" };
    }
    static class Program
    {
        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);

            //Open system's language. If not found then open language select
            string langcode = Globals.sysLang;
            bool lang = File.Exists(Path.Combine(Globals.dir, langcode + Globals.langExt));
            if (!lang)
            {
                if (langcode.Contains('-'))
                    langcode = langcode.Substring(0, langcode.IndexOf('-'));
                if (!langcode.Contains('-'))
                {
                    if (Directory.GetFiles(Globals.dir, langcode + "-*" + Globals.langExt).Length > 0)
                        langcode = Path.GetFileNameWithoutExtension(Directory.GetFiles(Globals.dir, langcode + "-*" + Globals.langExt)[0]);
                    else
                        langcode = "-1";
                }
                lang = Directory.GetFiles(Globals.dir, langcode + "-*" + Globals.langExt).Length > 0
                       || File.Exists(Path.Combine(Globals.dir, langcode + Globals.langExt));
                if (langcode == "-1")
                {
                    langcode = "en-US";
                    lang = false;
                }
            }
            Globals.proLang = langcode;
            Globals.strings = System.IO.File.ReadAllText(Path.Combine(Globals.dir, langcode + Globals.langExt)).Split(new string[] { System.Environment.NewLine }, StringSplitOptions.RemoveEmptyEntries);

            Form1 f1 = new Form1();
            Form2 f2 = new Form2();
            if (lang)
                Application.Run(f1);
            else
                Application.Run(f2);
        }
    }
}
