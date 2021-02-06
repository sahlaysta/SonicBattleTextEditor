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
        public Form1()
        {
            InitializeComponent();

            this.Text = Globals.strings[5];
            fileToolStripMenuItem.Text = Globals.strings[1];
            openToolStripMenuItem.Text = Globals.strings[2];
            toolStripMenuItem1.Text = Globals.strings[3];
            toolStripMenuItem3.Text = Globals.strings[4];
            tabPage1.Text = Globals.strings[12];
            tabPage2.Text = Globals.strings[13];
        }
        private void openToolStripMenuItem_Click(object sender, EventArgs e)
        {
            using (OpenFileDialog openFileDialog = new OpenFileDialog())
            {
                openFileDialog.InitialDirectory = Globals.dir;
                openFileDialog.Filter = Globals.strings[15] + " (*.*)|*.*|" + Globals.strings[16] + " (*.gba)|*.gba";
                openFileDialog.FilterIndex = 2;
                openFileDialog.RestoreDirectory = true;

                if (openFileDialog.ShowDialog() == DialogResult.OK)
                {
                    rompath = openFileDialog.FileName;
                    listBox1.DataSource = new string[] { Globals.strings[17] };
                }
                else
                {
                    return;
                }
            }

            //read sb.lib file to tuple arrays
            string[] liblines = File.ReadAllLines(Path.Combine(Globals.dir, Globals.libn));
            string[] liblinesx = new string[liblines.Length];
            string[] liblinesy = new string[liblines.Length];
            for (int i = 0; i < liblinesx.Length; i++)
                liblinesx[i] = liblines[i].Substring(1, liblines[i].Substring(1).IndexOf('"'));
            for (int i = 0; i < liblinesy.Length; i++)//3rd occurence of '"'
                liblinesy[i] = liblines[i].Substring(liblines[i].Substring(1).IndexOf('"', liblines[i].Substring(1).IndexOf('"') + 1)).Replace("\"", "").Substring(1);
            lib = new ValueTuple<string[], string[]> (liblinesx, liblinesy);

            StringBuilder rom = new StringBuilder();
            foreach (char ch in File.ReadAllBytes(@"C:\\Users\\tswoo\\Desktop\\Stuff\\00sbter\\sb.gba"))
                rom.AppendFormat(Convert.ToInt32(ch).ToString("X2"));

            List<string> sbstrings = new List<string>();
            string endline = "FEFF";
            var rule = new[] { ("EDFE8C", 2299) };
            foreach(var v in rule)
                sbstrings.AddRange(rom.ToString().Substring(2 * getloc(v.Item1), (2 * getloc((v.Item2 * 4) + int.Parse(v.Item1, System.Globalization.NumberStyles.HexNumber))) - (2 * getloc(v.Item1))).Split(new string[] { endline }, StringSplitOptions.RemoveEmptyEntries));

            //d(sbstrings.Aggregate((i, j) => i + j).Length);
            for (int i = 0; i < sbstrings.Count; i++)
                sbstrings[i] = readstring(sbstrings[i]);

            listBox1.DataSource = sbstrings;
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
            for (int i = 0; i < v.Length; i+=4)
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
                            MessageBox.Show(Globals.strings[14] + ": FBFF" + v.Substring(i+4, 4));
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
    }
}
