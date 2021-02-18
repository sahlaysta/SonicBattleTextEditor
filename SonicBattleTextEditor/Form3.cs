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
    public partial class Form3 : Form
    {
        public int ans = -1;
        private int use = -1;
        private void startup()
        {
            InitializeComponent();
            Icon = Icon.ExtractAssociatedIcon(System.Reflection.Assembly.GetExecutingAssembly().Location);
            this.ShowIcon = false;
            this.StartPosition = FormStartPosition.CenterScreen;
            this.ShowInTaskbar = false;
            this.MinimizeBox = false;
            this.MaximizeBox = false;
            this.FormBorderStyle = FormBorderStyle.FixedSingle;
            label1.MaximumSize = new Size(230, 100);
            label1.AutoSize = true;
            textBox1.MaxLength = 9;
            //dark theme
            if (Globals.prefs[2] == "dark")
            {
                settheme(SystemColors.ControlText, SystemColors.ControlDarkDark);
            }
        }
        public Form3()
        {
            startup();
        }
        public Form3(bool h)
        {
            
        }

        private void button1_Click(object sender, EventArgs e)
        {
            if (use == 0)
            {
                Globals.promptchoice = 1;
                this.Close();
            }
            else if (use == 1)
            {
                try
                {
                    ans = Int32.Parse(textBox1.Text) - 1;
                    this.Close();
                }
                catch (Exception ex)
                {
                    if (textBox1.Text.Length == 0)
                        this.Close();
                    else
                        MessageBox.Show(Globals.strings[38], Globals.strings[39]);
                }
            }
        }
        public Form3(string title, string text, string yes, string no)
        {
            use = 0;
            startup();
            this.Text = title;
            textBox1.Hide();
            Globals.promptchoice = 0;
            label1.Text = text;
            button1.Text = yes;
            button2.Text = no;
        }
        public Form3(string title, string text)
        {
            use = 1;
            startup();
            this.Text = title;
            label1.Text = text;
            button2.Hide();
            button1.Text = "OK";
        }

        private void button2_Click(object sender, EventArgs e)
        {
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
