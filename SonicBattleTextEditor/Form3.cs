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
        public Form3()
        {
        }

        private void button1_Click(object sender, EventArgs e)
        {
            Globals.promptchoice = 1;
            this.Close();
        }
        public Form3(string title, string text, string yes, string no)
        {
            InitializeComponent();
            this.StartPosition = FormStartPosition.CenterScreen;
            this.Text = title;
            this.ShowInTaskbar = false;
            this.MinimizeBox = false;
            this.MaximizeBox = false;
            Globals.promptchoice = 0;
            label1.Text = text;
            button1.Text = yes;
            button2.Text = no;
        }

        private void button2_Click(object sender, EventArgs e)
        {
            this.Close();
        }
    }
}
