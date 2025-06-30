using Autoparts.DB;
using System;
using System.Collections.Generic;
using System.Windows.Forms;
using Npgsql;

namespace Autoparts.UI
{
    public class AddOrUpdatePriceForm : Form
    {
        private Dictionary<string, int> supplierMap = new Dictionary<string, int>();
        private Dictionary<string, int> partMap = new Dictionary<string, int>();

        private ComboBox supplierComboBox;
        private ComboBox partComboBox;
        private TextBox priceTextBox;
        private TextBox startDateTextBox;
        private Button saveButton;

        public AddOrUpdatePriceForm()
        {
            InitializeComponent();
            LoadSuppliers();
        }

        private void InitializeComponent()
        {
            this.Text = "Установить цену на деталь";
            this.Width = 480;
            this.Height = 300;
            this.FormBorderStyle = FormBorderStyle.FixedDialog;
            this.MaximizeBox = false;

            Label supplierLabel = new Label { Text = "Поставщик:", Left = 20, Top = 20, Width = 180 };
            supplierComboBox = new ComboBox { Left = 220, Top = 20, Width = 200 };
            supplierComboBox.SelectedIndexChanged += SupplierComboBox_SelectedIndexChanged;

            Label partLabel = new Label { Text = "Деталь:", Left = 20, Top = 60, Width = 180 };
            partComboBox = new ComboBox { Left = 220, Top = 60, Width = 200 };

            Label priceLabel = new Label { Text = "Цена:", Left = 20, Top = 100, Width = 180 };
            priceTextBox = new TextBox { Left = 220, Top = 100, Width = 200 };

            Label startDateLabel = new Label { Text = "Дата начала действия (ГГГГ-ММ-ДД):", Left = 20, Top = 140, Width = 180 };
            startDateTextBox = new TextBox { Left = 220, Top = 140, Width = 200, Text = DateTime.Now.ToString("yyyy-MM-dd") };

            saveButton = new Button { Text = "Сохранить", Left = 220, Top = 180, Width = 100 };
            saveButton.Click += SaveButton_Click;

            this.Controls.AddRange(new Control[] {
                supplierLabel, supplierComboBox,
                partLabel, partComboBox,
                priceLabel, priceTextBox,
                startDateLabel, startDateTextBox,
                saveButton
            });
        }

        private void LoadSuppliers()
        {
            supplierComboBox.Items.Clear();
            supplierMap.Clear();

            try
            {
                using (var conn = DBConnection.GetConnection())
                {
                    conn.Open();
                    using (var cmd = new NpgsqlCommand("SELECT id, name FROM supplier ORDER BY name", conn))
                    using (var reader = cmd.ExecuteReader())
                    {
                        while (reader.Read())
                        {
                            int id = reader.GetInt32(0);
                            string name = reader.GetString(1);
                            supplierMap[name] = id;
                            supplierComboBox.Items.Add(name);
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("Ошибка загрузки поставщиков: " + ex.Message, "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private void LoadAllParts()
        {
            partComboBox.Items.Clear();
            partMap.Clear();

            try
            {
                using (var conn = DBConnection.GetConnection())
                {
                    conn.Open();
                    using (var cmd = new NpgsqlCommand("SELECT id, name FROM part ORDER BY name", conn))
                    using (var reader = cmd.ExecuteReader())
                    {
                        while (reader.Read())
                        {
                            int id = reader.GetInt32(0);
                            string name = reader.GetString(1);
                            partMap[name] = id;
                            partComboBox.Items.Add(name);
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("Ошибка загрузки деталей: " + ex.Message, "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private void SupplierComboBox_SelectedIndexChanged(object sender, EventArgs e)
        {
            if (supplierComboBox.SelectedItem != null)
            {
                LoadAllParts();
                partComboBox.SelectedIndex = -1;
            }
        }

        private void SaveButton_Click(object sender, EventArgs e)
        {
            string supplier = supplierComboBox.SelectedItem as string;
            string part = partComboBox.SelectedItem as string;
            string priceText = priceTextBox.Text.Trim();
            string dateText = startDateTextBox.Text.Trim();

            if (string.IsNullOrEmpty(supplier) || string.IsNullOrEmpty(part) ||
                string.IsNullOrEmpty(priceText) || string.IsNullOrEmpty(dateText))
            {
                MessageBox.Show("Пожалуйста, заполните все поля", "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            double price;
            if (!double.TryParse(priceText, out price))
            {
                MessageBox.Show("Неверный формат цены", "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            DateTime startDate;
            if (!DateTime.TryParse(dateText, out startDate))
            {
                MessageBox.Show("Неверный формат даты", "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            int supplierId = supplierMap[supplier];
            int partId = partMap[part];

            try
            {
                using (var conn = DBConnection.GetConnection())
                {
                    conn.Open();
                    using (var cmd = new NpgsqlCommand(
                        "INSERT INTO part_price (supplier_id, part_id, price, start_date) VALUES (@supplierId, @partId, @price, @startDate)",
                        conn))
                    {
                        cmd.Parameters.AddWithValue("supplierId", supplierId);
                        cmd.Parameters.AddWithValue("partId", partId);
                        cmd.Parameters.AddWithValue("price", price);
                        cmd.Parameters.AddWithValue("startDate", startDate);

                        cmd.ExecuteNonQuery();
                    }
                }

                MessageBox.Show("Цена успешно сохранена!", "Успех", MessageBoxButtons.OK, MessageBoxIcon.Information);
                this.Close();
            }
            catch (Exception ex)
            {
                MessageBox.Show("Ошибка при сохранении цены: " + ex.Message, "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }
    }
}
