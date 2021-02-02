using System;
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

        public static string lt1 = "";
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
            Application.Run(new Form1());
        }
    }
}
