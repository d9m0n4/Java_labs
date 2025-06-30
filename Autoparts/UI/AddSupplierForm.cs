using Autoparts.DB;
using System;
using System.Windows.Forms;
using Npgsql;

namespace Autoparts.UI
{
    public class AddSupplierForm : Form
    {
        private TextBox nameTextBox;
        private TextBox addressTextBox;
        private TextBox phoneTextBox;
        private Button addButton;

        public AddSupplierForm()
        {
            InitializeComponent();
        }

        private void InitializeComponent()
        {
            this.Text = "Добавить поставщика";
            this.Width = 400;
            this.Height = 260;
            this.FormBorderStyle = FormBorderStyle.FixedDialog;
            this.MaximizeBox = false;

            Label nameLabel = new Label
            {
                Text = "Имя поставщика:",
                Left = 20,
                Top = 20,
                Width = 140
            };

            nameTextBox = new TextBox
            {
                Left = 170,
                Top = 20,
                Width = 180
            };

            Label addressLabel = new Label
            {
                Text = "Адрес:",
                Left = 20,
                Top = 60,
                Width = 140
            };

            addressTextBox = new TextBox
            {
                Left = 170,
                Top = 60,
                Width = 180
            };

            Label phoneLabel = new Label
            {
                Text = "Телефон:",
                Left = 20,
                Top = 100,
                Width = 140
            };

            phoneTextBox = new TextBox
            {
                Left = 170,
                Top = 100,
                Width = 180
            };

            addButton = new Button
            {
                Text = "Добавить",
                Left = 170,
                Top = 150,
                Width = 100
            };
            addButton.Click += AddButton_Click;

            this.Controls.Add(nameLabel);
            this.Controls.Add(nameTextBox);
            this.Controls.Add(addressLabel);
            this.Controls.Add(addressTextBox);
            this.Controls.Add(phoneLabel);
            this.Controls.Add(phoneTextBox);
            this.Controls.Add(addButton);
        }

        private void AddButton_Click(object sender, EventArgs e)
        {
            string name = nameTextBox.Text.Trim();
            string address = addressTextBox.Text.Trim();
            string phone = phoneTextBox.Text.Trim();

            if (string.IsNullOrEmpty(name) || string.IsNullOrEmpty(address))
            {
                MessageBox.Show("Пожалуйста, заполните поля имя и адрес", "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            try
            {
                using (var conn = DBConnection.GetConnection())
                {
                    conn.Open();
                    using (var cmd = new NpgsqlCommand("INSERT INTO supplier (name, address, phone) VALUES (@name, @address, @phone)", conn))
                    {
                        cmd.Parameters.AddWithValue("name", name);
                        cmd.Parameters.AddWithValue("address", address);
                        cmd.Parameters.AddWithValue("phone", phone);
                        cmd.ExecuteNonQuery();
                    }
                }

                MessageBox.Show("Поставщик успешно добавлен!", "Успех", MessageBoxButtons.OK, MessageBoxIcon.Information);
                this.Close();
            }
            catch (Exception ex)
            {
                MessageBox.Show("Ошибка при добавлении поставщика: " + ex.Message, "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }
    }

}
