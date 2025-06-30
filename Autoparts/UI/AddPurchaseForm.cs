using Autoparts.DB;
using System;
using System.Collections.Generic;
using System.Windows.Forms;
using Npgsql;

namespace Autoparts.UI
{
    public class AddPurchaseForm : Form
    {
        private Dictionary<string, int> supplierMap = new Dictionary<string, int>();
        private Dictionary<string, int> partMap = new Dictionary<string, int>();

        private ComboBox supplierComboBox;
        private ComboBox partComboBox;
        private TextBox quantityTextBox;
        private TextBox dateTextBox;
        private Button saveButton;

        public AddPurchaseForm()
        {
            InitializeComponent();
            LoadSuppliers();
            supplierComboBox.SelectedIndexChanged += SupplierComboBox_SelectedIndexChanged;
        }

        private void InitializeComponent()
        {
            this.Text = "Добавить покупку";
            this.Width = 500;
            this.Height = 280;
            this.FormBorderStyle = FormBorderStyle.FixedDialog;
            this.MaximizeBox = false;

            Label supplierLabel = new Label { Text = "Поставщик:", Left = 20, Top = 20, Width = 120 };
            supplierComboBox = new ComboBox { Left = 180, Top = 20, Width = 280 };

            Label partLabel = new Label { Text = "Деталь:", Left = 20, Top = 60, Width = 120 };
            partComboBox = new ComboBox { Left = 180, Top = 60, Width = 280 };

            Label quantityLabel = new Label { Text = "Количество:", Left = 20, Top = 100, Width = 120 };
            quantityTextBox = new TextBox { Left = 180, Top = 100, Width = 280 };

            Label dateLabel = new Label { Text = "Дата (ГГГГ-ММ-ДД):", Left = 20, Top = 140, Width = 120 };
            dateTextBox = new TextBox { Left = 180, Top = 140, Width = 280, Text = DateTime.Now.ToString("yyyy-MM-dd") };

            saveButton = new Button { Text = "Сохранить", Left = 180, Top = 190, Width = 120 };
            saveButton.Click += SaveButton_Click;

            this.Controls.AddRange(new Control[] {
                supplierLabel, supplierComboBox,
                partLabel, partComboBox,
                quantityLabel, quantityTextBox,
                dateLabel, dateTextBox,
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

        private void SupplierComboBox_SelectedIndexChanged(object sender, EventArgs e)
        {
            if (supplierComboBox.SelectedItem != null)
            {
                string supplierName = supplierComboBox.SelectedItem.ToString();
                int supplierId = supplierMap[supplierName];
                LoadPartsForSupplier(supplierId);
                partComboBox.SelectedIndex = -1;
            }
        }

        private void LoadPartsForSupplier(int supplierId)
        {
            partComboBox.Items.Clear();
            partMap.Clear();

            try
            {
                using (var conn = DBConnection.GetConnection())
                {
                    conn.Open();
                    string sql = @"
                        SELECT p.id, p.name
                        FROM part p
                        JOIN part_price pp ON pp.part_id = p.id
                        WHERE pp.supplier_id = @supplierId
                        GROUP BY p.id, p.name
                        ORDER BY p.name;
                    ";

                    using (var cmd = new NpgsqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("supplierId", supplierId);
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
            }
            catch (Exception ex)
            {
                MessageBox.Show("Ошибка загрузки деталей для поставщика: " + ex.Message, "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private void SaveButton_Click(object sender, EventArgs e)
        {
            if (supplierComboBox.SelectedItem == null || partComboBox.SelectedItem == null)
            {
                MessageBox.Show("Пожалуйста, выберите поставщика и деталь", "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            string supplierName = supplierComboBox.SelectedItem.ToString();
            string partName = partComboBox.SelectedItem.ToString();
            string quantityText = quantityTextBox.Text.Trim();
            string dateText = dateTextBox.Text.Trim();

            if (string.IsNullOrEmpty(quantityText) || string.IsNullOrEmpty(dateText))
            {
                MessageBox.Show("Пожалуйста, заполните все поля", "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            int quantity;
            if (!int.TryParse(quantityText, out quantity) || quantity <= 0)
            {
                MessageBox.Show("Неверный формат количества", "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            DateTime date;
            if (!DateTime.TryParse(dateText, out date))
            {
                MessageBox.Show("Неверный формат даты", "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            int supplierId = supplierMap[supplierName];
            int partId = partMap[partName];

            try
            {
                using (var conn = DBConnection.GetConnection())
                {
                    conn.Open();

                    // Чтобы сохранить цену на момент покупки, берем последнюю цену для поставщика и детали
                    decimal priceAtPurchase = 0;

                    using (var cmdPrice = new NpgsqlCommand(
                        @"SELECT price FROM part_price 
                          WHERE supplier_id = @supplierId AND part_id = @partId
                          ORDER BY start_date DESC LIMIT 1", conn))
                    {
                        cmdPrice.Parameters.AddWithValue("supplierId", supplierId);
                        cmdPrice.Parameters.AddWithValue("partId", partId);

                        var result = cmdPrice.ExecuteScalar();
                        if (result != null && result != DBNull.Value)
                            priceAtPurchase = (decimal)result;
                    }

                    using (var cmd = new NpgsqlCommand(
                        @"INSERT INTO purchase (supplier_id, part_id, purchase_date, quantity, price_at_purchase) 
                          VALUES (@supplierId, @partId, @purchaseDate, @quantity, @priceAtPurchase)", conn))
                    {
                        cmd.Parameters.AddWithValue("supplierId", supplierId);
                        cmd.Parameters.AddWithValue("partId", partId);
                        cmd.Parameters.AddWithValue("purchaseDate", date);
                        cmd.Parameters.AddWithValue("quantity", quantity);
                        cmd.Parameters.AddWithValue("priceAtPurchase", priceAtPurchase);

                        cmd.ExecuteNonQuery();
                    }
                }

                MessageBox.Show("Покупка успешно добавлена!", "Успех", MessageBoxButtons.OK, MessageBoxIcon.Information);
                this.Close();
            }
            catch (Exception ex)
            {
                MessageBox.Show("Ошибка при добавлении покупки: " + ex.Message, "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }
    }
}
