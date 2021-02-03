using System;
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
    public partial class Form2 : Form
    {
        private string setlang = Globals.proLang;
        public Form2()
        {
            InitializeComponent();
            this.MinimumSize = new Size(200, 375);
            
            List<string> jsonlist = new List<string>();
            foreach (string str in Directory.GetFiles(Path.Combine(Globals.dir), "*" + Globals.langExt)) {
                jsonlist.Add(File.ReadLines(str).First());
            }

            listBox1.DataSource = jsonlist;

            this.Text = Globals.strings[6];
            button1.Text = Globals.strings[8];
            label1.Text = Globals.strings[7] + " " + Globals.sysLang + "\n" + Globals.strings[9] + " " + Globals.proLang;
        }

        private void Form2_Load(object sender, EventArgs e)
        {

        }

        private void listBox1_SelectedIndexChanged(object sender, EventArgs e)
        {
            int sel = listBox1.FindString(listBox1.SelectedItem.ToString());
        }

        private void label1_Click(object sender, EventArgs e)
        {

        }

        private void button1_Click(object sender, EventArgs e)
        {

        }

    }
}
