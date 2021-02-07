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
        private string rompath = "";
        private ValueTuple<string[], string[]> lib = (new string[0], new string[0]);
        private BindingList<string> sbstrings = new BindingList<string>();
        private int previndex = -2;
        public Form1()
        {
            InitializeComponent();

            this.Text = Globals.strings[5];
            fileToolStripMenuItem.Text = Globals.strings[1];
            openToolStripMenuItem.Text = Globals.strings[2];
            toolStripMenuItem1.Text = Globals.strings[3];
            toolStripMenuItem3.Text = Globals.strings[4];
            tabPage1.Text = Globals.strings[12];
            textBox1.Enabled = false;
            textBox1.ScrollBars = ScrollBars.Vertical;
            sToolStripMenuItem.Text = Globals.strings[18];
            sToolStripMenuItem.Enabled = false;
        }
        private void openToolStripMenuItem_Click(object sender, EventArgs e)
        {
            using (OpenFileDialog openFileDialog = new OpenFileDialog())
            {
                if (Globals.prefs[1] == "|/" || !Directory.Exists(Path.GetDirectoryName(Globals.prefs[1])))
                    openFileDialog.InitialDirectory = Globals.dir;
                else
                    openFileDialog.InitialDirectory = Path.GetDirectoryName(Globals.prefs[1]);
                openFileDialog.Filter = Globals.strings[15] + " (*.*)|*.*|" + Globals.strings[16] + " (*.gba)|*.gba";
                openFileDialog.FilterIndex = 2;
                openFileDialog.RestoreDirectory = true;
                openFileDialog.Title = Globals.strings[2];

                if (openFileDialog.ShowDialog() == DialogResult.OK)
                {
                    rompath = openFileDialog.FileName;
                    listBox1.DataSource = new string[] { Globals.strings[17] };
                    if (Globals.prefs[1] != rompath)
                    {
                        Globals.prefs[1] = rompath;
                        Globals.saveprefs();
                    }
                }
                else
                {
                    return;
                }
            }

            //read sb.lib file to tuple arrays
            string[] liblines = File.ReadAllLines(Path.Combine(Globals.dir, Globals.libn));
            var temp = new List<string>();
            foreach (var s in liblines)
            {
                if (!string.IsNullOrEmpty(s))
                    temp.Add(s);
            }
            liblines = temp.ToArray();
            string[] liblinesx = new string[liblines.Length/2];
            string[] liblinesy = new string[liblines.Length/2];
            for (int i = 0; i < liblinesx.Length; i++)
                liblinesx[i] = liblines[i*2];
            for (int i = 0; i < liblinesy.Length; i++)
                liblinesy[i] = liblines[i*2+1];
            lib = new ValueTuple<string[], string[]> (liblinesx, liblinesy);

            StringBuilder rom = new StringBuilder();
            foreach (char ch in File.ReadAllBytes(rompath))
                rom.AppendFormat(Convert.ToInt32(ch).ToString("X2"));
            List<string> sbstringst = new List<string>();
            string endline = "FEFF";
            var textobj = new[] { ("EDFE8C", 2299) };
            foreach(var v in textobj)
                sbstringst.AddRange(rom.ToString().Substring(2 * getloc(v.Item1), (2 * getloc((v.Item2 * 4) + int.Parse(v.Item1, System.Globalization.NumberStyles.HexNumber))) - (2 * getloc(v.Item1))).Split(new string[] { endline }, StringSplitOptions.RemoveEmptyEntries));

            //d(sbstrings.Aggregate((i, j) => i + j).Length);
            for (int i = 0; i < sbstringst.Count; i++)
                sbstringst[i] = readstring(sbstringst[i]);

            foreach(string str in sbstringst)
            {
                sbstrings.Add(str);
            }
            sToolStripMenuItem.Enabled = true;
            listBox1.DataSource = sbstrings;
            textBox1.Enabled = true;
            listBox1.SelectedIndex = 1;
            listBox1.SelectedIndex = 0;
        }
        private void d(object v)
        {
            MessageBox.Show("" + v);
        }
        private int getloc(int v)
        {
            return int.Parse(reversepointer((File.ReadAllBytes(rompath).Skip(v).Take(2).ToArray()[0].ToString("X2") + File.ReadAllBytes(rompath).Skip(v + 1).Take(2).ToArray()[0].ToString("X2") + File.ReadAllBytes(rompath).Skip(v + 2).Take(2).ToArray()[0].ToString("X2"))), System.Globalization.NumberStyles.HexNumber);
        }
        private int getloc(string v)
        {
            return getloc(int.Parse(v, System.Globalization.NumberStyles.HexNumber));
        }
        private string reversepointer(string v)
        {
            return v.Substring(4, 2) + v.Substring(2, 2) + v.Substring(0, 2);
        }
        private string readstring(string v)
        {
            StringBuilder result = new StringBuilder();
            int q = 0;
            bool found = false;
            for (int i = 0; i < v.Length; i += 4)
            {
                found = false;
                q = 0;
                foreach (string str in lib.Item2)
                {
                    if (v.Substring(i, 4) == "FBFF" && !found)
                    {
                        if (v.Substring(i + 4, 4) == "0500")
                            result.Append("<BLUE>");
                        else if (v.Substring(i + 4, 4) == "0300")
                            result.Append("<BLACK>");
                        else if (v.Substring(i + 4, 4) == "0600")
                            result.Append("<GREEN>");
                        else if (v.Substring(i + 4, 4) == "0400")
                            result.Append("<RED>");
                        else if (v.Substring(i + 4, 4) == "0700")
                            result.Append("<PURPLE>");
                        else if (v.Substring(i + 4, 4) == "0000")
                            result.Append("<WHITE>");
                        else
                        {
                            MessageBox.Show(Globals.strings[14] + ": FBFF" + v.Substring(i + 4, 4));
                            result.Append("□□");
                        }

                        found = true;
                        i += 4;
                    }
                    else if (v.Substring(i, 4) == lib.Item2[q] && !found)
                    {
                        result.Append(lib.Item1[q]);
                        found = true;
                        //MessageBox.Show("hit");
                    }
                    else if (v.Substring(i, 4) == "0200" && !found)
                    {
                        result.Append("\"");
                        found = true;
                    }
                    q++;
                }
                if (!found)
                {
                    MessageBox.Show(Globals.strings[14] + ": " + v.Substring(i, 4) + "\n" + result.ToString());
                    result.Append("□");
                }
            }
            return result.ToString();
        }
        private string match(string hex)
        {
            int i = 0;
            foreach (string str in lib.Item2)
            {
                if (str == hex)
                {
                    return lib.Item1[i];
                }
                i++;
            }

            return "□";
        }
        private void menuStrip1_ItemClicked(object sender, ToolStripItemClickedEventArgs e)
        {

        }
        private void fileToolStripMenuItem_Click(object sender, EventArgs e)
        {

        }
        private void toolStripMenuItem3_Click(object sender, EventArgs e) //change language
        {
            new Form2().Show();
        }
        private void Form1_Load(object sender, EventArgs e)
        {

        }
        private void button1_Click(object sender, EventArgs e)
        {

        }
        private string replacen(string v)
        {
            if (v.Contains("\\n"))
                return v.Replace("\\n", "\n");

            return v;
        }

        private void listBox1_SelectedIndexChanged(object sender, EventArgs e)
        {
            if (listBox1.SelectedIndex != -1)
            {
                if (previndex != listBox1.SelectedIndex)
                {
                    textBox1.Enabled = false;
                    textBox1.Clear();
                    foreach (char ch in replacen(listBox1.Items[listBox1.SelectedIndex].ToString()))
                    {
                        if (ch == '\n')
                        {
                            textBox1.AppendText(Environment.NewLine);
                        }
                        else
                            textBox1.AppendText(ch.ToString());
                    }
                }
                textBox1.Enabled = true;
                previndex = listBox1.SelectedIndex;
            }
        }
        private void listBox1_DoubleClick(object sender, MouseEventArgs e)
        {

        }

        private void textBox1_TextChanged(object sender, EventArgs e)
        {
            if (textBox1.Enabled)
            {
                listBox1.BeginUpdate();
                sbstrings[listBox1.SelectedIndex] = textBox1.Text.Replace("\n", "\\n");
                listBox1.EndUpdate();
                this.ActiveControl = textBox1;
            }
        }

        private void sToolStripMenuItem_Click(object sender, EventArgs e)
        {
            d(1);
        }
    }
}
