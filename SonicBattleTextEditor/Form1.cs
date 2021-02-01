using System;
using System.Globalization;
using System.IO;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace SonicBattleTextEditor
{
    public partial class Form1 : Form
    {
        private string dir = @Application.StartupPath;
        private string sysLang = CultureInfo.InstalledUICulture.TwoLetterISOLanguageName;
        public Form1()
        {
            InitializeComponent();
            setlang(sysLang);
        }

        private void openToolStripMenuItem_Click(object sender, EventArgs e)
        {
            
        }
        private void menuStrip1_ItemClicked(object sender, ToolStripItemClickedEventArgs e)
        {

        }

        private void fileToolStripMenuItem_Click(object sender, EventArgs e)
        {
            
        }
        private void setlang(string ls)
        {
            string textdir = @Path.Combine(dir, "lang_" + ls + ".json");
            if (!(File.Exists(textdir)))
                return;
            string[] strings = System.IO.File.ReadAllText(textdir).Split(new string[] {System.Environment.NewLine }, StringSplitOptions.RemoveEmptyEntries);
            setmenustrings(strings);
        }
        private void setmenustrings(string[] str)
        {
            fileToolStripMenuItem.Text = str[1];
            openToolStripMenuItem.Text = str[2];
            toolStripMenuItem1.Text = str[3];
            toolStripMenuItem3.Text = str[4];
        }

        private void toolStripMenuItem3_Click(object sender, EventArgs e)
        {

        }
    }
}
