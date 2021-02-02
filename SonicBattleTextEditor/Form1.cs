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
        private string Form2n = "Form2"; //"Select a language" text
        public Form1()
        {
            InitializeComponent();
            setlang(Globals.sysLang);
        }
        private void setlang(string ls)
        {
            string textdir = @Path.Combine(Globals.dir, ls + ".json");
            if (!(File.Exists(textdir)))
            {
                if (@Directory.GetFiles(Path.Combine(Globals.dir), Globals.sysLang.Substring(0, Globals.sysLang.IndexOf('-') + 1) + "*.json").Length>0)
                    textdir = @Directory.GetFiles(Path.Combine(Globals.dir), Globals.sysLang.Substring(0, Globals.sysLang.IndexOf('-') + 1) + "*.json")[0];
                if (!(File.Exists(textdir)))
                    return;
            }
            string[] strings = System.IO.File.ReadAllText(textdir).Split(new string[] {System.Environment.NewLine }, StringSplitOptions.RemoveEmptyEntries);
            setmenustrings(strings);
        }
        private void setmenustrings(string[] str)
        {
            fileToolStripMenuItem.Text = str[1];
            openToolStripMenuItem.Text = str[2];
            toolStripMenuItem1.Text = str[3];
            toolStripMenuItem3.Text = str[4];
            this.Text = str[5];
            Globals.lt1 = str[7];
        }
        private void languageselector()
        {
            Form2 form = new Form2();
            form.Text = Form2n;
            form.Size=new Size(400, 400);
            form.ShowDialog();
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
        private void toolStripMenuItem3_Click(object sender, EventArgs e) //change language
        {
            languageselector();
        }

        private void Form1_Load(object sender, EventArgs e)
        {

        }
    }
}
