using Autoparts.UI;
using System;
using System.Data;
using System.Windows.Forms;
using Npgsql;
using System.Drawing;

namespace Autoparts
{
    public partial class Form1 : Form
    {
        private Button addPartButton;
        private Button addSupplierButton;
        private Button setPriceButton;
        private Button addPurchaseButton;
        private TextBox searchTextBox;   // Поле поиска
        private DataGridView purchaseGrid;

        private string searchPlaceholder = "Поиск по поставщику, детали или артикулу";


        public Form1()
        {
            InitializeComponent();
            InitializeUI();
            LoadPurchases();
        }

        private void InitializeUI()
        {
            this.Text = "Система управления автозапчастями";
            this.Width = 900;
            this.Height = 600;
            this.FormBorderStyle = FormBorderStyle.FixedDialog;
            this.MaximizeBox = false;

            addPartButton = new Button
            {
                Text = "Добавить деталь",
                Left = 20,
                Top = 40,
                Width = 180
            };
            addPartButton.Click += (s, e) => { new AddPartForm().ShowDialog(); LoadPurchases(GetFilterText()); };

            addSupplierButton = new Button
            {
                Text = "Добавить поставщика",
                Left = 220,
                Top = 40,
                Width = 180
            };
            addSupplierButton.Click += (s, e) => { new AddSupplierForm().ShowDialog(); LoadPurchases(GetFilterText()); };

            setPriceButton = new Button
            {
                Text = "Установить цену",
                Left = 420,
                Top = 40,
                Width = 180
            };
            setPriceButton.Click += (s, e) => { new AddOrUpdatePriceForm().ShowDialog(); LoadPurchases(GetFilterText()); };

            addPurchaseButton = new Button
            {
                Text = "Добавить закупку",
                Left = 620,
                Top = 40,
                Width = 180
            };
            addPurchaseButton.Click += (s, e) => { new AddPurchaseForm().ShowDialog(); LoadPurchases(GetFilterText()); };

            searchTextBox = new TextBox
            {
                Left = 20,
                Top = 10,
                Width = 840,
                ForeColor = SystemColors.GrayText,
                Text = searchPlaceholder
            };

            searchTextBox.GotFocus += (s, e) =>
            {
                if (searchTextBox.Text == searchPlaceholder)
                {
                    searchTextBox.Clear();
                    searchTextBox.ForeColor = SystemColors.WindowText;
                }
            };

            searchTextBox.LostFocus += (s, e) =>
            {
                if (string.IsNullOrWhiteSpace(searchTextBox.Text))
                {
                    searchTextBox.Text = searchPlaceholder;
                    searchTextBox.ForeColor = SystemColors.GrayText;
                }
            };

            searchTextBox.TextChanged += (s, e) =>
            {
                if (searchTextBox.ForeColor == SystemColors.GrayText || searchTextBox.Text == searchPlaceholder)
                {
                    return;
                }

                LoadPurchases(searchTextBox.Text.Trim());
            };

            this.Controls.Add(searchTextBox);

            purchaseGrid = new DataGridView
            {
                Left = 20,
                Top = 80,
                Width = 840,
                Height = 460,
                ReadOnly = true,
                AllowUserToAddRows = false,
                AutoSizeColumnsMode = DataGridViewAutoSizeColumnsMode.Fill
            };

            this.Controls.Add(addPartButton);
            this.Controls.Add(addSupplierButton);
            this.Controls.Add(setPriceButton);
            this.Controls.Add(addPurchaseButton);
            this.Controls.Add(purchaseGrid);
        }

        private string GetFilterText()
        {
            if (searchTextBox.ForeColor == SystemColors.GrayText || searchTextBox.Text == searchPlaceholder)
                return "";
            return searchTextBox.Text.Trim();
        }

        private void LoadPurchases(string filter = "")
        {
            try
            {
                using (var conn = DB.DBConnection.GetConnection())
                {
                    conn.Open();

                    string query = @"
                        SELECT 
                            purchase.id,
                            supplier.name AS Поставщик,
                            part.name AS Деталь,
                            part.article_number AS Артикул,
                            purchase.quantity AS Количество,
                            purchase.purchase_date AS Дата_покупки,
                            purchase.price_at_purchase * purchase.quantity AS Цена_покупки
                        FROM purchase
                        JOIN supplier ON supplier.id = purchase.supplier_id
                        JOIN part ON part.id = purchase.part_id
                    ";

                    if (!string.IsNullOrWhiteSpace(filter))
                    {
                        query += @"
                            WHERE
                                supplier.name ILIKE @filter OR
                                part.name ILIKE @filter OR
                                part.article_number ILIKE @filter
                        ";
                    }

                    query += " ORDER BY purchase.purchase_date DESC;";

                    using (var cmd = new NpgsqlCommand(query, conn))
                    {
                        if (!string.IsNullOrWhiteSpace(filter))
                        {
                            cmd.Parameters.AddWithValue("filter", "%" + filter + "%");
                        }

                        using (var adapter = new NpgsqlDataAdapter(cmd))
                        {
                            var dt = new DataTable();
                            adapter.Fill(dt);
                            purchaseGrid.DataSource = dt;
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("Ошибка загрузки покупок: " + ex.Message, "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }
    }
}
