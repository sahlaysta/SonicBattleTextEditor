using System;
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
    public partial class Form4 : Form
    {
        private string[] array = new string[0];
        private List<int> ind = new List<int>();
        private bool userclose = true;
        public int ans = -1;
        private void startup()
        {
            InitializeComponent();
            Icon = Icon.ExtractAssociatedIcon(System.Reflection.Assembly.GetExecutingAssembly().Location);
            this.ShowIcon = false;
            this.StartPosition = FormStartPosition.CenterScreen;
            this.ShowInTaskbar = false;
            this.MinimumSize = new Size(200, 200);
            listBox1.HorizontalScrollbar = true;
            this.FormClosing += new FormClosingEventHandler(myForm_FormClosing);
            this.Text = Globals.strings[40];
            this.Size = new Size(350, 400);
            button1.Text = Globals.strings[45];
            //dark theme
            if (Globals.prefs[2] == "dark")
            {
                settheme(SystemColors.ControlText, SystemColors.ControlDarkDark);
            }
        }
        void myForm_FormClosing(object sender, FormClosingEventArgs e)
        {
            if (userclose)
                ans = -1;
        }
        public Form4()
        {
            startup();
        }
        public Form4(string[] a)
        {
            array = a;
            startup();

            ind.Clear();
            int i = 0;
            foreach (string s in array)
            {
                listBox1.Items.Add(s);
                ind.Add(i);
                i++;
            }
        }

        private void textBox1_TextChanged(object sender, EventArgs e)
        {
            listBox1.Items.Clear();
            ind.Clear();

            int i = 0;
            foreach (string s in array)
            {
                if (contall(s, textBox1.Text))
                {
                    listBox1.Items.Add(s);
                    ind.Add(i);
                }
                i++;
            }
                
        }
        private bool contall(string x, string y)
        {
            bool flag = true;
            string[] arr = y.Split(' ');

            foreach (string sub in arr)
            {
                if (x.IndexOf(sub, 0, StringComparison.CurrentCultureIgnoreCase) == -1)
                    flag = false;
            }

            return flag;
        }

        private void listBox1_SelectedIndexChanged(object sender, EventArgs e)
        {
            ans = ind[listBox1.SelectedIndex];
        }

        private void button1_Click(object sender, EventArgs e)
        {
            userclose = false;
            this.Close();
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
    }
}
