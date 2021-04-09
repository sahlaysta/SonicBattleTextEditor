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
        private List<string> langlist = new List<string>();
        public Form2()
        {
            InitializeComponent();
            Icon = Icon.ExtractAssociatedIcon(System.Reflection.Assembly.GetExecutingAssembly().Location);
            this.ShowIcon = false;
            this.MinimumSize = new Size(200, 130);
            
            List<string> jsonlist = new List<string>();
            foreach (string str in Directory.GetFiles(Path.Combine(Globals.dir), "*" + Globals.langExt)) {
                langlist.Add(str);
                jsonlist.Add(File.ReadLines(str).First());
            }

            listBox1.DataSource = jsonlist;
            listBox1.SelectedIndex = -1;
            listBox1.DoubleClick += new EventHandler(doubleclick);

            this.Text = Globals.strings[6];
            button1.Text = Globals.strings[8];
            label1.Text = Globals.strings[7] + " " + Globals.sysLang + "\n" + Globals.strings[9] + " " + Globals.proLang;

            //dark theme
            if (Globals.prefs[2] == "dark")
            {
                settheme(SystemColors.ControlText, SystemColors.ControlDarkDark);
            }
        }
        void doubleclick(object sender, EventArgs e)
        {
            button1.PerformClick();
        }
        private void settheme(Color a, Color b)
        {
            this.BackColor = a;
            this.ForeColor = b;
            foreach (Control x in this.Controls)
            {
                x.BackColor = a;
                x.ForeColor = b;
                foreach (Control subx in x.Controls)
                {
                    subx.BackColor = a;
                    subx.ForeColor = b;
                    foreach (Control y in subx.Controls)
                    {
                        y.BackColor = a;
                        y.ForeColor = b;
                        foreach (Control suby in y.Controls)
                        {
                            suby.BackColor = a;
                            suby.ForeColor = b;
                            foreach (Control w in suby.Controls)
                            {
                                w.BackColor = a;
                                w.ForeColor = b;
                            }
                        }
                    }
                }
            }
        }
        private void Form2_Load(object sender, EventArgs e)
        {

        }

        private void listBox1_SelectedIndexChanged(object sender, EventArgs e)
        {
            
        }

        private void label1_Click(object sender, EventArgs e)
        {

        }

        private void button1_Click(object sender, EventArgs e)
        {
            bool restart = Globals.prefs[0] == "-1";
            string targetlang = Path.GetFileNameWithoutExtension(langlist[listBox1.FindString(listBox1.SelectedItem.ToString())]).Split('.')[0];
            Globals.prefs[0] = targetlang;
            Globals.saveprefs();
            if (restart)
                Application.Restart();
            else
            {
                MessageBox.Show(Globals.strings[10] + "\n--\n" + System.IO.File.ReadAllText(Path.Combine(Globals.dir, targetlang + Globals.langExt)).Split(new string[] { System.Environment.NewLine }, StringSplitOptions.RemoveEmptyEntries)[10], Globals.strings[11]);
            }
        }
    }
}
