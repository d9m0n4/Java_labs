using Autoparts.DB;
using System;
using System.Windows.Forms;
using Npgsql;


namespace Autoparts.UI
{
    public class AddPartForm : Form
    {
        private TextBox nameTextBox;
        private TextBox articleTextBox;
        private Button addButton;

        public AddPartForm()
        {
            InitializeComponent();
        }

        private void InitializeComponent()
        {
            this.Text = "Добавить деталь";
            this.Width = 400;
            this.Height = 200;
            this.FormBorderStyle = FormBorderStyle.FixedDialog;
            this.MaximizeBox = false;

            Label nameLabel = new Label() { Text = "Название детали:", Left = 20, Top = 20, Width = 120 };
            nameTextBox = new TextBox() { Left = 150, Top = 20, Width = 200 };

            Label articleLabel = new Label() { Text = "Артикул:", Left = 20, Top = 60, Width = 120 };
            articleTextBox = new TextBox() { Left = 150, Top = 60, Width = 200 };

            addButton = new Button() { Text = "Добавить", Left = 150, Top = 100, Width = 100 };
            addButton.Click += AddButton_Click;

            this.Controls.Add(nameLabel);
            this.Controls.Add(nameTextBox);
            this.Controls.Add(articleLabel);
            this.Controls.Add(articleTextBox);
            this.Controls.Add(addButton);
        }

        private void AddButton_Click(object sender, EventArgs e)
        {
            string name = nameTextBox.Text.Trim();
            string article = articleTextBox.Text.Trim();

            if (string.IsNullOrEmpty(name) || string.IsNullOrEmpty(article))
            {
                MessageBox.Show("Пожалуйста, заполните все поля", "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            try
            {
                using (var conn = DBConnection.GetConnection())
                {
                    conn.Open();

                    string sql = "INSERT INTO part(article_number, name) VALUES (@article, @name)";
                    using (var cmd = new Npgsql.NpgsqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("article", article);
                        cmd.Parameters.AddWithValue("name", name);
                        cmd.ExecuteNonQuery();
                    }
                }

                MessageBox.Show("Деталь добавлена!", "Успех", MessageBoxButtons.OK, MessageBoxIcon.Information);
                this.Close();
            }
            catch (Exception ex)
            {
                MessageBox.Show("Ошибка при добавлении детали: " + ex.Message, "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }
    }
}
