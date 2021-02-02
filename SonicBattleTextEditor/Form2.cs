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
        public Form2()
        {
            InitializeComponent();
            this.label1.Text = Globals.lt1 + " " + Globals.sysLang;
            
            List<string> jsonlist = new List<string>();
            foreach (string str in Directory.GetFiles(Path.Combine(Globals.dir), "*.json")) {
                jsonlist.Add(File.ReadLines(str).First());
            }

            listBox1.DataSource = jsonlist;
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
    }
}
