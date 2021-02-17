using System;
using System.Globalization;
using System.IO;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Media;
using System.Threading.Tasks;
using System.Windows.Forms;
using Microsoft.VisualBasic;

namespace SonicBattleTextEditor
{
    public partial class Form1 : Form
    {
        private string rompath = "";
        private ValueTuple<string[], string[]> lib = (new string[0], new string[0]);
        private BindingList<string> sbstrings = new BindingList<string>();
        private int previndex = -2;
        private ValueTuple<string, int, int>[] textobj = new[] { ("EDFE8C", 2299, 0), ("EDCD88", 14, 0) };
        private bool edited = false;
        private string problems = "";
        private int swritten = 0;
        //prefs
        private Size formsize = new Size(0, 0);
        private int splitd = -1;
        private int stop = -1;
        private int sleft = -1;
        public Form1()
        {
            InitializeComponent();
            Icon = Icon.ExtractAssociatedIcon(System.Reflection.Assembly.GetExecutingAssembly().Location);

            this.Text = Globals.strings[5];
            this.MinimumSize = new Size(200, 300);
            this.FormClosing += new FormClosingEventHandler(myForm_FormClosing);
            fileToolStripMenuItem.Text = Globals.strings[1];
            openToolStripMenuItem.Text = Globals.strings[2];
            toolStripMenuItem1.Text = Globals.strings[3];
            toolStripMenuItem3.Text = Globals.strings[4];
            tabPage1.Text = Globals.strings[12];
            textBox1.Enabled = false;
            textBox1.ScrollBars = ScrollBars.Vertical;
            textBox1.KeyPress += new KeyPressEventHandler(keypressed);
            sToolStripMenuItem.Text = Globals.strings[18];
            sToolStripMenuItem.Enabled = false;
            toolStripMenuItem2.Text = Globals.strings[19];
            toolStripMenuItem5.Text = Globals.strings[20];
            thToolStripMenuItem.Text = Globals.strings[27];
            splitContainer1.Panel1MinSize = 70;
            splitContainer1.Panel2MinSize = 70;
            toolStripMenuItem4.Text = Globals.strings[28];
            toolStripMenuItem7.Text = Globals.strings[29];
            toolStripMenuItem8.Enabled = false;
            toolStripMenuItem7.Enabled = false;
            toolStripMenuItem8.Text = Globals.strings[30];
            toolStripMenuItem6.Text = Globals.strings[25];
            toolStripMenuItem10.Text = Globals.strings[26];
            toolStripMenuItem10.Enabled = false;
            toolStripMenuItem11.Enabled = false;
            toolStripMenuItem11.Text = Globals.strings[40];
            listView1.Scrollable = true;
            listView1.View = View.Details;
            listView1.FullRowSelect = true;
            listView1.HeaderStyle = ColumnHeaderStyle.None;
            listView1.MultiSelect = false;
            listView1.HideSelection = false;
            label1.Text = "";
            saToolStripMenuItem.Text = Globals.strings[44];
            saToolStripMenuItem.Enabled = false;
            settheme(SystemColors.ControlText, SystemColors.ControlDarkDark);
            settheme(SystemColors.Control, SystemColors.ControlText);
            this.listView1.Columns.Add("", -2);
            this.StartPosition = FormStartPosition.Manual;

            stop = Int32.Parse(Globals.prefs[7]);
            this.Top = stop;
            sleft = Int32.Parse(Globals.prefs[8]);
            this.Left = sleft;

            formsize = new Size(Int32.Parse(Globals.prefs[4]), Int32.Parse(Globals.prefs[5]));
            this.Size = formsize;

            splitd = Int32.Parse(Globals.prefs[6]);
            splitContainer1.SplitterDistance = splitd;

            readsblib();

            //dark theme
            if (Globals.prefs[2] == "dark")
            {
                settheme(SystemColors.ControlText, SystemColors.ControlDarkDark);
                thToolStripMenuItem.Checked = true;
            }
        }
        void myForm_FormClosing(object sender, FormClosingEventArgs e)
        {
            if (edited)
            {
                Form3 pr = new Form3(Globals.strings[42], Globals.strings[43], Globals.strings[35], Globals.strings[36]);
                SystemSounds.Asterisk.Play();
                pr.ShowDialog();
                if (Globals.promptchoice == 0)
                {
                    e.Cancel = true;
                    return;
                }
            }
            bool prefchange = false;
            if (formsize != this.Size)
            {
                Globals.prefs[4] = this.Size.Width + "";
                Globals.prefs[5] = this.Size.Height + "";
                prefchange = true;
            }
            if (splitd != splitContainer1.SplitterDistance)
            {
                Globals.prefs[6] = splitContainer1.SplitterDistance + "";
                prefchange = true;
            }
            if (this.Top != stop)
            {
                Globals.prefs[7] = this.Top + "";
                prefchange = true;
            }
            if (this.Left != sleft)
            {
                Globals.prefs[8] = this.Left + "";
                prefchange = true;
            }
            if (prefchange)
                Globals.saveprefs();
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
                    listView1.Items.Add(Globals.strings[17]);
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

            textBox1.Enabled = false;
            textBox1.Text = "";
            //read sb.lib file to tuple arrays
            readsblib();

            List<string> sbstringst = new List<string>();
            string endline = "FEFF";
            
            foreach(var v in textobj)
            {
                List<int> locs = new List<int>();
                string r = BitConverter.ToString(File.ReadAllBytes(rompath).Skip(int.Parse(v.Item1, System.Globalization.NumberStyles.HexNumber)).Take(v.Item2*4).ToArray()).Replace("-", "");
                for (int i = 0; i < r.Length; i += 8)
                    locs.Add(int.Parse(reversepointer(r.Substring(i, 6)), System.Globalization.NumberStyles.HexNumber) - int.Parse(reversepointer(r.Substring(0, 6)), System.Globalization.NumberStyles.HexNumber));
                string txch = BitConverter.ToString(File.ReadAllBytes(rompath).Skip(getloc(v.Item1)).Take(locs[locs.Count-1]).ToArray()).Replace("-", "");
                int q = 0;
                foreach (var x in locs)
                {
                    if (q >= locs.Count-1)
                        break;
                    string go = txch.Substring(x * 2);
                    sbstringst.Add(go.Substring(0, go.IndexOf(endline)));
                    q++;
                }

                //add last
                int pos = locs[locs.Count - 1] + int.Parse(reversepointer(r.Substring(0, 6)), System.Globalization.NumberStyles.HexNumber);
                string read = "";
                StringBuilder sb = new StringBuilder();
                while (read != endline)
                {
                    read = BitConverter.ToString(File.ReadAllBytes(rompath).Skip(pos).Take(2).ToArray()).Replace("-", "");
                    sb.Append(read);
                    pos += 2;
                }
                sbstringst.Add(sb.ToString().Substring(0, sb.Length - endline.Length));
            }

            //d(sbstrings.Aggregate((i, j) => i + j).Length);
            for (int i = 0; i < sbstringst.Count; i++)
                sbstringst[i] = readstring(sbstringst[i]);

            sbstrings = new BindingList<string>();
            foreach (string str in sbstringst)
            {
                sbstrings.Add(str);
            }
            sToolStripMenuItem.Enabled = true;
            listView1.Items.Clear();
            foreach (string s in sbstrings)
                listView1.Items.Add(s);
            textBox1.Enabled = true;
            toolStripMenuItem8.Enabled = true;
            toolStripMenuItem7.Enabled = true;
            toolStripMenuItem10.Enabled = true;
            toolStripMenuItem11.Enabled = true;
            saToolStripMenuItem.Enabled = true;
            this.Text = Globals.strings[5];
            edited = false;
            foreach (ListViewItem i in listView1.SelectedItems)
                i.Selected = false;
            listView1.Items[0].Selected = true;
            listView1.EnsureVisible(0);
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
            string s = v.Replace(" ", "").Replace("-", "");
            int avgskip = 4;
            StringBuilder result = new StringBuilder();
            List<string> fcarr = new List<string>();
            foreach (string w in lib.Item2)
                fcarr.Add(w.Substring(0, avgskip));
            fcarr = fcarr.Distinct().ToList();

            List<List<string>> arrg = new List<List<string>>();
            foreach (string w in fcarr)
                arrg.Add(new List<string>());

            foreach (string w in lib.Item2)
            {
                int i = 0;
                foreach (string match in fcarr)
                {
                    if (w.Substring(0, avgskip) == match)
                        arrg[i].Add(w);
                    i++;
                }
            }

            for (int w = 0; w < arrg.Count; w++)
            {
                arrg[w] = arrg[w].OrderBy(x => x.Length).ToList();
                arrg[w].Reverse();
            }

            //read
            List<string> convarr = new List<string>();
            string focus = "";
            bool found = false;
            int ind = -1;
            for (int i = 0; i < s.Length; i++)
            {
                ind = -1;
                found = false;
                focus = s.Substring(i, 4);
                int q = 0;
                foreach (string w in fcarr)
                {
                    if (focus == w)
                    {
                        found = true;
                        ind = q;
                    }
                    q++;
                }
                if (!found)
                    continue;

                foreach (string w in arrg[ind])
                {
                    if (s.Substring(i).Length >= w.Length)
                    {
                        if (s.Substring(i, w.Length) == w)
                        {
                            convarr.Add(w);
                            i += w.Length - 1;
                        }
                    }
                }
            }

            foreach (string w in convarr)
            {
                int i = 0;
                foreach (string x in lib.Item2)
                {
                    if (x == w)
                        result.Append(lib.Item1[i]);
                    i++;
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
            new Form2().ShowDialog();
        }
        private void Form1_Load(object sender, EventArgs e)
        {

        }
        private void button1_Click(object sender, EventArgs e)
        {

        }
        private string replacen(string v)
        {
            StringBuilder s = new StringBuilder();
            char[] c = v.ToArray();
            for (int i = 0; i < c.Length; i++)
            {
                if (c[i] == '\\')
                {
                    if (i + 1 < c.Length && c[i + 1] == 'n')
                    {
                        s.Append("\n");
                        i++;
                    }
                    else
                        s.Append("\\");
                }
                else
                    s.Append(c[i]);
            }

            return s.ToString();
        }

        private void listView1_SelectedIndexChanged_1(object sender, EventArgs e)
        {
            if (listView1.SelectedIndices.Count > 0)
            {
                if (previndex != listView1.SelectedIndices[0])
                {
                    textBox1.Enabled = false;
                    textBox1.Clear();

                    StringBuilder s = new StringBuilder();
                    char[] c = replacen(listView1.SelectedItems[0].Text.ToString()).Replace("\\\\", "\\").ToArray();
                    for (int i = 0; i < c.Length; i++)
                    {
                        if (c[i] == '\n')
                        {
                            if (c[i - 1] == '\\')
                                textBox1.AppendText("n");
                            else
                                textBox1.AppendText(Environment.NewLine);
                        }
                        else
                            textBox1.AppendText(c[i].ToString());
                    }
                }
                textBox1.Enabled = true;
                previndex = listView1.SelectedIndices[0];
            }
        }
        private void listView1_DoubleClick(object sender, MouseEventArgs e)
        {

        }
        private bool illegal(string s)
        {
            List<string> h = new List<string>();
            bool result = false;
            bool found = false;
            foreach (char ch in s)
            {
                found = false;
                foreach(string q in lib.Item1)
                {
                    if (ch.ToString() == q)
                    {
                        found = true;
                    }
                    if (ch == '<' || ch == '>' || (int) ch == 13 || (int)ch == 10)
                    {
                        found = true;
                    }
                }
                if (!found)
                {
                    result = true;
                    h.Add("'" + ch + "'");
                }
            }
            problems = Globals.strings[14] + ": " + (string.Join(", ", h.Distinct().ToArray()));
            return result;
        }

        private void textBox1_TextChanged(object sender, EventArgs e)
        {
            if (textBox1.Enabled)
            {
                listView1.BeginUpdate();
                string upd = textBox1.Text.Replace("\\", "\\\\").Replace("\n", "\\n");
                sbstrings[listView1.SelectedIndices[0]] = upd;
                listView1.Items[listView1.SelectedIndices[0]].Text = upd;
                listView1.EndUpdate();
                this.ActiveControl = textBox1;
            }
            if (illegal(textBox1.Text))
            {
                listView1.Items[listView1.SelectedIndices[0]].ForeColor = Color.Red;
            }
            else
            {
                listView1.Items[listView1.SelectedIndices[0]].ForeColor = new System.Drawing.Color();
                problems = "";
            }
            label1.Text = problems;
            label1.ForeColor = Color.Red;
        }
        private string parseSB(string v)
        {
            string s = (v.Replace("\r\n", "").Replace("\n", "").Replace("\r", ""));
            //s = replacen(s).Replace("\\\n", "\\\\n").Replace("\\\\", "\\");

            StringBuilder result = new StringBuilder();
            List<string> fcarr = new List<string>();
            foreach (string w in lib.Item1)
                fcarr.Add(w[0].ToString());
            fcarr = fcarr.Distinct().ToList();

            List<List<string>> arrg = new List<List<string>>();
            foreach (string w in fcarr)
                arrg.Add(new List<string>());

            foreach (string w in lib.Item1)
            {
                int i = 0;
                foreach (string match in fcarr)
                {
                    if (w[0].ToString() == match)
                        arrg[i].Add(w);
                    i++;
                }
            }

            for (int w = 0; w < arrg.Count; w++)
            {
                arrg[w] = arrg[w].OrderBy(x => x.Length).ToList();
                arrg[w].Reverse();
            }

            //read
            List<string> convarr = new List<string>();
            string focus = "";
            bool found = false;
            int ind = -1;
            for (int i = 0; i < s.Length; i++)
            {
                ind = -1;
                found = false;
                focus = s.Substring(i, 1);
                int q = 0;
                foreach (string w in fcarr)
                {
                    if (focus == w)
                    {
                        found = true;
                        ind = q;
                    }
                    q++;
                }
                if (!found)
                    continue;

                foreach(string w in arrg[ind])
                {
                    if (s.Substring(i).Length >= w.Length)
                    {
                        if (s.Substring(i, w.Length) == w)
                        {
                            convarr.Add(w);
                            i += w.Length-1;
                        }
                    }
                }
            }

            foreach(string w in convarr)
            {
                int i = 0;
                foreach(string x in lib.Item1)
                {
                    if (x == w)
                        result.Append(lib.Item2[i]);
                    i++;
                }
            }

            return result.ToString() + "FEFF";
        }
        private int write(ValueTuple<string, int, int> p)
        {
            StringBuilder result = new StringBuilder();
            StringBuilder points = new StringBuilder();
            int pos = p.Item3;

            string current = "";
            string initial = p.Item1;
            for (int w = swritten; w < swritten + p.Item2; w++)
            {
                current = parseSB(sbstrings[w].Replace("\n", "\\\\n"));
                result.Append(current);
                points.Append(reversepointer(pos.ToString("X2")) + "08");
                pos = pos + current.Length / 2;
            }
            swritten += p.Item2;

            pos = p.Item3;

            try
            {
                using (var fs = new FileStream(rompath, FileMode.Open, FileAccess.Write))
                {
                    fs.Seek(pos, SeekOrigin.Begin);
                    fs.Write(StringToByteArray(result.ToString()), 0, StringToByteArray(result.ToString()).Length);
                    //pos += StringToByteArray(result.ToString()).Length;
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("" + ex);
                return -1;
            }

            try
            {
                using (var fs = new FileStream(rompath, FileMode.Open, FileAccess.Write))
                {
                    fs.Seek(int.Parse(p.Item1, System.Globalization.NumberStyles.HexNumber), SeekOrigin.Begin);
                    fs.Write(StringToByteArray(points.ToString()), 0, StringToByteArray(points.ToString()).Length);
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("" + ex);
                return -1;
            }

            return result.ToString().Length/2;
        }
        private void sToolStripMenuItem_Click(object sender, EventArgs e)
        {
            saverom(true);
        }
        private void saverom(bool prompt)
        {
            if (prompt)
            {
                Form3 pr = new Form3(Globals.strings[18], Globals.strings[34].Replace("[f]", Path.GetFileName(rompath)), Globals.strings[35], Globals.strings[36]);
                SystemSounds.Asterisk.Play();
                pr.ShowDialog();
                if (Globals.promptchoice == 0)
                    return;
            }
            for (int w = 0; w < textobj.Length; w++)
            {
                textobj[w].Item3 = getloc(textobj[w].Item1);
            }
            int i = 0;
            int q = 0;
            foreach (var v in textobj)
            {
                q = write(v);
                if (q == -1)
                    return;
                else
                    textobj[i].Item3 = textobj[i].Item3 + q;
                i++;
            }
            foreach (var v in textobj)
            {
                d(v.Item1 + "\n" + v.Item2 + "\n" + v.Item3);
            }

            this.Text = Globals.strings[5];
            edited = false;
            swritten = 0;
            MessageBox.Show(Globals.strings[24]);
        }

        private void toolStripMenuItem5_Click(object sender, EventArgs e)
        {
            string message = Globals.strings[22] + ": porog" + "\n" + Globals.strings[23] + ": https://github.com/sahlaysta/SonicBattleTextEditor" + "\n" + Globals.strings[33] + ": 2.2";
            MessageBox.Show(message, Globals.strings[21], MessageBoxButtons.OK, MessageBoxIcon.None);
        }
        private void readsblib()
        {
            string[] liblines = new string[0];
            try
            {
                liblines = File.ReadAllLines(Path.Combine(Globals.dir, Globals.libn));
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex + "");
                Environment.Exit(1);
            }
            var temp = new List<string>();
            foreach (var s in liblines)
            {
                if (!string.IsNullOrEmpty(s))
                    temp.Add(s);
            }
            liblines = temp.ToArray();
            string[] liblinesx = new string[liblines.Length / 2];
            string[] liblinesy = new string[liblines.Length / 2];
            for (int i = 0; i < liblinesx.Length; i++)
                liblinesx[i] = liblines[i * 2];
            for (int i = 0; i < liblinesy.Length; i++)
                liblinesy[i] = liblines[i * 2 + 1];
            lib = new ValueTuple<string[], string[]>(liblinesx, liblinesy);
        }
        private static byte[] StringToByteArray(string hex)
        {
            return Enumerable.Range(0, hex.Length)
                             .Where(x => x % 2 == 0)
                             .Select(x => Convert.ToByte(hex.Substring(x, 2), 16))
                             .ToArray();
        }

        private void thToolStripMenuItem_Click(object sender, EventArgs e)
        {
            if (Globals.prefs[2] == "light")
            {
                Globals.prefs[2] = "dark";
                Globals.saveprefs();
                settheme(SystemColors.ControlText, SystemColors.ControlDarkDark);
            }
            else
            {
                Globals.prefs[2] = "light";
                Globals.saveprefs();
                settheme(SystemColors.Control, SystemColors.ControlText);
            }
        }

        private void splitContainer1_Panel2_Paint(object sender, PaintEventArgs e)
        {

        }

        private void toolStripMenuItem7_Click(object sender, EventArgs e)
        {
            string exportpath = "";
            using (SaveFileDialog openFileDialog = new SaveFileDialog())
            {
                if (Globals.prefs[3] == "|/" || !Directory.Exists(Path.GetDirectoryName(Globals.prefs[3])))
                    openFileDialog.InitialDirectory = Globals.dir;
                else
                    openFileDialog.InitialDirectory = Path.GetDirectoryName(Globals.prefs[3]);
                openFileDialog.Filter = Globals.strings[15] + " (*.*)|*.*|" + Globals.strings[31] + " (*.txt)|*.txt";
                openFileDialog.FilterIndex = 2;
                openFileDialog.RestoreDirectory = true;
                openFileDialog.Title = Globals.strings[29];

                if (openFileDialog.ShowDialog() == DialogResult.OK)
                {
                    exportpath = openFileDialog.FileName;
                    if (Globals.prefs[3] != exportpath)
                    {
                        Globals.prefs[3] = exportpath;
                        Globals.saveprefs();
                    }
                }
                else
                {
                    return;
                }
            }
            List<string> export = new List<string>();
            for (int i = 0; i < listView1.Items.Count; i++)
            {
                export.Add(listView1.Items[i].ToString());
            }

            System.IO.File.WriteAllLines(exportpath, export);
            MessageBox.Show(Globals.strings[32]);
        }

        private void toolStripMenuItem8_Click(object sender, EventArgs e)
        {
            string exportpath = "";
            using (OpenFileDialog openFileDialog = new OpenFileDialog())
            {
                if (Globals.prefs[3] == "|/" || !Directory.Exists(Path.GetDirectoryName(Globals.prefs[3])))
                    openFileDialog.InitialDirectory = Globals.dir;
                else
                    openFileDialog.InitialDirectory = Path.GetDirectoryName(Globals.prefs[3]);
                openFileDialog.Filter = Globals.strings[15] + " (*.*)|*.*|" + Globals.strings[31] + " (*.txt)|*.txt";
                openFileDialog.FilterIndex = 2;
                openFileDialog.RestoreDirectory = true;
                openFileDialog.Title = Globals.strings[30];

                if (openFileDialog.ShowDialog() == DialogResult.OK)
                {
                    exportpath = openFileDialog.FileName;
                    if (Globals.prefs[3] != exportpath)
                    {
                        Globals.prefs[3] = exportpath;
                        Globals.saveprefs();
                    }
                }
                else
                {
                    return;
                }
            }
            string[] lines = File.ReadLines(exportpath).ToArray();
            for (int i = 0; i < sbstrings.Count; i++)
            {
                sbstrings[i] = lines[i];
            }
            foreach (ListViewItem i in listView1.SelectedItems)
            {
                i.Selected = false;
            }
            textBox1.Enabled = false;
            textBox1.Text = "";
            textBox1.Enabled = true;
            MessageBox.Show(Globals.strings[37]);
        }

        private void toolStripMenuItem10_Click(object sender, EventArgs e)
        {
            Form3 pr = new Form3(Globals.strings[26], Globals.strings[38]);
            pr.ShowDialog();
            if (pr.ans == -1)
                return;
            try
            {
                foreach (ListViewItem i in listView1.SelectedItems)
                {
                    i.Selected = false;
                }
                listView1.Items[pr.ans].Selected = true;
                listView1.EnsureVisible(pr.ans);
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex + "");
            }
        }

        private void toolStripMenuItem11_Click(object sender, EventArgs e)
        {
            Form4 search = new Form4(sbstrings.ToArray());
            search.ShowDialog();
            if (search.ans == -1)
                return;

            foreach (ListViewItem i in listView1.SelectedItems)
            {
                i.Selected = false;
            }
            listView1.Items[search.ans].Selected = true;
            listView1.EnsureVisible(search.ans);
        }
        private void keypressed(object sender, KeyPressEventArgs e)
        {
            if (!edited)
            {
                edited = true;
                this.Text = this.Text + "*";
            }
        }

        private void saToolStripMenuItem_Click(object sender, EventArgs e)
        {
            string copy = "";
            using (SaveFileDialog openFileDialog = new SaveFileDialog())
            {
                if (Globals.prefs[1] == "|/" || !Directory.Exists(Path.GetDirectoryName(Globals.prefs[1])))
                    openFileDialog.InitialDirectory = Globals.dir;
                else
                    openFileDialog.InitialDirectory = Path.GetDirectoryName(Globals.prefs[1]);
                openFileDialog.Filter = Globals.strings[15] + " (*.*)|*.*|" + Globals.strings[16] + " (*.gba)|*.gba";
                openFileDialog.FilterIndex = 2;
                openFileDialog.RestoreDirectory = true;
                openFileDialog.Title = Globals.strings[44];

                if (openFileDialog.ShowDialog() == DialogResult.OK)
                {
                    copy = openFileDialog.FileName;
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
            File.Copy(rompath, copy);
            rompath = copy;
            saverom(false);
        }
    }
}
