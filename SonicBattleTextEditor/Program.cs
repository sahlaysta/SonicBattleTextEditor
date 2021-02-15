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
        public static string defaultLang = "en-US";
        public static string langExt = ".json";
        public static string libn = "sb.lib";
        public static string[] strings = new string[] { "" };
        public static string[] prefs = new string[] { "" };
            public static string prefn = "sbte.prefs";
        public static int promptchoice = 0;
        
        public static bool prefsexists()
        {
            return File.Exists(Path.Combine(Globals.dir, prefn));
        }
        public static void readprefs()
        {
            prefs = System.IO.File.ReadAllText(Path.Combine(Globals.dir, prefn)).Split(new string[] { System.Environment.NewLine }, StringSplitOptions.RemoveEmptyEntries);
        }
        public static void saveprefs()
        {
            System.IO.File.WriteAllLines(Path.Combine(dir, prefn), prefs);
        }
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
            string[] defaultprefs = new string[] { "en-US", "|/", "light", "|/", "816", "495", "259" };
            
            if (Globals.prefsexists())
            {
                Globals.readprefs();
            }
            else
            {
                Globals.prefs = defaultprefs;
                Globals.saveprefs();
            }

            if (Globals.prefs.Length < defaultprefs.Length)
            {
                List<string> temp = new List<string>();
                temp.AddRange(Globals.prefs);
                for(int i = Globals.prefs.Length; i<defaultprefs.Length;i++)
                {
                    temp.Add(defaultprefs[i]);
                }
                Globals.prefs = temp.ToArray();
                Globals.saveprefs();
            }

            //Open system's language. If not found then open language select
            string langcode = Globals.sysLang;
            if (Globals.prefs[0] != "-1")
                langcode = Globals.prefs[0];
            bool lang = File.Exists(Path.Combine(Globals.dir, langcode + Globals.langExt));
            {
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
                        langcode = Globals.defaultLang;
                        lang = false;
                    }
                }
            }
            Globals.proLang = langcode;

            try
            {
                Globals.strings = System.IO.File.ReadAllText(Path.Combine(Globals.dir, langcode + Globals.langExt)).Split(new string[] { System.Environment.NewLine }, StringSplitOptions.RemoveEmptyEntries);
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex+"");
            }

            Form1 f1 = new Form1();
            Form2 f2 = new Form2();
            if (lang)
            {
                if (Globals.prefs[0] != langcode)
                {
                    Globals.prefs[0] = langcode;
                    Globals.saveprefs();
                }
                Application.Run(f1);
            }
            else
                Application.Run(f2);
        }
    }
}
